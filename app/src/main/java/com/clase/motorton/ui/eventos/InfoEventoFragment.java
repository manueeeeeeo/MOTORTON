package com.clase.motorton.ui.eventos;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.clase.motorton.R;
import com.clase.motorton.adaptadores.ParticipanteAdapter;
import com.clase.motorton.modelos.Evento;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InfoEventoFragment extends Fragment {
    private TextView textNombre = null, textDescripcion = null, textUbicacion = null, textProvincia = null,
            textTipoEvento = null, textOrganizador = null, textFecha = null, textActivo = null, textParticipantes1 = null;

    private MapView mapView = null;
    private Polyline routeLine = null;
    private FirebaseFirestore db = null;
    private RecyclerView recyclerViewParticipantes = null;
    private ParticipanteAdapter participanteAdapter = null;
    private List<String> participantesList = new ArrayList<>();

    private Double ubicacionLat = null;
    private Double ubicacionLon = null;
    private Double startLat = null;
    private Double startLon = null;
    private Double endLat = null;
    private Double endLon = null;
    private boolean esRuta = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info_evento, container, false);

        db = FirebaseFirestore.getInstance();
        textNombre = view.findViewById(R.id.textNombre);
        textDescripcion = view.findViewById(R.id.textDescripcion);
        textUbicacion = view.findViewById(R.id.textUbicacion);
        textProvincia = view.findViewById(R.id.textProvincia);
        textTipoEvento = view.findViewById(R.id.textTipoEvento);
        textOrganizador = view.findViewById(R.id.textOrganizador);
        textFecha = view.findViewById(R.id.textFecha);
        textActivo = view.findViewById(R.id.textActivo);
        textParticipantes1 = view.findViewById(R.id.textParticipantes);
        recyclerViewParticipantes = view.findViewById(R.id.recyclerViewParticipantes);
        mapView = view.findViewById(R.id.mapViewInfo);

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        recyclerViewParticipantes.setLayoutManager(new LinearLayoutManager(getContext()));
        participanteAdapter = new ParticipanteAdapter(participantesList);
        recyclerViewParticipantes.setAdapter(participanteAdapter);

        if (getArguments() != null && getArguments().containsKey("eventoId")) {
            String eventoId = getArguments().getString("eventoId");
            cargarEvento(eventoId);
        }

        return view;
    }

    private void cargarEvento(String eventoId) {
        db.collection("eventos")
                .whereEqualTo("id", eventoId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Evento evento = queryDocumentSnapshots.getDocuments().get(0).toObject(Evento.class);

                        if (evento != null) {
                            textNombre.setText(evento.getNombre());
                            textDescripcion.setText(evento.getDescripcion());
                            textUbicacion.setText("Ubicación: " + evento.getUbicacion());
                            textProvincia.setText("Provincia: " + evento.getProvincia());
                            textTipoEvento.setText("Tipo: " + evento.getTipoEvento());

                            cargarNombreOrganizador(evento.getOrganizador());

                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            String fechaFormateada = evento.getFecha() != null ? sdf.format(evento.getFecha()) : "Sin fecha";
                            textFecha.setText("Fecha: " + fechaFormateada);

                            textActivo.setText("Estado: " + (evento.isActivo() ? "Activo" : "Inactivo"));

                            List<String> participantes = evento.getParticipantes();
                            if (participantes != null) {
                                int cantidadParticipantes = participantes.size();
                                textParticipantes1.setText("Participantes: " + cantidadParticipantes);
                                participantesList.clear();
                                participantesList.addAll(participantes);
                                participanteAdapter.notifyDataSetChanged();
                                recyclerViewParticipantes.setVisibility(View.VISIBLE);
                            } else {
                                textParticipantes1.setText("Participantes: 0");
                                recyclerViewParticipantes.setVisibility(View.GONE);
                            }

                            mapView.getOverlays().clear();

                            if(evento.isEsRuta()){
                                startLat = evento.getStartLat();
                                startLon = evento.getStartLon();
                                endLat = evento.getEndLat();
                                endLon = evento.getEndLon();
                                if (startLat != null && startLon != null && endLat != null && endLon != null) {
                                    esRuta = true;
                                    dibujarMarcadoresRuta(startLat, startLon, endLat, endLon);
                                    obtenerRutaConCallOSRM(new GeoPoint(startLat, startLon), new GeoPoint(endLat, endLon));
                                }else {
                                    Toast.makeText(getContext(), "Datos de ruta incompletos", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                ubicacionLat = evento.getUbicacionLat();
                                ubicacionLon = evento.getUbicacionLon();
                                if (ubicacionLat != null && ubicacionLon != null){
                                    esRuta = false;
                                    dibujarMarcadorUnico(ubicacionLat, ubicacionLon);
                                }else {
                                    Toast.makeText(getContext(), "Datos de ubicación incompletos", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    } else {
                        textNombre.setText("Evento no encontrado.");
                    }
                })
                .addOnFailureListener(e -> {
                    textNombre.setText("Error al cargar el evento.");
                });
    }

    private void dibujarMarcadorUnico(double lat, double lon) {
        Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(lat, lon));
        marker.setTitle("Ubicación del evento");
        mapView.getOverlays().add(marker);
        mapView.getController().setCenter(new GeoPoint(lat, lon));
        mapView.invalidate();
    }

    private void dibujarMarcadoresRuta(double startLat, double startLon, double endLat, double endLon) {
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(new GeoPoint(startLat, startLon));
        startMarker.setTitle("Inicio");
        mapView.getOverlays().add(startMarker);

        Marker endMarker = new Marker(mapView);
        endMarker.setPosition(new GeoPoint(endLat, endLon));
        endMarker.setTitle("Fin");
        mapView.getOverlays().add(endMarker);
    }

    private void obtenerRutaConCallOSRM(GeoPoint start, GeoPoint end) {
        new Thread(() -> {
            try {
                String urlString = "https://router.project-osrm.org/route/v1/driving/" +
                        start.getLongitude() + "," + start.getLatitude() + ";" +
                        end.getLongitude() + "," + end.getLatitude() +
                        "?overview=full&geometries=geojson";

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject json = new JSONObject(result.toString());
                JSONArray coordinates = json.getJSONArray("routes")
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONArray("coordinates");

                List<GeoPoint> geoPointsRuta = new ArrayList<>();
                for (int i = 0; i < coordinates.length(); i++) {
                    JSONArray coord = coordinates.getJSONArray(i);
                    double lon = coord.getDouble(0);
                    double lat = coord.getDouble(1);
                    geoPointsRuta.add(new GeoPoint(lat, lon));
                }

                requireActivity().runOnUiThread(() -> {
                    if (routeLine != null) mapView.getOverlays().remove(routeLine);

                    routeLine = new Polyline();
                    routeLine.setPoints(geoPointsRuta);
                    routeLine.setColor(Color.BLUE);
                    routeLine.setWidth(5);
                    mapView.getOverlays().add(routeLine);

                    BoundingBox boundingBox = calculateBoundingBox(geoPointsRuta);
                    mapView.zoomToBoundingBox(boundingBox, true);
                    mapView.invalidate();
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error al cargar la ruta", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private BoundingBox calculateBoundingBox(List<GeoPoint> puntosRuta) {
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;

        for (GeoPoint point : puntosRuta) {
            minLat = Math.min(minLat, point.getLatitude());
            maxLat = Math.max(maxLat, point.getLatitude());
            minLon = Math.min(minLon, point.getLongitude());
            maxLon = Math.max(maxLon, point.getLongitude());
        }

        return new BoundingBox(maxLat, maxLon, minLat, minLon);
    }

    private void cargarNombreOrganizador(String uidOrganizador) {
        db.collection("perfiles")
                .document(uidOrganizador)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombreOrganizador = documentSnapshot.getString("username");
                        textOrganizador.setText("Organizador: " + nombreOrganizador);
                    } else {
                        textOrganizador.setText("Organizador: Desconocido");
                    }
                })
                .addOnFailureListener(e -> {
                    textOrganizador.setText("Error al cargar el organizador.");
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
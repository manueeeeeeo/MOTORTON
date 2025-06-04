package com.clase.motorton.ui.mapas;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.clase.motorton.R;
import com.clase.motorton.servicios.InternetController;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapaEventFragment extends Fragment {
    // Variable para controlar el mapa de la interfaz
    private MapView map = null;
    // Variable para marcar el punto de empezado
    private Marker startMarker = null;
    // Variable para marcar el punto de finalización
    private Marker endMarker = null;
    // Variable para manejar la línea de la ruta
    private Polyline routeLine = null;
    // Variable para controlar el botón de confirmar ruta
    private Button btnConfirmarRuta = null;
    // Variable para controlar el botón de borrar ruta
    private Button btnBorrarRuta = null;
    // Variable para controlar el buscador
    private SearchView searchView = null;
    private double distanciaRuta = Double.NaN;
    private double tiempoRuta = Double.NaN;

    // Variable para controlar todos los Toast de la actividad
    private Toast mensajeToast = null;

    private InternetController internetController = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflo la vista del fragmento
        View view = inflater.inflate(R.layout.fragment_mapa_event, container, false);

        // Obtengo de la interfaz todos los elementos
        map = view.findViewById(R.id.map);
        btnConfirmarRuta = view.findViewById(R.id.btnConfirmarRuta);
        btnBorrarRuta = view.findViewById(R.id.btnBorrarRuta);
        searchView = view.findViewById(R.id.searchView);

        // Inicializo el controlador de internet
        internetController = new InternetController(getContext());

        if(!internetController.tieneConexion()){
            showToast("No tienes acceso a internet, conectese a una red!!");
            return view;
        }

        String tipoSeleccion = getArguments().getString("tipoSeleccion", "ubicacion");

        // Configuramos para obtener la API de OSM
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        // Establecemos como activa que se pueda controlar el mapa con multicontrol
        map.setMultiTouchControls(true);
        // Establecemos el zoom del mapa a 15
        map.getController().setZoom(15.0);
        // Establecemos que se quede centrado en españa
        map.getController().setCenter(new GeoPoint(40.4168, -3.7038));

        map.setOnTouchListener((v, event) -> false);

        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                if(tipoSeleccion.equals("ruta")){
                    // Comprobamos que la marca inicial sea nula
                    if (startMarker == null) {
                        // Inicializamos el marcador
                        startMarker = new Marker(map);
                        // Establecemos la posición en el marcador
                        startMarker.setPosition(p);
                        // Establecemos el título
                        startMarker.setTitle("Punto de inicio");
                        // Marcamos en el mapa el marcador
                        map.getOverlays().add(startMarker);
                        startMarker.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker_start));
                    } else if (endMarker == null) { // Comprobamos que la marca final sea nula
                        // Inicializamos el marcador
                        endMarker = new Marker(map);
                        // Establecemos la posición en el marcador
                        endMarker.setPosition(p);
                        // Establecemos el título
                        endMarker.setTitle("Punto de fin");
                        // Marcamos en el mapa el marcador
                        map.getOverlays().add(endMarker);
                        endMarker.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker_end));

                        // Llamamos al método para dibujar la línea de la ruta
                        dibujarLineaRuta();
                    }
                }else{
                    if (startMarker != null) {
                        map.getOverlays().remove(startMarker);
                    }

                    startMarker = new Marker(map);
                    startMarker.setPosition(p);
                    startMarker.setTitle("Ubicación del evento");
                    map.getOverlays().add(startMarker);
                    startMarker.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker_start));
                }

                // Invalidamos el mapa
                map.invalidate();
                // Retornamos true
                return true;
            }

            // Retornamos false
            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        MapEventsOverlay eventsOverlay = new MapEventsOverlay(mReceive);
        map.getOverlays().add(eventsOverlay);

        // Establecemos la acción que sucede al pulsar el botón de confirmar la ruta
        btnConfirmarRuta.setOnClickListener(v -> {
            Bundle bundle = new Bundle();

            if (tipoSeleccion.equals("ruta")) {
                if (startMarker != null && endMarker != null) {
                    bundle.putDouble("startLat", startMarker.getPosition().getLatitude());
                    bundle.putDouble("startLon", startMarker.getPosition().getLongitude());
                    bundle.putDouble("endLat", endMarker.getPosition().getLatitude());
                    bundle.putDouble("endLon", endMarker.getPosition().getLongitude());
                } else {
                    showToast("Selecciona inicio y fin");
                    return;
                }
                if(!Double.isNaN(distanciaRuta) && !Double.isNaN(tiempoRuta)){
                    bundle.putDouble("distanciaRuta", distanciaRuta);
                    bundle.putDouble("tiempoRuta", tiempoRuta);
                }else{
                    showToast("Ocurrió un error a la hora de guardar la distancia y el tiempo!!!");
                }
            } else {
                if (startMarker != null) {
                    bundle.putDouble("ubicacionLat", startMarker.getPosition().getLatitude());
                    bundle.putDouble("ubicacionLon", startMarker.getPosition().getLongitude());
                } else {
                    showToast("Selecciona la ubicación del evento");
                    return;
                }
            }

            bundle.putString("tipoEventoActual", getArguments().getString("tipoEventoActual", "otro"));
            getParentFragmentManager().setFragmentResult("rutaSeleccionada", bundle);
            Navigation.findNavController(v).popBackStack();
        });

        // Establecemos la acción que sucede al pulsar el botón de borrar la ruta
        btnBorrarRuta.setOnClickListener(v -> {
            // Comprobamos que la marca inicial no sea nula
            if (startMarker != null) {
                // Eliminarlo del mapa
                map.getOverlays().remove(startMarker);
                // Establecemos como nulo
                startMarker = null;
            }
            // Comprobamos que la marca de final no sea nula
            if (endMarker != null) {
                // Eliminarlo del mapa
                map.getOverlays().remove(endMarker);
                // Establecemos como nulo
                endMarker = null;
            }
            // Comprobamos que la línea dibuja no sea nula
            if (routeLine != null) {
                // Eliminarlo del mapa
                map.getOverlays().remove(routeLine);
                // Establecemos como nulo
                routeLine = null;
            }
            // Invalidamos el mapa
            map.invalidate();
        });

        // Establezco la acción que sucede al escribir sobre el searchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Llamo al método para buscar la localización
                buscarLocalizacion(query);
                // Retornamos true
                return true;
            }

            // Retornamos false
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Retornamos la vista
        return  view;
    }

    /**
     * @param query
     * Método en el que le pasamos como parametro
     * la query o busqueda que vamos a realizar y gracias
     * a un try catch para poder establecer en el mapa
     * lo que hemos buscado
     */
    private void buscarLocalizacion(String query) {
        if(!internetController.tieneConexion()){
            showToast("No tienes acceso a internet, conectese a una red!!");
            return;
        }

        // Utilizamos el geocoder para poder buscar
        Geocoder geocoder = new Geocoder(getContext());
        // Utilizamos un try catch para poder captar y tratar todas las posibles excepciones
        try {
            // Obtengo una lista de address con la query y el primer resultado que obtengo
            List<Address> addresses = geocoder.getFromLocationName(query, 1);
            // Compruebo que no sea nula, es decir que no exista el sitio
            if (addresses != null && !addresses.isEmpty()) { // En caso de que no sea nulo
                // Obtenemos la primera parte de la dirección
                android.location.Address address = addresses.get(0);
                // Creamos un geopunto obteniendo la latitud y longitu de la ubicación
                GeoPoint geoPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
                // Establecemos que se centre en el geopunto
                map.getController().setCenter(geoPoint);
                // Establecemos el zoom
                map.getController().setZoom(15.0);

                // Utilizamos un marker para marcar la ubicación que hemos obtenido
                Marker marker = new Marker(map);
                // Establecemos la posición en el marcador
                marker.setPosition(geoPoint);
                // Establecemos el título
                marker.setTitle(address.getAddressLine(0));
                // Establecemos el marcador en el mapa
                map.getOverlays().add(marker);
                // Invalidamos el mapa
                map.invalidate();
            } else { // En caso de que la ubicación sea nula
                // Lanzamos un toast indicando que la ubicación no fue encontrada
                showToast("Ubicación no encontrada");
            }
        } catch (IOException e) { // En caso de que surja alguna excepción
            // La imprimimos por consola
            e.printStackTrace();
            // Lanzamos un toast indicando que ocurrio un error al buscar la ubicación
            showToast("Error al buscar la ubicación");
        }
    }

    private void obtenerRutaConCallOSRM(GeoPoint start, GeoPoint end) {
        if(!internetController.tieneConexion()){
            showToast("No tienes acceso a internet, conectese a una red!!");
            return;
        }

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

                double durationInSeconds = json
                        .getJSONArray("routes")
                        .getJSONObject(0)
                        .getDouble("duration");

                int durationInMinutes = (int) Math.round(durationInSeconds / 60.0);

                double distanceInMeters = json
                        .getJSONArray("routes")
                        .getJSONObject(0)
                        .getDouble("distance");
                int distanceInKm = (int) Math.round(distanceInMeters / 1000.0);

                List<GeoPoint> geoPointsRuta = new ArrayList<>();
                for (int i = 0; i < coordinates.length(); i++) {
                    JSONArray coord = coordinates.getJSONArray(i);
                    double lon = coord.getDouble(0);
                    double lat = coord.getDouble(1);
                    geoPointsRuta.add(new GeoPoint(lat, lon));
                }

                requireActivity().runOnUiThread(() -> {
                    tiempoRuta = (double) durationInMinutes;
                    distanciaRuta = (double) distanceInKm;
                    showToast("Duración estimada: " + durationInMinutes + " min\nDistancia: " + distanceInKm + " km");
                    if (routeLine != null) map.getOverlayManager().remove(routeLine);

                    routeLine = new Polyline();
                    routeLine.setPoints(geoPointsRuta);
                    map.getOverlayManager().add(routeLine);
                    map.invalidate();
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> showToast("Error al calcular ruta"));
            }
        }).start();
    }


    private void dibujarLineaRuta() {
        if (startMarker != null && endMarker != null) {
            if (internetController != null && internetController.tieneConexion()) {
                obtenerRutaConCallOSRM(startMarker.getPosition(), endMarker.getPosition());
            } else {
                showToast("No se puede calcular la ruta. Verifica tu conexión a Internet.");
            }
        }
    }

    /**
     * @param mensaje
     * Método para ir matando los Toast y mostrar todos en el mismo para evitar
     * colas de Toasts y que se ralentice el dispositivo
     */
    public void showToast(String mensaje){
        if (this != null){
            // Comprobamos si existe algun toast cargado en el toast de la variable global
            if (mensajeToast != null) { // En caso de que si que exista
                mensajeToast.cancel(); // Le cancelamos, es decir le "matamos"
            }

            // Creamos un nuevo Toast con el mensaje que nos dan de argumento en el método
            mensajeToast = Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT);
            // Mostramos dicho Toast
            mensajeToast.show();
        }
    }
}
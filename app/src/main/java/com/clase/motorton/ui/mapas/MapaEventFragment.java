package com.clase.motorton.ui.mapas;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.clase.motorton.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapaEventFragment extends Fragment {
    private MapView map = null;
    private Marker startMarker = null;
    private Marker endMarker = null;
    private Polyline routeLine = null;
    private Button btnConfirmarRuta = null;
    private Button btnBorrarRuta = null;
    private SearchView searchView = null;

    private Toast mensajeToast = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mapa_event, container, false);
        map = view.findViewById(R.id.map);
        btnConfirmarRuta = view.findViewById(R.id.btnConfirmarRuta);
        btnBorrarRuta = view.findViewById(R.id.btnBorrarRuta);
        searchView = view.findViewById(R.id.searchView);

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);
        map.getController().setCenter(new GeoPoint(40.4168, -3.7038));

        map.setOnTouchListener((v, event) -> false);

        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                if (startMarker == null) {
                    startMarker = new Marker(map);
                    startMarker.setPosition(p);
                    startMarker.setTitle("Punto de inicio");
                    map.getOverlays().add(startMarker);
                } else if (endMarker == null) {
                    endMarker = new Marker(map);
                    endMarker.setPosition(p);
                    endMarker.setTitle("Punto de fin");
                    map.getOverlays().add(endMarker);

                    dibujarLineaRuta();
                }
                map.invalidate();
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        MapEventsOverlay eventsOverlay = new MapEventsOverlay(mReceive);
        map.getOverlays().add(eventsOverlay);

        btnConfirmarRuta.setOnClickListener(v -> {
            if (startMarker != null && endMarker != null) {
                Bundle bundle = new Bundle();
                bundle.putDouble("startLat", startMarker.getPosition().getLatitude());
                bundle.putDouble("startLon", startMarker.getPosition().getLongitude());
                bundle.putDouble("endLat", endMarker.getPosition().getLatitude());
                bundle.putDouble("endLon", endMarker.getPosition().getLongitude());

                getParentFragmentManager().setFragmentResult("rutaSeleccionada", bundle);

                Navigation.findNavController(view).navigate(R.id.navigation_createEvent, bundle);
            } else {
                showToast("Selecciona inicio y fin");
            }
        });

        btnBorrarRuta.setOnClickListener(v -> {
            if (startMarker != null) {
                map.getOverlays().remove(startMarker);
                startMarker = null;
            }
            if (endMarker != null) {
                map.getOverlays().remove(endMarker);
                endMarker = null;
            }
            if (routeLine != null) {
                map.getOverlays().remove(routeLine);
                routeLine = null;
            }
            map.invalidate();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                buscarLocalizacion(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return  view;
    }

    private void buscarLocalizacion(String query) {
        Geocoder geocoder = new Geocoder(getContext());
        try {
            List<Address> addresses = geocoder.getFromLocationName(query, 1);
            if (addresses != null && !addresses.isEmpty()) {
                android.location.Address address = addresses.get(0);
                GeoPoint geoPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
                map.getController().setCenter(geoPoint);
                map.getController().setZoom(15.0);

                Marker marker = new Marker(map);
                marker.setPosition(geoPoint);
                marker.setTitle(address.getAddressLine(0));
                map.getOverlays().add(marker);
                map.invalidate();
            } else {
                showToast("Ubicación no encontrada");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showToast("Error al buscar la ubicación");
        }
    }

    private void dibujarLineaRuta() {
        if (startMarker != null && endMarker != null) {
            List<GeoPoint> geoPoints = new ArrayList<>();
            geoPoints.add(startMarker.getPosition());
            geoPoints.add(endMarker.getPosition());

            if (routeLine != null) map.getOverlayManager().remove(routeLine);

            routeLine = new Polyline();
            routeLine.setPoints(geoPoints);
            map.getOverlayManager().add(routeLine);
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
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

    // Variable para controlar todos los Toast de la actividad
    private Toast mensajeToast = null;

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

    /**
     * @param query
     * Método en el que le pasamos como parametro
     * la query o busqueda que vamos a realizar y gracias
     * a un try catch para poder establecer en el mapa
     * lo que hemos buscado
     */
    private void buscarLocalizacion(String query) {
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

    /**
     * Método para dibujar la línea
     * de la ruta para así poder mostrar
     * al usuario la ruta que ha establecido
     */
    private void dibujarLineaRuta() {
        // Procedemos a comprobar si el marcador del inicio y del final no sean nulos
        if (startMarker != null && endMarker != null) { // En caso de que no sean nulos
            // Creamos una lista de geopuntos y la inicializamos
            List<GeoPoint> geoPoints = new ArrayList<>();
            // Agregamos los dos puntos
            geoPoints.add(startMarker.getPosition());
            geoPoints.add(endMarker.getPosition());

            // Comprobamos que el polyline no sea nulo, en caso de no serlo, le eliminamos del mapa
            if (routeLine != null) map.getOverlayManager().remove(routeLine);

            // Inicializamos una nueva polyline para dibujar
            routeLine = new Polyline();
            // Establecemos la lista de puntos
            routeLine.setPoints(geoPoints);
            // Agregamos el dibujo al mapa
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
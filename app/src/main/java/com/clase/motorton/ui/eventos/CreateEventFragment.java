package com.clase.motorton.ui.eventos;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.clase.motorton.R;
import com.clase.motorton.adaptadores.SpinnerAdaptarNormal;
import com.clase.motorton.cifrado.CifradoDeDatos;
import com.clase.motorton.modelos.Evento;
import com.clase.motorton.servicios.InternetController;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

public class CreateEventFragment extends Fragment {
    // Variable para manejar el autenticado de firebase
    private FirebaseAuth mAuth = null;
    // Variable para manejar la base de datos de firebase
    private FirebaseFirestore db = null;

    // Variable para manejar todos los editText del fragmento
    private EditText editTextNombreEvento = null, editTextDescripcion = null;
    // Variable para manejar el spinner del tipo de evento
    private Spinner spinnerTipoEvento = null;
    // Variable para manejar el selector de fecha
    private DatePicker datePickerFecha = null;
    // Variable para manejar el botón de crear el evento
    private Button buttonCrearEvento = null;
    // Variable para visualizar el mapa
    private MapView mapView = null;
    // Variable para manejar el botón para ir a seleccionar la ruta
    private Button btnIrRuta = null;
    // Variable para manejar el spinner de las provincias
    private Spinner spinnerProvincia = null;

    // Variable para manejar la latitud del inicio
    private double latInicio = Double.NaN;
    // Variable para manejar la latitud del final
    private double latFin = Double.NaN;
    // Variable para manejar la longitud del inicio
    private double lonInicio = Double.NaN;
    // Variable para manejar la longitud del final
    private double lonFin = Double.NaN;
    private double distancia = Double.NaN;
    private double tiempo = Double.NaN;
    // Variable para manejar la provincia elegida
    private String provincia = null;

    // Variable para manejar la lista de los tipos de eventos
    private List<String> tiposEvento = new ArrayList<>();
    // Variable para manejar la lista de las provincias
    private List<String> provincias = new ArrayList<>();

    // Variable para controlar el cifrado de datos
    private CifradoDeDatos cifrar = null;
    // Variable para controlar la clave secreta del cifrado de datos
    private SecretKey claveSecreta = null;
    // Variable para manejar los Toast de está actividad
    private Toast mensajeToast= null;

    private String tipoEvento = null;
    // Variable para manejar la línea de la ruta
    private Polyline routeLine = null;

    private InternetController internetController = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflamos la vista
        View root = inflater.inflate(R.layout.fragment_create_event, container, false);

        // Inicializamos Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializo el controlador de internet
        internetController = new InternetController(getContext());

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        // Obtenemos referencias a los elementos de la interfaz
        editTextNombreEvento = root.findViewById(R.id.editTextNombreEvento);
        editTextDescripcion = root.findViewById(R.id.editTextDescripcion);
        spinnerTipoEvento = root.findViewById(R.id.spinnerTipoEvento);
        datePickerFecha = root.findViewById(R.id.datePickerFecha);
        buttonCrearEvento = root.findViewById(R.id.buttonCrearEvento);
        mapView = root.findViewById(R.id.map);
        btnIrRuta = root.findViewById(R.id.buttonIrRuta);
        spinnerProvincia = root.findViewById(R.id.spinnerProvincia);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(new GeoPoint(40.4168, -3.7038));

        // Agrego todos los tipos de eventos posibles
        tiposEvento.add("Quedada Motos");
        tiposEvento.add("Quedada Coches");
        tiposEvento.add("Ruta MultiVehículo");
        tiposEvento.add("Ruta Moto");
        tiposEvento.add("Ruta Coche");
        tiposEvento.add("Ruta");
        tiposEvento.add("Slalom");
        tiposEvento.add("Rally");
        tiposEvento.add("Otros");

        // Agrego todas las provincias posibles
        provincias.add("Álava");
        provincias.add("Albacete");
        provincias.add("Alicante");
        provincias.add("Almería");
        provincias.add("Asturias");
        provincias.add("Ávila");
        provincias.add("Badajoz");
        provincias.add("Barcelona");
        provincias.add("Burgos");
        provincias.add("Cáceres");
        provincias.add("Cádiz");
        provincias.add("Cantabria");
        provincias.add("Castellón");
        provincias.add("Ciudad Real");
        provincias.add("Córdoba");
        provincias.add("La Coruña");
        provincias.add("Cuenca");
        provincias.add("Gerona");
        provincias.add("Granada");
        provincias.add("Guadalajara");
        provincias.add("Guipúzcoa");
        provincias.add("Huelva");
        provincias.add("Huesca");
        provincias.add("Islas Baleares");
        provincias.add("Jaén");
        provincias.add("León");
        provincias.add("Lérida");
        provincias.add("Lugo");
        provincias.add("Madrid");
        provincias.add("Málaga");
        provincias.add("Murcia");
        provincias.add("Navarra");
        provincias.add("Orense");
        provincias.add("Palencia");
        provincias.add("Las Palmas");
        provincias.add("Pontevedra");
        provincias.add("La Rioja");
        provincias.add("Salamanca");
        provincias.add("Santa Cruz de Tenerife");
        provincias.add("Segovia");
        provincias.add("Sevilla");
        provincias.add("Soria");
        provincias.add("Tarragona");
        provincias.add("Teruel");
        provincias.add("Toledo");
        provincias.add("Valencia");
        provincias.add("Valladolid");
        provincias.add("Vizcaya");
        provincias.add("Zamora");
        provincias.add("Zaragoza");
        provincias.add("Ceuta");
        provincias.add("Melilla");

        // Inicializo el cifrador de datos
        cifrar = new CifradoDeDatos();

        // Inicializo el adaptador para el spinner de eventos
        SpinnerAdaptarNormal adapter = new SpinnerAdaptarNormal(getContext(), tiposEvento);
        // Establezco el adaptador al spinner
        spinnerTipoEvento.setAdapter(adapter);

        // Inicializo el adaptador para el spinner de provincias
        SpinnerAdaptarNormal adapter2 = new SpinnerAdaptarNormal(getContext(), provincias);
        // Establezco el adaptador al spinner
        spinnerProvincia.setAdapter(adapter2);

        // Establezco la acción que sucede cuando clicamos el botón de crear evento
        buttonCrearEvento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(internetController.tieneConexion()){
                    // Llamo al método para crear el evento
                    crearEvento();
                }else{
                    // Lanzo un toast para indicar al usuario que no tiene internet
                    showToast("No tienes acceso a internet, conectese a una red!!");
                }
            }
        });

        // Establecemos el evento al elegir una opción en el spinner
        spinnerTipoEvento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Obtengo el item elegido
                String selectedItem = parentView.getItemAtPosition(position).toString();

                if (selectedItem.contains("Ruta") || selectedItem.contains("ruta")) {
                    tipoEvento = "ruta";
                } else {
                    tipoEvento = "otro";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        // Establezco la acción que sucede cuando clicamos el botón de ir a seleccionar Ruta
        btnIrRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Utilizamos un try catch para capturar y tratar todas las posibles excepciones
                try {
                    Bundle bundle = new Bundle();
                    if(tipoEvento.equals("ruta")){
                        bundle.putString("tipoSeleccion", "ruta");
                    }else{
                        bundle.putString("tipoSeleccion", "ubicacion");
                    }
                    String selectedItem = spinnerTipoEvento.getSelectedItem().toString();
                    bundle.putString("tipoEventoActual", selectedItem);
                    Navigation.findNavController(view).navigate(R.id.navigation_map_ruta, bundle);
                } catch (Exception e) { // En caso de que surja alguna excepción
                    // Imprimimos la excepción por consola
                    e.printStackTrace();
                    // Lanzamos un toast indicando que ocurrió un error al navegar
                    showToast("Error al navegar");
                }
            }
        });

        getParentFragmentManager().setFragmentResultListener("rutaSeleccionada", this, (key, bundle) -> {
            double startLat = bundle.getDouble("startLat", Double.NaN);
            double startLon = bundle.getDouble("startLon", Double.NaN);
            double endLat = bundle.getDouble("endLat", Double.NaN);
            double endLon = bundle.getDouble("endLon", Double.NaN);
            double ubicacionLat = bundle.getDouble("ubicacionLat", Double.NaN);
            double ubicacionLon = bundle.getDouble("ubicacionLon", Double.NaN);

            double tiem = bundle.getDouble("tiempoRuta", Double.NaN);
            double dista = bundle.getDouble("distanciaRuta", Double.NaN);

            mapView.getOverlays().clear();

            if (getArguments() != null && getArguments().containsKey("tipoEventoActual")) {
                String tipoSeleccion = getArguments().getString("tipoEventoActual");
                int posicion = tiposEvento.indexOf(tipoSeleccion);
                if (posicion != -1) {
                    spinnerTipoEvento.setSelection(posicion);
                }
            }

            if(!Double.isNaN(tiem) && !Double.isNaN(dista)){
                tiempo = tiem;
                distancia = dista;
            }else{
                tiempo = 0.0;
                distancia = 0.0;
            }

            if (!Double.isNaN(startLat) && !Double.isNaN(startLon) &&
                    !Double.isNaN(endLat) && !Double.isNaN(endLon)) {
                // Caso RUTA
                this.latInicio = startLat;
                this.lonInicio = startLon;
                this.latFin = endLat;
                this.lonFin = endLon;
                dibujarMarcadoresRuta(startLat, startLon, endLat, endLon);
                dibujarRuta(startLat, startLon, endLat, endLon);
            } else if (!Double.isNaN(ubicacionLat) && !Double.isNaN(ubicacionLon)) {
                // Caso UBICACIÓN ÚNICA
                this.latInicio = ubicacionLat;
                this.lonInicio = ubicacionLon;
                this.latFin = Double.NaN;
                this.lonFin = Double.NaN;
                dibujarMarcadorUnico(ubicacionLat, ubicacionLon);
            } else {
                showToast("Error: No se recibieron coordenadas válidas");
            }

            mapView.invalidate();
        });

        return root;
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

    private void dibujarMarcadorUnico(double lat, double lon) {
        Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(lat, lon));
        marker.setTitle("Ubicación del evento");
        mapView.getOverlays().add(marker);

        mapView.getController().setCenter(new GeoPoint(lat, lon));
        mapView.getController().setZoom(15.0);
    }

    private void dibujarRuta(double latInicio, double lonInicio, double latFin, double lonFin) {
        new Thread(() -> {
            try {
                String urlString = "https://router.project-osrm.org/route/v1/driving/" +
                        lonInicio + "," + latInicio + ";" +
                        lonFin + "," + latFin +
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
                requireActivity().runOnUiThread(() -> showToast("Error al cargar la ruta"));
            }
        }).start();
    }

    /**
     * @param puntosRuta
     * Método en el calculamos los maximos y minimos
     * posibles de los double y además calculamos
     * las coordenadas maximas y minimas para
     * ajustar todo
     */
    private BoundingBox calculateBoundingBox(List<GeoPoint> puntosRuta) {
        // Calculo los máximos y minimos relativos
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;

        // Gracias a un foreach para ir estableciento los minimos y maximos basandonos en los calculados antes y en las
        // coordenadas
        for (GeoPoint point : puntosRuta) {
            minLat = Math.min(minLat, point.getLatitude());
            maxLat = Math.max(maxLat, point.getLatitude());
            minLon = Math.min(minLon, point.getLongitude());
            maxLon = Math.max(maxLon, point.getLongitude());
        }

        // Retornamos para justar e boundingbox del mapa
        return new BoundingBox(maxLat, maxLon, minLat, minLon);
    }

    /**
     * Método en el que obtenemos todos los valores necesarios
     * para crear un evento en variables, posteriormente
     * creamos un objeto de tipo evento y luego ya le subimos a nuestra
     * base de datos en firebase
     */
    private void crearEvento() {
        if (!internetController.tieneConexion()) {
            showToast("No hay conexión a Internet");
            return;
        }

        // Obtenemos todos los datos de los spinners y de los editText
        String nombre = editTextNombreEvento.getText().toString().trim();
        String descripcion = editTextDescripcion.getText().toString().trim();
        String provincia = spinnerProvincia.getSelectedItem().toString();
        String tipoEvento = spinnerTipoEvento.getSelectedItem().toString();

        if(nombre.isEmpty() || descripcion.isEmpty() ||provincia.isEmpty() ||tipoEvento.isEmpty() ||
                nombre == null || descripcion == null  ||provincia == null ||tipoEvento == null){
            showToast("Existen campos vacíos, rellenelos. Por favor!!!");
            return;
        }

        if (Double.isNaN(latInicio) && Double.isNaN(lonInicio) &&
                Double.isNaN(latFin) && Double.isNaN(lonFin)) {
            showToast("Selecciona una ubicación o ruta válida");
            return;
        }

        // Obtengo del seleccionador de fecha todos los datos necesarios
        int dia = datePickerFecha.getDayOfMonth();
        int mes = datePickerFecha.getMonth();
        int anio = datePickerFecha.getYear();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, anio);
        calendar.set(Calendar.MONTH, mes);
        calendar.set(Calendar.DAY_OF_MONTH, dia);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date fechaEvento = calendar.getTime();

        boolean esRuta = spinnerTipoEvento.getSelectedItem().toString().contains("Ruta");

        // Obtengo el uid del organizador del evento
        String uidOrganizador = mAuth.getCurrentUser().getUid();

        // Creo el objeto
        Evento evento = new Evento();
        // Establezco todos los parametros que componen al evento
        evento.setNombre(nombre);
        evento.setDescripcion(descripcion);
        evento.setProvincia(provincia);
        evento.setTipoEvento(tipoEvento);
        evento.setFecha(fechaEvento);
        evento.setOrganizador(uidOrganizador);
        String eventoId = uidOrganizador + "_" + System.currentTimeMillis();
        evento.setId(eventoId);
        evento.setActivo(true);
        evento.setParticipantes(new ArrayList<String>());
        evento.setDistanciaRuta(distancia);
        evento.setTiempoRuta(tiempo);

        if (esRuta && !Double.isNaN(latInicio) && !Double.isNaN(lonInicio) &&
                !Double.isNaN(latFin) && !Double.isNaN(lonFin)) {
            evento.setStartLat(latInicio);
            evento.setStartLon(lonInicio);
            evento.setEndLat(latFin);
            evento.setEndLon(lonFin);
            evento.setEsRuta(true);
        } else if (!Double.isNaN(latInicio) && !Double.isNaN(lonInicio)) {
            evento.setUbicacionLat(latInicio);
            evento.setUbicacionLon(lonInicio);
            evento.setEsRuta(false);
        } else {
            showToast("Selecciona una ubicación o ruta válida");
            return;
        }

        // Procedemos a entrar dentro de la colección de la base de datos de eventos
        db.collection("eventos")
                .add(evento) // Agregamos el nuevo evento
                .addOnSuccessListener(documentReference -> { // En caso de que salga bien
                    // Lanzamos un toast indicandoselo
                    showToast("Evento creado con éxito");
                    // Llamamos al método para limpiar el formulario
                    limpiarFormulario();
                })
                .addOnFailureListener(e -> { // En caso de que algo salga mal
                    // Lanzamos un toast indicandoselo
                    showToast("Error al crear el evento");
                });
    }

    /**
     * Método en el que limpiamos el formulario
     * restableciendo la fecha del selector de fecha
     * y los editText
     */
    private void limpiarFormulario() {
        editTextNombreEvento.setText("");
        editTextDescripcion.setText("");
        datePickerFecha.updateDate(Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
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
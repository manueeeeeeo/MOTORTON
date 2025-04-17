package com.clase.motorton.ui.eventos;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

public class CreateEventFragment extends Fragment {
    private FirebaseAuth mAuth = null;
    private FirebaseFirestore db = null;

    private EditText editTextNombreEvento = null, editTextDescripcion = null, editTextUbicacion = null;
    private Spinner spinnerTipoEvento = null;
    private DatePicker datePickerFecha = null;
    private Button buttonCrearEvento = null;
    private MapView mapView = null;
    private Button btnIrRuta = null;
    private Spinner spinnerProvincia = null;

    private double latInicio = 0.0;
    private double latFin = 0.0;
    private double lonInicio = 0.0;
    private double lonFin = 0.0;
    private String provincia = null;

    private List<String> tiposEvento = new ArrayList<>();
    private List<String> provincias = new ArrayList<>();

    private CifradoDeDatos cifrar = null;
    private SecretKey claveSecreta = null;
    private Toast mensajeToast= null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar la vista
        View root = inflater.inflate(R.layout.fragment_create_event, container, false);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Obtener referencias a los elementos de la interfaz
        editTextNombreEvento = root.findViewById(R.id.editTextNombreEvento);
        editTextDescripcion = root.findViewById(R.id.editTextDescripcion);
        editTextUbicacion = root.findViewById(R.id.editTextUbicacion);
        spinnerTipoEvento = root.findViewById(R.id.spinnerTipoEvento);
        datePickerFecha = root.findViewById(R.id.datePickerFecha);
        buttonCrearEvento = root.findViewById(R.id.buttonCrearEvento);
        mapView = root.findViewById(R.id.map);
        btnIrRuta = root.findViewById(R.id.buttonIrRuta);
        spinnerProvincia = root.findViewById(R.id.spinnerProvincia);

        mapView.setTileSource(TileSourceFactory.MAPNIK);  // Usar Mapnik para el fondo del mapa
        mapView.setBuiltInZoomControls(true);  // Activar controles de zoom
        mapView.setMultiTouchControls(true);

        tiposEvento = new ArrayList<>();
        tiposEvento.add("Quedada Motos");
        tiposEvento.add("Quedada Coches");
        tiposEvento.add("Ruta MultiVehículo");
        tiposEvento.add("Ruta Moto");
        tiposEvento.add("Ruta Coche");
        tiposEvento.add("Ruta");
        tiposEvento.add("Slalom");
        tiposEvento.add("Rally");
        tiposEvento.add("Otros");

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



        cifrar = new CifradoDeDatos();
        try {
            CifradoDeDatos.generarClaveSiNoExiste();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SpinnerAdaptarNormal adapter = new SpinnerAdaptarNormal(getContext(), tiposEvento);
        spinnerTipoEvento.setAdapter(adapter);

        SpinnerAdaptarNormal adapter2 = new SpinnerAdaptarNormal(getContext(), provincias);
        spinnerProvincia.setAdapter(adapter2);

        buttonCrearEvento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crearEvento();
            }
        });

        spinnerTipoEvento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedItem = parentView.getItemAtPosition(position).toString();

                if ("Ruta Moto".equals(selectedItem) || "Ruta Coche".equals(selectedItem)) {
                    mapView.setVisibility(View.VISIBLE);
                    btnIrRuta.setVisibility(View.VISIBLE);
                } else {
                    mapView.setVisibility(View.GONE);
                    btnIrRuta.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        btnIrRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        getParentFragmentManager().setFragmentResultListener("rutaSeleccionada", this, (key, bundle) -> {
            double startLat = bundle.getDouble("startLat", Double.NaN);
            double startLon = bundle.getDouble("startLon", Double.NaN);
            double endLat = bundle.getDouble("endLat", Double.NaN);
            double endLon = bundle.getDouble("endLon", Double.NaN);

            if (Double.isNaN(startLat) || Double.isNaN(startLon) || Double.isNaN(endLat) || Double.isNaN(endLon)) {
                showToast("Error: coordenadas de la ruta no válidas");
                return;
            }

            this.latInicio = startLat;
            this.lonInicio = startLon;
            this.latFin = endLat;
            this.lonFin = endLon;

            dibujarRuta(latInicio, lonInicio, latFin, lonFin);
        });

        return root;
    }

    private void dibujarRuta(double latInicio, double lonInicio, double latFin, double lonFin) {
        GeoPoint puntoInicio = new GeoPoint(latInicio, lonInicio);
        GeoPoint puntoFin = new GeoPoint(latFin, lonFin);

        List<GeoPoint> puntosRuta = new ArrayList<>();
        puntosRuta.add(puntoInicio);
        puntosRuta.add(puntoFin);

        Polyline polyline = new Polyline();
        polyline.setPoints(puntosRuta);
        polyline.setColor(Color.BLUE);
        polyline.setWidth(5);

        mapView.getOverlays().add(polyline);

        BoundingBox boundingBox = calculateBoundingBox(puntosRuta);

        mapView.zoomToBoundingBox(boundingBox, true);
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

    private void crearEvento() {
        String nombre = editTextNombreEvento.getText().toString().trim();
        String descripcion = editTextDescripcion.getText().toString().trim();
        String ubicacion = editTextUbicacion.getText().toString().trim();
        String provincia = spinnerProvincia.getSelectedItem().toString();
        String tipoEvento = spinnerTipoEvento.getSelectedItem().toString();

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

        String uidOrganizador = mAuth.getCurrentUser().getUid();

        Evento evento = new Evento();
        evento.setNombre(nombre);
        evento.setDescripcion(descripcion);
        evento.setUbicacion(ubicacion);
        evento.setProvincia(provincia);
        evento.setTipoEvento(tipoEvento);
        evento.setFecha(fechaEvento);
        evento.setOrganizador(uidOrganizador);
        String eventoId = uidOrganizador + "_" + System.currentTimeMillis();
        evento.setId(eventoId);
        evento.setActivo(true);
        evento.setParticipantes(new ArrayList<>());

        db.collection("eventos")
                .add(evento)
                .addOnSuccessListener(documentReference -> {
                    showToast("Evento creado con éxito");
                    limpiarFormulario();
                })
                .addOnFailureListener(e -> {
                    showToast("Error al crear el evento");
                });
    }

    // Limpiar los campos después de crear el evento
    private void limpiarFormulario() {
        editTextNombreEvento.setText("");
        editTextDescripcion.setText("");
        editTextUbicacion.setText("");
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
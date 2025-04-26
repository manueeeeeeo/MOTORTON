package com.clase.motorton.ui.eventos;

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
import android.widget.TextView;
import android.widget.Toast;

import com.clase.motorton.R;
import com.clase.motorton.adaptadores.SpinnerAdaptarNormal;
import com.clase.motorton.cifrado.CifradoDeDatos;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.crypto.SecretKey;

public class EditarEventoFragment extends Fragment {
    // Variable para manejar el autenticado de firebase
    private FirebaseAuth mAuth = null;
    // Variable para manejar la base de datos de firebase
    private FirebaseFirestore db = null;

    // Variable para manejar todos los editText del fragmento
    private EditText editTextDescripcion = null;
    // Variable para manejar el selector de fecha
    private DatePicker datePickerFecha = null;
    // Variable para manejar el botón de crear el evento
    private Button buttonCrearEvento = null;
    // Variable para visualizar el mapa
    private MapView mapView = null;
    private TextView tituloEvento = null;

    // Variable para manejar la latitud del inicio
    private double latInicio = 0.0;
    // Variable para manejar la latitud del final
    private double latFin = 0.0;
    // Variable para manejar la longitud del inicio
    private double lonInicio = 0.0;
    // Variable para manejar la longitud del final
    private double lonFin = 0.0;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_editar_evento, container, false);

        // Inicializamos Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Obtenemos referencias a los elementos de la interfaz
        editTextDescripcion = view.findViewById(R.id.editTextDescripcion);
        datePickerFecha = view.findViewById(R.id.datePickerFecha);
        buttonCrearEvento = view.findViewById(R.id.buttonCrearEvento);
        mapView = view.findViewById(R.id.map);
        tituloEvento = view.findViewById(R.id.textView11);

        cargarEvento();

        mapView.setTileSource(TileSourceFactory.MAPNIK);  // Usar Mapnik para el fondo del mapa
        mapView.setBuiltInZoomControls(true);  // Activar controles de zoom
        mapView.setMultiTouchControls(true);

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

        // Establezco la acción que sucede cuando clicamos el botón de crear evento
        buttonCrearEvento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Llamo al método para crear el evento
                actualizarEvento();
            }
        });

        // Obtenemos si hay datos pasados de un fragmento a otro
        getParentFragmentManager().setFragmentResultListener("rutaSeleccionada", this, (key, bundle) -> {
            // Obtengo el dato pasado que es un double
            double startLat = bundle.getDouble("startLat", Double.NaN);
            // Obtengo el dato pasado que es un double
            double startLon = bundle.getDouble("startLon", Double.NaN);
            // Obtengo el dato pasado que es un double
            double endLat = bundle.getDouble("endLat", Double.NaN);
            // Obtengo el dato pasado que es un double
            double endLon = bundle.getDouble("endLon", Double.NaN);

            // Compruebo que sean números y no estén vacíos
            if (Double.isNaN(startLat) || Double.isNaN(startLon) || Double.isNaN(endLat) || Double.isNaN(endLon)) { // En caso negativo
                // Lanzo un toast indicando que las coordenadas de la ruta no son válidas
                showToast("Error: coordenadas de la ruta no válidas");
                // Retornamos para no proseguir
                return;
            }

            // Inicializo el valor de inicio de la latitud
            this.latInicio = startLat;
            // Inicializo el valor de inicio de la longitud
            this.lonInicio = startLon;
            // Inicializo el valor de final de la latidud
            this.latFin = endLat;
            // Inicializo el valor de final de la longitud
            this.lonFin = endLon;

            // Llamamos al método para dibujar la ruta en el mapa
            //dibujarRuta(latInicio, lonInicio, latFin, lonFin);
        });

        return view;
    }

    private void cargarEvento(){

    }

    private void actualizarEvento(){

    }

    /**
     * Método en el que limpiamos el formulario
     * restableciendo la fecha del selector de fecha
     * y los editText
     */
    private void limpiarFormulario() {
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
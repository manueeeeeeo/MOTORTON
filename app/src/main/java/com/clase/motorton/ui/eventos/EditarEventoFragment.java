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
import com.google.firebase.firestore.DocumentSnapshot;
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

        String idPasado = null;
        if (getArguments() != null && getArguments().containsKey("eventoId")) {
            idPasado = getArguments().getString("eventoId");
            cargarEvento(idPasado);
        }

        mapView.setTileSource(TileSourceFactory.MAPNIK);  // Usar Mapnik para el fondo del mapa
        mapView.setBuiltInZoomControls(true);  // Activar controles de zoom
        mapView.setMultiTouchControls(true);

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

    private void cargarEvento(String eventoID) {
        db.collection("eventos")
                .whereEqualTo("id", eventoID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                        String nombre = document.getString("nombre");
                        String fecha = document.getString("fecha");
                        String descripcion = document.getString("descripcion");

                        tituloEvento.setText("Evento "+nombre);
                        editTextDescripcion.setText(descripcion);

                        if (fecha != null) {
                            String[] partes = fecha.split("-");
                            if (partes.length == 3) {
                                int year = Integer.parseInt(partes[0]);
                                int month = Integer.parseInt(partes[1]) - 1; // DatePicker: enero=0
                                int day = Integer.parseInt(partes[2]);
                                datePickerFecha.updateDate(year, month, day);
                            }
                        }

                    } else {
                        showToast("No se encontró el evento.");
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Error al cargar el evento.");
                });
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
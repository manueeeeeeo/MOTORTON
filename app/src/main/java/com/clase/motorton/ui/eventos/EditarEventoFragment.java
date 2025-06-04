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
import com.clase.motorton.servicios.InternetController;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;


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
    // Variable para manejar el textview del título
    private TextView tituloEvento = null;

    // Variable para controlar el cifrado de datos
    private CifradoDeDatos cifrar = null;
    // Variable para manejar los Toast de está actividad
    private Toast mensajeToast= null;

    private String descripAnti = null;
    private Timestamp fechaAnti = null;
    private Date fechaEvento = null;
    private String documentoEventoID = null;

    private InternetController internetController = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_editar_evento, container, false);

        // Inicializamos Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializo el controlador de internet
        internetController = new InternetController(getContext());

        if(!internetController.tieneConexion()){
            showToast("No tienes acceso a internet, conectese a una red!!");
        }

        // Obtenemos referencias a los elementos de la interfaz
        editTextDescripcion = (EditText) view.findViewById(R.id.editTextDescripcion);
        datePickerFecha = (DatePicker) view.findViewById(R.id.datePickerFecha);
        buttonCrearEvento = (Button) view.findViewById(R.id.buttonActualizarEvento);
        tituloEvento = (TextView) view.findViewById(R.id.textViewTituloEventoAc);

        // Inicializo el cifrador de datos
        cifrar = new CifradoDeDatos();

        // Establezco la acción que sucede cuando clicamos el botón de crear evento
        buttonCrearEvento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(internetController.tieneConexion()){
                    // Llamo al método para crear el evento
                    actualizarEvento();
                }else{
                    showToast("No tienes acceso a internet, conectese a una red!!");
                }
            }
        });

        if (getArguments() != null && getArguments().containsKey("eventoId")) {
            String eventId = getArguments().getString("eventoId");
            if (eventId != null) {
                documentoEventoID = eventId;
                if (internetController.tieneConexion()) {
                    cargarEvento(eventId);
                } else {
                    showToast("No tienes conexión a internet. No se puede cargar el evento.");
                }
            } else {
                showToast("Error: No se recibió el ID del evento");
            }
        } else {
            showToast("Error: Argumentos nulos");
        }

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
                        if (nombre == null) {
                            nombre = "Sin nombre"; // Valor predeterminado
                        }
                        Timestamp timestamp = document.getTimestamp("fecha");
                        String descripcion = document.getString("descripcion");
                        if (descripcion == null) {
                            descripcion = ""; // Valor predeterminado
                        }

                        descripAnti = descripcion;
                        fechaAnti = timestamp;

                        tituloEvento.setText("Evento "+nombre);
                        editTextDescripcion.setText(descripcion);

                        if (timestamp != null) {
                            Date fechaDate = timestamp.toDate();

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(fechaDate);

                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH);
                            int day = calendar.get(Calendar.DAY_OF_MONTH);

                            datePickerFecha.updateDate(year, month, day);

                            fechaEvento = calendar.getTime();
                        }

                    } else {
                        showToast("No se encontró el evento.");
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Error al cargar el evento.");
                });
    }

    private void actualizarEvento() {
        Map<String, Object> cambios = new HashMap<>();

        if (!descripAnti.equals(editTextDescripcion.getText().toString())) {
            if(editTextDescripcion.getText().toString() != null || !editTextDescripcion.getText().toString().isEmpty()){
                cambios.put("descripcion", editTextDescripcion.getText().toString());
            }else{
                showToast("No puede dejar la descripción vacía");
                return;
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(datePickerFecha.getYear(), datePickerFecha.getMonth(), datePickerFecha.getDayOfMonth());
        Date nuevaFecha = calendar.getTime();

        if (!fechaAnti.toDate().equals(nuevaFecha)) {
            cambios.put("fecha", nuevaFecha);
        }

        if (!cambios.isEmpty() && documentoEventoID != null) {
            db.collection("eventos")
                    .whereEqualTo("id", documentoEventoID)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String docId = queryDocumentSnapshots.getDocuments().get(0).getId();

                            db.collection("eventos").document(docId)
                                    .update(cambios)
                                    .addOnSuccessListener(aVoid -> {
                                        showToast("Evento actualizado correctamente.");
                                        Navigation.findNavController(requireView()).navigate(R.id.navigation_home, null);
                                    })
                                    .addOnFailureListener(e -> {
                                        showToast("Error al actualizar el evento.");
                                    });
                        } else {
                            showToast("No se encontró el evento para actualizar.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        showToast("Error al buscar el evento.");
                    });
        } else {
            showToast("No hay cambios para actualizar.");
        }
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
package com.clase.motorton.ui.vehiculos;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.clase.motorton.R;
import com.clase.motorton.modelos.Evento;
import com.clase.motorton.modelos.Vehiculo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FichaVehiculoFragment extends Fragment {
    // Variable para manejar la base de datos de firestore
    private FirebaseFirestore db = null;

    // Variable para manejar la autentificación del usuario
    private FirebaseAuth auth = null;

    private ImageView imagenVehiculo = null;
    private TextView marcaYModelo = null;
    private TextView descripcion = null;
    private TextView tuboEscape = null;
    private TextView ruedas = null;
    private TextView aleron = null;
    private TextView anos = null;
    private TextView matricula = null;
    private TextView bodyKit = null;
    private TextView lucesLd = null;
    private TextView cv = null;
    private TextView maxV = null;
    private TextView exportado = null;
    private TextView choques = null;

    private Toast mensajeToast = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_ficha_vehiculo, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        marcaYModelo = (TextView) root.findViewById(R.id.txtModelo);
        descripcion = (TextView) root.findViewById(R.id.txtDescripcion);
        tuboEscape = (TextView) root.findViewById(R.id.txtTubo);
        ruedas = (TextView) root.findViewById(R.id.txtRuedas);
        aleron = (TextView) root.findViewById(R.id.txtAleron);
        anos = (TextView) root.findViewById(R.id.txtAnos);
        matricula = (TextView) root.findViewById(R.id.txtMatricula);
        bodyKit = (TextView) root.findViewById(R.id.txtBodyKit);
        lucesLd = (TextView) root.findViewById(R.id.txtLucesLed);
        choques = (TextView) root.findViewById(R.id.txtChoques);
        exportado = (TextView) root.findViewById(R.id.txtExportado);
        maxV = (TextView) root.findViewById(R.id.txtMaxv);
        cv = (TextView) root.findViewById(R.id.txtCv);
        imagenVehiculo = (ImageView) root.findViewById(R.id.fotoVehiculo);

        if (getArguments() != null && getArguments().containsKey("matriculaVeh")) {
            String matri = getArguments().getString("matriculaVeh");
            cargarDatosVehiculo(matri);
        }

        return root;
    }

    public void cargarDatosVehiculo(String matriculaVe){
        db.collection("vehiculos")
                .whereEqualTo("matricula", matriculaVe)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Vehiculo veh = queryDocumentSnapshots.getDocuments().get(0).toObject(Vehiculo.class);

                        if (veh != null) {
                            marcaYModelo.setText(veh.getMarca()+" "+veh.getModelo());
                            descripcion.setText(veh.getDescripción());
                            tuboEscape.setText(veh.getTuboEscape());
                            matricula.setText(veh.getMatricula());
                            maxV.setText("Máxima Velocidad: "+veh.getMaxVelocidad()+" km/h");
                            cv.setText("Potencia: "+veh.getCv()+" CV");
                            choques.setText("Choques: "+veh.getChoques());
                            ruedas.setText(veh.getRuedas());
                            aleron.setText(veh.getAleron());
                            cv.setText("Potencia: "+veh.getCv()+" CV");

                            if(veh.isBodyKit()){
                                bodyKit.setText("Lleva Body Kit");
                            }else{
                                bodyKit.setText("No lleva Body Kit");
                            }

                            if(veh.isLucesLed()){
                                lucesLd.setText("Llevas Luces Led Decoración");
                            }else{
                                lucesLd.setText("No llevas Luces Led Decoración");
                            }

                            if(veh.isExportado()){
                                exportado.setText("Exportado");
                            }else{
                                exportado.setText("No Exportado");
                            }
                        }
                    } else {
                        marcaYModelo.setText("Vehiculo no encontrado.");
                    }
                })
                .addOnFailureListener(e -> {
                    marcaYModelo.setText("Error al cargar el vehículo.");
                });
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
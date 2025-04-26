package com.clase.motorton.ui.perfil;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.clase.motorton.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class InfoOtroPerfilFragment extends Fragment {
    // Variable para manejar los Toast de está actividad
    private Toast mensajeToast = null;
    private TextView textUsername = null;
    private TextView textEdad = null;
    private TextView textNombreCompleto = null;
    private TextView textAniosConduciendo = null;
    private TextView textUbicacion = null;
    private ImageView imagenPerfil = null;
    private Button btnLike = null;

    // Variable para manejar el autenticado de firebase
    private FirebaseAuth mAuth = null;
    // Variable para manejar la base de datos de firebase
    private FirebaseFirestore db = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_info_otro_perfil, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        textUsername = (TextView) root.findViewById(R.id.textViewUsernameOtroUsuario);
        textUbicacion = (TextView) root.findViewById(R.id.textViewUbicacionOtroUsuario);
        textAniosConduciendo = (TextView) root.findViewById(R.id.textViewAnosConduciendoOtroUsuario);
        textEdad = (TextView) root.findViewById(R.id.textViewEdadOtroUsuario);
        textNombreCompleto = (TextView) root.findViewById(R.id.textViewNombreCompletoOtroUsuario);
        imagenPerfil = (ImageView) root.findViewById(R.id.imageViewPerfilOtroUsuario);
        btnLike = (Button) root.findViewById(R.id.buttonLike);

        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return root;
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
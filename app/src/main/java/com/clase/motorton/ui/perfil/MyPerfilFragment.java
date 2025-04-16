package com.clase.motorton.ui.perfil;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.clase.motorton.R;
import com.clase.motorton.adaptadores.VehiculosAdapter;
import com.clase.motorton.cifrado.CifradoDeDatos;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.clase.motorton.modelos.Vehiculo;

import java.util.ArrayList;

import javax.crypto.SecretKey;

public class MyPerfilFragment extends Fragment {
    private ProgressBar progressBar;
    private ImageView imageViewPerfil;
    private TextView textViewUsername, textViewEdad, textViewUbicacion, textViewNombreCompleto;
    private Button buttonEditarPerfil;
    private ImageView btnAgregarVeiculo = null;
    private ImageView btnAjustes = null;
    private RecyclerView recyclerViewVehiculos;
    private VehiculosAdapter vehiculosAdapter;
    private ArrayList<Vehiculo> listaVehiculos = new ArrayList<>();

    private CifradoDeDatos cifrar = null;
    private SecretKey claveSecreta = null;
    private Toast mensajeToast = null;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public MyPerfilFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        progressBar = root.findViewById(R.id.progressBar);
        imageViewPerfil = root.findViewById(R.id.imageViewPerfil);
        textViewUsername = root.findViewById(R.id.textViewUsername);
        textViewEdad = root.findViewById(R.id.textViewEdad);
        textViewUbicacion = root.findViewById(R.id.textViewUbicacion);
        textViewNombreCompleto = root.findViewById(R.id.textViewNombreCompleto);
        buttonEditarPerfil = root.findViewById(R.id.buttonEditarPerfil);
        recyclerViewVehiculos = root.findViewById(R.id.recyclerViewVehiculos);
        btnAgregarVeiculo = root.findViewById(R.id.buttonAgregarVehiculo);
        btnAjustes = root.findViewById(R.id.buttonAjustes);

        cifrar = new CifradoDeDatos();
        try {
            CifradoDeDatos.generarClaveSiNoExiste();
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnAjustes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnAgregarVeiculo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireContext(), AdministrarVehiculos.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        buttonEditarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        cargarPerfil();

        return root;
    }

    private void cargarPerfil(){
        progressBar.setVisibility(View.VISIBLE);

        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (uid == null) {
            showToast("Usuario no autenticado");
            progressBar.setVisibility(View.GONE);
            return;
        }

        DocumentReference docRef = db.collection("perfiles").document(uid);
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
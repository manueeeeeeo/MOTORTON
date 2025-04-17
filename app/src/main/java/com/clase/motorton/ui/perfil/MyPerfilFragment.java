package com.clase.motorton.ui.perfil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;

import javax.crypto.SecretKey;

public class MyPerfilFragment extends Fragment {
    private ProgressBar progressBar;
    private ImageView imageViewPerfil;
    private TextView textViewUsername, textViewEdad, textViewUbicacion, textViewNombreCompleto;
    private Button buttonEditarPerfil;
    private ImageView btnAgregarVeiculo = null;
    private ImageView btnAjustes = null;
    private ImageView btnCoches = null;
    private ImageView btnEventosCreados = null;
    private ImageView btnEventosParticipas = null;
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
        btnCoches = root.findViewById(R.id.buttonMisCoches);
        btnEventosCreados = root.findViewById(R.id.buttonEventosCreados);
        btnEventosParticipas = root.findViewById(R.id.buttonEventosActivos);

        cifrar = new CifradoDeDatos();
        try {
            CifradoDeDatos.generarClaveSiNoExiste();
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnAjustes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireContext(), Ajustes.class);
                startActivity(intent);
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

        btnCoches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnEventosParticipas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnEventosCreados.setOnClickListener(new View.OnClickListener() {
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

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String username = documentSnapshot.getString("username");
                Long edadLong = documentSnapshot.getLong("edad");
                String edad = edadLong != null ? edadLong.toString() : "N/A";
                String fotoPerfilBase64 = documentSnapshot.getString("fotoPerfil");
                String email = documentSnapshot.getString("email");
                String nombreCompleto = documentSnapshot.getString("nombre_completo");

                Map<String, Object> ubicacionMap = (Map<String, Object>) documentSnapshot.get("ubicacion");
                String direccion = "Ubicación no disponible";

                if (ubicacionMap != null) {
                    direccion = (String) ubicacionMap.get("direccion");
                }

                if (username != null && edad != null && direccion != null && email != null && nombreCompleto != null) {
                    textViewUsername.setText(username);
                    textViewEdad.setText("Edad: " + edad);
                    textViewUbicacion.setText("Ubicación: " + CifradoDeDatos.descifrar(direccion));
                    textViewNombreCompleto.setText("Nombre Completo: "+CifradoDeDatos.descifrar(nombreCompleto));

                    if (fotoPerfilBase64 != null && !fotoPerfilBase64.isEmpty()) {
                        Bitmap bitmap = convertirBase64AImagen(fotoPerfilBase64);
                        if (bitmap != null) {
                            imageViewPerfil.setImageBitmap(bitmap);
                        } else {
                            imageViewPerfil.setImageResource(R.drawable.icono);
                        }
                    } else {
                        imageViewPerfil.setImageResource(R.drawable.icono);
                    }

                    cargarVehiculos(uid);
                } else {
                    showToast("Datos incompletos en el perfil");
                }
            } else {
                showToast("Perfil no encontrado");
            }
            progressBar.setVisibility(View.GONE);
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            showToast("Error al cargar perfil: " + e.getMessage());
        });
    }

    private Bitmap convertirBase64AImagen(String base64String) {
        try {
            base64String = base64String.replace("\n", "").replace("\r", "");

            byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);

            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            showToast("Error al convertir imagen Base64");
            return null;
        }
    }

    private void cargarVehiculos(String uid) {
        listaVehiculos.clear();

        db.collection("vehiculos")
                .whereEqualTo("uidDueno", uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Vehiculo vehiculo = document.toObject(Vehiculo.class);
                                listaVehiculos.add(vehiculo);
                            }
                        } else {
                            showToast("No se encontraron vehículos para este usuario.");
                        }

                        if (vehiculosAdapter != null) {
                            vehiculosAdapter.notifyDataSetChanged();
                        } else {
                            vehiculosAdapter = new VehiculosAdapter(listaVehiculos, getContext());
                            recyclerViewVehiculos.setAdapter(vehiculosAdapter);
                        }

                    } else {
                        showToast("Error al cargar vehículos: " + task.getException().getMessage());
                    }
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
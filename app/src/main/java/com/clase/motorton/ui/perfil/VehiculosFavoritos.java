package com.clase.motorton.ui.perfil;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clase.motorton.R;
import com.clase.motorton.adaptadores.VehiculosAdapter;
import com.clase.motorton.modelos.Vehiculo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class VehiculosFavoritos extends AppCompatActivity {
    // Variable para manejar el progressbar de la actividad
    private ProgressBar progressBar = null;
    // Variable para manejar el recyclerview de vehículos del usuario
    private RecyclerView recyclerViewVehiculosFavs = null;
    private boolean isLoading = false;
    private boolean isInitialLoad = false;
    private VehiculosAdapter adapter = null;

    // Variable para manejar los Toast de está actividad
    private Toast mensajeToast = null;
    private ArrayList<Vehiculo> listaVehiculosFav = new ArrayList<>();

    // Variable para manejar el autenticado de firebase
    private FirebaseAuth mAuth = null;
    // Variable para manejar la base de datos de firebase
    private FirebaseFirestore db = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vehiculos_favoritos);

        progressBar = findViewById(R.id.progressLoadingFavoritos);
        recyclerViewVehiculosFavs = findViewById(R.id.recyclerVehiculosFavoritos);
        recyclerViewVehiculosFavs.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new VehiculosAdapter(listaVehiculosFav, this);
        recyclerViewVehiculosFavs.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        cargarFavoritos();
    }

    private void cargarFavoritos() {
        progressBar.setVisibility(View.VISIBLE);
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("perfiles").document(userId).get().addOnSuccessListener(document -> {
            List<String> favMatriculas = (List<String>) document.get("listaFavVeh");
            if (favMatriculas == null || favMatriculas.isEmpty()) {
                Toast.makeText(this, "No tienes vehículos favoritos.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            obtenerVehiculosFavoritos(favMatriculas);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al cargar favoritos.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        });
    }

    private void obtenerVehiculosFavoritos(List<String> matriculas) {
        listaVehiculosFav.clear();
        List<List<String>> chunks = new ArrayList<>();
        for (int i = 0; i < matriculas.size(); i += 10) {
            chunks.add(matriculas.subList(i, Math.min(i + 10, matriculas.size())));
        }

        for (List<String> grupo : chunks) {
            db.collection("vehiculos")
                    .whereIn("matricula", grupo)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                            Vehiculo vehiculo = doc.toObject(Vehiculo.class);
                            if (vehiculo != null) {
                                listaVehiculosFav.add(vehiculo);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al obtener vehículos.", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
        }
    }
}
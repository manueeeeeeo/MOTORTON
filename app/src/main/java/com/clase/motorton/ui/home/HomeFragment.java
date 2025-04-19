package com.clase.motorton.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clase.motorton.R;
import com.clase.motorton.adaptadores.EventosAdapter;
import com.clase.motorton.databinding.FragmentHomeBinding;
import com.clase.motorton.modelos.Evento;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RecyclerView recyclerView = null;
    private EventosAdapter eventoAdapter = null;
    private List<Evento> eventoList = null;
    private FirebaseFirestore db = null;
    private DocumentSnapshot lastVisible = null;
    private boolean isLoading = false;
    private boolean isInitialLoad = false;
    private Toast mensajeToast = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Configuración del RecyclerView
        recyclerView = root.findViewById(R.id.recyclerEventos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializamos la lista y el adaptador
        String uidUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
        eventoList = new ArrayList<>();
        eventoAdapter = new EventosAdapter(getContext(), eventoList, uidUsuario);
        recyclerView.setAdapter(eventoAdapter);

        // Inicializamos Firestore
        db = FirebaseFirestore.getInstance();

        binding.swipeRefresh.setOnRefreshListener(() -> cargarConRefresh());

        cargarInicial();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && layoutManager != null && layoutManager.findLastVisibleItemPosition() == eventoList.size() - 1) {
                    cargarMasEventos();
                }
            }
        });

        return root;
    }

    private void cargarInicial() {
        ProgressBar progress = binding.progressLoading;
        progress.setVisibility(View.VISIBLE);

        isLoading = true;
        lastVisible = null;

        db.collection("eventos")
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        eventoList.addAll(queryDocumentSnapshots.toObjects(Evento.class));
                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                        eventoAdapter.notifyDataSetChanged();

                        if (queryDocumentSnapshots.size() < 10) {
                            showToast("No hay más eventos");
                        }
                    }
                    progress.setVisibility(View.GONE);
                    isLoading = false;
                    isInitialLoad = true;
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    isLoading = false;
                    Log.e("HomeFragment", "Error al cargar eventos", e);
                });
    }

    private void cargarMasEventos() {
        if (lastVisible == null) return;

        ProgressBar progress = binding.progressLoading;
        progress.setVisibility(View.VISIBLE);

        isLoading = true;

        db.collection("eventos")
                .startAfter(lastVisible)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        eventoList.addAll(queryDocumentSnapshots.toObjects(Evento.class));
                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                        eventoAdapter.notifyDataSetChanged();

                        if (queryDocumentSnapshots.size() < 10) {
                            showToast("No hay más eventos");
                        }
                    }
                    progress.setVisibility(View.GONE);
                    isLoading = false;
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    isLoading = false;
                    Log.e("HomeFragment", "Error al cargar más eventos", e);
                });
    }

    private void cargarConRefresh() {
        binding.swipeRefresh.setRefreshing(true);
        isLoading = true;

        eventoList.clear();
        eventoAdapter.notifyDataSetChanged();
        lastVisible = null;

        db.collection("eventos")
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        eventoList.addAll(queryDocumentSnapshots.toObjects(Evento.class));
                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                        eventoAdapter.notifyDataSetChanged();
                    }
                    binding.swipeRefresh.setRefreshing(false);
                    isLoading = false;
                })
                .addOnFailureListener(e -> {
                    binding.swipeRefresh.setRefreshing(false);
                    isLoading = false;
                    Log.e("HomeFragment", "Error al refrescar eventos", e);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
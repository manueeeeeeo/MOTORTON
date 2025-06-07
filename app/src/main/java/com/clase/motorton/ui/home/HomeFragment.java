package com.clase.motorton.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clase.motorton.R;
import com.clase.motorton.adaptadores.EventosAdapter;
import com.clase.motorton.adaptadores.SpinnerAdaptarNormal;
import com.clase.motorton.databinding.FragmentHomeBinding;
import com.clase.motorton.modelos.Evento;
import com.clase.motorton.servicios.InternetController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

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
    private Spinner spinnerPronvincia = null;
    // Variable para manejar la lista de las provincias
    private List<String> provincias = new ArrayList<>();

    private boolean spinnerInitialized = false;
    private String lastProvinciaFilter = null;
    private boolean yaCargoInicial = false;

    private String currentProvinciaFilter = null;

    private InternetController internetController = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Configuración del RecyclerView
        recyclerView = root.findViewById(R.id.recyclerEventos);
        spinnerPronvincia = root.findViewById(R.id.spinnerProvincia);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializo el controlador de internet
        internetController = new InternetController(getContext());

        if(!internetController.tieneConexion()){
            showToast("No tienes acceso a internet, conectese a una red!!");
        }

        // Agrego todas las provincias posibles
        provincias.add("-- Elija una Opción --");
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

        // Inicializo el adaptador para el spinner de provincias
        SpinnerAdaptarNormal adapter = new SpinnerAdaptarNormal(getContext(), provincias);
        // Establezco el adaptador al spinner
        spinnerPronvincia.setAdapter(adapter);

        // Inicializamos la lista y el adaptador
        String uidUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
        eventoList = new ArrayList<>();
        eventoAdapter = new EventosAdapter(getContext(), eventoList, uidUsuario);
        recyclerView.setAdapter(eventoAdapter);

        spinnerPronvincia.setSelection(0, false);
        spinnerInitialized = false;

        // Inicializamos Firestore
        db = FirebaseFirestore.getInstance();

        binding.swipeRefresh.setOnRefreshListener(() -> cargarConRefresh(currentProvinciaFilter));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && layoutManager != null && layoutManager.findLastVisibleItemPosition() == eventoList.size() - 1) {
                    cargarMasEventos(currentProvinciaFilter);
                }
            }
        });

        spinnerPronvincia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!spinnerInitialized) {
                    spinnerInitialized = true;
                    return;
                }

                String selectedProvincia = provincias.get(position);
                if (selectedProvincia.equals("-- Elija una Opción --")) {
                    selectedProvincia = null;
                }

                if ((lastProvinciaFilter == null && selectedProvincia == null) ||
                        (lastProvinciaFilter != null && lastProvinciaFilter.equals(selectedProvincia))) {
                    return;
                }

                currentProvinciaFilter = selectedProvincia;
                lastProvinciaFilter = selectedProvincia;
                cargarConRefresh(currentProvinciaFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if (eventoList.isEmpty()) {
            currentProvinciaFilter = null;
            lastProvinciaFilter = null;
            cargarConRefresh(null);
            yaCargoInicial = true;
        }
        return root;
    }

    private void cargarInicial() {
        ProgressBar progress = binding.progressLoading;
        progress.setVisibility(View.VISIBLE);

        isLoading = true;
        lastVisible = null;

        db.collection("eventos")
                .limit(10)
                .orderBy("fecha", Query.Direction.DESCENDING)
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

    private void cargarMasEventos(String provincia) {
        if (!internetController.tieneConexion()) {
            showToast("Sin conexión. No se pueden cargar eventos.");
            return;
        }
        if (lastVisible == null) return;
        ProgressBar progress = binding.progressLoading;
        progress.setVisibility(View.VISIBLE);
        isLoading = true;

        Query query = db.collection("eventos")
                .orderBy("fecha", Query.Direction.DESCENDING);

        if (provincia != null && !provincia.isEmpty() && !provincia.equals("-- Elija una Opción --")) {
            query = db.collection("eventos")
                    .whereEqualTo("provincia", provincia)
                    .orderBy("fecha", Query.Direction.DESCENDING);
        }

        query.startAfter(lastVisible)
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
                    } else {
                        showToast("No hay más eventos para esta provincia");
                    }
                    progress.setVisibility(View.GONE);
                    isLoading = false;
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    isLoading = false;
                    Log.e("HomeFragment", "Error al cargar más eventos", e);
                    showToast("Error al cargar más eventos");
                });
    }

    private void cargarConRefresh(String provincia) {
        if (!internetController.tieneConexion()) {
            showToast("Sin conexión. No se pueden actualizar los eventos.");
            binding.swipeRefresh.setRefreshing(false);
            return;
        }

        binding.swipeRefresh.setRefreshing(true);
        isLoading = true;
        eventoList.clear();
        eventoAdapter.notifyDataSetChanged();
        lastVisible = null;

        Query query = db.collection("eventos")
                .orderBy("fecha", Query.Direction.DESCENDING);

        if (provincia != null && !provincia.isEmpty() && !provincia.equals("-- Elija una Opción --")) {
            query = db.collection("eventos")
                    .whereEqualTo("provincia", provincia)
                    .orderBy("fecha", Query.Direction.DESCENDING);
        }

        query.limit(10)
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
                    } else {
                        showToast("No se encontraron eventos para esta provincia");
                    }
                    binding.swipeRefresh.setRefreshing(false);
                    isLoading = false;
                })
                .addOnFailureListener(e -> {
                    binding.swipeRefresh.setRefreshing(false);
                    isLoading = false;
                    Log.e("HomeFragment", "Error al refrescar eventos", e);
                    showToast("Error al cargar eventos");
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
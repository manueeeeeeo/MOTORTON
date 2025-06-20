package com.clase.motorton.ui.search;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clase.motorton.R;
import com.clase.motorton.adaptadores.BusquedaAdapter;
import com.clase.motorton.databinding.FragmentDashboardBinding;
import com.clase.motorton.modelos.BusquedaItem;
import com.clase.motorton.servicios.InternetController;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private RecyclerView recyclerView = null;
    private BusquedaAdapter busquedaAdapter = null;
    private List<BusquedaItem> resultadosList = new ArrayList<>();
    private FirebaseFirestore db = null;
    private EditText searchInput = null;
    private ProgressBar progressBar = null;
    private String lastQuery = "";
    private Map<String, BusquedaItem> resultadosMap = new HashMap<>();

    private Toast mensajeToast = null;

    private InternetController internetController = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = root.findViewById(R.id.recyclerResultados);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        resultadosList = new ArrayList<>();
        resultadosMap = new HashMap<>();
        busquedaAdapter = new BusquedaAdapter(getContext(), resultadosList);
        recyclerView.setAdapter(busquedaAdapter);

        searchInput = root.findViewById(R.id.editBuscar);
        progressBar = root.findViewById(R.id.progressBuscar);
        db = FirebaseFirestore.getInstance();

        // Inicializo el controlador de internet
        internetController = new InternetController(getContext());

        if(!internetController.tieneConexion()){
            showToast("No tienes acceso a internet, conectese a una red!!");
        }

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (!query.equals(lastQuery)) {
                    lastQuery = query;

                    if (!internetController.tieneConexion()) {
                        showToast("Sin conexión. No se puede buscar.");
                        return;
                    }

                    buscar(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        if (!lastQuery.isEmpty()) {
            buscar(lastQuery);
        }

        return root;
    }

    private void buscar(String texto) {
        if (!internetController.tieneConexion()) {
            showToast("No hay conexión. No se puede realizar la búsqueda.");
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (texto.isEmpty()) {
            resultadosList.clear();
            resultadosMap.clear();
            busquedaAdapter.notifyDataSetChanged();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        resultadosMap.clear();
        AtomicInteger consultasPendientes = new AtomicInteger(3);

        Query queryPerfiles = db.collection("perfiles").limit(20);

        Query queryEventos = db.collection("eventos").limit(20);

        Query queryVehiculos = db.collection("vehiculos").limit(20);

        queryPerfiles.get().addOnSuccessListener(perfiles -> {
            for (DocumentSnapshot doc : perfiles.getDocuments()) {
                String username = doc.getString("username");
                String idPerfil = doc.getId();

                if (username != null && username.toLowerCase().contains(texto.toLowerCase())) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        resultadosMap.putIfAbsent(username, new BusquedaItem(idPerfil, username, "perfil", null));
                    }
                }
            }
            actualizarResultados(consultasPendientes);
        });

        queryEventos.get().addOnSuccessListener(eventos -> {
            for (DocumentSnapshot doc : eventos.getDocuments()) {
                String nombreEvento = doc.getString("nombre");
                String idEvento = doc.getString("id");

                if (nombreEvento != null && nombreEvento.toLowerCase().contains(texto.toLowerCase())) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        resultadosMap.putIfAbsent(nombreEvento, new BusquedaItem(idEvento, nombreEvento, "evento", null));
                    }
                }
            }
            actualizarResultados(consultasPendientes);
        });

        queryVehiculos.get().addOnSuccessListener(vehiculos -> {
            for (DocumentSnapshot doc : vehiculos.getDocuments()) {
                String matricula = doc.getString("matricula");
                String idVehiculo = doc.getId();

                if (matricula != null && matricula.toLowerCase().contains(texto.toLowerCase())) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        resultadosMap.putIfAbsent(matricula, new BusquedaItem(idVehiculo, matricula, "vehiculo", null));
                    }
                }
            }
            actualizarResultados(consultasPendientes);
        });
    }

    private void actualizarResultados(AtomicInteger consultasPendientes) {
        if (consultasPendientes.decrementAndGet() == 0) {
            resultadosList.clear();
            resultadosList.addAll(resultadosMap.values());
            busquedaAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        }
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
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clase.motorton.R;
import com.clase.motorton.adaptadores.BusquedaAdapter;
import com.clase.motorton.databinding.FragmentDashboardBinding;
import com.clase.motorton.modelos.BusquedaItem;
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

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (!query.equals(lastQuery)) {
                    lastQuery = query;
                    buscar(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return root;
    }

    private void buscar(String texto) {
        if (texto.isEmpty()) {
            resultadosList.clear();
            resultadosMap.clear();
            busquedaAdapter.notifyDataSetChanged();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        resultadosMap.clear();
        AtomicInteger consultasPendientes = new AtomicInteger(2);

        Query queryPerfiles = db.collection("perfiles")
                .orderBy("username")
                .startAt(texto).endAt(texto + "\uf8ff")
                .limit(5);

        Query queryEventos = db.collection("eventos")
                .orderBy("nombre")
                .startAt(texto).endAt(texto + "\uf8ff")
                .limit(5);

        queryPerfiles.get().addOnSuccessListener(perfiles -> {
            for (DocumentSnapshot doc : perfiles.getDocuments()) {
                String username = doc.getString("username");
                if (username != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        resultadosMap.putIfAbsent(username, new BusquedaItem(username, "perfil"));
                    }
                }
            }
            actualizarResultados(consultasPendientes);
        });

        queryEventos.get().addOnSuccessListener(eventos -> {
            for (DocumentSnapshot doc : eventos.getDocuments()) {
                String nombreEvento = doc.getString("nombre");
                if (nombreEvento != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        resultadosMap.putIfAbsent(nombreEvento, new BusquedaItem(nombreEvento, "evento"));
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
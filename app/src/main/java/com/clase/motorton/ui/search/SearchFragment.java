package com.clase.motorton.ui.search;

import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
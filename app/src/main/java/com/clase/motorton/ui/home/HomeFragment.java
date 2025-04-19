package com.clase.motorton.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
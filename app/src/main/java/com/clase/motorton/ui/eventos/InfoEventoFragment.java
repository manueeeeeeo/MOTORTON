package com.clase.motorton.ui.eventos;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.clase.motorton.R;
import com.clase.motorton.adaptadores.ParticipanteAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class InfoEventoFragment extends Fragment {
    private TextView textNombre = null, textDescripcion = null, textUbicacion = null, textProvincia = null,
            textTipoEvento = null, textOrganizador = null, textFecha = null, textActivo = null, textParticipantes = null;

    private FirebaseFirestore db = null;
    private RecyclerView recyclerViewParticipantes = null;
    private ParticipanteAdapter participanteAdapter = null;
    private List<String> participantesList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_info_evento, container, false);

        db = FirebaseFirestore.getInstance();
        textNombre = view.findViewById(R.id.textNombre);
        textDescripcion = view.findViewById(R.id.textDescripcion);
        textUbicacion = view.findViewById(R.id.textUbicacion);
        textProvincia = view.findViewById(R.id.textProvincia);
        textTipoEvento = view.findViewById(R.id.textTipoEvento);
        textOrganizador = view.findViewById(R.id.textOrganizador);
        textFecha = view.findViewById(R.id.textFecha);
        textActivo = view.findViewById(R.id.textActivo);
        recyclerViewParticipantes = view.findViewById(R.id.recyclerViewParticipantes);

        recyclerViewParticipantes.setLayoutManager(new LinearLayoutManager(getContext()));
        participanteAdapter = new ParticipanteAdapter(participantesList);
        recyclerViewParticipantes.setAdapter(participanteAdapter);

        return view;
    }
}
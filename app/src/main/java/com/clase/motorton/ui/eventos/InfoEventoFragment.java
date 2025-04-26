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
import com.clase.motorton.modelos.Evento;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InfoEventoFragment extends Fragment {
    private TextView textNombre = null, textDescripcion = null, textUbicacion = null, textProvincia = null,
            textTipoEvento = null, textOrganizador = null, textFecha = null, textActivo = null, textParticipantes1 = null;

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
        textParticipantes1 = view.findViewById(R.id.textParticipantes);
        recyclerViewParticipantes = view.findViewById(R.id.recyclerViewParticipantes);

        recyclerViewParticipantes.setLayoutManager(new LinearLayoutManager(getContext()));
        participanteAdapter = new ParticipanteAdapter(participantesList);
        recyclerViewParticipantes.setAdapter(participanteAdapter);

        if (getArguments() != null && getArguments().containsKey("eventoId")) {
            String eventoId = getArguments().getString("eventoId");
            cargarEvento(eventoId);
        }

        return view;
    }

    private void cargarEvento(String eventoId) {
        db.collection("eventos")
                .whereEqualTo("id", eventoId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Evento evento = queryDocumentSnapshots.getDocuments().get(0).toObject(Evento.class);

                        if (evento != null) {
                            textNombre.setText(evento.getNombre());
                            textDescripcion.setText(evento.getDescripcion());
                            textUbicacion.setText("Ubicación: " + evento.getUbicacion());
                            textProvincia.setText("Provincia: " + evento.getProvincia());
                            textTipoEvento.setText("Tipo: " + evento.getTipoEvento());

                            cargarNombreOrganizador(evento.getOrganizador());

                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            String fechaFormateada = evento.getFecha() != null ? sdf.format(evento.getFecha()) : "Sin fecha";
                            textFecha.setText("Fecha: " + fechaFormateada);

                            textActivo.setText("Estado: " + (evento.isActivo() ? "Activo" : "Inactivo"));

                            List<String> participantes = evento.getParticipantes();
                            if (participantes != null) {
                                int cantidadParticipantes = participantes.size();
                                textParticipantes1.setText("Participantes: " + cantidadParticipantes);
                                participantesList.clear();
                                participantesList.addAll(participantes);
                                participanteAdapter.notifyDataSetChanged();
                                recyclerViewParticipantes.setVisibility(View.VISIBLE);
                            } else {
                                textParticipantes1.setText("Participantes: 0");
                                recyclerViewParticipantes.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        textNombre.setText("Evento no encontrado.");
                    }
                })
                .addOnFailureListener(e -> {
                    textNombre.setText("Error al cargar el evento.");
                });
    }

    private void cargarNombreOrganizador(String uidOrganizador) {
        db.collection("perfiles")
                .document(uidOrganizador)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombreOrganizador = documentSnapshot.getString("username");
                        textOrganizador.setText("Organizador: " + nombreOrganizador);
                    } else {
                        textOrganizador.setText("Organizador: Desconocido");
                    }
                })
                .addOnFailureListener(e -> {
                    textOrganizador.setText("Error al cargar el organizador.");
                });
    }
}
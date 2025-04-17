package com.clase.motorton.adaptadores;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.clase.motorton.R;
import com.clase.motorton.modelos.Evento;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class EventosAdapter extends RecyclerView.Adapter<EventosAdapter.ViewHolder> {
    private List<Evento> eventoList;
    private Context context;
    private String currentUserId;

    public EventosAdapter(Context context, List<Evento> eventos, String currentUserId) {
        this.context = context;
        this.eventoList = eventos;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_evento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Evento evento = eventoList.get(position);

        holder.nombre.setText(evento.getNombre());
        holder.tipoEvento.setText("Tipo: " + evento.getTipoEvento());
        holder.fecha.setText("Fecha: " + evento.getFecha());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (evento.getOrganizador() != null && evento.getOrganizador().equals(currentUserId)) {
            holder.botonEditar.setVisibility(View.VISIBLE);
        } else {
            holder.botonEditar.setVisibility(View.GONE);
        }

        db.collection("eventos").whereEqualTo("id", evento.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                        List<String> participantes = (List<String>) documentSnapshot.get("participantes");
                        if (participantes != null && participantes.contains(uid)) {
                            holder.botonApuntarse.setText("Quitarme");
                        } else {
                            holder.botonApuntarse.setText("Apuntarse");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error al obtener el evento", Toast.LENGTH_SHORT).show();
                });

        holder.botonApuntarse.setOnClickListener(v -> {
            holder.progressBar.setVisibility(View.VISIBLE);

            db.collection("eventos").whereEqualTo("id", evento.getId())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                            String eventoId = documentSnapshot.getId();
                            List<String> participantes = (List<String>) documentSnapshot.get("participantes");

                            if (participantes != null && participantes.contains(uid)) {
                                participantes.remove(uid);

                                db.collection("eventos").document(eventoId)
                                        .update("participantes", participantes)
                                        .addOnSuccessListener(unused -> {
                                            holder.botonApuntarse.setText("Apuntarse");
                                            Toast.makeText(context, "Te has quitado del evento", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(context, "Error al quitarte", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                if (participantes == null) participantes = new java.util.ArrayList<>();
                                participantes.add(uid);

                                db.collection("eventos").document(eventoId)
                                        .update("participantes", participantes)
                                        .addOnSuccessListener(unused -> {
                                            holder.botonApuntarse.setText("Quitarme");
                                            Toast.makeText(context, "Te has apuntado al evento", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(context, "Error al apuntarte", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error al obtener el evento", Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task -> {
                        holder.progressBar.setVisibility(View.GONE);
                    });
        });
    }

    @Override
    public int getItemCount() {
        return eventoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, tipoEvento, fecha;
        ImageView imagenEvento, botonEditar;
        Button botonApuntarse, botonVerMas;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
            imagenEvento = itemView.findViewById(R.id.imagenEvento);
            nombre = itemView.findViewById(R.id.tituloEvento);
            tipoEvento = itemView.findViewById(R.id.tipoEvento);
            fecha = itemView.findViewById(R.id.fechaEvento);
            botonApuntarse = itemView.findViewById(R.id.botonApuntarse);
            botonVerMas = itemView.findViewById(R.id.botonVerMas);
            botonEditar = itemView.findViewById(R.id.botonEditar);
        }
    }
}
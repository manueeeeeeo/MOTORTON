package com.clase.motorton.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clase.motorton.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ParticipanteAdapter extends RecyclerView.Adapter<ParticipanteAdapter.ParticipanteViewHolder> {

    private List<String> participantes;
    private FirebaseFirestore db;
    private List<String> nombresParticipantes;

    public ParticipanteAdapter(List<String> participantes) {
        this.participantes = participantes;
        this.db = FirebaseFirestore.getInstance();
        this.nombresParticipantes = new ArrayList<>();
    }

    @NonNull
    @Override
    public ParticipanteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_participante, parent, false);
        return new ParticipanteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipanteViewHolder holder, int position) {
        if (nombresParticipantes.size() > position) {
            holder.textParticipante.setText(nombresParticipantes.get(position));
        } else {
            String uid = participantes.get(position);
            obtenerNombreUsuario(uid, holder);
        }
    }


    @Override
    public int getItemCount() {
        return participantes.size();
    }

    private void obtenerNombreUsuario(String uid, ParticipanteViewHolder holder) {
        db.collection("perfiles").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("username");
                        if (nombre != null) {
                            nombresParticipantes.add(nombre);
                            notifyItemChanged(participantes.indexOf(uid));
                            holder.textParticipante.setText(nombre);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(), "Error al cargar el nombre", Toast.LENGTH_SHORT).show();
                });
    }

    public static class ParticipanteViewHolder extends RecyclerView.ViewHolder {

        TextView textParticipante;

        public ParticipanteViewHolder(View itemView) {
            super(itemView);
            textParticipante = itemView.findViewById(R.id.textParticipante);
        }
    }
}
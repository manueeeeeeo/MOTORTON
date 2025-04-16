package com.clase.motorton.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clase.motorton.modelos.Vehiculo;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.clase.motorton.R;

import java.util.ArrayList;

public class VehiculosAdapter extends RecyclerView.Adapter<VehiculosAdapter.VehiculoViewHolder> {

    private final ArrayList<Vehiculo> vehiculosList;
    private final Context context;

    public VehiculosAdapter(ArrayList<Vehiculo> vehiculosList, Context context) {
        this.vehiculosList = vehiculosList;
        this.context = context;
    }

    @NonNull
    @Override
    public VehiculoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vehiculo, parent, false);
        return new VehiculoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehiculoViewHolder holder, int position) {
        Vehiculo vehiculo = vehiculosList.get(position);

        if (vehiculo == null) return;

        holder.textViewModeloYMarca.setText(vehiculo.getMarca() + " " + vehiculo.getModelo());
        holder.textViewDescripcion.setText(vehiculo.getDescripción());

        switch (vehiculo.getTipoVehiculo().toLowerCase()) {
            case "coches":
                holder.imageViewTipoVehiculo.setImageResource(R.drawable.ic_coche);
                break;
            case "motos":
                holder.imageViewTipoVehiculo.setImageResource(R.drawable.ic_moto);
                break;
            default:
                break;
        }

        holder.imageViewDelete.setOnClickListener(v -> eliminarVehiculo(vehiculo.getMatricula(), position));

        /*holder.imageViewEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AgregarVehiculos.class);
            intent.putExtra("vehiculo", vehiculo);
            context.startActivity(intent);
        });*/
    }

    private void eliminarVehiculo(String matricula, int position) {
        if (matricula == null || matricula.isEmpty()) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("vehiculos")
                .whereEqualTo("matricula", matricula)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            db.collection("vehiculos").document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        vehiculosList.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, vehiculosList.size());
                                        Toast.makeText(context, "Vehículo eliminado", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(context, "Error al eliminar el vehículo", Toast.LENGTH_SHORT).show()
                                    );
                        }
                    } else {
                        Toast.makeText(context, "No se encontró el vehículo", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error al conectar con la base de datos", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public int getItemCount() {
        return vehiculosList != null ? vehiculosList.size() : 0;
    }

    public static class VehiculoViewHolder extends RecyclerView.ViewHolder {
        TextView textViewModeloYMarca, textViewDescripcion;
        ImageView imageViewTipoVehiculo, imageViewEdit, imageViewDelete;

        public VehiculoViewHolder(View itemView) {
            super(itemView);
            textViewModeloYMarca = itemView.findViewById(R.id.textViewMarcaModelo);
            textViewDescripcion = itemView.findViewById(R.id.textViewDescripcion);
            imageViewTipoVehiculo = itemView.findViewById(R.id.imageViewTipoVehiculo);
            imageViewEdit = itemView.findViewById(R.id.imageViewEdit);
            imageViewDelete = itemView.findViewById(R.id.imageViewDelete);
        }
    }
}
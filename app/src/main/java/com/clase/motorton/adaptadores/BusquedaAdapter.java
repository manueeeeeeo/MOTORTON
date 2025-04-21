package com.clase.motorton.adaptadores;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.clase.motorton.R;
import com.clase.motorton.modelos.BusquedaItem;

import java.util.List;

public class BusquedaAdapter extends RecyclerView.Adapter<BusquedaAdapter.ViewHolder> {

    private final List<BusquedaItem> listaResultados;
    private final Context context;

    public BusquedaAdapter(Context context, List<BusquedaItem> listaResultados) {
        this.context = context;
        this.listaResultados = listaResultados;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_busqueda, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusquedaItem item = listaResultados.get(position);
        holder.nombre.setText(item.getNombre());

        int icono = item.getTipo().equals("perfil") ? R.drawable.icono_persona : R.drawable.evento;
        holder.icono.setImageResource(icono);

        holder.cardView.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString("documentoNombre", item.getId());

            if (item.getTipo().equals("perfil")) {
                Navigation.findNavController(view).navigate(R.id.navigation_info_otro_perfil, bundle);
            } else {
                Navigation.findNavController(view).navigate(R.id.navigation_info_evento, bundle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaResultados.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre;
        ImageView icono;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.textNombre);
            icono = itemView.findViewById(R.id.iconoTipo);
            cardView = itemView.findViewById(R.id.cardViewResultado);
        }
    }
}
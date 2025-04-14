package com.clase.motorton.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.clase.motorton.R;

public class SpinnerAdapter extends BaseAdapter {

    private Context context; // Variable para manejar el contexto
    private String[] tipos; // Variable para manejar los tipos de vehículos
    private int[] iconos; // Variable para manejar los iconos de los tipos

    /**
     * @param context
     * @param iconos
     * @param tipos
     * Constructor con todos los parametros
     * de la clase
     */
    public SpinnerAdapter(Context context, String[] tipos, int[] iconos) {
        this.context = context;
        this.tipos = tipos;
        this.iconos = iconos;
    }

    @Override
    public int getCount() {
        return tipos.length;
    }

    @Override
    public Object getItem(int position) {
        return tipos[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.spinner_item, parent, false);
        }

        // Obtenengo las vistas del layout personalizado que he diseñado
        ImageView icono = convertView.findViewById(R.id.icono);
        TextView texto = convertView.findViewById(R.id.texto);

        // Procedo a asignar el icono basandonos en la posición
        icono.setImageResource(iconos[position]);
        // Procedo a asignar el tipo de vehículo basandonos en la posición
        texto.setText(tipos[position]);

        // Retorno la vista
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
package com.clase.motorton.adaptadores;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SpinnerAdaptarNormal extends ArrayAdapter<String> {

    private Context context = null; // Variable para manejar el contexto
    private List<String> items = new ArrayList<>(); // Variable para manejar la lista de elementos

    /**
     * @param context
     * @param items
     * Constructor en donde inicializzamos el contexto
     * y la lista de items de la lista para mostrar
     * en el spinner
     */
    public SpinnerAdaptarNormal(Context context, List<String> items) {
        super(context, android.R.layout.simple_spinner_item, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            // Inflamos el diseño personalizado para cada elemento del spinner
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        // Configuramos el texto del elemento actual
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(items.get(position));

        return convertView;
    }
}
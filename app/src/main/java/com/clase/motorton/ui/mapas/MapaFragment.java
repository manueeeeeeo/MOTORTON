package com.clase.motorton.ui.mapas;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.clase.motorton.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class MapaFragment extends Fragment {
    private FirebaseFirestore db = null;
    private FrameLayout mapaContainer = null;
    private Map<String, Integer> eventosPorProvincia = null;
    private Map<String, float[]> coordsRelativas = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mapa, container, false);

        db = FirebaseFirestore.getInstance();

        mapaContainer = view.findViewById(R.id.mapaContainer);

        eventosPorProvincia = new HashMap<>();
        coordsRelativas = new HashMap<>();

        coordsRelativas.put("Álava", new float[]{0.512f, 0.130f});
        coordsRelativas.put("Albacete", new float[]{0.565f, 0.650f});
        coordsRelativas.put("Alicante", new float[]{0.687f, 0.690f});
        coordsRelativas.put("Almería", new float[]{0.550f, 0.850f});
        coordsRelativas.put("Asturias", new float[]{0.264f, 0.070f});
        coordsRelativas.put("Ávila", new float[]{0.350f, 0.423f});
        coordsRelativas.put("Badajoz", new float[]{0.233f, 0.660f});
        coordsRelativas.put("Barcelona", new float[]{0.890f, 0.275f});
        coordsRelativas.put("Burgos", new float[]{0.444f, 0.200f});
        coordsRelativas.put("Cáceres", new float[]{0.253f, 0.535f});
        coordsRelativas.put("Cádiz", new float[]{0.270f, 0.935f});
        coordsRelativas.put("Cantabria", new float[]{0.415f, 0.080f});
        coordsRelativas.put("Castellón", new float[]{0.725f, 0.445f});
        coordsRelativas.put("Ciudad Real", new float[]{0.430f, 0.630f});
        coordsRelativas.put("Córdoba", new float[]{0.353f, 0.749f});
        coordsRelativas.put("La Coruña", new float[]{0.085f, 0.079f});
        coordsRelativas.put("Cuenca", new float[]{0.565f, 0.525f});
        coordsRelativas.put("Gerona", new float[]{0.945f, 0.212f});
        coordsRelativas.put("Granada", new float[]{0.457f, 0.850f});
        coordsRelativas.put("Guadalajara", new float[]{0.525f, 0.390f});
        coordsRelativas.put("Guipúzcoa", new float[]{0.550f, 0.090f});
        coordsRelativas.put("Huelva", new float[]{0.190f, 0.815f});
        coordsRelativas.put("Huesca", new float[]{0.730f, 0.230f});
        coordsRelativas.put("Jaén", new float[]{0.460f, 0.750f});
        coordsRelativas.put("León", new float[]{0.270f, 0.150f});
        coordsRelativas.put("Lérida", new float[]{0.810f, 0.225f});
        coordsRelativas.put("La Rioja", new float[]{0.525f, 0.193f});
        coordsRelativas.put("Las Palmas", new float[]{0.850f, 0.992f});
        coordsRelativas.put("Islas Baleares", new float[]{0.810f, 0.525f});
        coordsRelativas.put("Lugo", new float[]{0.137f, 0.090f});
        coordsRelativas.put("Madrid", new float[]{0.435f, 0.425f});
        coordsRelativas.put("Málaga", new float[]{0.370f, 0.895f});
        coordsRelativas.put("Murcia", new float[]{0.620f, 0.750f});
        coordsRelativas.put("Navarra", new float[]{0.595f, 0.150f});
        coordsRelativas.put("Orense", new float[]{0.116f, 0.205f});
        coordsRelativas.put("Palencia", new float[]{0.372f, 0.190f});
        coordsRelativas.put("Pontevedra", new float[]{0.063f, 0.198f});
        coordsRelativas.put("Salamanca", new float[]{0.255f, 0.385f});
        coordsRelativas.put("Santa Cruz de Tenerife", new float[]{0.800f, 0.992f});
        coordsRelativas.put("Segovia", new float[]{0.415f, 0.338f});
        coordsRelativas.put("Sevilla", new float[]{0.282f, 0.827f});
        coordsRelativas.put("Soria", new float[]{0.525f, 0.293f});
        coordsRelativas.put("Tarragona", new float[]{0.800f, 0.350f});
        coordsRelativas.put("Teruel", new float[]{0.660f, 0.425f});
        coordsRelativas.put("Toledo", new float[]{0.390f, 0.515f});
        coordsRelativas.put("Valencia", new float[]{0.685f, 0.570f});
        coordsRelativas.put("Valladolid", new float[]{0.345f, 0.285f});
        coordsRelativas.put("Vizcaya", new float[]{0.510f, 0.065f});
        coordsRelativas.put("Zamora", new float[]{0.255f, 0.256f});
        coordsRelativas.put("Zaragoza", new float[]{0.650f, 0.300f});
        coordsRelativas.put("Ceuta", new float[]{0.335f, 0.992f});
        coordsRelativas.put("Melilla", new float[]{0.465f, 0.999f});

        for (String prov : coordsRelativas.keySet()) {
            eventosPorProvincia.put(prov, 0);
        }

        return view;
    }

    private int getColorFromValue(int value, int max) {
        if (max == 0) return Color.rgb(0, 100, 0);

        float ratio = (float) value / max;

        int r, g;
        if (ratio < 0.5f) {
            r = (int) (200 * (ratio * 2));
            g = 180 + (int)(75 * ratio * 2);
        } else {
            r = 200 + (int)(55 * (ratio - 0.5f) * 2);
            g = (int) (180 * (1 - (ratio - 0.5f) * 2));
        }

        return Color.rgb(r, g, 0);
    }

    private int getContrastingTextColor(int backgroundColor) {
        int r = Color.red(backgroundColor);
        int g = Color.green(backgroundColor);
        int b = Color.blue(backgroundColor);

        double luminance = (0.299 * r + 0.587 * g + 0.114 * b);
        return luminance > 140 ? Color.BLACK : Color.WHITE;
    }
}
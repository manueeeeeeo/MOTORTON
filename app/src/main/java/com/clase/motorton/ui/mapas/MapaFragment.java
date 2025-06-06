package com.clase.motorton.ui.mapas;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.clase.motorton.R;
import com.clase.motorton.servicios.InternetController;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class MapaFragment extends Fragment {
    private FirebaseFirestore db = null;
    private FrameLayout mapaContainer = null;
    private Map<String, Integer> eventosPorProvincia = null;
    private Map<String, float[]> coordsRelativas = null;

    private InternetController internetController = null;
    private Toast mensajeToast = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mapa, container, false);

        db = FirebaseFirestore.getInstance();

        // Inicializo el controlador de internet
        internetController = new InternetController(getContext());

        if(!internetController.tieneConexion()){
            showToast("No tienes acceso a internet, conectese a una red!!");
        }

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

        if(internetController.tieneConexion()){
            db.collection("eventos").get().addOnSuccessListener(snapshot -> {
                for (QueryDocumentSnapshot doc : snapshot) {
                    String provincia = doc.getString("provincia");
                    if (provincia != null && eventosPorProvincia.containsKey(provincia)) {
                        int count = eventosPorProvincia.get(provincia);
                        eventosPorProvincia.put(provincia, count + 1);
                    }
                }

                ImageView mapaEspana = view.findViewById(R.id.mapaEspana);

                mapaEspana.post(() -> {
                    int viewWidth = mapaEspana.getWidth();
                    int viewHeight = mapaEspana.getHeight();

                    Drawable drawable = mapaEspana.getDrawable();
                    if (drawable == null) return;

                    int imgWidth = drawable.getIntrinsicWidth();
                    int imgHeight = drawable.getIntrinsicHeight();

                    float scale = Math.min((float) viewWidth / imgWidth, (float) viewHeight / imgHeight);

                    int scaledImgWidth = (int) (imgWidth * scale);
                    int scaledImgHeight = (int) (imgHeight * scale);

                    int offsetX = (viewWidth - scaledImgWidth) / 2;
                    int offsetY = (viewHeight - scaledImgHeight) / 2;

                    for (Map.Entry<String, float[]> entry : coordsRelativas.entrySet()) {
                        String provincia = entry.getKey();
                        float[] rel = entry.getValue();
                        int eventos = eventosPorProvincia.get(provincia);

                        TextView label = new TextView(getContext());
                        label.setText(String.valueOf(eventos));
                        label.setTextSize(10);
                        label.setPadding(6, 3, 6, 3);
                        int maxEventos = Collections.max(eventosPorProvincia.values());
                        int color = getColorFromValue(eventos, maxEventos);

                        label.setTextColor(getContrastingTextColor(color));
                        label.setBackgroundColor(color);
                        label.setGravity(Gravity.CENTER);

                        int x = offsetX + (int) (rel[0] * scaledImgWidth);
                        int y = offsetY + (int) (rel[1] * scaledImgHeight);

                        label.setLayoutParams(new FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));

                        mapaContainer.addView(label);

                        label.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                        int labelWidth = label.getMeasuredWidth();
                        int labelHeight = label.getMeasuredHeight();

                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) label.getLayoutParams();
                        params.leftMargin = x - (labelWidth / 2);
                        params.topMargin = y - (labelHeight / 2);
                        label.setLayoutParams(params);

                    }
                });

            });
        }else{
            showToast("No se puede cargar la información sin acceso a internet!!");
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

    /**
     * @param mensaje
     * Método para ir matando los Toast y mostrar todos en el mismo para evitar
     * colas de Toasts y que se ralentice el dispositivo
     */
    public void showToast(String mensaje){
        if (this != null){
            // Comprobamos si existe algun toast cargado en el toast de la variable global
            if (mensajeToast != null) { // En caso de que si que exista
                mensajeToast.cancel(); // Le cancelamos, es decir le "matamos"
            }

            // Creamos un nuevo Toast con el mensaje que nos dan de argumento en el método
            mensajeToast = Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT);
            // Mostramos dicho Toast
            mensajeToast.show();
        }
    }

    private int getContrastingTextColor(int backgroundColor) {
        int r = Color.red(backgroundColor);
        int g = Color.green(backgroundColor);
        int b = Color.blue(backgroundColor);

        double luminance = (0.299 * r + 0.587 * g + 0.114 * b);
        return luminance > 140 ? Color.BLACK : Color.WHITE;
    }
}
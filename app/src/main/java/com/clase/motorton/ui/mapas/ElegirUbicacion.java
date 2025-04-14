package com.clase.motorton.ui.mapas;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.clase.motorton.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class ElegirUbicacion extends AppCompatActivity {
    private MapView mapView = null;
    private FloatingActionButton botonBuscar = null;
    private TextInputEditText editBuscar = null;
    private Toast mensajeToast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_elegir_ubicacion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mapView = findViewById(R.id.mapView);
        editBuscar = findViewById(R.id.inputBusqueda);
        botonBuscar = findViewById(R.id.btnBuscar);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(5.0);
        mapView.getController().setCenter(new GeoPoint(40.0, -3.0));

        botonBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String texto = editBuscar.getText().toString().trim();
                if(!texto.isEmpty()){
                    buscarUbicacion(texto);
                }else{
                    showToast("Escribe un lugar para buscar");
                }
            }
        });
    }

    private void buscarUbicacion(String nombreLugar){

    }

    /**
     * @param mensaje
     * Método para ir matando los Toast y mostrar todos en el mismo para evitar
     * colas de Toasts y que se ralentice el dispositivo*/
    public void showToast(String mensaje){
        if (this != null){
            // Comprobamos si existe algun toast cargado en el toast de la variable global
            if (mensajeToast != null) { // En caso de que si que exista
                mensajeToast.cancel(); // Le cancelamos, es decir le "matamos"
            }

            // Creamos un nuevo Toast con el mensaje que nos dan de argumento en el método
            mensajeToast = Toast.makeText(this, mensaje, Toast.LENGTH_SHORT);
            // Mostramos dicho Toast
            mensajeToast.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
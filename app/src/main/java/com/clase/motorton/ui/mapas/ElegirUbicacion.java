package com.clase.motorton.ui.mapas;

import android.app.AlertDialog;
import android.content.Intent;
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

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ElegirUbicacion extends AppCompatActivity {
    // Variable para manejar la vista del mapa
    private MapView mapView = null;
    // Variable para manejar el botón de buscar
    private FloatingActionButton botonBuscar = null;
    // Variable para manejar el editText para la busqueda
    private TextInputEditText editBuscar = null;
    // Variable para manejar todos los Toast de está pantalla
    private Toast mensajeToast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_elegir_ubicacion);

        // Obtenemos de la interfaz gráfica todos los elementos que vayamos a necesitar
        mapView = findViewById(R.id.mapView);
        editBuscar = findViewById(R.id.inputBusqueda);
        botonBuscar = findViewById(R.id.btnBuscar);

        BoundingBox spainBounds = new BoundingBox(43.79, 4.33, 27.63, -9.30);
        mapView.setScrollableAreaLimitDouble(spainBounds);


        mapView.setTileSource(TileSourceFactory.MAPNIK);
        // Habilitamos los multicontroles de toques
        mapView.setMultiTouchControls(true);
        // Establecemos que haya un 5 de Zoom
        mapView.getController().setZoom(5.0);
        // Establecemos el punto principal en España Centro
        mapView.getController().setCenter(new GeoPoint(40.0, -3.0));

        // Establecemos el evento que se ejecutará cuando clickemos sobre el botón de buscar
        botonBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Obtenemos en una variable la busqueda introducida en el editText
                String texto = editBuscar.getText().toString().trim();
                // Procedemos a comprobar que la variable no esté vacía
                if(!texto.isEmpty()){ // En caso de no estar vacía
                    // Llamamos al método para buscar una ubicación pasandole el texto
                    buscarUbicacion(texto);
                }else{ // En caso de estar vacía
                    // Lanzamos un Toast indicando al usuario que ha de ingresar un sitio que buscar
                    showToast("Escribe un lugar para buscar");
                }
            }
        });
    }

    /**
     * @param nombreLugar
     * Método en el que le pasamos como una especie de consulta de un nombre
     * de un lugar, procedemos a abrir un hilo para no bloquear el principal
     * y gracias a la API de OSM, una vez encontrador el lugar llamamos al método
     * para mostrar el resultado que hemos obtenido y en caso de que surja algún error
     * mostraremos tanto por la consola como por un Toast
     */
    private void buscarUbicacion(String nombreLugar){
        // Generamos un nuevo hilo para no bloquear la interfaz ni el mapa
        new Thread(() -> {
            // Procedemos a utilizar un try catch para poder captar y tratar todas las posibles excepciones
            try {
                // Generamos una url que será la que utilicemos para hacer la consulta en OSM
                //String urlStr = "https://nominatim.openstreetmap.org/search?q=" +
                        //nombreLugar.replace(" ", "+") + "&format=json&limit=1";
                String urlStr = "https://nominatim.openstreetmap.org/search?q=" +
                        nombreLugar.replace(" ", "+") + "&format=json&limit=1&countrycodes=es";

                // Creamos una url indicandole que use el string anterior
                URL url = new URL(urlStr);
                // Creamos una conexión con una url http indicandole que abriremos la conexión
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                // Establecemos las necesidades
                conn.setRequestProperty("User-Agent", "MotortonApp/1.0");
                // Utilizamos un BufferedReader para leer todo lo que nos devuelva la API
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                // Creamos una variable en donde iremos apuntando y agregando todas las respuesta de la API
                StringBuilder response = new StringBuilder();
                // Creamos un string para manejar la respuesta
                String linea;
                // Mientras que la linea sea igual a la siguiente línea del reader y esto no sea nulo
                while ((linea = reader.readLine()) != null) {
                    // Agregamos al final la línea
                    response.append(linea);
                }
                // Cerramos el BufferedReader para librear recursos
                reader.close();

                // Creamos un JSONArray en donde guardamos todas las respuestas de la API
                JSONArray resultados = new JSONArray(response.toString());
                // Procedemos a comprobar si la logntiud de los resultados es mayor que 0, lo que indica que trae información
                if (resultados.length() > 0) { // En caso de traer información
                    // Creamod un JSONObject para poder acceder a todos los items del objeto
                    JSONObject lugar = resultados.getJSONObject(0);
                    // Obtenemos en una variable de tipo double la latitud
                    double lat = lugar.getDouble("lat");
                    // Obtenemos en una variable de tipo double la longitud
                    double lon = lugar.getDouble("lon");
                    // Obtenemos en una variable de tipo string el nombre de la ubicación
                    String direccion = lugar.getString("display_name");

                    // Llamamos al método para mostrar el resultado en el MapView
                    runOnUiThread(() -> mostrarResultado(lat, lon, direccion));
                } else { // En caso de no traer información
                    // Lanzaremos un Toast indicando al usuario que no encontró el lugar
                    runOnUiThread(() -> showToast("No se encontró el lugar"));
                }

            } catch (Exception e) { // En caso de que surja alguna excepción
                // Imprimiremos por consola el error
                e.printStackTrace();
                // Lanzaremos un hilo en donde mostraremos un Toast indicando que surguio un error al buscar
                runOnUiThread(() -> showToast("Error al buscar"));
            }
        }).start(); // Arrancamos el hilo
    }

    /**
     * @param direccion
     * @param lat
     * @param lon
     * Método para mostrar el resultado de la busqueda,
     * basandonos en la latidud, longitud y dirección
     * obtenidas en la busqueda de la ubicación
     */
    private void mostrarResultado(double lat, double lon, String direccion) {
        // Creo un nuevo GeoPunto para poder obtener una ubicación y marcarla
        GeoPoint punto = new GeoPoint(lat, lon);
        // Esteblecemos que realizamos una animación hacia el punto definido
        mapView.getController().animateTo(punto);
        // Establecemos el zoom con el que queremos hacer en el punto
        mapView.getController().setZoom(15.0);

        // Limpiamos todo lo que hayamos sobrepuesto al mapa
        mapView.getOverlays().clear();
        // Creamos un nuevo marcador para marcar la ubicación en el mapa
        Marker marcador = new Marker(mapView);
        // Establecemos en la posición del punto exacto de la busqueda
        marcador.setPosition(punto);
        // Establecemos el titúlo al marcador
        marcador.setTitle(direccion);
        // Agregamos el marcador al mapa
        mapView.getOverlays().add(marcador);
        // Invalidamos el mapa para dejar elegir al usuario
        mapView.invalidate();

        // Creo un nuevo dialogo indicandole al usuario si quiere confirmar está ubicación
        new AlertDialog.Builder(this)
                .setTitle("¿Usar esta ubicación?") // Establecemos titulo
                .setMessage(direccion) // Establecemos el mensaje
                .setPositiveButton("Sí", (dialog, which) -> { // Cuando toquemos al botón de aceptar
                    Intent resultIntent = new Intent(); // Creamos un nuevo Intent donde guardar todo
                    resultIntent.putExtra("latitud", lat); // Pasamos en el Intent la latitud
                    resultIntent.putExtra("longitud", lon); // Pasamos en el Intent la longitud
                    resultIntent.putExtra("direccion", direccion); // Pasamos en el Intent la dirección
                    setResult(RESULT_OK, resultIntent); // Establecemos al Intent todos los datos a pasar
                    finish(); // Cerramos la actividad actual
                })
                .setNegativeButton("No", null) // Cuando toquemos el botón de negar
                .show(); // Mostramos el dialogo
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
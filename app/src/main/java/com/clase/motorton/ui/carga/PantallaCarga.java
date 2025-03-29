package com.clase.motorton.ui.carga;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.clase.motorton.MainActivity;
import com.clase.motorton.R;
import com.clase.motorton.ui.perfil.CreacionPerfil;
import com.clase.motorton.ui.perfil.InicioSesion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PantallaCarga extends AppCompatActivity {
    // Variable para cambiar el mensaje de bienvenida
    private TextView welcomeText = null;
    // Variable para manejar el progreso de la barra de carga
    private ProgressBar progressBar = null;
    // Variable para manejar todos los toast de esta pantalla
    private Toast mensajeToast = null;
    // Variable para manejar la autentificación de los usuarios
    private FirebaseAuth auth = null;
    // Variable para manejar la base de datos de la aplicación
    private FirebaseFirestore db = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantalla_carga);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializo la variable de autentificación
        auth = FirebaseAuth.getInstance();
        // Inicializo la variable de la base de datos
        db = FirebaseFirestore.getInstance();

        // Incializo y obtengo de la interfaz los dos elementos que voy a utilizar
        welcomeText = findViewById(R.id.welcome_text);
        progressBar = findViewById(R.id.progress_bar);

        // Procedo a comprobar si el usuario tiene acceso a internet
        if (comprobarInternet()) { // En caso de tener acceso a internet
            // Llamo al método para simular la carga y hacer las comprobaciones pertinentes
            simularCarga();
        } else { // En caso de que no tenga acceso a internet
            // LLamo al método para mostrar el mensaje de que no se puede acceder sin internet
            mostrarMensajeNoInternet();
        }
    }

    /**
     * @return
     * Método que retorna un booleano para indicar al usuario
     * si tiene acceso a internet o no
     */
    private boolean comprobarInternet() {
        // Declaramos un objeto ConnectivityManager para manejar la conectividad de la red
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Verificamos si connectivityManager no es nulo para evitar NullPointerException
        if (connectivityManager != null) { // En caso de no ser nula
            // Obtenemos las capacidades de la red activa actualmente
            NetworkCapabilities capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

            // Devolvemos true si capabilities no es nulo y además si se cumple alguna de estas condiciones:
            // - La red tiene Wi-Fi disponible.
            // - La red tiene datos móviles disponible.
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        }

        // Si llegamos hasta aquí esque no tenemos conexión asique retornamos false
        return false;
    }

    /**
     * Método al que llamo para que en caso de que el usuario
     * tenga acceso a interent, simular una carga de datos
     * y mientras comprobar que el mismo usuario tenga acceso y perfil en la aplicación
     */
    private void simularCarga() {
        // Establezco el texto en el textview de la interfaz
        welcomeText.setText("Cargando tu experiencia...");
        // Establezco como visible la barra de progresión
        progressBar.setVisibility(View.VISIBLE);
        // Obtengo en una variable el usuario autenticado actualmente
        FirebaseUser user = auth.getCurrentUser();

        // Establecezco el progreso inicial de la barra de progreso a 0
        progressBar.setProgress(0);

        // Simulo el avance de la barra de progreso
        final Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            // Guardo en una variable el progreso que va a ir subiendo con la simulación a 0
            int progreso = 0;

            @Override
            public void run() {
                // Compruebo que el progreso sea menor a 100
                if (progreso < 100) { // En caso de que sea menor a 100
                    // Aumento el progreso +2 cada vez
                    progreso += 2;
                    // Cambio el progreso de la barra de progreso basandome en la variable de progreso
                    progressBar.setProgress(progreso);
                    // Ejecuto el runnable cada 50 ms
                    handler.postDelayed(this, 50);
                } else { // En caso de que ya haya superado el 100 la variable
                    // Compruebo si el usuario es nulo
                    if (user == null) { // En caso de que sea nulo
                        startActivity(new Intent(PantallaCarga.this, InicioSesion.class));
                        finish();
                    }else{ // En caso de que no sea nulo
                        // Verifico si el usuario tiene perfil en la base de datos
                        verificarPerfil(user);
                    }
                }
            }
        };

        // Ejecuto el runnable inmediatamente
        handler.post(runnable);
    }

    /**
     * @param user
     * Método en donde pasandole por parametro el usuario de firebase
     * comprobamos si tiene un perfil creado en la base de datos o no
     */
    private void verificarPerfil(FirebaseUser user) {
        // Procedo a comprobar en la colección de perfiles si existe un dogumento con el uid del usuario que le paso
        db.collection("perfiles").document(user.getUid()).get()
                .addOnCompleteListener(task -> { // En caso de que todo vaya bien
                    // Procedo a comprobar que todo haya ido bien y que además el documento exista
                    if (task.isSuccessful() && task.getResult().exists()) { // En caso afirmativo
                        // Lanzo una nueva actividad en donde llevo al usuario a la pantalla principal
                        startActivity(new Intent(PantallaCarga.this, MainActivity.class));
                    } else { // En caso negativo
                        // Lanzo una nueva actividad en donde llevo al usuario a crear su perfil
                        startActivity(new Intent(PantallaCarga.this, CreacionPerfil.class));
                    }
                    // Cierro la actividad actual
                    finish();
                })
                .addOnFailureListener(e -> { // En caso de que algo vaya mal
                    // Lanzo un toast indicando que ha ocurrido un error a la hora verificar el perfil
                    showToast("Error al verificar perfil");
                    // Inicio una nueva actividad para mandar al usuario a la pantalla de inicio de sesión
                    startActivity(new Intent(PantallaCarga.this, InicioSesion.class));
                    // Finalizo la actividad actual
                    finish();
                });
    }

    /**
     * Método al que llamo en caso de detectar que el
     * usuario no tiene acceso a internet
     */
    private void mostrarMensajeNoInternet() {
        // Cambio el texto del textview de la interfaz
        welcomeText.setText("No se detectó conexión a Internet.");
        // Lanzo un toast indicando que tiene que conectarse a internet
        showToast("Conectese a Internet");
        // Oculto la barra de progreso ya que no va a servir ahora para nada
        progressBar.setVisibility(View.INVISIBLE);
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
            mensajeToast = Toast.makeText(this, mensaje, Toast.LENGTH_SHORT);
            // Mostramos dicho Toast
            mensajeToast.show();
        }
    }
}
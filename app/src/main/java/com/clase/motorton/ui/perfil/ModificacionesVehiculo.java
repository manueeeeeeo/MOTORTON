package com.clase.motorton.ui.perfil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.clase.motorton.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;

public class ModificacionesVehiculo extends AppCompatActivity {
    private Button btnVolver = null;
    private Button btnGuardarCambios = null;
    private EditText editTuboEscape = null;
    private EditText editCv = null;
    private EditText editMaxVel = null;
    private EditText editRuedas = null;
    private EditText editAleron = null;
    private EditText editChoques = null;
    private ImageView fotoVehiculo = null;
    private Switch spinnerBodyKit = null;
    private Switch spinnerLucesLed = null;

    private boolean luces = false;
    private boolean bodykit = false;
    private String tubo = null;
    private String ruedas = null;
    private String aleron = null;
    private int choques = 0;
    private double maxVe = 0.0;
    private double cv = 0.0;
    private String foto = null;

    // Variable para manejar la autentificación del usuario
    private FirebaseAuth auth = null;
    // Variable para manejar la base de datos de firestore
    private FirebaseFirestore db = null;
    // Variable para manejar el contexto de la activdad
    private Context context = null;
    // Variable manejar todos los Toast de está actividad
    private Toast mensajeToast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_modificaciones_vehiculo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnVolver = (Button) findViewById(R.id.btnVolverNada);
        btnGuardarCambios = (Button) findViewById(R.id.btnConfirmarModi);
        editAleron = (EditText) findViewById(R.id.editAleron);
        editChoques = (EditText) findViewById(R.id.editChoques);
        editCv = (EditText) findViewById(R.id.editTCV);
        editRuedas = (EditText) findViewById(R.id.editRuedas);
        editMaxVel = (EditText) findViewById(R.id.edittMaxVel);
        editTuboEscape = (EditText) findViewById(R.id.editTuboEscape);
        fotoVehiculo = (ImageView) findViewById(R.id.imageViewVehiculo);
        spinnerBodyKit = (Switch) findViewById(R.id.switchBodyKit);
        spinnerLucesLed = (Switch) findViewById(R.id.switchLuces);

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnGuardarCambios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void guardarDatosModificaciones(){
        tubo = editTuboEscape.getText().toString();
        aleron = editAleron.getText().toString();
        ruedas = editRuedas.getText().toString();
        choques = Integer.parseInt(editChoques.getText().toString());
        cv = Double.parseDouble(editCv.getText().toString());
        maxVe = Double.parseDouble(editMaxVel.getText().toString());

        // Obtenemos en una variable el recurso que se ha puesto en la imageview de la imagen de perfil
        BitmapDrawable drawable = (BitmapDrawable) fotoVehiculo.getDrawable();
        // Obtenemos en una variable el bitmao de la imagen elegida
        Bitmap fotoBitmap = drawable != null ? drawable.getBitmap() : null;

        if (fotoBitmap != null) { // En caso de no ser nulo
            // Creamos un array de bytes para comprimir la imagen
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // Procedemos a comprimmir la imagen a un 85% y además, le establecemos como un JPEG
            fotoBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);

            // Creamos una cadena de bytes en donde almacenamos la imagen
            byte[] fotoBytes = baos.toByteArray();
            // Y guardamos en la variable antes creado la codificación en base64
            foto = Base64.encodeToString(fotoBytes, Base64.DEFAULT);
        }
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
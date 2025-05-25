package com.clase.motorton.ui.vehiculos;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.clase.motorton.R;
import com.clase.motorton.servicios.InternetController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class VerInfoVehiculoFavorito extends AppCompatActivity {
    // Variable para manejar la base de datos de firestore
    private FirebaseFirestore db = null;

    // Variable para manejar la autentificación del usuario
    private FirebaseAuth auth = null;

    private InternetController internetController = null;
    private Toast mensajeToast = null;

    private ImageView imagenVehiculo = null;
    private TextView marcaYModelo = null;
    private TextView descripcion = null;
    private TextView tuboEscape = null;
    private TextView ruedas = null;
    private TextView aleron = null;
    private TextView anos = null;
    private TextView matricula = null;
    private TextView bodyKit = null;
    private TextView lucesLd = null;
    private TextView cv = null;
    private TextView maxV = null;
    private TextView exportado = null;
    private TextView choques = null;
    private ImageView vehFav = null;

    private String fotoV = null;
    private String uidUser = null;
    private String matriculaVeh = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ver_info_vehiculo_favorito);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        // Inicializo el controlador de internet
        internetController = new InternetController(VerInfoVehiculoFavorito.this);

        if(!internetController.tieneConexion()){
            showToast("No tienes acceso a internet, conectese a una red!!");
        }

        uidUser = auth.getUid();

        marcaYModelo = (TextView) findViewById(R.id.txtModelo);
        descripcion = (TextView) findViewById(R.id.txtDescripcion);
        tuboEscape = (TextView) findViewById(R.id.txtTubo);
        ruedas = (TextView) findViewById(R.id.txtRuedas);
        aleron = (TextView) findViewById(R.id.txtAleron);
        anos = (TextView) findViewById(R.id.txtAnos);
        matricula = (TextView) findViewById(R.id.txtMatricula);
        bodyKit = (TextView) findViewById(R.id.txtBodyKit);
        lucesLd = (TextView) findViewById(R.id.txtLucesLed);
        choques = (TextView) findViewById(R.id.txtChoques);
        exportado = (TextView) findViewById(R.id.txtExportado);
        maxV = (TextView) findViewById(R.id.txtMaxv);
        cv = (TextView) findViewById(R.id.txtCv);
        imagenVehiculo = (ImageView) findViewById(R.id.fotoVehiculo);
        vehFav = (ImageView) findViewById(R.id.estrellaFavorito);

        if (getArguments() != null && getArguments().containsKey("matriculaVeh")) {
            String matri = getArguments().getString("matriculaVeh");
            matriculaVeh = matri;
            cargarDatosVehiculo(matri);
        }

        vehFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AgregarVehiculoFav(uidUser, matriculaVeh);
            }
        });
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
            mensajeToast = Toast.makeText(VerInfoVehiculoFavorito.this, mensaje, Toast.LENGTH_SHORT);
            // Mostramos dicho Toast
            mensajeToast.show();
        }
    }
}
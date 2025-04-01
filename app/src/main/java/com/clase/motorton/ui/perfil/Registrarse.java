package com.clase.motorton.ui.perfil;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.clase.motorton.R;
import com.clase.motorton.controllers.ControladorEmail;
import com.clase.motorton.controllers.ControladorPassword;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Registrarse extends AppCompatActivity {
    // Variable para manejar el boton de registro
    private Button btnRegis = null;
    // Variable para manejar el botón de borrar campos
    private Button btnBorrar = null;
    // Variable para manejar el botón de volver
    private Button btnVolver = null;
    // Variable para manejar el editText del correo
    private EditText editCorreo = null;
    // Variable para manejar el editText de la clave
    private EditText editClave = null;
    // Variable para manejar el editText de confirmación de la clave
    private EditText editConfirmarClave = null;
    // Variable de tipo string para guardar y manejar el correo introducido
    private String correo = null;
    // Variable de tipo string para guardar y manejar la clave
    private String clave = null;
    // Variable de tipo string para guardar y manejar la clave de confirmación
    private String clave2 = null;
    // Variable para manejar la imagen para ver la primera clave
    private ImageView verClave1 = null;
    // Variable para manejar la imagen para ver la segunda clave
    private ImageView verClave2 = null;
    // Variable para manejar todos los toast de está actividad
    private Toast mensajeToast = null;

    // Variable para manejar el controlador del email
    private ControladorEmail controEmail = null;
    // Variable para manejar el controlador de la clave
    private ControladorPassword controPass = null;

    // Variable para comprender si la clave 1 es visible o no
    boolean clave1Visible = false;
    // Variable para comprender si la clave 2 es visible o no
    boolean clave2Visible = false;

    // Variable para manejar la autentificación de usuarios por Firebase
    private FirebaseAuth auth=null;

    // Variable para manejar la base de datos por Firebase
    private FirebaseFirestore db = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrarse);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtengo la instancia de Firebase Auth
        auth = FirebaseAuth.getInstance();
        // Obtengo la isntancia de Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Obtengo todos los elementos de la interfaz gráfica
        btnRegis = (Button) findViewById(R.id.btnRegis);
        btnVolver = (Button) findViewById(R.id.btnVolver);
        btnBorrar = (Button) findViewById(R.id.btnBorrar);
        editCorreo = (EditText) findViewById(R.id.editEmail);
        editClave = (EditText) findViewById(R.id.editPass);
        editConfirmarClave = (EditText) findViewById(R.id.editPass2);
        verClave1 = (ImageView) findViewById(R.id.ver_clave);
        verClave2 = (ImageView) findViewById(R.id.ver_clave2);

        // Acción que sucederá al tocar la imagen de visibilidad de la clave 1
        verClave1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Comprobamos la variable que declare antes para el manejo de si mostrar o no la clave
                if (clave1Visible) { // En caso de que sea true
                    // Oculto la contraseña
                    editClave.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else { // En caso de que sea false
                    // Muestro la contraseña
                    editClave.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }

                // Mantengo el cursor al final del texto
                editClave.setSelection(editClave.getText().length());
                // Aseguro que el EditText mantenga el foco
                editClave.requestFocus();

                // Alterno el estado de visibilidad
                clave1Visible = !clave1Visible;
            }
        });

        // Acción que sucederá al tocar la imagen de visibilidad de la clave 2
        verClave2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Comprobamos la variable que declare antes para el manejo de si mostrar o no la clave
                if (clave2Visible) { // En caso de que sea true
                    // Oculto la contraseña
                    editConfirmarClave.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else { // En caso de que sea false
                    // Muestro la contraseña
                    editConfirmarClave.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }

                // Mantengo el cursor al final del texto
                editConfirmarClave.setSelection(editConfirmarClave.getText().length());
                // Aseguro que el EditText mantenga el foco
                editConfirmarClave.requestFocus();

                // Alterno el estado de visibilidad
                clave2Visible = !clave2Visible;
            }
        });

        // Acción que sucederá al tocar el botónde de volver
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Creo un nuevo Intent con el que voy a volver a la pantalla de inicio de sesion
                Intent i = new Intent(Registrarse.this, InicioSesion.class);
                // Inicio la nueva actividad
                startActivity(i);
                // Establezco una transcición para dar un poco de estilo al cambio de actividad
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                // Finalizo la actividad actual
                finish();
            }
        });
    }
}
package com.clase.motorton.ui.perfil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.clase.motorton.R;
import com.clase.motorton.controllers.ControladorEmail;
import com.clase.motorton.controllers.ControladorPassword;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(uiOptions);
        }

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

        // Acción que sucederá al tocar el botón de de volver
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

        // Acción que sucederá al tocar el botón de borrar campos
        btnBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Llamamos al método de borrar campos
                borrarCampos();
            }
        });

        // Acción que sucederá al tocar el botón de registrarse
        btnRegis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                correo = (String) editCorreo.getText().toString(); // Obtenemos en una variable el correo
                clave = (String) editClave.getText().toString(); // Obtenemos en una variable la clave
                clave2 = (String) editConfirmarClave.getText().toString(); // Obtenemos en una variable la confirmación de la clave
                if(correo.isEmpty()){ // En caso de que el correo esté vacío
                    // Lanzamos un Toast indicandolo
                    showToast("El campo del correo está vacio!!");
                }else{ // En caso de que esté relleno
                    if(clave.isEmpty()){ // En caso de que la clave esté vacía
                        // Lanzamos in Toast indicandolo
                        showToast("El campo de la clave está vacio!!");
                    }else{ // En caso de que esté rellena
                        if(clave2.isEmpty()){ // En caso de que la confirmación de clave esté vacía
                            // Lanzamos un Toast indicandolo
                            showToast("El campo de confirmar la clave está vacio!!");
                        }else{ // En caso de que esté rellena
                            // Procedemos a comprobar que la clave y la confirmación sean la misma
                            if(clave.equals(clave2)){ // En caso de que sean la misma
                                // Comprobamos con el controlador que sea un correo valido
                                if(controEmail.esCorreoValido(correo)){ // En caso de ser valido
                                    // Comprobamos con el controlador que la clave sea valida
                                    if(controPass.validarPassword(clave).equals("OK")){ // En caso de ser valida
                                        // Comprobamos con el controlador que la confirmación sea valida
                                        if(controPass.validarPassword(clave2).equals("OK")){ // En caso de ser valida
                                            // Llamamos al método para registrarnos con el correo y la clave
                                            registrarseConClaveYEmail(correo, clave);
                                        }else{ // En caso de que la confirmación no sea valida
                                            // Lanzamos un Toast indicandolo
                                            showToast("Error: " + controPass.validarPassword(clave2));
                                        }
                                    }else{ // En caso de que la clave no sea valida
                                        // Lanzamos un Toast indicandolo
                                        showToast("Error: " + controPass.validarPassword(clave));
                                    }
                                }else{ // En caso de que el correo no sea valido
                                    // Lanzamos un Toast indicandolo
                                    showToast("El correo no tiene el formato adecuado!!");
                                }
                            }else{ // En caso de que sean diferentes
                                // Lanzamos un Toast indicandolo
                                showToast("La clave y la confirmación tienen que ser la misma");
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * @param email
     * @param clave
     * Método para registrar al usuario con un email y una clave
     * en nuestra aplicación, además también, comprobamos si el usuario ya existe
     * en la base de datos de la misma para realizar un procedeimiento u otro
     * */
    public void registrarseConClaveYEmail(String email, String clave){
        auth.createUserWithEmailAndPassword(email, clave)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Obtenemos el usuario de firebase autenticado
                            FirebaseUser user = auth.getCurrentUser();
                            // Comprobamos que no sea nulo
                            if (user != null) {
                                // Obtenemos en una variable el uid del usuario
                                String uid = user.getUid();
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    showToast("Correo de verificación enviado correctamente");
                                                } else {
                                                    showToast("Error al enviar el corrreo de verificación!!");
                                                }
                                            }
                                        });
                                // Verificamos si el usuario ya existe en Firestore
                                db.collection("perfiles").document(uid).get()
                                        // Una vez completado
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                // Procedemos a comprobar si la tarea ha sido hecha correctamente o no
                                                if (task.isSuccessful()) { // En caso afirmativo
                                                    // Obtenemos un documento que hace referencia a lo obtenido de la tarea de busqueda
                                                    DocumentSnapshot document = task.getResult();
                                                    // Comprobamos si existe el documento
                                                    if (document.exists()) { // En caso de si existir
                                                        // Si el documento existe, redirigimos al usuario al inicio de sesión
                                                        showToast("El usuario ya está registrado.");
                                                        volverAlLogin();
                                                    } else { // En caso de no existir
                                                        // Lanzamos un Toast para que el usuario sepa que está pasando algo
                                                        showToast("Redirigiendo para completar perfil...");
                                                        SharedPreferences prefs = getSharedPreferences("BetaPrefs", MODE_PRIVATE);
                                                        String codigo = prefs.getString("codigoBeta", null);
                                                        Log.d("BetaCode", "Código beta recuperado: " + codigo);

                                                        if (codigo != null) {
                                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                            DocumentReference docRef = db.collection("invitationCodes").document(codigo);

                                                            Map<String, Object> data = new HashMap<>();
                                                            data.put("userUID", uid);

                                                            docRef.get().addOnSuccessListener(snapshot -> {
                                                                if (snapshot.exists()) {
                                                                    docRef.update(data)
                                                                            .addOnSuccessListener(unused -> {
                                                                                Log.d("Firebase", "Código beta actualizado correctamente");
                                                                                showToast("Código beta actualizado correctamente");
                                                                            })
                                                                            .addOnFailureListener(e -> {
                                                                                Log.e("Firebase", "Error al actualizar código beta", e);
                                                                                showToast("Error al actualizar código beta: " + e.getMessage());
                                                                            });
                                                                } else {
                                                                    Log.e("Firebase", "El documento del código beta no existe");
                                                                    showToast("Error: el código beta no existe");
                                                                }
                                                            }).addOnFailureListener(e -> {
                                                                Log.e("Firebase", "Error al comprobar existencia del documento", e);
                                                                showToast("Error al validar el código beta: " + e.getMessage());
                                                            });
                                                        } else {
                                                            Log.e("BetaCode", "El código beta es null en SharedPreferences");
                                                            showToast("Código beta no encontrado en preferencias");
                                                        }

                                                        // Creamos un nuevo intent indicando de donde partimos y a donde vamos
                                                        Intent intent = new Intent(Registrarse.this, CreacionPerfil.class);
                                                        intent.putExtra("uid", uid); // Pasamos el UID del usuario
                                                        intent.putExtra("email", email); // Pasamos el correo del usuario
                                                        // Iniciamos la nueva actividad
                                                        startActivity(intent);
                                                        // Establecemos una animación para que sea más suave la transición
                                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                                        finish(); // Cerramos la actividad actual
                                                    }
                                                } else { // En caso de que haya ocurrido un error al verificar el usaurio
                                                    // Lanzamos un Toast con el error de verificación
                                                    showToast("Error al verificar usuario: " + task.getException().getMessage());
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    /**
     * Método para borrar y reiniciar los campos
     * del formulario de registro
     * */
    public void borrarCampos(){
        editCorreo.setText("");
        editClave.setText("");
        editConfirmarClave.setText("");
    }

    /**
     * Método para volver a la pantalla
     * de inicio de sesión tras el registro del
     * usuario
     * */
    public void volverAlLogin(){
        // Genero un nuevo Intent indicando de donde inicio y a donde voy
        Intent in = new Intent(Registrarse.this, InicioSesion.class);
        // Inicializo la nueva actividad
        startActivity(in);
        // Establecemos una transición suave
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        // Finalizamos la actividad actual
        finish();
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
}
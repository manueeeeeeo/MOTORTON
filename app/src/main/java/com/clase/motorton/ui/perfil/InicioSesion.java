package com.clase.motorton.ui.perfil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.clase.motorton.MainActivity;
import com.clase.motorton.R;
import com.clase.motorton.controllers.ControladorEmail;
import com.clase.motorton.controllers.ControladorPassword;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class InicioSesion extends AppCompatActivity {
    // Variable para manejar la autentificación de los usuarios
    private FirebaseAuth auth = null;
    // Variable para manejar la base de datos de la aplicación
    private FirebaseFirestore db = null;
    // Variable para manejar todos los toast de esta pantalla
    private Toast mensajeToast = null;

    // Variable para manejar el botón de inicio de sesión
    private Button btnIniciar = null;
    // Variable para manejar el botón de registro
    private Button btnRegistrar = null;
    // Variable para manejar el editText del correo
    private EditText editCorreo = null;
    // Variable para manejar el editText de la clave
    private EditText editClave = null;
    // Variable que representa la imagen de poder ver la clave o no
    private ImageView imagenVerClave = null;
    // Variable que representa el textview para poder pinchar sobre el perder la clave
    private TextView olvidoClave = null;
    // Variable para manejar el botón de inicio con Google
    private SignInButton iniciarGoogle = null;
    // Variable para guardar en tipo string el correo
    private String correo = null;
    // Variable para guardar en tipo string la clave
    private String clave = null;
    // Variable para manejar el controlador de email
    private ControladorEmail controEmail = null;
    // Variable para manejar el controlador de clave
    private ControladorPassword controPass = null;

    // Variable para manejar el cliente que inicia sesión con Google
    private GoogleSignInClient googleSignInClient=null;
    // Variable para manejar el resultado del lanzador de actividad de inicio de sesión
    private ActivityResultLauncher<Intent> signInLauncher=null;

    // Variable booleana para comprobar si la clave es visibles o no
    boolean claveVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio_sesion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializo la variable de autentificación
        auth = FirebaseAuth.getInstance();
        // Inicializo la variable de la base de datos
        db = FirebaseFirestore.getInstance();

        // Obtengo de la interfaz todos los componentes necesarios para el buen funcionamiento del programa
        btnIniciar = (Button) findViewById(R.id.btnIniciar);
        btnRegistrar = (Button) findViewById(R.id.btnRegistrar);
        editCorreo = (EditText) findViewById(R.id.editEmail);
        editClave = (EditText) findViewById(R.id.editPass);
        imagenVerClave = (ImageView) findViewById(R.id.ver_clave);
        olvidoClave = (TextView) findViewById(R.id.recuperar_text);

        // Obtengo el elemento del botón de iniciar sesión con Google
        iniciarGoogle = findViewById(R.id.sign_in_button);

        // Llamo al método para poder cambiar el contenido de las letras del botón
        cambiarLetrasBoton();

        // Acción que sucede al pulsar el botón de registro
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Creo un nuevo intent indicando que va a redirigir a la pantalla de registrarse
                Intent i = new Intent(InicioSesion.this, Registrarse.class);
                // Inicio la actividad
                startActivity(i);
                // Establezco una animación suave
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                // Finalizo la actividad actual
                finish();
            }
        });

        // Acción que sucede al pulsar el texto de clave olvidada
        olvidoClave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Obtengo en una variable el correo del que se tiene que recuperar la clave
                String correoEnviar = editCorreo.getText().toString().trim();
                // Compruebo que el email no sea nulo
                if (correoEnviar.isEmpty()) { // En caso de que sea nulo o esté vacio
                    // Lanzo un toast indicando al usuario que tiene que ingresar su correo en el editText
                    showToast("Por favor, ingresa tu correo electrónico.");
                } else { // En caso de que no sea nulo
                    // Comprobamos que el controlador del email sea valido o no lo sea
                    if(controEmail.esCorreoValido(correoEnviar)){ // En caso de que si que lo sea
                        auth.sendPasswordResetEmail(correoEnviar)
                                // En caso de que todo se complete correctamente
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        // Procedemos a comprobar si la tarea fue correcta
                                        if (task.isSuccessful()) { // En caso afirmativo
                                            // Lanzamos un toast indicandoselo al usuario
                                            showToast("Correo de restablecimiento enviado.");
                                        } else { // En caso negativo
                                            // Le lanzamos otro toast al usuario indicandoselo
                                            showToast("Error: " + task.getException().getMessage());
                                        }
                                    }
                                });
                    }else{ // En caso de que no lo sea
                        // Lanzamos un toast indicando al usuario que tiene que ingresar un correo valido
                        showToast("Por favor, ingrese un correo electrónico válido.");
                    }
                }
            }
        });

        // Evento que sucede cuando toquemos el botón de iniciar sesión con correo y clave
        btnIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Guardo en la variable el correo que ha introducido el usuario
                correo = (String) editCorreo.getText().toString();
                // Guardo en la variable la clave que ha introducido el usuario
                clave = (String) editClave.getText().toString();
                // Procedemos a comprobar si alguno de los dos está nulo
                if(correo.isEmpty() || clave.isEmpty()){ // En caso de alguno estar nulo
                    // Mostramos un toast diciendo que hay algo nulo
                    showToast("Existe algún campo vacio");
                }else{ // En caso de que todo este correcto
                    // Procedemos a comprobar si el correo es valido
                    if(controEmail.esCorreoValido(correo)){ // En caso de ser valido
                        // Procedemos a comprobar si la clave cumple con los requisitos
                        if(controPass.validarPassword(clave).equals("OK")){ // En caso de si cumplir
                            // Llamamos al método parra iniciar la sesión con el correo y la clave
                            entrarConCorreoYClave(correo, clave);
                        }else{ // En caso de que no cumpla
                            // Lanzamos un toast indicando que lo cumple la clave
                            showToast("Error: " + controPass.validarPassword(clave));
                        }
                    }else{ // En caso de no ser valido
                        // Lanzamos un toast indicando al usuario que ponga un correo de formato adecuado
                        showToast("El correo no tiene el formato adecuado!!");
                    }
                }
            }
        });

        // Evento que sucede cuando tocamos la imagen para ver la clave
        imagenVerClave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Comprobamos la variable que declare antes para el manejo de si mostrar o no la clave
                if (claveVisible) { // En caso de que sea true
                    // Oculto la contraseña
                    editClave.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else { // En caso de que sea false
                    // Muestro la contraseña
                    editClave.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }

                // Mantengo el cursor al final del texto
                editClave.setSelection(editClave.getText().length());
                editClave.requestFocus(); // Aseguro que el EditText mantenga el foco

                // Alterno el estado de visibilidad
                claveVisible = !claveVisible;
            }
        });

        // Método que se ejecutará a la hora de pulsar el botón de iniciar sesión con google
        iniciarGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Llamaremos al método para iniciar sesión con Google
                signInWithGoogle();
            }
        });

        // Crearemos una nueva variable para las opciones de inicio de sesión con Google
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id)) // Obtenemos el token
                .requestEmail() // Obtenemos el email
                .requestProfile() // Obtenemos el perfil del usuario
                .build(); // Construimos las opciones

        // Estableceremos el cliente de login con Google
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // Configuraremos el launcher de inicio de sesion
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Comprobaremos que el código de estado sea el de OK
                    if (result.getResultCode() == RESULT_OK) { // En caso correcto
                        // En un intent guardaremos todos los datos del resultado de este launcher
                        Intent data = result.getData();
                        // Generaremos una tarea para poder obtener el usuario de google con el que se ha iniciado
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        // Utilizaremos un try catch para capturar y tratar todas las posibles excepciones
                        try {
                            // Obtendremos en una variable la cuenta de Google de inicio del usuario
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            // Llamaremos al método para iniciar sesión con Google pasandole la cuenta del usuario
                            firebaseAuthWithGoogle(account);
                        } catch (ApiException e) { // En caso de que surja alguna excepción
                            // Imprimiremos por consola el error al iniciar sesión
                            Log.w("Inicio", "Google sign-in failed", e);
                        }
                    }
                }
        );
    }

    /**
     * Método simple en donde lanzamos un nuevo Intent que será
     * para abrir la pestaña de las cuentas de Google del usuario
     * */
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    /**
     * @param account
     * Método iniciar sesión con Google en nuestra aplicación
     * además a la hora de inciar sesión, en caso de que todo haya ido bien, comprobaremos
     * si el usuario ya tiene una cuenta en la base de datos de perfiles de la app o no, para
     * así redirigirle a una pantalla u otra
     * */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Una vez completado, comprobamos si la tarea fue correctamente realizada
                        if (task.isSuccessful()) { // En caso afirmativo
                            // Obtengo en una variable el usuario de firebase autenticado en la app
                            FirebaseUser user = auth.getCurrentUser();
                            //Comprobamos si el usuario tiene algo
                            if (user != null) { // En caso afirmativo
                                // Guardamos en una variable el uid del usuario
                                String uid = user.getUid();

                                // Verificamos si el perfil está creado en Firestore
                                db.collection("perfiles").document(uid).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                // Una vez completado, comprobamos si la tarea fue correctamente realizada
                                                if (task.isSuccessful()) { // En caso afirmativo
                                                    // Obtenemos un documento de la base de datos para comprobar su contenido
                                                    DocumentSnapshot document = task.getResult();
                                                    // Comprobamos si existe
                                                    if (document.exists()) { // En caso afirmativo
                                                        // Perfil encontrado, redirigimos al MainActivity
                                                        showToast("Inicio de sesión exitoso");
                                                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                                        SharedPreferences prefs = getSharedPreferences("BetaPrefs", MODE_PRIVATE);
                                                        String codigo = prefs.getString("codigoBeta", null);
                                                        Log.d("BetaCode", "Código beta recuperado: " + codigo);

                                                        if (codigo != null) {
                                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                            DocumentReference docRef = db.collection("invitationCodes").document(codigo);

                                                            Map<String, Object> data = new HashMap<>();
                                                            data.put("userUID", uid);

                                                            // Verifica que el documento existe antes de hacer update
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

                                                        Intent intent = new Intent(InicioSesion.this, MainActivity.class);
                                                        intent.putExtra("name", user.getDisplayName()); // Pasamos el username del usuario
                                                        intent.putExtra("email", user.getEmail()); // Pasamos el email del usuario
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        // Perfil no encontrado, redirigimos a la pantalla de creación del perfil
                                                        showToast("Redirigiendo para completar perfil...");
                                                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                                                        Intent intent = new Intent(InicioSesion.this, CreacionPerfil.class);
                                                        intent.putExtra("uid", uid); // Pasamos el uid del usuario
                                                        intent.putExtra("email", user.getEmail()); // Pasamos el email del usuario
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                } else { // En caso negativo
                                                    // Lanzamos un toast indicando que surgio algún error al verificar el perfil
                                                    showToast("Error al verificar el perfil: " + task.getException().getMessage());
                                                }
                                            }
                                        });
                            }
                        } else { // En caso negativo a la hora del inicio de sesión
                            // Imprimimos por el logcat el error ocurrido
                            Log.w("Inicio", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    /**
     * @param email
     * @param pass
     * Método iniciar sesión con un correo y una clave en nuestra aplicación
     * además a la hora de inciar sesión, en caso de que todo haya ido bien, comprobaremos
     * si el usuario ya tiene una cuenta en la base de datos de perfiles de la app o no, para
     * así redirigirle a una pantalla u otra
     * */
    public void entrarConCorreoYClave(String email, String pass){
        auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Una vez completado, comprobamos si la tarea fue correctamente realizada
                        if (task.isSuccessful()) { // En caso positivo
                            // Obtenemos en una variable el usuario actual autentifcado en la app
                            FirebaseUser user = auth.getCurrentUser();
                            // Comprobamos si hay un usuario autenticado
                            if (user != null) { // En caso de que si que lo haya
                                // Guardamos en una variable el uid del usuario
                                String uid = user.getUid();

                                // Verificamos si el perfil está creado en Firestore
                                db.collection("perfiles").document(uid).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                // Una vez completado, comprobamos si la tarea fue correctamente realizada
                                                if (task.isSuccessful()) { // En caso positivo
                                                    // Obtenemos un documento de la base de datos para comprobar su contenido
                                                    DocumentSnapshot document = task.getResult();
                                                    // Comprobamos si existe
                                                    if (document.exists()) {
                                                        // Perfil encontrado, redirigimos al MainActivity
                                                        showToast("Inicio de sesión exitoso");
                                                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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

                                                        Intent intent = new Intent(InicioSesion.this, MainActivity.class);
                                                        intent.putExtra("email", email); // Pasamos el correo del usuario
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        // Perfil no encontrado, redirigimos a la pantalla de creación del perfil
                                                        showToast("Redirigiendo para completar perfil...");
                                                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                                                        Intent intent = new Intent(InicioSesion.this, CreacionPerfil.class);
                                                        intent.putExtra("uid", uid); // Pasamos el UID del usuario
                                                        intent.putExtra("email", email); // Pasamos el correo del usuario
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                } else { // En caso negativo
                                                    // Indicamos un toast en donde establecemos que hubo un error a la hora de verificar el perfil
                                                    showToast("Error al verificar el perfil: " + task.getException().getMessage());
                                                }
                                            }
                                        });
                            }
                        } else { // En caso de que surja un error al iniciar sesión
                            // Guardamos en una variable el error
                            String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                            // Lanzamos un toast indicandole
                            showToast("Error: " + error);
                        }
                    }
                });
    }

    /**
     * Método para poder cambiar lo escrito en el botón de inicio de sesión con Google,
     * tengo que cambiarlo así porque manualmente no he encontrado forma, lo que pasa esque
     * tengo que ir recorriendo todos los elementos y objetos hijos del botón hasta encontrar
     * algo que seá un TextView que es el que representa el mensaje, una vez le encontremos
     * lo que tengo que hacer es establecer el texto que quiero poner que en mi caso es
     * Sign in with Google
     */
    public void cambiarLetrasBoton(){
        // Recorro todos los elementos y objetos hijos del botón de inicio de sesión
        for (int i = 0; i < iniciarGoogle.getChildCount(); i++) {
            // Voy obteniendo las vistas
            android.view.View view = iniciarGoogle.getChildAt(i);
            // En caso de que encuentre el TextView que conforma el mensaje del botón
            if (view instanceof TextView) {
                // Le cambio el contenido al TetxView
                ((TextView) view).setText("Sign in with Google");
                // Y termino con el bucle
                break;
            }
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
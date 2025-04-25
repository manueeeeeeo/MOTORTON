package com.clase.motorton.ui.perfil;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.clase.motorton.R;
import com.clase.motorton.cifrado.CifradoDeDatos;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

public class EditarPerfilActivity extends AppCompatActivity {
    // Variable para manejar el botón de crear perfil
    private Button btnCrear = null;
    // Variable para manejar el botón de borrar campos
    private Button btnBorrarCampos = null;
    // Variable para manejar el editText del username
    private EditText editUsername = null;
    // Variable para manejar el editText de la ubicación
    private EditText editUbi = null;
    // Variable para manejar el editText del código postal
    private EditText editCP = null;
    // Variable para manejar el editText de la descripción
    private EditText editDescrip = null;
    // Variable para manejar el editText de los años conduciendo
    private EditText editAnoCon = null;
    // Variable para manejar el imageview de la foto de perfil del usuario
    private ImageView imagenPerfil = null;
    // Variable para manejar el botón de ir a elegir la ubicación
    private Button btnElegirUbicacion = null;
    // Variable para manejar todos los Toast de está pantalla
    private Toast mensajeToast = null;

    // Variable para guardar la ubicación
    private String ubicacion = null;
    // Variable para guardar el username
    private String username = null;
    // Variable para guardar el código postal
    private String cp = null;
    // Variable para guardar la descripción
    private String descripcion = null;
    // Variable para guardar los años que ha conducido el usuario
    private String anosPermiso = null;

    // Variable para formatear la la fecha de nacimiento
    private SimpleDateFormat dateFormat = null;

    // Variable en donde guardo todos los datos de la ubicación obtenida
    private Map<String, Object> ubicacionSeleccionada;

    // Variable para manejar el código de solicitud de permisos
    private static final int PERMISSION_REQUEST_CODE = 100;

    // Variable para cifrar los datos sensibles
    private CifradoDeDatos cifrar = null;
    // Variable para generar claves secretas a la hora de cifrar datos
    private SecretKey claveSecreta = null;

    // Lanzador de actividad para obtener el resultado de la foto hecha con la camara
    private final ActivityResultLauncher<Intent> cameraResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) { // En caso de que el resultado sea OK
                    // Obtengo en una variable el resultado data obtenido con el Intent
                    Intent data = result.getData();
                    // Comprobamos que la data no sea nula
                    if (data != null) { // En caso de no ser nulo
                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                        // Establezco la imagen uri en el componente de ImageView
                        imagenPerfil.setImageBitmap(photo);
                    }
                }
            });

    // Lanzador de actividad para obtener el resultado de la foto escogida de galeria
    private final ActivityResultLauncher<Intent> galleryResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) { // En caso de que el resultado sea OK
                    // Obtengo en una variable el resultado data obtenido con el Intent
                    Intent data = result.getData();
                    // Comprobamos que la data no sea nula
                    if (data != null) { // En caso de no ser nulo
                        // Obtengo en una variable de tipo Uri la data
                        Uri imageUri = data.getData();
                        // Establezco la imagen uri en el componente de ImageView
                        imagenPerfil.setImageURI(imageUri);
                    }
                }
            });

    // Variable para manejar la base de datos de firestore
    private FirebaseFirestore db = null;

    // Variable para manejar la autentificación del usuario
    private FirebaseAuth auth = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_perfil);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializamos tanto la autentificación como la base de datos de firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Obtengo en las variables todos los elementos visuales interactivos de la interfaz
        btnCrear = (Button) findViewById(R.id.create_profile_button);
        btnBorrarCampos = (Button) findViewById(R.id.clear_fields_button);
        editCP = (EditText) findViewById(R.id.zipcode_input);
        editDescrip = (EditText) findViewById(R.id.description_input);
        editUsername = (EditText) findViewById(R.id.username_input);
        imagenPerfil = (ImageView) findViewById(R.id.imagenPerfil);
        editAnoCon = (EditText) findViewById(R.id.years_driving_input);
        btnElegirUbicacion = findViewById(R.id.btnElegirUbicacion);

        // Inicializamos el hashMap
        ubicacionSeleccionada = new HashMap<>();

        // Establecemos el formato a establecer
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Inicializamos el cifrado de datos
        cifrar = new CifradoDeDatos();
    }

    /**
     * Método para resetear el valor
     * de todos los campos del formulario
     */
    public void resetarCampos(){
        editAnoCon.setText("");
        editUsername.setText("");
        editDescrip.setText("");
        editUsername.setText("");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                showImagePickerDialog();
            } else {
                showToast("Permisos necesarios no otorgados");
            }
        }
    }

    /**
     * Método en el que mostramos al usuario un
     * dialogo para que él, eliga de donde quiere sacar
     * la imagen para su perfil, o desde camara o desde
     * galeria
     */
    private void showImagePickerDialog() {
        // Variable en donde cargamos todas las opciones a la hora de subir la foto
        CharSequence[] options = {"Tomar Foto", "Elegir de Galería"};

        // Creo un nuevo dialogo de alerta
        new AlertDialog.Builder(EditarPerfilActivity.this)
                .setTitle("Elegir Imagen") // Establecemos el título
                .setItems(options, (dialog, which) -> {
                    // Utilizamos un if para comprobar la opción que eligio el usuario
                    if (which == 0) { // En caso de ser la opción 0 (Foto de la Camara)
                        // Creamos un intent para abrir la camara dentro de nuestra app
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        // Al lanzar obtenemos el resultado de la camara
                        cameraResult.launch(takePictureIntent);
                    } else if (which == 1) { // En caso de ser la opción 1 (Foto de la Galeria)
                        // Creamos un intent para abrir la galeria y elegir una foto de la misma
                        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        // Al lanzar obtenemos el resultado de la galeri
                        galleryResult.launch(pickPhotoIntent);
                    }
                })
                .show(); // Mostramos el dialogo
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

    /**
     * @param data
     * @param requestCode
     * @param resultCode
     * Método en el que tratamos y obtenemos todos
     * los parametros de la ubicación del usuario.
     * Obtenemos la latitud, longitud y dirección
     * de la ubicación elegida por el usuario
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Obtenemos la latitud y si no existe la ponemos como 0.0
            double latitud = data.getDoubleExtra("latitud", 0.0);
            // Obtenemos la longitud y si no existe la ponemos como 0.0
            double longitud = data.getDoubleExtra("longitud", 0.0);
            // Obtenemos la dirección
            String direccion = data.getStringExtra("direccion");

            // Inicializamos la ubicación seleccionada como un nuevo HashMap
            ubicacionSeleccionada = new HashMap<>();
            // Guardamos la latitud cifrada
            ubicacionSeleccionada.put("latitud", CifradoDeDatos.cifrar(String.valueOf(latitud)));
            // Guardamos la longtiud cifrada
            ubicacionSeleccionada.put("longitud", CifradoDeDatos.cifrar(String.valueOf(longitud)));
            // Guardamos la dirección cifrada
            ubicacionSeleccionada.put("direccion", CifradoDeDatos.cifrar(direccion));

            // Establecemos al botón el texto de la dirección obtenida
            btnElegirUbicacion.setText(direccion);
        }
    }
}
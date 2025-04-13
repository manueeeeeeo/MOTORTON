package com.clase.motorton.ui.perfil;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
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
import com.clase.motorton.modelos.Perfil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

public class CreacionPerfil extends AppCompatActivity {
    // Variable para manejar el editText de la fecha de nacimiento
    private EditText editFechaNacimiento = null;
    // Variable para manejar el botón de crear perfil
    private Button btnCrear = null;
    // Variable para manejar el botón de borrar campos
    private Button btnBorrarCampos = null;
    // Variable para manejar el editText del username
    private EditText editUsername = null;
    // Variable para manejar el editText del nombre completo
    private EditText editNombre = null;
    // Variable para manejar el editText de los años conduciendo
    private EditText anosConduciendo = null;
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
    // Variable para guardar el nombre
    private String nombre = null;
    // Variable para guardar la fecha de nacimiento
    private String fechaNaci = null;
    // Variable para guardar el código postal
    private String cp = null;
    // Variable para guardar la descripción
    private String descripcion = null;
    // Variable para guardar los años que ha conducido el usuario
    private String anosPermiso = null;
    // Variable para guardar la edad del usuario
    private int edad = 0;

    // Variable para cifrar los datos sensibles
    private CifradoDeDatos cifrar = null;
    // Variable para generar claves secretas a la hora de cifrar datos
    private SecretKey claveSecreta = null;

    // Variable para formatear la la fecha de nacimiento
    private SimpleDateFormat dateFormat = null;

    // Variable en donde guardo todos los datos de la ubicación obtenida
    private Map<String, Object> ubicacionSeleccionada;

    // Variable para manejar el código de solicitud de permisos
    private static final int PERMISSION_REQUEST_CODE = 100;

    // Lanzador de actividad para obtener el resultado de la foto hecha con la camara
    private final ActivityResultLauncher<Intent> cameraResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Bitmap photo = (Bitmap) data.getExtras().get("data");
                        imagenPerfil.setImageBitmap(photo);
                    }
                }
            });

    // Lanzador de actividad para obtener el resultado de la foto escogida de galeria
    private final ActivityResultLauncher<Intent> galleryResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri imageUri = data.getData();
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
        setContentView(R.layout.activity_creacion_perfil);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializamos tanto la autentificación como la base de datos de firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Obtengo en las variables todos los elementos visuales interactivos de la interfaz
        editFechaNacimiento = findViewById(R.id.dob_input);
        btnCrear = (Button) findViewById(R.id.create_profile_button);
        btnBorrarCampos = (Button) findViewById(R.id.clear_fields_button);
        editCP = (EditText) findViewById(R.id.zipcode_input);
        editNombre = (EditText) findViewById(R.id.full_name_input);
        editDescrip = (EditText) findViewById(R.id.description_input);
        editUsername = (EditText) findViewById(R.id.username_input);
        imagenPerfil = (ImageView) findViewById(R.id.imagenPerfil);
        editAnoCon = (EditText) findViewById(R.id.years_driving_input);
        btnElegirUbicacion = findViewById(R.id.btnElegirUbicacion);

        // Inicializamos el hashMap
        ubicacionSeleccionada = new HashMap<>();

        // Establecemos el formato a establecer
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        editFechaNacimiento.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    CreacionPerfil.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        editFechaNacimiento.setText(selectedDate);
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });
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
        editNombre.setText("");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                showImagePickerDialog();
            } else {
                Toast.makeText(this, "Permisos necesarios no otorgados", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showImagePickerDialog() {
        CharSequence[] options = {"Tomar Foto", "Elegir de Galería"};

        new AlertDialog.Builder(CreacionPerfil.this)
                .setTitle("Elegir Imagen")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraResult.launch(takePictureIntent);
                    } else if (which == 1) {
                        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        galleryResult.launch(pickPhotoIntent);
                    }
                })
                .show();
    }

    public void insertarPerfil() {
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "No se pudo obtener la información del usuario. Inicia sesión nuevamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        String email = user.getEmail();

        String campoUsername = editUsername.getText().toString();
        String nombreCifrado = CifradoDeDatos.cifrar(editNombre.getText().toString());
        String descripcionCifrada = CifradoDeDatos.cifrar(editDescrip.getText().toString());
        String fechaNaciCifrada = CifradoDeDatos.cifrar(editFechaNacimiento.getText().toString());
        String cpCifrado = CifradoDeDatos.cifrar(editCP.getText().toString());
        String anosPermisoCifrado = CifradoDeDatos.cifrar(editAnoCon.getText().toString());
        String emailCifrado = CifradoDeDatos.cifrar(email);

        BitmapDrawable drawable = (BitmapDrawable) imagenPerfil.getDrawable();
        Bitmap fotoBitmap = drawable != null ? drawable.getBitmap() : null;

        String fotoPerfilBase64 = null;
        if (fotoBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            fotoBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);

            byte[] fotoBytes = baos.toByteArray();
            fotoPerfilBase64 = Base64.encodeToString(fotoBytes, Base64.DEFAULT);
        }

        Perfil perfil = new Perfil(
                uid,
                campoUsername,
                nombreCifrado,
                null,
                edad,
                fechaNaciCifrada,
                cp.isEmpty() ? 0 : Integer.parseInt(cp),
                new ArrayList<>(),
                fotoBitmap,
                emailCifrado
        );

        Map<String, Object> perfilMap = new HashMap<>();
        perfilMap.put("uid", perfil.getUid());
        perfilMap.put("username", perfil.getUsername());
        perfilMap.put("nombre_completo", perfil.getNombre_completo());
        perfilMap.put("ubicacion", ubicacionSeleccionada);
        perfilMap.put("edad", perfil.getEdad());
        perfilMap.put("fechaNaci", perfil.getFechaNaci());
        perfilMap.put("cp", perfil.getCp());
        perfilMap.put("email", perfil.getEmail());
        perfilMap.put("aniosConduciendo", anosPermisoCifrado);
        perfilMap.put("listaVehiculos", perfil.getListaVehiculos());
        perfilMap.put("fotoPerfil", fotoPerfilBase64);

        db.collection("perfiles").document(uid).set(perfilMap)
                .addOnSuccessListener(aVoid -> {
                    showToast("Perfil creado exitosamente");
                    Intent i = new Intent(CreacionPerfil.this, AdministrarVehiculos.class);
                    i.putExtra("uid", uid);
                    startActivity(i);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToast("Error al crear perfil: " + e.getMessage());
                });
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            double latitud = data.getDoubleExtra("latitud", 0.0);
            double longitud = data.getDoubleExtra("longitud", 0.0);
            String direccion = data.getStringExtra("direccion");

            ubicacionSeleccionada = new HashMap<>();
            ubicacionSeleccionada.put("latitud", CifradoDeDatos.cifrar(String.valueOf(latitud)));
            ubicacionSeleccionada.put("longitud", CifradoDeDatos.cifrar(String.valueOf(longitud)));
            ubicacionSeleccionada.put("direccion", CifradoDeDatos.cifrar(direccion));

            btnElegirUbicacion.setText(direccion);
        }
    }
}
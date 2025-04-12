package com.clase.motorton.ui.perfil;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
}
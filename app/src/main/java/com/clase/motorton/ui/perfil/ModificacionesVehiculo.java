package com.clase.motorton.ui.perfil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.clase.motorton.R;
import com.clase.motorton.modelos.FotoVehiculoTemporal;
import com.clase.motorton.modelos.Vehiculo;
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
    private Switch switchBodyKit = null;
    private Switch switchLucesLed = null;

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

    // Variable para manejar el código de solicitud de permisos
    private static final int PERMISSION_REQUEST_CODE = 100;

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
                        fotoVehiculo.setImageBitmap(photo);
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
                        fotoVehiculo.setImageURI(imageUri);
                    }
                }
            });

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
        switchBodyKit = (Switch) findViewById(R.id.switchBodyKit);
        switchLucesLed = (Switch) findViewById(R.id.switchLuces);

        Vehiculo vehiculo = (Vehiculo) getIntent().getSerializableExtra("vehiculo");

        if (vehiculo != null) {
            editRuedas.setText(vehiculo.getRuedas());
            editTuboEscape.setText(vehiculo.getTuboEscape());
            editAleron.setText(vehiculo.getAleron());
            editChoques.setText(String.valueOf(vehiculo.getChoques()));
            editCv.setText(String.valueOf(vehiculo.getCv()));
            editMaxVel.setText(String.valueOf(vehiculo.getMaxVelocidad()));

            String fotoAnti = vehiculo.getFoto();

            if (fotoAnti != null && !fotoAnti.isEmpty()) {
                Bitmap decodedBitmap = convertirBase64ABitmap(fotoAnti);
                fotoVehiculo.setImageBitmap(decodedBitmap);
            }

            switchBodyKit.setChecked(vehiculo.isBodyKit());
            switchLucesLed.setChecked(vehiculo.isLucesLed());
        }

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnGuardarCambios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarDatosModificaciones();
            }
        });

        fotoVehiculo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean cameraPermission = ContextCompat.checkSelfPermission(ModificacionesVehiculo.this, android.Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED;

                boolean storagePermission;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    storagePermission = ContextCompat.checkSelfPermission(ModificacionesVehiculo.this, android.Manifest.permission.READ_MEDIA_IMAGES)
                            == PackageManager.PERMISSION_GRANTED;
                } else {
                    storagePermission = ContextCompat.checkSelfPermission(ModificacionesVehiculo.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED;
                }

                if (!cameraPermission || !storagePermission) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ActivityCompat.requestPermissions(ModificacionesVehiculo.this,
                                new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.READ_MEDIA_IMAGES},
                                PERMISSION_REQUEST_CODE);
                    } else {
                        ActivityCompat.requestPermissions(ModificacionesVehiculo.this,
                                new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                PERMISSION_REQUEST_CODE);
                    }
                } else {
                    showImagePickerDialog();
                }
            }
        });
    }

    public static Bitmap convertirBase64ABitmap(String base64) {
        if (base64 == null || base64.isEmpty()) return null;
        byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    private void guardarDatosModificaciones(){
        try {
            tubo = editTuboEscape.getText().toString();
            aleron = editAleron.getText().toString();
            ruedas = editRuedas.getText().toString();
            choques = Integer.parseInt(editChoques.getText().toString());
            cv = Double.parseDouble(editCv.getText().toString());
            maxVe = Double.parseDouble(editMaxVel.getText().toString());
            luces = switchLucesLed.isChecked();
            bodykit = switchBodyKit.isChecked();

            // Obtenemos en una variable el recurso que se ha puesto en la imageview de la imagen de perfil
            BitmapDrawable drawable = (BitmapDrawable) fotoVehiculo.getDrawable();
            // Obtenemos en una variable el bitmao de la imagen elegida
            Bitmap fotoBitmap = drawable != null ? drawable.getBitmap() : null;

            if (fotoBitmap != null) {
                Bitmap resized = resizeBitmap(fotoBitmap, 800, 800);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 70, baos);

                byte[] fotoBytes = baos.toByteArray();
                foto = Base64.encodeToString(fotoBytes, Base64.DEFAULT);

                if (fotoBytes.length > 900_000) {
                    showToast("La imagen es demasiado grande. Usa una más pequeña.");
                    return;
                }
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("tubo", tubo);
            resultIntent.putExtra("aleron", aleron);
            resultIntent.putExtra("ruedas", ruedas);
            resultIntent.putExtra("choques", choques);
            resultIntent.putExtra("cv", cv);
            resultIntent.putExtra("maxVe", maxVe);
            resultIntent.putExtra("luces", luces);
            resultIntent.putExtra("bodykit", bodykit);
            FotoVehiculoTemporal.setTempFotoBase64(foto);

            setResult(RESULT_OK, resultIntent);
        }catch (Exception e) {
            showToast("Error guardando datos: " + e.getMessage());
        } finally {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
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
        new AlertDialog.Builder(ModificacionesVehiculo.this)
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

    private Bitmap resizeBitmap(Bitmap original, int maxWidth, int maxHeight) {
        int width = original.getWidth();
        int height = original.getHeight();

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;
        if (ratioMax > ratioBitmap) {
            finalWidth = (int) ((float)maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float)maxWidth / ratioBitmap);
        }
        return Bitmap.createScaledBitmap(original, finalWidth, finalHeight, true);
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
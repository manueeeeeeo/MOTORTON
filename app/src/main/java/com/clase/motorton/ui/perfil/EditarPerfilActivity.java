package com.clase.motorton.ui.perfil;

import android.Manifest;
import android.app.AlertDialog;
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

import com.clase.motorton.MainActivity;
import com.clase.motorton.R;
import com.clase.motorton.cifrado.CifradoDeDatos;
import com.clase.motorton.modelos.Perfil;
import com.clase.motorton.ui.mapas.ElegirUbicacion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    // Variable para manejar el editText para editar el nombre completo
    private EditText editNombreComple = null;
    // Variable para manejar todos los Toast de está pantalla
    private Toast mensajeToast = null;

    // Variable para guardar el username
    private String username = null;
    // Variable para guardar el código postal
    private String cp = null;
    // Variable para guardar la descripción
    private String descripcion = null;
    // Variable para guardar los años que ha conducido el usuario
    private String anosPermiso = null;
    // Variable para manejar la foto de perfil anterior
    private String fotoPerfilAnterior = null;

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
    private FirebaseUser user = null;

    // Variable en donde guardamos los datos actuales del perfil del usuario
    private Perfil perfilActual = null;

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
        editNombreComple = (EditText) findViewById(R.id.full_name_input);

        // Inicializamos el hashMap
        ubicacionSeleccionada = new HashMap<>();

        // Establecemos el formato a establecer
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Inicializamos el cifrado de datos
        cifrar = new CifradoDeDatos();

        // Evento que sucede cuando pulsamos sobre el imageview de la imagen de perfil
        imagenPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean cameraPermission = ContextCompat.checkSelfPermission(EditarPerfilActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED;

                boolean storagePermission;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    storagePermission = ContextCompat.checkSelfPermission(EditarPerfilActivity.this, Manifest.permission.READ_MEDIA_IMAGES)
                            == PackageManager.PERMISSION_GRANTED;
                } else {
                    storagePermission = ContextCompat.checkSelfPermission(EditarPerfilActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED;
                }

                if (!cameraPermission || !storagePermission) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ActivityCompat.requestPermissions(EditarPerfilActivity.this,
                                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES},
                                PERMISSION_REQUEST_CODE);
                    } else {
                        ActivityCompat.requestPermissions(EditarPerfilActivity.this,
                                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                                PERMISSION_REQUEST_CODE);
                    }
                } else {
                    showImagePickerDialog();
                }
            }
        });

        // Evento que sucede cuando pulsamos el botón de elegir ubicación
        btnElegirUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Creamos un nuevo intent para ir a la actividad en donde elegimos nuestra ubicación
                Intent intent = new Intent(EditarPerfilActivity.this, ElegirUbicacion.class);
                // Obtenemos lo que nos devuelva la actividad de elegir nuestra ubicación
                startActivityForResult(intent, 1);
            }
        });

        // Evento que sucede cuando pulsamos el botón de borrar los campos
        btnBorrarCampos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Llamamos al método para resetear los campos
                resetarCampos();
            }
        });

        // Evento que sucede cuando pulsamos el botón de crear perfil
        btnCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Obtengo en las variable todos los valores de los editText
                username = editUsername.getText().toString();
                anosPermiso = editAnoCon.getText().toString();
                descripcion = editDescrip.getText().toString();
                cp = editCP.getText().toString();

                // Procedemos a comprobar si están todos los campos rellenos
                if(username.isEmpty() || anosPermiso.isEmpty() || descripcion.isEmpty()
                        || cp.isEmpty()){ // En caso de faltar alguno
                    // Lanzamos un Toast indicando que tiene que completar todos los campos
                    showToast("Usted ha de completar todos los campos, por favor!!");
                }else{ // En caso de estar todos rellenos
                    // Llamamos al método para insertar el perfil
                    actualizarPerfil();
                }
            }
        });

        cargarDatos();
    }

    public void cargarDatos() {
        user = auth.getCurrentUser();

        if (user == null) {
            showToast("No se pudo obtener la información del usuario. Inicia sesión nuevamente.");
            return;
        }

        String uid = user.getUid();

        db.collection("perfiles")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username1 = documentSnapshot.getString("username");
                        String nombreCompleto = documentSnapshot.getString("nombre_completo");
                        String fechaNaci = documentSnapshot.getString("fechaNaci");
                        String emailCifrado = documentSnapshot.getString("email");
                        Map<String, Object> ubicacion = (Map<String, Object>) documentSnapshot.get("ubicacion");
                        Long cpLong = documentSnapshot.getLong("cp");
                        int cp = (cpLong != null) ? cpLong.intValue() : 0;
                        String descripcion1 = documentSnapshot.getString("descripcion");
                        Long aniosPermisoLong = documentSnapshot.getLong("aniosConduciendo");
                        int aniosPermiso = (aniosPermisoLong != null) ? aniosPermisoLong.intValue() : 0;
                        String fotoBase64 = documentSnapshot.getString("fotoPerfil");
                        fotoPerfilAnterior = fotoBase64;

                        String nombreCompletoDescifrado = "";
                        String emailDescifrado = "";

                        try {
                            if (nombreCompleto != null)
                                nombreCompletoDescifrado = CifradoDeDatos.descifrar(nombreCompleto);
                            if (emailCifrado != null)
                                emailDescifrado = CifradoDeDatos.descifrar(emailCifrado);
                        } catch (Exception e) {
                            showToast("Error al descifrar datos personales.");
                        }

                        editUsername.setText(username1 != null ? username1 : "");
                        editCP.setText(String.valueOf(cp));
                        editDescrip.setText(descripcion1 != null ? descripcion1 : "");
                        editAnoCon.setText(String.valueOf(aniosPermiso));

                        if (fotoBase64 != null && !fotoBase64.isEmpty()) {
                            Bitmap decodedBitmap = convertirBase64ABitmap(fotoBase64);
                            imagenPerfil.setImageBitmap(decodedBitmap);
                        }

                        if (ubicacion != null) {
                            ubicacionSeleccionada = new HashMap<>(ubicacion);
                            Object direccionCifrada = ubicacion.get("direccion");
                            if (direccionCifrada != null) {
                                String direccion = "";
                                if (direccionCifrada != null) {
                                    try {
                                        direccion = CifradoDeDatos.descifrar(direccionCifrada.toString());
                                    } catch (Exception e) {
                                        showToast("Error al descifrar la dirección.");
                                    }
                                }
                                btnElegirUbicacion.setText(direccion);
                            }
                        }

                        perfilActual = new Perfil(
                                uid,
                                username1,
                                emailDescifrado,
                                nombreCompletoDescifrado,
                                ubicacion,
                                0,
                                fechaNaci,
                                cp,
                                new ArrayList<>(),
                                null,
                                descripcion1,
                                aniosPermiso
                        );

                    } else {
                        showToast("No se encontró el perfil del usuario.");
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Error al obtener los datos del perfil: " + e.getMessage());
                });
    }

    public void actualizarPerfil() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            showToast("No se pudo obtener la información del usuario. Inicia sesión nuevamente.");
            return;
        }

        if (perfilActual == null) {
            showToast("No se ha cargado el perfil original.");
            return;
        }

        String uid = user.getUid();

        String nuevoUsername = editUsername.getText().toString().trim();
        String nuevoNombreCompleto = editNombreComple.getText().toString().trim();
        String nuevoCPStr = editCP.getText().toString().trim();
        int nuevoCP = 0;
        try {
            nuevoCP = nuevoCPStr.isEmpty() ? 0 : Integer.parseInt(nuevoCPStr);
        } catch (NumberFormatException e) {
            showToast("El código postal debe ser un número válido.");
            return;
        }
        String nuevaDescripcion = editDescrip.getText().toString().trim();
        String nuevosAniosPermisoStr = editAnoCon.getText().toString().trim();
        int nuevosAniosPermiso = 0;
        try {
            nuevosAniosPermiso = nuevosAniosPermisoStr.isEmpty() ? 0 : Integer.parseInt(nuevosAniosPermisoStr);
        } catch (NumberFormatException e) {
            showToast("Los años conduciendo deben ser un número válido.");
            return;
        }

        BitmapDrawable drawable = (BitmapDrawable) imagenPerfil.getDrawable();
        Bitmap nuevaFotoBitmap = drawable != null ? drawable.getBitmap() : null;

        String nuevaFotoBase64 = null;
        if (nuevaFotoBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            nuevaFotoBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
            byte[] fotoBytes = baos.toByteArray();
            nuevaFotoBase64 = Base64.encodeToString(fotoBytes, Base64.DEFAULT);
        }

        Map<String, Object> cambios = new HashMap<>();
        if (!nuevoUsername.equals(perfilActual.getUsername())) {
            cambios.put("username", nuevoUsername);
        }
        if (nuevoCP != perfilActual.getCp()) {
            cambios.put("cp", nuevoCP);
        }
        if (!nuevaDescripcion.equals(perfilActual.getDescripcion())) {
            cambios.put("descripcion", nuevaDescripcion);
        }
        if (nuevosAniosPermiso != perfilActual.getAniosConduciendo()) {
            cambios.put("aniosConduciendo", nuevosAniosPermiso);
        }
        if (nuevaFotoBase64 != null && !nuevaFotoBase64.equals(fotoPerfilAnterior)) {
            cambios.put("fotoPerfil", nuevaFotoBase64);
        }
        if (!ubicacionesSonIguales(perfilActual.getUbicacion(), ubicacionSeleccionada)) {
            cambios.put("ubicacion", ubicacionSeleccionada);
        }
        if (!nuevoNombreCompleto.equals(perfilActual.getNombre_completo())) {
            try {
                String nombreCifrado = CifradoDeDatos.cifrar(nuevoNombreCompleto);
                cambios.put("nombre_completo", nombreCifrado);
            } catch (Exception e) {
                showToast("Error al cifrar el nombre.");
                return;
            }
        }

        if (cambios.isEmpty()) {
            showToast("No se detectaron cambios para actualizar.");
            return;
        }

        if (cambios.containsKey("username")) {
            db.collection("perfiles")
                    .whereEqualTo("username", nuevoUsername)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty() || (queryDocumentSnapshots.size() == 1 && queryDocumentSnapshots.getDocuments().get(0).getId().equals(uid))) {
                            actualizarEnFirestore(uid, cambios);
                        } else {
                            showToast("El nombre de usuario ya está en uso. Por favor elige otro.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        showToast("Error al verificar el nombre de usuario: " + e.getMessage());
                    });
        } else {
            actualizarEnFirestore(uid, cambios);
        }
    }

    private boolean ubicacionesSonIguales(Map<String, Object> a, Map<String, Object> b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        if (a.size() != b.size()) return false;

        for (String key : a.keySet()) {
            Object valorA = a.get(key);
            Object valorB = b.get(key);
            if (valorA == null && valorB != null) return false;
            if (valorA != null && !valorA.equals(valorB)) return false;
        }
        return true;
    }

    private void actualizarEnFirestore(String uid, Map<String, Object> cambios) {
        db.collection("perfiles")
                .document(uid)
                .update(cambios)
                .addOnSuccessListener(aVoid -> {
                    showToast("Perfil actualizado exitosamente.");
                    Intent i = new Intent(EditarPerfilActivity.this, MainActivity.class);
                    i.putExtra("uid", uid);
                    startActivity(i);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToast("Error al actualizar el perfil: " + e.getMessage());
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
    }

    public static Bitmap convertirBase64ABitmap(String base64) {
        if (base64 == null || base64.isEmpty()) return null;
        byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
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
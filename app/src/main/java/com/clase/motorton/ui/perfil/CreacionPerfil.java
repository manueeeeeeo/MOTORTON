package com.clase.motorton.ui.perfil;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import com.clase.motorton.R;
import com.clase.motorton.cifrado.CifradoDeDatos;
import com.clase.motorton.modelos.Perfil;
import com.clase.motorton.ui.mapas.ElegirUbicacion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
        setContentView(R.layout.activity_creacion_perfil);
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

        // Inicializamos el cifrado de datos
        cifrar = new CifradoDeDatos();

        // Evento que sucede cuando tocamos el ediText de la fecha de nacimeinto
        editFechaNacimiento.setOnClickListener(v -> {
            // Creamos un nuevo calendario con la instancia actual para que marque el día de hoy
            Calendar calendar = Calendar.getInstance();
            // Guardamos en una variable al año
            int year = calendar.get(Calendar.YEAR);
            // Guardamos en una variable el mes
            int month = calendar.get(Calendar.MONTH);
            // Guardamos en una variable el día
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Creo un nuevo dialogo de selección de fecha
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    CreacionPerfil.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Le damos formato a la fecha para que quede bien
                        String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        editFechaNacimiento.setText(selectedDate); // Introducimos la fecha seleccionada en el ediText
                    },
                    year, month, day // Establecemos que queremos el año, mes y día
            );

            // Mostramos el dialogo de elección de la fecha
            datePickerDialog.show();
        });

        // Evento que sucede cuando pulsamos sobre el imageview de la imagen de perfil
        imagenPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean cameraPermission = ContextCompat.checkSelfPermission(CreacionPerfil.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED;

                boolean storagePermission;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    storagePermission = ContextCompat.checkSelfPermission(CreacionPerfil.this, Manifest.permission.READ_MEDIA_IMAGES)
                            == PackageManager.PERMISSION_GRANTED;
                } else {
                    storagePermission = ContextCompat.checkSelfPermission(CreacionPerfil.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED;
                }

                if (!cameraPermission || !storagePermission) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ActivityCompat.requestPermissions(CreacionPerfil.this,
                                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES},
                                PERMISSION_REQUEST_CODE);
                    } else {
                        ActivityCompat.requestPermissions(CreacionPerfil.this,
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
                Intent intent = new Intent(CreacionPerfil.this, ElegirUbicacion.class);
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
                nombre = editNombre.getText().toString();
                anosPermiso = editAnoCon.getText().toString();
                descripcion = editDescrip.getText().toString();
                cp = editCP.getText().toString();
                fechaNaci = editFechaNacimiento.getText().toString();

                // Procedemos a comprobar si están todos los campos rellenos
                if(username.isEmpty() || nombre.isEmpty() || anosPermiso.isEmpty() || descripcion.isEmpty()
                || cp.isEmpty() || fechaNaci.isEmpty()){ // En caso de faltar alguno
                    // Lanzamos un Toast indicando que tiene que completar todos los campos
                    showToast("Usted ha de completar todos los campos, por favor!!");
                }else{ // En caso de estar todos rellenos
                    // Guardamos en una variable la edad calculada con el método pasandole la fecha de nacimiento del usuario
                    edad = calcularEdad(fechaNaci);

                    // Llamamos al método para insertar el perfil
                    insertarPerfil();
                }
            }
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

    /**
     * @return
     * @param fechaNacimiento
     * Método en el que le pasamos un string con la fecha de nacimiento
     * y obtenemos la edad que tiene el usuario basandonos en la
     * fecha actual
     */
    private int calcularEdad(String fechaNacimiento) {
        // Utilizamos un try catch para poder captar y tratar todas las posibles excepciones
        try {
            // Creo un objeto date y le doy formato a la fecha de nacimiento que paso por argumentos
            Date fecha = dateFormat.parse(fechaNacimiento);
            // Creo un objeto calendario para obtener la fecha
            Calendar fechaNac = Calendar.getInstance();
            // Establezco en el calendario creado la fecha de nacimiento del usuario
            fechaNac.setTime(fecha);

            // Creo otro calendar para obtener la fecha actual y así obtener la edad
            Calendar hoy = Calendar.getInstance();

            // Obtenemos en una variable la resta del año actual y el año de nuestro nacimiento
            int edad = hoy.get(Calendar.YEAR) - fechaNac.get(Calendar.YEAR);

            // En caso de que el día de hoy sea menor que el día de la fecha de nacimiento
            if (hoy.get(Calendar.DAY_OF_YEAR) < fechaNac.get(Calendar.DAY_OF_YEAR)) {
                // Restamos uno a la edad
                edad--;
            }

            // Retornamos la variable entera de edad
            return edad;
        } catch (ParseException e) { // En caso de que surja alguna excepción
            // Pintaremos por consola la excepción
            e.printStackTrace();
            // Retornaremos 0
            return 0;
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
        new AlertDialog.Builder(CreacionPerfil.this)
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
     * Método en el que obtenemos todos los valores de posibilidades
     * del usuario, su foto de perfil, cifrados datos, convertimos la imagen
     * a base64, comprobamos que el username elegido no esté ya en uso,
     * y una vez hecho todo eso y comprobado, insertamos el perfil en la bd
     */
    public void insertarPerfil() {
        // Obtenemos la instancia e la base de datos de firestore
        db = FirebaseFirestore.getInstance();
        // Obtenemos el usuario que está autenticado en ese momento
        FirebaseUser user = auth.getCurrentUser();

        // Procedemos a comprobar si el usuario no es nulo
        if (user == null) { // En caso de ser nulo
            // Indicamos al usuario que ha de iniciar sesión de nuevo
            showToast("No se pudo obtener la información del usuario. Inicia sesión nuevamente.");
            // Retornamos para no proseguir con el método
            return;
        }

        // Obtenemos en una variable el uid del usuario
        String uid = user.getUid();
        // Obtenemos en una variable el email del usuario
        String email = user.getEmail();
        // Obtenemos en una variable el username del usuario
        String campoUsername = editUsername.getText().toString();

        // Obtenemos en una variable el nombre completo del usuario cifrado
        String nombreCifrado = CifradoDeDatos.cifrar(editNombre.getText().toString());
        // Obtenemos en una variable la fecha de nacimeinto del usuario cifrada
        String fechaNaciCifrada = CifradoDeDatos.cifrar(editFechaNacimiento.getText().toString());
        // Obtenemos en una variable el email cifrado del usuario
        String emailCifrado = CifradoDeDatos.cifrar(email);

        // Obtenemos en una variable el recurso que se ha puesto en la imageview de la imagen de perfil
        BitmapDrawable drawable = (BitmapDrawable) imagenPerfil.getDrawable();
        // Obtenemos en una variable el bitmao de la imagen elegida
        Bitmap fotoBitmap = drawable != null ? drawable.getBitmap() : null;

        // Creo una variable de tipo texto donde almacenar la foto en base64
        String fotoPerfilBase64 = null;
        // Procedemos a comprobar si el bitmap es nulo
        if (fotoBitmap != null) { // En caso de no ser nulo
            // Creamos un array de bytes para comprimir la imagen
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // Procedemos a comprimmir la imagen a un 85% y además, le establecemos como un JPEG
            fotoBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);

            // Creamos una cadena de bytes en donde almacenamos la imagen
            byte[] fotoBytes = baos.toByteArray();
            // Y guardamos en la variable antes creado la codificación en base64
            fotoPerfilBase64 = Base64.encodeToString(fotoBytes, Base64.DEFAULT);
        }

        // Creamos un objeto del modelo de perfil de usuario para tener todos los valores rellenos y a mano
        Perfil perfil = new Perfil(
          uid,
          campoUsername,
          emailCifrado,
          nombreCifrado,
          ubicacionSeleccionada,
          edad,
          fechaNaciCifrada,
          cp.isEmpty() ? 0 : Integer.parseInt(cp),
          new ArrayList<>(),
          fotoBitmap,
          descripcion,
          anosPermiso.isEmpty() ? 0 : Integer.parseInt(anosPermiso),
          new ArrayList<>(),
          new ArrayList<>()
        );

        // Creamos un nuevo Map para darle los nombres y los valores a los documentos dentro de la colección
        Map<String, Object> perfilMap = new HashMap<>();
        // Guardamos el uid del usuario
        perfilMap.put("uid", perfil.getUid());
        // Guardamos el username del usuario
        perfilMap.put("username", perfil.getUsername());
        // Guardamos el nombre completo del usuario cifrado
        perfilMap.put("nombre_completo", perfil.getNombre_completo());
        // Guardamos la ubicación del usuario cifrada
        perfilMap.put("ubicacion", perfil.getUbicacion());
        // Guardamos la edad del usuario
        perfilMap.put("edad", perfil.getEdad());
        // Guardamos la fecha de nacimiento del usuario
        perfilMap.put("fechaNaci", perfil.getFechaNaci());
        // Guardamos el código postal del usuario
        perfilMap.put("cp", perfil.getCp());
        // Guardamos el correo del usuario cifrado
        perfilMap.put("email", perfil.getEmail());
        // Guardamos los años que lleva conduciendo el usuario
        perfilMap.put("aniosConduciendo", perfil.getAniosConduciendo());
        // Guardamos la lista de vehículos vacía del usuairo
        perfilMap.put("listaVehiculos", perfil.getListaVehiculos());
        // Guardamos la foto de perfil en base 64
        perfilMap.put("fotoPerfil", fotoPerfilBase64);
        // Guardamos la descripción del usuario
        perfilMap.put("descripcion", perfil.getDescripcion());
        // Guardamos la lista de likes del usuario
        perfilMap.put("likes", perfil.getUsuariosLikeYou());

        // Obtenemos en una variable el username a querer ingresar
        String usernameNuevo = perfilMap.get("username").toString();

        // Procedemos a comprobar si ya existe un perfil con ese username
        db.collection("perfiles")
                .whereEqualTo("username", usernameNuevo)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> { // En caso de que todo vaya bien
                    if (queryDocumentSnapshots.isEmpty()) { // En caso de que la consulta esté vacía
                        // No existe ningún usuario con ese username, entonces podemos crear el perfil
                        db.collection("perfiles").document(uid).set(perfilMap)
                                .addOnSuccessListener(aVoid -> { // En caso de que todo vaya bien
                                    // Lanzamos un Toast indicando que se creo el perfil correctamente
                                    showToast("Perfil creado exitosamente");
                                    // Creamos un nuevo intent indicando a la nueva pantalla que vamos a saltar
                                    Intent i = new Intent(CreacionPerfil.this, AdministrarVehiculos.class);
                                    // Pasamos como parametro un uid
                                    i.putExtra("uid", uid);
                                    // Iniciamos la nueva actividad
                                    startActivity(i);
                                    // Establecemos una animcación para que se vea más visual
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                    finish(); // Cerramos la actividad actual
                                })
                                .addOnFailureListener(e -> { // En caso de que algo falle
                                    // Lanzamos un Toast indicando al usuario que ocurrio un error al crear el perfil
                                    showToast("Error al crear perfil: " + e.getMessage());
                                });
                    } else { // En caso de que si que exista
                        // Lanzaremos un Toast indicando al usuario que ese username ya está en uso
                        showToast("El nombre de usuario ya está en uso. Por favor elige otro.");
                    }
                })
                .addOnFailureListener(e -> { // En caso de que surja algún error
                    // Lanzaremos un toast indicando que ocurrido un error al verificar el username
                    showToast("Error al verificar username: " + e.getMessage());
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
package com.clase.motorton.ui.perfil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.clase.motorton.R;
import com.clase.motorton.adaptadores.VehiculosAdapter;
import com.clase.motorton.cifrado.CifradoDeDatos;
import com.clase.motorton.modelos.Vehiculo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InfoOtroPerfilFragment extends Fragment {
    // Variable para manejar los Toast de está actividad
    private Toast mensajeToast = null;
    private TextView textUsername = null;
    private TextView textEdad = null;
    private TextView textNombreCompleto = null;
    private TextView textAniosConduciendo = null;
    private TextView textUbicacion = null;
    private ImageView imagenPerfil = null;
    private Button btnLike = null;
    private RecyclerView recycleVehiculos = null;

    private ProgressBar progressBarOtroUsuario = null;
    private View progressOverlay = null;

    // Variable para manejar la lista de vehículos del usuario
    private ArrayList<Vehiculo> listaVehiculos = new ArrayList<>();

    // Variable para manejar el aaptador para los vehículos
    private VehiculosAdapter vehiculosAdapter = null;

    // Variable para manejar el autenticado de firebase
    private FirebaseAuth mAuth = null;
    // Variable para manejar la base de datos de firebase
    private FirebaseFirestore db = null;
    private String uidUser = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_info_otro_perfil, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        textUsername = (TextView) root.findViewById(R.id.textViewUsernameOtroUsuario);
        textUbicacion = (TextView) root.findViewById(R.id.textViewUbicacionOtroUsuario);
        textAniosConduciendo = (TextView) root.findViewById(R.id.textViewAnosConduciendoOtroUsuario);
        textEdad = (TextView) root.findViewById(R.id.textViewEdadOtroUsuario);
        textNombreCompleto = (TextView) root.findViewById(R.id.textViewNombreCompletoOtroUsuario);
        imagenPerfil = (ImageView) root.findViewById(R.id.imageViewPerfilOtroUsuario);
        btnLike = (Button) root.findViewById(R.id.buttonLike);
        recycleVehiculos = (RecyclerView) root.findViewById(R.id.recyclerViewVehiculosOtroUsuario);
        progressBarOtroUsuario = root.findViewById(R.id.progressBarOtroUsuario);
        progressOverlay = root.findViewById(R.id.progressOverlay);

        recycleVehiculos.setLayoutManager(new LinearLayoutManager(getContext()));

        uidUser = mAuth.getUid();

        String uidPasado = null;
        if (getArguments() != null && getArguments().containsKey("perfilId")) {
            uidPasado = getArguments().getString("perfilId");
            cargarDatosPerfil(uidPasado);
        }

        String finalUidPasado = uidPasado;

        comprobarLike(finalUidPasado, uidUser);

        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                darLike(finalUidPasado, uidUser);
            }
        });

        return root;
    }

    private void comprobarLike(String uidPerfil, String miUid) {
        db.collection("perfiles").document(uidPerfil)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> likes = (List<String>) documentSnapshot.get("likes");

                        if (likes != null && likes.contains(miUid)) {
                            btnLike.setText("❤\uFE0F Quitar Like");
                        } else {
                            btnLike.setText("❤\uFE0F Dar Like");
                        }
                    } else {
                        showToast("Perfil no encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Error al obtener el perfil");
                })
                .addOnCompleteListener(task -> {
                });
    }


    private void darLike(String uidPerfil, String miUid) {
        db.collection("perfiles").document(uidPerfil)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> likes = (List<String>) documentSnapshot.get("likes");

                        if (likes == null) {
                            likes = new ArrayList<>();
                        }

                        boolean yaDioLike = likes.contains(miUid);

                        if (yaDioLike) {
                            likes.remove(miUid);
                        } else {
                            likes.add(miUid);
                        }

                        db.collection("perfiles").document(uidPerfil)
                                .update("likes", likes)
                                .addOnSuccessListener(aVoid -> {
                                    if (yaDioLike) {
                                        btnLike.setText("❤\uFE0F Dar Like");
                                        showToast("Has quitado el like");
                                    } else {
                                        btnLike.setText("❤\uFE0F Quitar Like");
                                        showToast("Has dado like");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    showToast("Error al actualizar el like");
                                });
                    } else {
                        showToast("Perfil no encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Error al obtener el perfil");
                })
                .addOnCompleteListener(task -> {
                });
    }

    private void mostrarLoader() {
        if (progressOverlay != null) {
            progressOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void ocultarLoader() {
        if (progressOverlay != null) {
            progressOverlay.setVisibility(View.GONE);
        }
    }

    public void cargarDatosPerfil(String uidPasado){
        // Establezco visible el progressbar
        mostrarLoader();

        // Obtengo el uid del usuario autenticado
        String uid = uidPasado;
        // Procedo a comprobar que el uid no sea nulo
        if (uid == null) { // En caso de que sea nulo
            // Lanzamos un toast indicando que el usuario no está autenticado
            showToast("Usuario no autenticado");
            // Ponemos como invisible el progressbar
            ocultarLoader();
            // Retornamos para no seguir ejecutando el método
            return;
        }

        // Ontenemos la refrencia a la colección de perfiles basandonos en el uid del usuario para acceder al documento
        DocumentReference docRef = db.collection("perfiles").document(uid);

        // Procedemos a comprobar si ha salido bien
        docRef.get().addOnSuccessListener(documentSnapshot -> { // En caso de que vaya bien
            // Comprobamos si el documento existe o no
            if (documentSnapshot.exists()) { // En caso de que no exista
                // Obtengo todos los valores del perfil del usuario
                String username = documentSnapshot.getString("username");
                Long edadLong = documentSnapshot.getLong("edad");
                String edad = edadLong != null ? edadLong.toString() : "N/A";
                String fotoPerfilBase64 = documentSnapshot.getString("fotoPerfil");
                String email = documentSnapshot.getString("email");
                String nombreCompleto = documentSnapshot.getString("nombre_completo");

                // Obtenemos en un mapa todos los valores de la ubicación guardados
                Map<String, Object> ubicacionMap = (Map<String, Object>) documentSnapshot.get("ubicacion");
                // Inicializamos la dirección como no disponible
                String direccion = "Ubicación no disponible";

                // Procedemos a comprobar que la ubicación no sea nula
                if (ubicacionMap != null) { // En caso de que no sea nula
                    // Establecemos en el string solo la dirección
                    direccion = (String) ubicacionMap.get("direccion");
                }

                // Comprobamos que estén todos los datos
                if (username != null && edad != null && direccion != null && email != null && nombreCompleto != null) { // En caso afirmativo
                    // Establecemos en los textview todos los valores del usuario descifrados y todo
                    textUsername.setText(username);
                    textEdad.setText("Edad: " + edad);
                    textUbicacion.setText("Ubicación: " + CifradoDeDatos.descifrar(direccion));
                    textNombreCompleto.setText(CifradoDeDatos.descifrar(nombreCompleto));

                    // Comprobamos que la foto de perfil no sea nula o esté vacía
                    if (fotoPerfilBase64 != null && !fotoPerfilBase64.isEmpty()) { // En caso de no estar vacía
                        // Creo un bitmap de lo obtenido en la decodificación de base64 a bitmap
                        Bitmap bitmap = convertirBase64AImagen(fotoPerfilBase64);
                        if (bitmap != null) { // En caso de que el bitmap esté vacío
                            imagenPerfil.setImageBitmap(bitmap);
                        } else { // En caso de estar vacía
                            imagenPerfil.setImageResource(R.drawable.icono);
                        }
                    } else { // En caso de que la foto sea nula o esté vacía
                        // Establecemos en el imageview el recurso de imagen por defecto del icono de la app
                        imagenPerfil.setImageResource(R.drawable.icono);
                    }

                    // Llamamos al método para cargar los vehículos
                    cargarVehiculosOtroPerfil(uid);
                } else { // En caso de que falle algún dato
                    // Lanzamos un toast indicando que los datos del perfil son incompletos
                    showToast("Datos incompletos en el perfil");
                }
            } else { // En caso de que no exista
                // Lanzamos un toast indicando que el perfil no se encontró
                showToast("Perfil no encontrado");
            }
            // Ponemos invisible la progressbar
            ocultarLoader();
        }).addOnFailureListener(e -> { // En caso de que falle algo
            // Ponemos invisible la progressbar
            ocultarLoader();
            // Lanzamos un toast indicando al usuario que ha ocurrido un error al cagar el perfil
            showToast("Error al cargar perfil: " + e.getMessage());
        });
    }

    public void cargarVehiculosOtroPerfil(String uidPasado){
        // Muesto loader y ocultar RecyclerView
        mostrarLoader();
        recycleVehiculos.setVisibility(View.GONE);

        // Limpio la lista de vehículos
        listaVehiculos.clear();

        // Accedo a la colección de vehículos
        db.collection("vehiculos")
                .whereEqualTo("uidDueno", uidPasado) // Filtramos por el uid del sueño
                .get()
                .addOnCompleteListener(task -> {
                    // Ponemos inivisible el progressbar
                    ocultarLoader();

                    // Comprobamos que la tarea se ejecuto correctamente
                    if (task.isSuccessful()) { // En caso afirmativo
                        if (!task.getResult().isEmpty()) { // En caso de habe resultado
                            // Utilizamos un for para obtener todos los objetos, convertirlos en Vehículo y agregarlos a la lista
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Vehiculo vehiculo = document.toObject(Vehiculo.class);
                                listaVehiculos.add(vehiculo);
                            }
                        } else { // En caso de no haber resultados
                            // Lanzamos un toast indicando que no se encontraron los vehículos del usuario
                            showToast("No se encontraron vehículos para usted.");
                        }

                        // Genero el adaptador para los vehículos
                        vehiculosAdapter = new VehiculosAdapter(listaVehiculos, getContext());
                        // Establezco al recycler el adaptador
                        recycleVehiculos.setAdapter(vehiculosAdapter);
                        // Establezco la posición y estilo del mismp
                        recycleVehiculos.setLayoutManager(new LinearLayoutManager(getContext()));
                        // Ponemos visible el recycler
                        recycleVehiculos.setVisibility(View.VISIBLE);

                    } else { // En caso negativo
                        // Lanzamos un toast indicando que ocurrió un error al cargar los vehículos
                        showToast("Error al cargar vehículos: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * @return
     * @param base64String
     * Método en donde pasamos una cadena y procedemos a convertir
     * el texto en base 64 a un bitmap legible para establecerle
     * en un imageview
     */
    private Bitmap convertirBase64AImagen(String base64String) {
        // Utilizamos un try catch para capturar y tratar las posibles excepciones
        try {
            // Remplazo algunos parametros para evitar errores al descifrar la imagen
            base64String = base64String.replace("\n", "").replace("\r", "");

            // Genero un conjutno de bytes en donde decodifico la cadena
            byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);

            // Retornamos la descodificación de texto a bitmap
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } catch (IllegalArgumentException e) { // En caso de que surja alguna excepción
            // Imprimimos por consola la excepción
            e.printStackTrace();
            // Lanzamos un Toast indicando que ocurrió un error
            showToast("Error al convertir imagen Base64");
            // Retornamos nulo
            return null;
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
            mensajeToast = Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT);
            // Mostramos dicho Toast
            mensajeToast.show();
        }
    }
}
package com.clase.motorton.ui.perfil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.clase.motorton.R;
import com.clase.motorton.adaptadores.VehiculosAdapter;
import com.clase.motorton.cifrado.CifradoDeDatos;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.clase.motorton.modelos.Vehiculo;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;

import javax.crypto.SecretKey;

public class MyPerfilFragment extends Fragment {
    // Variable para manejar el progressbar de la actividad
    private ProgressBar progressBar = null;
    // Variable para manejar las imageview de la foto de perfil
    private ImageView imageViewPerfil = null;
    // Variable para manejar todos los editText de dicho fragmento
    private TextView textViewUsername = null, textViewEdad = null, textViewUbicacion = null, textViewNombreCompleto = null;
    // Variable para manejar el botón de editarPerfil
    private Button buttonEditarPerfil = null;
    // Variable para manejar el imageview para ir a agregar vehículos
    private ImageView btnAgregarVeiculo = null;
    // Variable para manejar el iamgeview para ir a ajustes
    private ImageView btnAjustes = null;
    // Variable para manejar el imageview para ir a ver lo vehículos del usuario
    private ImageView btnCoches = null;
    // Variable para manejar el imageview para ir a ver los eventos creados
    private ImageView btnEventosCreados = null;
    // Variable para manejar el imageview para ir a ver los eventos en los que participa el usuario
    private ImageView btnEventosParticipas = null;
    // Variable para manejar el recyclerview de vehículos del usuario
    private RecyclerView recyclerViewVehiculos = null;
    // Variable para manejar el aaptador para los vehículos
    private VehiculosAdapter vehiculosAdapter = null;
    // Variable para manejar la lista de vehículos del usuario
    private ArrayList<Vehiculo> listaVehiculos = new ArrayList<>();

    // Variable para controlar el cifrado de datos
    private CifradoDeDatos cifrar = null;
    // Variable para controlar la clave secreta del cifrado de datos
    private SecretKey claveSecreta = null;
    // Variable para manejar los Toast de está actividad
    private Toast mensajeToast = null;

    // Variable para manejar el autenticado de firebase
    private FirebaseAuth mAuth = null;
    // Variable para manejar la base de datos de firebase
    private FirebaseFirestore db = null;

    public MyPerfilFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflamos la vista
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        // Inicializamos Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Obtenemos referencias a los elementos de la interfaz
        progressBar = root.findViewById(R.id.progressBar);
        imageViewPerfil = root.findViewById(R.id.imageViewPerfil);
        textViewUsername = root.findViewById(R.id.textViewUsername);
        textViewEdad = root.findViewById(R.id.textViewEdad);
        textViewUbicacion = root.findViewById(R.id.textViewUbicacion);
        textViewNombreCompleto = root.findViewById(R.id.textViewNombreCompleto);
        buttonEditarPerfil = root.findViewById(R.id.buttonEditarPerfil);
        recyclerViewVehiculos = root.findViewById(R.id.recyclerViewVehiculos);
        btnAgregarVeiculo = root.findViewById(R.id.buttonAgregarVehiculo);
        btnAjustes = root.findViewById(R.id.buttonAjustes);
        btnCoches = root.findViewById(R.id.buttonMisCoches);
        btnEventosCreados = root.findViewById(R.id.buttonEventosCreados);
        btnEventosParticipas = root.findViewById(R.id.buttonEventosActivos);

        // Inicializo el cifrador de datos
        cifrar = new CifradoDeDatos();
        // Utilizo un try catch para capturar y tratar todas las excepciones que surjan
        try {
            // Generamos la clave en caso de que no exista
            CifradoDeDatos.generarClaveSiNoExiste();
        } catch (Exception e) { // En caso de que surja alguna excepción
            // Imprimimos por consola la misma
            e.printStackTrace();
        }

        // Establezco la acción que sucede cuando clicamos el botón de ir a ajustes
        btnAjustes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireContext(), Ajustes.class);
                startActivity(intent);
            }
        });

        // Establezco la acción que sucede cuando clicamos el botón de ir a agregar vehículos
        btnAgregarVeiculo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireContext(), AdministrarVehiculos.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        // Establezco la acción que sucede cuando clicamos el botón de ir a editar el perfil
        buttonEditarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // Establezco la acción que sucede cuando clicamos el botón de ver la lista de coches
        btnCoches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // Establezco la acción que sucede cuando clicamos el botón de ver la lista de eventos en los que participamos
        btnEventosParticipas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // Establezco la acción que sucede cuando clicamos el botón de ver la lista de los eventos creados
        btnEventosCreados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // Llamo al método para cargar los datos del perfil
        cargarPerfil();

        // Retornamos la vista
        return root;
    }

    /**
     * Método en el que procedo a cargar todos los
     * datos del usuario autenticado gracias a su uid
     * además los datos cifrados que son visibles, los descifro
     * para que el usuario pueda verlos
     */
    private void cargarPerfil(){
        // Establezco visible el progressbar
        progressBar.setVisibility(View.VISIBLE);

        // Obtengo el uid del usuario autenticado
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        // Procedo a comprobar que el uid no sea nulo
        if (uid == null) { // En caso de que sea nulo
            // Lanzamos un toast indicando que el usuario no está autenticado
            showToast("Usuario no autenticado");
            // Ponemos como invisible el progressbar
            progressBar.setVisibility(View.GONE);
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
                    textViewUsername.setText(username);
                    textViewEdad.setText("Edad: " + edad);
                    textViewUbicacion.setText("Ubicación: " + CifradoDeDatos.descifrar(direccion));
                    textViewNombreCompleto.setText("Nombre Completo: "+CifradoDeDatos.descifrar(nombreCompleto));

                    // Comprobamos que la foto de perfil no sea nula o esté vacía
                    if (fotoPerfilBase64 != null && !fotoPerfilBase64.isEmpty()) { // En caso de no estar vacía
                        // Creo un bitmap de lo obtenido en la decodificación de base64 a bitmap
                        Bitmap bitmap = convertirBase64AImagen(fotoPerfilBase64);
                        if (bitmap != null) { // En caso de que el bitmap esté vacío
                            imageViewPerfil.setImageBitmap(bitmap);
                        } else { // En caso de estar vacía
                            imageViewPerfil.setImageResource(R.drawable.icono);
                        }
                    } else { // En caso de que la foto sea nula o esté vacía
                        // Establecemos en el imageview el recurso de imagen por defecto del icono de la app
                        imageViewPerfil.setImageResource(R.drawable.icono);
                    }

                    // Llamamos al método para cargar los vehículos
                    cargarVehiculos(uid);
                } else { // En caso de que falle algún dato
                    // Lanzamos un toast indicando que los datos del perfil son incompletos
                    showToast("Datos incompletos en el perfil");
                }
            } else { // En caso de que no exista
                // Lanzamos un toast indicando que el perfil no se encontró
                showToast("Perfil no encontrado");
            }
            // Ponemos invisible la progressbar
            progressBar.setVisibility(View.GONE);
        }).addOnFailureListener(e -> { // En caso de que falle algo
            // Ponemos invisible la progressbar
            progressBar.setVisibility(View.GONE);
            // Lanzamos un toast indicando al usuario que ha ocurrido un error al cagar el perfil
            showToast("Error al cargar perfil: " + e.getMessage());
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
     * @param uid
     * Método en el que entramos en la colección de vehículos
     * y cargamos en una lista todos los vehículos del usuario
     * que tiene el uid que le pasamos como parametro
     */
    private void cargarVehiculos(String uid) {
        // Limpiamos la lista e vehículos
        listaVehiculos.clear();

        // Accedemos a la colección de vehículos
        db.collection("vehiculos")
                .whereEqualTo("uidDueno", uid) // Filtrando por el uid del usuario
                .get()
                .addOnCompleteListener(task -> { // En caso de que vaya bien
                    if (task.isSuccessful()) { // Comprobamos que haya salido bien la tarea
                        // Comprobamos que la tarea no sea nula
                        if (!task.getResult().isEmpty()) { // En caso de no ser nula
                            // Utilizo un foreach para recorrer la lista resultante de la consulta de la base de datos
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Obtengo en un objeto de tipo vehículo el vehículo de la colección
                                Vehiculo vehiculo = document.toObject(Vehiculo.class);
                                // Agrego el vehículo a la lista
                                listaVehiculos.add(vehiculo);
                            }
                        } else { // En caso de que sea nula
                            // Lanzamos un toast indicando que el usuario no tiene vehículos a obtener
                            showToast("No se encontraron vehículos para este usuario.");
                        }

                        // Procedo a comprobar que el adaptador no sea nulo
                        if (vehiculosAdapter != null) { // En caso de que no sea nulo
                            // Notificamos al adaptador cambios de datos
                            vehiculosAdapter.notifyDataSetChanged();
                        } else { // En caso de que sea nulo
                            // Inicializamos el nuevo adaptador de vehículos
                            vehiculosAdapter = new VehiculosAdapter(listaVehiculos, getContext());
                            // Establecemos al recyclerview el adaptador nuevo inicializado
                            recyclerViewVehiculos.setAdapter(vehiculosAdapter);
                        }

                    } else { // En caso de no poder
                        // Lanzamos un toast indicando al usuario que ocurrió algún error
                        showToast("Error al cargar vehículos: " + task.getException().getMessage());
                    }
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
            mensajeToast = Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT);
            // Mostramos dicho Toast
            mensajeToast.show();
        }
    }
}
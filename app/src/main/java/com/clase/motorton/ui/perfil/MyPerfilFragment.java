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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clase.motorton.R;
import com.clase.motorton.adaptadores.EventosAdapter;
import com.clase.motorton.adaptadores.VehiculosAdapter;
import com.clase.motorton.cifrado.CifradoDeDatos;
import com.clase.motorton.modelos.Evento;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.clase.motorton.modelos.Vehiculo;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
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
    private TextView textVievNLikes = null;
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
        textVievNLikes = root.findViewById(R.id.textViewLikes);

        // Inicializo el cifrador de datos
        cifrar = new CifradoDeDatos();

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
                Intent intent = new Intent(requireContext(), EditarPerfilActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        // Establezco la acción que sucede cuando clicamos el botón de ver la lista de coches
        btnCoches.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerViewVehiculos.setLayoutManager(new GridLayoutManager(getContext(), 2));
                cargarVehiculos(mAuth.getCurrentUser().getUid());
            }
        });

        // Establezco la acción que sucede cuando clicamos el botón de ver la lista de eventos en los que participamos
        btnEventosParticipas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cargarEventosParticipas(mAuth.getCurrentUser().getUid());
            }
        });

        // Establezco la acción que sucede cuando clicamos el botón de ver la lista de los eventos creados
        btnEventosCreados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerViewVehiculos.setLayoutManager(new LinearLayoutManager(getContext()));
                cargarEventosCreados(mAuth.getCurrentUser().getUid());
            }
        });

        recyclerViewVehiculos.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Llamo al método para cargar los datos del perfil
        cargarPerfil();

        // Retornamos la vista
        return root;
    }

    private void cargarEventosParticipas(String uid) {
        // Muesto loader y ocultar RecyclerView
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewVehiculos.setVisibility(View.GONE);

        // Accedemos a la colección de eventos
        db.collection("eventos")
                .whereArrayContains("participantes", uid) // Filtramos por la lista de participantes
                .get()
                .addOnCompleteListener(task -> { // En caso de que vaya bien
                    // Ponemos invisible el pogressbar
                    progressBar.setVisibility(View.GONE);

                    // Comprobamos que la tarea se ejecuto correctamente
                    if (task.isSuccessful()) { // En caso positivo
                        ArrayList<Evento> listaEventosParticipas = new ArrayList<>();

                        // Utilizamos un for para recorrer la respuesta e ir agregando los objetos a la lista o array
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Evento evento = doc.toObject(Evento.class);
                            listaEventosParticipas.add(evento);
                        }

                        // Limpiamos el adaptador anterior
                        recyclerViewVehiculos.setAdapter(null);
                        // Iniciamos y le damos el estilo al layout del recycler
                        recyclerViewVehiculos.setLayoutManager(new LinearLayoutManager(getContext()));
                        // Inicializamos el nuevo adaptador
                        EventosAdapter adapter = new EventosAdapter(getContext(), listaEventosParticipas, uid);
                        // Establecemos el apdatador al recycler
                        recyclerViewVehiculos.setAdapter(adapter);
                        // Ponemos visible el recycler
                        recyclerViewVehiculos.setVisibility(View.VISIBLE);

                        if (listaEventosParticipas.isEmpty()) { // En caso de que la lista esté vacía
                            // Lanzamos un toast indicandolo
                            showToast("No participas en ningún evento.");
                        }

                    } else { // En caso negativo
                        // Lanzamos un toast indicando que ocurrió un error
                        showToast("Error al cargar eventos donde participas");
                    }
                });
    }


    private void cargarEventosCreados(String uid ){
        // Muesto loader y ocultar RecyclerView
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewVehiculos.setVisibility(View.GONE);

        // Accedemos a la colección de eventos
        db.collection("eventos")
                .whereEqualTo("organizador", uid) // Filtramos por organizador de evento
                .get()
                .addOnCompleteListener(task -> { // En caso de que vaya bien
                    // Ponemos invisible el pogressbar
                    progressBar.setVisibility(View.GONE);

                    // Comprobamos que la tarea se ejecuto correctamente
                    if (task.isSuccessful()) { // En caso afirmativo
                        ArrayList<Evento> listaEventosCreados = new ArrayList<>();
                        // Utilizamos un for para recorrer la respuesta e ir agregando los objetos a la lista o array
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Evento evento = doc.toObject(Evento.class);
                            listaEventosCreados.add(evento);
                        }

                        // Limpiamos el adaptador anterior
                        recyclerViewVehiculos.setAdapter(null);
                        // Iniciamos y le damos el estilo al layout del recycler
                        recyclerViewVehiculos.setLayoutManager(new LinearLayoutManager(getContext()));
                        // Inicializamos el nuevo adaptador
                        EventosAdapter adapter = new EventosAdapter(getContext(), listaEventosCreados, uid);
                        // Establecemos el apdatador al recycler
                        recyclerViewVehiculos.setAdapter(adapter);
                        // Ponemos visible el recycler
                        recyclerViewVehiculos.setVisibility(View.VISIBLE);

                        if (listaEventosCreados.isEmpty()) { // En caso de que la lista esté vacía
                            // Lanzamos un toast indicandolo
                            showToast("No has creado eventos todavía.");
                        }

                    } else { // En caso negativo
                        // Lanzamos un toast indicando que ocurrió un error
                        showToast("Error al cargar eventos creados");
                    }
                });
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

                List<String> numeroLikes = (List<String>) documentSnapshot.get("likes");
                if(numeroLikes!=null){
                    int cantidadLikes = numeroLikes.size();
                    textVievNLikes.setText("Nº Likes: "+cantidadLikes);
                }else{
                    textVievNLikes.setText("Nº Likes: 0");
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
        // Muesto loader y ocultar RecyclerView
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewVehiculos.setVisibility(View.GONE);

        // Limpio la lista de vehículos
        listaVehiculos.clear();

        // Accedo a la colección de vehículos
        db.collection("vehiculos")
                .whereEqualTo("uidDueno", uid) // Filtramos por el uid del sueño
                .get()
                .addOnCompleteListener(task -> {
                    // Ponemos inivisible el progressbar
                    progressBar.setVisibility(View.GONE);

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
                        recyclerViewVehiculos.setAdapter(vehiculosAdapter);
                        // Establezco la posición y estilo del mismp
                        recyclerViewVehiculos.setLayoutManager(new GridLayoutManager(getContext(), 2));
                        // Ponemos visible el recycler
                        recyclerViewVehiculos.setVisibility(View.VISIBLE);

                    } else { // En caso negativo
                        // Lanzamos un toast indicando que ocurrió un error al cargar los vehículos
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
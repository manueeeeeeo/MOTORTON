package com.clase.motorton.ui.perfil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.clase.motorton.MainActivity;
import com.clase.motorton.R;
import com.clase.motorton.adaptadores.SpinnerAdaptarNormal;
import com.clase.motorton.adaptadores.SpinnerAdapter;
import com.clase.motorton.api.ApiVehiculos;
import com.clase.motorton.modelos.Vehiculo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class AdministrarVehiculos extends AppCompatActivity {
    // Variable para manejar el botón de crear
    private Button btnCrear = null;
    // Variable para manejar el botón de borrar
    private Button btnBorrar = null;
    // Variable para manejar el botón de continuar
    private Button btnContinuar = null;
    // Variable para manejar el editText de la matricula
    private EditText editMatricula = null;
    // Variable para manejar el editText de la descripción
    private EditText editDescrip = null;
    // Variable para manejar el editText de los años del vehículo
    private EditText editAnos = null;
    // Variable para manejar el spinner del tipo de vehículo
    private Spinner spinnerTipo = null;
    // Variable para manejar el spinner de la marca del vehículo
    private Spinner spinnerMarca = null;
    // Variable para manejar el spinner del modelo del vehículo
    private Spinner spinnerModelo = null;
    // Variable para manejar el switch de si es exportado o no el vehículo
    private Switch esExportado = null;

    // Variable para manejar el adaptador con iconos del spinner
    private SpinnerAdapter adaptador = null;
    // Variable para manejar el adaptador de las marcas
    private SpinnerAdaptarNormal adaptador2 = null;
    // Variable para manejar el adaptador de los modelos
    private SpinnerAdaptarNormal adaptadorModelos = null;

    // Variable para almacenar la matricula
    private String matricula = null;
    // Variable para almacenar la marca
    private String marca = null;
    // Variable para almacenar la descripción
    private String descrip = null;
    // Variable para almacenar el tipo de vehículo
    private String tipoVehiculo = null;
    // Variable para almacenar los años
    private int anos = 0;
    // Variable para almacenar si es exportado o no
    private boolean export = false;
    // Variable manejar todos los Toast de está actividad
    private Toast mensajeToast = null;
    // Variable para almacenar el modelo
    private String modeloVehi = "";

    // Variable para manejar la autentificación del usuario
    private FirebaseAuth auth = null;
    // Variable para manejar la base de datos de firestore
    private FirebaseFirestore db = null;
    // Variable para manejar el contexto de la activdad
    private Context context = null;

    // Variable que contiene los tipos de vehículos
    private String[] tipos = {"Motos", "Coches"};
    // Variable que contiene los iconos que van asociados a los tipos
    private int[] iconos = {R.drawable.ic_moto, R.drawable.ic_coche};

    // Variable para guardar y manejar todas las marcas
    private List<String> marcas = new ArrayList<>();
    // Variable para guardar y manejar todos los modelos
    private List<String> modelos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_administrar_vehiculos);

        // Inicialización de Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtengo el spinner de tipo de vehículo de la inetrfaz
        spinnerTipo = findViewById(R.id.spinnerTipo);
        // Genero un nuevo adaptador en donde paso los tipos y los iconos
        adaptador = new SpinnerAdapter(this, tipos, iconos);
        // Establezco al spinner de los tipos el adaptador inicializado
        spinnerTipo.setAdapter(adaptador);

        // Obtenemos todos los elementos graficos restantes de la interfaz
        spinnerMarca = findViewById(R.id.spinnerMarca);
        editMatricula = findViewById(R.id.editMatricula);
        editDescrip = findViewById(R.id.editDesVe);
        editAnos = findViewById(R.id.editAnosVe);
        esExportado = findViewById(R.id.switchExportado);
        btnCrear = findViewById(R.id.btnGuardarV);
        btnContinuar = findViewById(R.id.btnProseguir);
        btnBorrar = findViewById(R.id.btnBorrarV);
        spinnerModelo = findViewById(R.id.spinnerModelo);

        // Obtengo el contexto
        context = this;

        // Llamo al método para llamar a las marcas desde la API indicando primeramente el tipo de motos
        cargarMarcasDesdeAPI("motos");
        // Configuro el adapter para el spinner de marcas, que es uno normal
        adaptador2 = new SpinnerAdaptarNormal(this, marcas);
        // Le establezco el adaptador al spinner de marcas
        spinnerMarca.setAdapter(adaptador2);

        // Establecemos la configuración de acciones del spinner de tipo de vehículo
        spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                // Obtenemos en una variable el tipo elegido
                String tipoSeleccionado = adapterView.getItemAtPosition(position).toString();
                // Convertimos el tipo seleccionado para pasarselo a los métodos
                String tipoApi = tipoSeleccionado.equalsIgnoreCase("Motos") ? "motos" : "coches";

                // Llamo al método para cargar las marcas de ese tipo de vehículo
                cargarMarcasDesdeAPI(tipoApi);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {} // No le uso
        });

        // Establecemos la configuración de acciones del spinner de marcas
        spinnerMarca.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Obtenemos la marca seleccionada en el spinner
                String marcaSeleccionada = parent.getItemAtPosition(position).toString();
                // Obtenemos el tipo también
                String tipo = spinnerTipo.getSelectedItem().toString().equalsIgnoreCase("Motos") ? "motos" : "coches";
                // Llamamos al método para cargar los modelos de esa marca
                cargarModelosDesdeAPI(tipo, marcaSeleccionada);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {} // No le uso
        });



        // Establezco la acción que succederá cuando pulsemos al botón de crear vehículo
        btnCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Obtengo en las variables los valores de los editText y elementos otros
                matricula = editMatricula.getText().toString().trim();
                marca = spinnerMarca.getSelectedItem().toString();
                modeloVehi = spinnerModelo.getSelectedItem().toString();
                descrip = editDescrip.getText().toString().trim();
                tipoVehiculo = spinnerTipo.getSelectedItem().toString();
                anos = Integer.parseInt(editAnos.getText().toString().trim());
                export = esExportado.isChecked();

                // Procedemos a comprobar si todos los campos están rellenos
                if (validarCampos()) { // En caso afirmativo
                    // Llamamos al método para validar si la matricula es única y no está repetida
                    validarMatriculaUnica(matricula);
                }
            }
        });

        // Establezco la acción que succederá cuando pulsemos al botón de continuar
        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Creo un nuevo intent para indicar a la actividad que vamos a pasar
                Intent i = new Intent(AdministrarVehiculos.this, MainActivity.class);
                // Inicio una nueva actividad
                startActivity(i);
                // Finalizo la actividad actual
                finish();
            }
        });

        // Establezco la acción que succederá cuando pulsemos al botón de borrar campos
        btnBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Llamo al método para limpiar los campos
                limpiarCampos();
            }
        });
    }

    /**
     * Método para limpiar y resetar los valores de los campos
     */
    public void limpiarCampos(){
        editMatricula.setText("");
        editAnos.setText("");
        editDescrip.setText("");
    }

    /**
     * Método en el que pasado por todos
     * los filtros procedemos a insertar el vehículo
     * en la base de datos de Firestore
     */
    private void insertarVehiculo() {
        // Obtengo en una variable el uid del usuario autenticado
        String uid = auth.getCurrentUser().getUid();
        // Creo un objeto de tio vehículo e inicializo todas las variables
        Vehiculo vehiculo = new Vehiculo(uid, matricula, marca, modeloVehi, anos, export, descrip, tipoVehiculo);

        // Procedo a guardar el vehículo en la colección "vehiculos"
        db.collection("vehiculos").document(matricula).set(vehiculo)
                .addOnSuccessListener(aVoid -> { // En caso de que todo vaya bien
                    // Lanzamos un toast indicando al usuario que el vehículo fue agregado
                    showToast("Vehículo agregado exitosamente.");
                    // Llamo al método para actualizar la lista de los vehículos en el perfil
                    actualizarListaVehiculosEnPerfil(uid, matricula);
                })
                // En caso de que algo vaya mal mostramos un toast con el error
                .addOnFailureListener(e -> showToast("Error al agregar el vehículo: " + e.getMessage()));
    }

    /**
     * @param matricula
     * @param uid
     * Método en el que le pasamos como parametros el uid
     * del usuario y la matricula a ingresar, comprobamos que el usuario
     * exista, una vez localizado, obtenemos toda su lista de vehículos
     * y agregamos la nueva matricula y actualizamos el documento
     */
    private void actualizarListaVehiculosEnPerfil(String uid, String matricula) {
        // Procedemos a obtener la lista de vehículos del usuario con ese uid
        db.collection("perfiles").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> { // En caso de que todo vaya bien
                    // Comprobamos si existe un documento
                    if (documentSnapshot.exists()) { // En caso afirmativo
                        // Obtenemos en una lista la lista
                        List<String> listaVehiculos = (List<String>) documentSnapshot.get("listaVehiculos");
                        // Comprobamos que la lista no sea nula
                        if (listaVehiculos == null) { // En caso de ser nula
                            // Inicializamos la lista como un nuevo array list
                            listaVehiculos = new ArrayList<>();
                        }
                        // Agregamos a la lista la matricula del vehículo
                        listaVehiculos.add(matricula);

                        // Procedemos a actualizar la lista de vehículos del usuario con ese uid
                        db.collection("perfiles").document(uid)
                                .update("listaVehiculos", listaVehiculos)
                                .addOnSuccessListener(aVoid -> { // En caso de que todo vaya bien
                                    // Lanzamos un Toast idnicando que la lista vehículos ha sido actualziada
                                    showToast("Lista de vehículos actualizada.");
                                    // Llamamos al método para limpiar los campos
                                    limpiarCampos();
                                })
                                // En caso de que algo falle lanzamos un toast indicando que hubo un error al actualizar la lista
                                .addOnFailureListener(e -> showToast("Error al actualizar la lista de vehículos."));
                    }
                })
                // En caso de que algo falle lanzamos un toast indicando que hubo un error al obtener el perfil
                .addOnFailureListener(e -> showToast("Error al obtener el perfil del usuario."));
    }

    /**
     * @param tipo
     * @param marca
     * Método en el que le pasamos el tipo de vehículo y
     * la marca del mismo y basandonos en mi API obtenemos
     * todos los modelos que tiene esa marca en ese tipo de
     * vehículos y lo establecemos en el spinner de modelos
     */
    private void cargarModelosDesdeAPI(String tipo, String marca) {
        // Codificar espacios y caracteres especiales en la marca por si acaso
        String marcaParam = marca.replace(" ", "%20");
        // Establecemos la url de la llamada a la API indicando el tipo en minusculas
        String url = "https://vehiculos-api.onrender.com/api/modelos/" + tipo + "?marca=" + marcaParam;

        // Llamamos al método para obtener los modelos desde la api pasandole la url del ednpoint
        ApiVehiculos.obtener(url, new ApiVehiculos.Callback() {
            @Override
            public void onResponse(String response) {
                // Comprobamos que tengamos alguna respuesta
                if (response != null) { // En caso de tener respuesta
                    // Utilizamos un try catch para captar y tratar las posibles excepciones
                    try {
                        // Creamos un objeto de JSONArray para cargar la respuesta y procesarla
                        JSONArray jsonArray = new JSONArray(response);
                        // Limpiamos la lista de modelos
                        modelos.clear();

                        // Utilizamos un for para rellenar la lista de modelos
                        for (int i = 0; i < jsonArray.length(); i++) {
                            modelos.add(jsonArray.getString(i));
                        }

                        // Inicializamos el adaptador del spiner de modelos pasandole la lista
                        adaptadorModelos = new SpinnerAdaptarNormal(context, modelos);
                        // Le establecemos ese adaptador al spinner de modelos
                        spinnerModelo.setAdapter(adaptadorModelos);

                    } catch (Exception e) { // En caso de que surja alguna excepción
                        // Lanzamos un Toast indicando que hubo un error al procesar los datos de las marcas
                        showToast("Error al procesar los modelos.");
                    }
                } else { // En caso de no tener respuesta
                    // Lanzamos un Toast indicando que no se pudieron obtener las marcas desde la API
                    showToast("No se pudo obtener modelos desde la API.");
                }
            }
        });
    }

    /**
     * @param tipo
     * Método en donde le pasamos el tipo de vehículo
     * y gracias a mi API hecha en spring boot nos pone en el spinner
     * de las marcas todas las marcas existentes en la API
     */
    private void cargarMarcasDesdeAPI(String tipo) {
        // Establecemos la url de la llamada a la API indicando el tipo en minusculas
        String url = "https://vehiculos-api.onrender.com/api/marcas/" + tipo.toLowerCase();

        // Llamamos al método para obtener los modelos desde la api pasandole la url del ednpoint
        ApiVehiculos.obtener(url, new ApiVehiculos.Callback() {
            @Override
            public void onResponse(String response) {
                // Comprobamos que tengamos alguna respuesta
                if (response != null) { // En caso de tener respuesta
                    // Utilizamos un try catch para captar y tratar las posibles excepciones
                    try {
                        // Creamos un objeto de JSONArray para cargar la respuesta y procesarla
                        JSONArray jsonArray = new JSONArray(response);
                        // Limpiamos la lista de marcas
                        marcas.clear();

                        // Utilizamos un for para rellenar la lista de marcas
                        for (int i = 0; i < jsonArray.length(); i++) {
                            marcas.add(jsonArray.getString(i));
                        }

                        // Inicializamos el adaptador del spiner de marcas pasandole la lista
                        adaptador2 = new SpinnerAdaptarNormal(context, marcas);
                        // Le establecemos ese adaptador al spinner de marcas
                        spinnerMarca.setAdapter(adaptador2);

                    } catch (Exception e) { // En caso de que surja alguna excepción
                        // Lanzamos un Toast indicando que hubo un error al procesar los datos de las marcas
                        showToast("Error al procesar datos de marcas.");
                    }
                } else { // En caso de no tener respuesta
                    // Lanzamos un Toast indicando que no se pudieron obtener las marcas desde la API
                    showToast("No se pudo obtener marcas desde la API.");
                }
            }
        });
    }

    /**
     * @return
     * Método en el que lo que comprobamos es si están todos los campos
     * completos o no y retornamos un true o false para proseguir
     * con la insercción
     */
    private boolean validarCampos() {
        // Procedemos a comprobar si algún campo está vacío
        if (matricula.isEmpty() || marca.isEmpty() || descrip.isEmpty() || editAnos.getText().toString().isEmpty()) {
            // Lanzamos un Toast indicando que hay campos por completar
            showToast("Por favor, completa todos los campos.");
            // Retornamos false
            return false;
        }
        // Retornamos true si llegamos hasta aquí, porque significa que todo está completo
        return true;
    }

    /**
     * @param matricula
     * Método en donde verificamos si la matricula que ha elegido
     * el usuario ya está asignada a algún coche en la base de datos
     * de Firestore
     */
    private void validarMatriculaUnica(String matricula) {
        // Procedemos a comprobar las matriculas de la colección de vehículos
        db.collection("vehiculos")
                .whereEqualTo("matricula", matricula)
                .get()
                .addOnCompleteListener(task -> { // En caso de que todo vaya bien
                    // Procedemos a comprobar que la tarea se completo con exito
                    if (task.isSuccessful()) { // En caso de que haya ido bien
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) { // En caso de que si que exista
                            // Lanzamos un Toast indicando que la matrícula ya está registrada
                            showToast("La matrícula ya está registrada.");
                        } else { // En caso de que no exista
                            // Llamamos al método para verificar la cantidad e vehículos y agregar uno nuevo
                            verificarCantidadVehiculosYAgregar();
                        }
                    } else { // En caso de que algo haya salido mal
                        // Lanzamos un Toast indicando que hubo un error al verificar la matrícula
                        showToast("Error al verificar la matrícula.");
                    }
                });
    }

    /**
     * Método en donde procedemos a verificar la
     * cantidad de vehículos que ya tiene el usuario
     * en su perfil y enc aso de no superar el límite de los
     * 5 vehículos le agregamos*/
    private void verificarCantidadVehiculosYAgregar() {
        // Obtenemos en una variable el uid actual del usuario
        String uid = auth.getCurrentUser().getUid();

        // Procedemos a comprobar si el usuario ya tiene todos los vehículos o no
        db.collection("vehiculos")
                .whereEqualTo("usuarioId", uid)  // Filtramos por usuario
                .get()
                .addOnCompleteListener(task -> { // En caso de que todo vaya bien
                    // Procedemos a comprobar si la tarea se completo con existo
                    if (task.isSuccessful()) { // En caso afirmativo
                        // Obtenemos una query gracias al resultado de la tarea
                        QuerySnapshot querySnapshot = task.getResult();
                        // Comprobamos si el usuario ya tiene el limite de vehículos o no
                        if (querySnapshot != null && querySnapshot.size() >= 5) { // En caso de sobrepasar el limite
                            // Lanzamos un Toast indicando que ya tiene 5 vehículos
                            showToast("No puedes agregar más de 5 vehículos.");
                        } else { // En caso de no sobrepsar el limite
                            // Llamamos al método para insertar el vehículo
                            insertarVehiculo();
                        }
                    } else { // En caso negativo
                        // Lanzamos un Toast en donde avisamos al usuario que hubo un error a la hora de verificar la cantidad de vehículos
                        showToast("Error al verificar la cantidad de vehículos.");
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
            mensajeToast = Toast.makeText(this, mensaje, Toast.LENGTH_SHORT);
            // Mostramos dicho Toast
            mensajeToast.show();
        }
    }
}
package com.clase.motorton.ui.perfil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.clase.motorton.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Ajustes extends AppCompatActivity {
    // Variable para manejar el boton de reporter problema
    private Button btnReportarProblema = null;
    // Variable para manejar el botón de borrar cuenta
    private Button btnBorrarCuenta = null;
    // Variable para manejar el botón de cerrar sesión
    private Button btnCerrarSesion = null;

    // Variable para manejar todos los Toast de está actividad
    private Toast mensajeToast = null;

    // Variable para manejar el autenticador de Firebase en esta actividad
    private FirebaseAuth mAuth = null;
    // Variable para manejar la base de datos en esta actividad
    private FirebaseFirestore db = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ajustes);

        // Inicializo el autenticador de firebase
        mAuth = FirebaseAuth.getInstance();
        // Inicializo la base de datos de firebase
        db = FirebaseFirestore.getInstance();

        // Obtengo todos los pelementos visuales de la interfaz
        btnBorrarCuenta = findViewById(R.id.btnEliminarCuenta);
        btnReportarProblema = findViewById(R.id.btnReportarProblema);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        // Establezco la acción que realiza al tocar el botón de borrar cuenta
        btnBorrarCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Llamo al método para mostrar el dialogo de confirmación y el motivo de borrado
                mostrarDialogoConfirmacion();
            }
        });

        // Establezco la acción que realiza al tocar el botón de reportar el problema
        btnReportarProblema.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Llamo al método para redirigir al usuario a su Email
                redirigirAEmail();
            }
        });

        // Establezco la acción que realiza al tocar el botón de cerrar sesión
        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Llamo al método para cerrar la sesión
                cerrarSesion();
            }
        });
    }

    /**
     * Método en el que cuando le llamamos cerramos
     * sesión de la cuenta en el dispositivo, es decir,
     * cerramos tanto el autenticador como la cuenta de Google,
     * para así evitar posibles errores
     */
    private void cerrarSesion(){
        // Cierro la sesión en el usuario de Google por si acaso
        GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
        // Cierro la sesión del usuario en la autentificación
        mAuth.signOut();

        // Creo un nuevo intent en donde volvemos al inicio de sesión
        Intent intent = new Intent(this, InicioSesion.class);
        // Inicio la nueva actividad
        startActivity(intent);
        // Cierro la nueva actividad
        finish();
    }

    /**
     * Método en el que establecemos el destinatario,
     * asuto y cuerpo del email para reportar problemas
     * mediante correo, posteriormente establecemos una nueva
     * actividad para poder abrir el gmail, otulook o cualquier
     * aplicación para enivar correos
     */
    private void redirigirAEmail() {
        // Establezco el correo del destinatario
        String destinatario = "manu.engenios@gmail.com";
        // Establezco el asunto
        String asunto = "Problema con MotorTon";
        // Establezco el cuerpo inicial del mensaje
        String cuerpo = "Estimado equipo de MotorTon,\n\nHe encontrado el siguiente problema:\n\n[Escribe aquí el detalle del problema]";

        // Creo un nuevo intent en donde indico que iremos a una app para enviar
        Intent intent = new Intent(Intent.ACTION_SEND);
        // Establecemos el tipo de intent para que detecte solo algunas apps
        intent.setType("message/rfc822");

        // Establecemos en el intent el destinatario
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{destinatario});
        // Establecemos en el intent el asunto
        intent.putExtra(Intent.EXTRA_SUBJECT, asunto);
        // Establecemos en el intent el cuerpo
        intent.putExtra(Intent.EXTRA_TEXT, cuerpo);

        // Comprobamos que el usuario tenga clientes de correo posibles
        if (intent.resolveActivity(this.getPackageManager()) != null) { // En caso de tener apps
            // Inicio la nueva actividad
            startActivity(intent);
        } else { // En caso de no tener aplicaciones posibles
            // Lanzamos un toast en donde indicamos que no se puede abrir el cliente del correo
            showToast("No se pudo abrir el cliente de correo");
        }
    }

    /**
     * Método en el que mostramos el dialogo de confirmación
     * personalizado para obtener el motivo por el que borrar la cuenta
     * comprobamos que no esté vacío el campo y una vez que se este todo
     * correcto llamamos al método para borrar la cuenta y borrar los
     * vehículos asociados*/
    private void mostrarDialogoConfirmacion() {
        // Inflamos la vista
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_eliminar_cuenta, null);
        final BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);

        // Obtenemos el editText para el motivo
        EditText editTextMotivo = view.findViewById(R.id.editTextMotivo);
        // Obtenemos el botón para confirmar
        Button btnConfirmar = view.findViewById(R.id.btnConfirmarEliminar);
        // Obtenemos el botón para cancelar
        Button btnCancelar = view.findViewById(R.id.btnCancelarEliminar);

        // Establecemos la acción al pulsar el botón de confirmar
        btnConfirmar.setOnClickListener(v -> {
            // Guardamos en una variable el texto que contiene el editText
            String motivo = editTextMotivo.getText().toString().trim();
            // Comprobamos que no sea nulo
            if(motivo.isEmpty()){ // En caso de ser nulo
                // Lanzamos un toast indicando que tiene que rellenar el motivo
                showToast("Ha de rellenar el motivo!!!");
            }else{ // En caso de no ser nulo
                // Llamamos al método para eliminar la cuenta
                eliminarCuenta(motivo);
                // Ocultamos el dialogo
                dialog.dismiss();
            }
        });

        // En caso de tocar el botón de cancelar ocultamos el dialogo
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        // Mostramos el dialogo
        dialog.show();
    }


    /**
     * @param motivo
     * Método en el que le pasamos como motivo
     * la eliminación de la cuenta del usuairo, obtenemos
     * la fecha actual en un formato especifico y le ponemos
     * un nombre al documento, subimos además la fecha
     * el uid del usuario y el motivo
     */
    private void eliminarCuenta(String motivo) {
        // Obtenemos el uid del usuario autenticado
        String uid = mAuth.getCurrentUser().getUid();
        // Obtenemos en una variable la fecha actual con un formato especial
        String fechaHoraActual = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        // Generamos el uid del documento en donde juntamos el uid y la fecha y hora actuales
        String idDocumento = uid + "_" + fechaHoraActual;

        // Creamos un mapa para poder darle keys y valores
        Map<String, Object> datosBorrado = new HashMap<>();
        // Establecemos la fecha de borrado
        datosBorrado.put("fechaHoraBorrado", new Date());
        // Establecemos el uid del usuario
        datosBorrado.put("uidUsuario", uid);
        // Establecemos el motivo de borrar la cuenta
        datosBorrado.put("motivo", motivo);

        // Creamos en la colección de firebase el documento con todos su datos
        db.collection("CuentasBorradas").document(idDocumento).set(datosBorrado)
                // Una vez que haya salido bien, llamamos al método para eliminar el perfil y los vehículos
                .addOnSuccessListener(aVoid -> eliminarPerfilYVehiculos(uid));
    }

    /**
     * @param uid
     * Método en el que le pasamos como parametro el uid del usuario
     * obtenemos la colección de perfiles, obtenemos la lista de los vechículos
     * de ese usuario y los vamos eliminando uno por uno, por otro lado, procedemos
     * a eliminar el documento de la colección de perfiles
     */
    private void eliminarPerfilYVehiculos(String uid) {
        // Obtenemos la referencia de la colección de perfiles
        CollectionReference perfilesRef = db.collection("perfiles");

        perfilesRef.document(uid).get().addOnSuccessListener(documentSnapshot -> {
            // Comprobamos que exista el documento
            if (documentSnapshot.exists()) { // En caso de que exista
                // Obtenengo la lista de vehículos si existen
                List<String> listaVehiculos = (List<String>) documentSnapshot.get("listaVehiculos");
                // Compruebo que la lista de vehículos no esté vacía
                if (listaVehiculos != null && !listaVehiculos.isEmpty()) { // En caso negativo
                    // Llamo al método para eliminar los vehículos de la base de datos
                    eliminarVehiculos(listaVehiculos);
                }

                perfilesRef.document(uid).delete().addOnSuccessListener(aVoid -> {
                    // Lanzo un toast indicando al usuario que la cuenta se eliminará
                    showToast("Cuenta eliminada correctamente");
                    // Cierro la sesión en el usuario de Google por si acaso
                    GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
                    // Cierro la sesión del usuario en la autentificación
                    mAuth.signOut();
                    // Creamos el nuevo intent para ir al inicio de sesión
                    Intent intent = new Intent(this, InicioSesion.class);
                    // Limpiamos la pila de actividades
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    // Iniciamos la nueva actividad
                    startActivity(intent);

                    // Finalizo la actividad actual
                    finish();
                });
            }
        });
    }

    /**
     * @param listaVehiculos
     * Método en el que le paso como parametros
     * la lista de los vehículos que en realidad
     * son las matriculas y vamos eliminandolas
     * todas para así eliminar vechículos asociados al
     * usuario
     */
    private void eliminarVehiculos(List<String> listaVehiculos) {
        WriteBatch batch = db.batch();
        CollectionReference vehiculosRef = db.collection("vehiculos");

        for (String matricula : listaVehiculos) {
            batch.delete(vehiculosRef.document(matricula));
        }

        batch.commit().addOnSuccessListener(aVoid ->
                // Lanzo un toast en donde indico que los vehículos fueron borrados
                showToast("Vehículos eliminados")
        );
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
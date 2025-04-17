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
        GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
        mAuth.signOut();

        Intent intent = new Intent(this, InicioSesion.class);
        startActivity(intent);
        finish();
    }

    /**
     * Método en el que */
    private void redirigirAEmail() {
        String destinatario = "manu.engenios@gmail.com";
        String asunto = "Problema con MotorTon";
        String cuerpo = "Estimado equipo de MotorTon,\n\nHe encontrado el siguiente problema:\n\n[Escribe aquí el detalle del problema]";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");

        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{destinatario});
        intent.putExtra(Intent.EXTRA_SUBJECT, asunto);
        intent.putExtra(Intent.EXTRA_TEXT, cuerpo);

        if (intent.resolveActivity(this.getPackageManager()) != null) {
            startActivity(intent);
        } else {
            showToast("No se pudo abrir el cliente de correo");
        }
    }

    private void mostrarDialogoConfirmacion() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_eliminar_cuenta, null);
        final BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);

        EditText editTextMotivo = view.findViewById(R.id.editTextMotivo);
        Button btnConfirmar = view.findViewById(R.id.btnConfirmarEliminar);
        Button btnCancelar = view.findViewById(R.id.btnCancelarEliminar);

        btnConfirmar.setOnClickListener(v -> {
            String motivo = editTextMotivo.getText().toString().trim();
            if(motivo.isEmpty()){
                showToast("Ha de rellenar el motivo!!!");
            }else{
                eliminarCuenta(motivo);
                dialog.dismiss();
            }
        });
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

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
        String uid = mAuth.getCurrentUser().getUid();
        String fechaHoraActual = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String idDocumento = uid + "_" + fechaHoraActual;

        Map<String, Object> datosBorrado = new HashMap<>();
        datosBorrado.put("fechaHoraBorrado", new Date());
        datosBorrado.put("uidUsuario", uid);
        datosBorrado.put("motivo", motivo);

        db.collection("CuentasBorradas").document(idDocumento).set(datosBorrado)
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
        CollectionReference perfilesRef = db.collection("perfiles");

        perfilesRef.document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Obtener lista de vehículos si existen
                List<String> listaVehiculos = (List<String>) documentSnapshot.get("listaVehiculos");
                if (listaVehiculos != null && !listaVehiculos.isEmpty()) {
                    eliminarVehiculos(listaVehiculos);
                }

                perfilesRef.document(uid).delete().addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cuenta eliminada correctamente", Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                    Intent intent = new Intent(this, InicioSesion.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpia la pila de actividades
                    startActivity(intent);

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
                Toast.makeText(this, "Vehículos eliminados", Toast.LENGTH_SHORT).show()
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
package com.clase.motorton.ui.perfil;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.clase.motorton.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
    private Button btnReportarProblema = null;
    private Button btnBorrarCuenta = null;
    private Button btnCerrarSesion = null;

    private Toast mensajeToast = null;

    private FirebaseAuth mAuth = null;
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

        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cerrarSesion();
            }
        });
    }

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

    /**
     * Método en el que */
    private void mostrarDialogoConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar cuenta");

        final EditText inputMotivo = new EditText(this);
        inputMotivo.setHint("Escribe el motivo...");
        builder.setView(inputMotivo);

        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            String motivo = inputMotivo.getText().toString().trim();
            if (TextUtils.isEmpty(motivo)) {
                Toast.makeText(this, "Debes escribir un motivo", Toast.LENGTH_SHORT).show();
                return;
            }
            eliminarCuenta(motivo);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    /**
     * @param motivo
     * Método en el que */
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
package com.clase.motorton.pasajero;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.clase.motorton.ui.perfil.InicioSesion;
import com.google.firebase.firestore.FirebaseFirestore;

public class VersionBeta extends AppCompatActivity {
    private FirebaseFirestore db = null;
    private Button btnAcceder = null;
    private EditText editCodigo = null;
    private String codigo = null;
    private Toast mensajeToast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_version_beta);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editCodigo = (EditText) findViewById(R.id.code_input);
        btnAcceder = (Button) findViewById(R.id.submit_button);

        btnAcceder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validarCodigo();
            }
        });
    }

    private void validarCodigo(){
        String codigoIngresado = editCodigo.getText().toString().trim().toUpperCase();

        if (codigoIngresado.isEmpty()) {
            showToast("Ingresa un código válido");
            return;
        }

        db = FirebaseFirestore.getInstance();
        db.collection("invitationCodes").document(codigoIngresado).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        showToast("Código no encontrado");
                        return;
                    }

                    Boolean activo = doc.getBoolean("active");
                    Long logoutCount = doc.getLong("logoutCount");
                    String usadoPor = doc.getString("usedBy");

                    if (activo == null || !activo) {
                        showToast("Este código está desactivado");
                        return;
                    }

                    if (logoutCount != null && logoutCount >= 3) {
                        showToast("Este código ya fue utilizado demasiado");
                        return;
                    }

                    SharedPreferences prefs = getSharedPreferences("BetaPrefs", MODE_PRIVATE);
                    prefs.edit().putString("codigoBeta", codigoIngresado).apply();

                    showToast("Código válido. Acceso concedido.");

                    startActivity(new Intent(this, InicioSesion.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Error al validar código"));
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
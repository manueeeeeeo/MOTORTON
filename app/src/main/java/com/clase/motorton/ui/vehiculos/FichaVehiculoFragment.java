package com.clase.motorton.ui.vehiculos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.clase.motorton.R;
import com.clase.motorton.modelos.Evento;
import com.clase.motorton.modelos.Vehiculo;
import com.clase.motorton.servicios.InternetController;
import com.clase.motorton.ui.perfil.Ajustes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FichaVehiculoFragment extends Fragment {
    // Variable para manejar la base de datos de firestore
    private FirebaseFirestore db = null;

    // Variable para manejar la autentificación del usuario
    private FirebaseAuth auth = null;

    private ImageView imagenVehiculo = null;
    private TextView marcaYModelo = null;
    private TextView descripcion = null;
    private TextView tuboEscape = null;
    private TextView ruedas = null;
    private TextView aleron = null;
    private TextView anos = null;
    private TextView matricula = null;
    private TextView bodyKit = null;
    private TextView lucesLd = null;
    private TextView cv = null;
    private TextView maxV = null;
    private TextView exportado = null;
    private TextView choques = null;
    private ImageView vehFav = null;

    private Toast mensajeToast = null;
    private String fotoV = null;
    private String uidUser = null;
    private String matriculaVeh = null;

    private InternetController internetController = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_ficha_vehiculo, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        // Inicializo el controlador de internet
        internetController = new InternetController(getContext());

        if(!internetController.tieneConexion()){
            showToast("No tienes acceso a internet, conectese a una red!!");
        }

        uidUser = auth.getUid();

        marcaYModelo = (TextView) root.findViewById(R.id.txtModelo);
        descripcion = (TextView) root.findViewById(R.id.txtDescripcion);
        tuboEscape = (TextView) root.findViewById(R.id.txtTubo);
        ruedas = (TextView) root.findViewById(R.id.txtRuedas);
        aleron = (TextView) root.findViewById(R.id.txtAleron);
        anos = (TextView) root.findViewById(R.id.txtAnos);
        matricula = (TextView) root.findViewById(R.id.txtMatricula);
        bodyKit = (TextView) root.findViewById(R.id.txtBodyKit);
        lucesLd = (TextView) root.findViewById(R.id.txtLucesLed);
        choques = (TextView) root.findViewById(R.id.txtChoques);
        exportado = (TextView) root.findViewById(R.id.txtExportado);
        maxV = (TextView) root.findViewById(R.id.txtMaxv);
        cv = (TextView) root.findViewById(R.id.txtCv);
        imagenVehiculo = (ImageView) root.findViewById(R.id.fotoVehiculo);
        vehFav = (ImageView) root.findViewById(R.id.estrellaFavorito);

        if (getArguments() != null && getArguments().containsKey("matriculaVeh")) {
            String matri = getArguments().getString("matriculaVeh");
            matriculaVeh = matri;
            if (internetController.tieneConexion()) {
                cargarDatosVehiculo(matri);
            } else {
                showToast("No hay conexión, no se pueden cargar los datos del vehículo.");
            }
        }

        vehFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (internetController.tieneConexion()) {
                    AgregarVehiculoFav(uidUser, matriculaVeh);
                } else {
                    showToast("Sin conexión. No se puede actualizar favoritos.");
                }
            }
        });

        return root;
    }

    private void AgregarVehiculoFav(String uid, String matricula){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("perfiles").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> favoritos = (List<String>) documentSnapshot.get("listaFavVeh");

                        if (favoritos == null) {
                            favoritos = new ArrayList<>();
                        }

                        boolean yaEnFavoritos = favoritos.contains(matricula);

                        if (yaEnFavoritos) {
                            favoritos.remove(matricula);
                        } else {
                            favoritos.add(matricula);
                        }

                        db.collection("perfiles").document(uid)
                                .update("listaFavVeh", favoritos)
                                .addOnSuccessListener(aVoid -> {
                                    if (yaEnFavoritos) {
                                        vehFav.setImageResource(R.drawable.sin_estrella);
                                        showToast("Has quitado el vehículo de favoritos");
                                    } else {
                                        vehFav.setImageResource(R.drawable.con_estrella);
                                        showToast("Has agregado el vehículo a favoritos");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    showToast("Error al actualizar la lista de favoritos");
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

    public void cargarDatosVehiculo(String matriculaVe){
        if (!internetController.tieneConexion()) {
            showToast("Sin conexión. No se pueden cargar los datos del vehículo.");
            return;
        }

        db.collection("vehiculos")
                .whereEqualTo("matricula", matriculaVe)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Vehiculo veh = queryDocumentSnapshots.getDocuments().get(0).toObject(Vehiculo.class);

                        if (veh != null) {
                            marcaYModelo.setText(veh.getMarca()+" "+veh.getModelo());
                            descripcion.setText(veh.getDescripción());
                            tuboEscape.setText(veh.getTuboEscape());
                            matricula.setText(veh.getMatricula());
                            maxV.setText("Máxima Velocidad: "+veh.getMaxVelocidad()+" km/h");
                            cv.setText("Potencia: "+veh.getCv()+" CV");
                            choques.setText("Choques: "+veh.getChoques());
                            ruedas.setText(veh.getRuedas());
                            aleron.setText(veh.getAleron());
                            cv.setText("Potencia: "+veh.getCv()+" CV");
                            anos.setText(String.valueOf(veh.getAnos()));
                            fotoV = veh.getFoto();

                            if(veh.isBodyKit()){
                                bodyKit.setText("Lleva Body Kit");
                            }else{
                                bodyKit.setText("No lleva Body Kit");
                            }

                            if(veh.isLucesLed()){
                                lucesLd.setText("Llevas Luces Led Decoración");
                            }else{
                                lucesLd.setText("No llevas Luces Led Decoración");
                            }

                            if(veh.isExportado()){
                                exportado.setText("Exportado");
                            }else{
                                exportado.setText("No Exportado");
                            }

                            // Comprobamos que la foto de perfil no sea nula o esté vacía
                            if (fotoV != null && !fotoV.isEmpty()) { // En caso de no estar vacía
                                // Creo un bitmap de lo obtenido en la decodificación de base64 a bitmap
                                Bitmap bitmap = convertirBase64AImagen(fotoV);
                                if (bitmap != null) { // En caso de que el bitmap esté vacío
                                    imagenVehiculo.setImageBitmap(bitmap);
                                } else { // En caso de estar vacía
                                    imagenVehiculo.setImageResource(R.drawable.icono);
                                }
                            } else { // En caso de que la foto sea nula o esté vacía
                                // Establecemos en el imageview el recurso de imagen por defecto del icono de la app
                                imagenVehiculo.setImageResource(R.drawable.icono);
                            }

                            db.collection("perfiles").document(uidUser)
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            List<String> favoritos = (List<String>) documentSnapshot.get("listaFavVeh");

                                            if (favoritos != null && favoritos.contains(veh.getMatricula())) {
                                                vehFav.setImageResource(R.drawable.con_estrella);
                                            } else {
                                                vehFav.setImageResource(R.drawable.sin_estrella);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        // En caso de error al obtener el perfil
                                        vehFav.setImageResource(R.drawable.sin_estrella);
                                    });
                        }
                    } else {
                        marcaYModelo.setText("Vehiculo no encontrado.");
                    }
                })
                .addOnFailureListener(e -> {
                    marcaYModelo.setText("Error al cargar el vehículo.");
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
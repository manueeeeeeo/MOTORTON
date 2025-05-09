package com.clase.motorton.adaptadores;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.clase.motorton.modelos.Vehiculo;
import com.clase.motorton.ui.perfil.AdministrarVehiculos;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.clase.motorton.R;

import java.util.ArrayList;
import java.util.List;

public class VehiculosAdapter extends RecyclerView.Adapter<VehiculosAdapter.VehiculoViewHolder> {

    // Variable para manejar la lista de los vehículas
    private final ArrayList<Vehiculo> vehiculosList;
    // Variable para manejar el contexto
    private final Context context;
    private Toast mensajeToast = null;

    /**
     * @param context
     * @param vehiculosList
     * Constructor en donde inicializo el contexto
     * y la lista de elementos para mostrar la información
     * de los vehículos
     */
    public VehiculosAdapter(ArrayList<Vehiculo> vehiculosList, Context context) {
        this.vehiculosList = vehiculosList;
        this.context = context;
    }

    @NonNull
    @Override
    public VehiculoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vehiculo, parent, false);
        return new VehiculoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehiculoViewHolder holder, int position) {
        // Obtengo en un vehículo el objeto elegido
        Vehiculo vehiculo = vehiculosList.get(position);

        // En caso de que el vehículo sea nulo retornamos par no proseguir
        if (vehiculo == null) return;

        String uidUser = FirebaseAuth.getInstance().getUid();

        // Marcamos el textview la marca y el modelo del vehículo
        holder.textViewModeloYMarca.setText(vehiculo.getMarca() + " " + vehiculo.getModelo());
        // Marcamos el textvew la descripción del mismo
        holder.textViewDescripcion.setText(vehiculo.getDescripción());

        if(!uidUser.equals(vehiculo.getUidDueno())){
            holder.imageViewDelete.setVisibility(View.INVISIBLE);
            holder.imageViewEdit.setVisibility(View.INVISIBLE);
        }

        // Comprobamos que tipo de vehículo es para marcar un icono u otro
        switch (vehiculo.getTipoVehiculo().toLowerCase()) {
            case "coches": // En caso de que sea coche
                holder.imageViewTipoVehiculo.setImageResource(R.drawable.ic_coche);
                break;
            case "motos": // En caso de que sea moto
                holder.imageViewTipoVehiculo.setImageResource(R.drawable.ic_moto);
                break;
            default:
                break;
        }

        holder.fondo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("matriculaVeh", vehiculo.getMatricula());

                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.navigation_info_vehiculo, bundle);
            }
        });

        // Cuando toquemos el botón de eliminar un vehículo llamamos al método de eliminar y le pasamos la matricula del mismo
        holder.imageViewDelete.setOnClickListener(v -> eliminarVehiculo(vehiculo.getMatricula(), position));

        holder.imageViewEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AdministrarVehiculos.class);
                intent.putExtra("vehiculo", vehiculo);
                view.getContext().startActivity(intent);
            }
        });

        holder.imageViewFavorito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                agregarListFavVeh(vehiculo.getMatricula(), uidUser, holder.imageViewFavorito);
            }
        });
    }

    private void agregarListFavVeh(String matricula, String uid, ImageView imageViewFavorito){
        // Obtenemos la instancia de la base de datos de firestore
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
                                        imageViewFavorito.setImageResource(R.drawable.sin_estrella);
                                        showToast("Has quitado el vehículo de favoritos");
                                    } else {
                                        imageViewFavorito.setImageResource(R.drawable.con_estrella);
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

    /**
     * @param matricula
     * @param position
     * Método en el que le pasmos la posición
     * del vehículo a eliminar, le eliminamos de la lista y
     * posteriormente eliminamos el vehículo de la base de datos
     * de firestore
     */
    private void eliminarVehiculo(String matricula, int position) {
        // Comprobamos que la matricula no sea nula, en caso afirmativo retornamos para no proseguir
        if (matricula == null || matricula.isEmpty()) return;

        // Obtenemos la instancia de la base de datos de firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("vehiculos") // Accedemos a la colección de vehículos
                .whereEqualTo("matricula", matricula) // Filtramos por una query para encontrar una matricula
                .get()
                .addOnCompleteListener(task -> { // En caso de que vaya bien
                    if (task.isSuccessful() && !task.getResult().isEmpty()) { // Comprobamos que la tarea sea correcta
                        // Utilizamos un for para obtener las respuestas de la tarea
                        for (DocumentSnapshot document : task.getResult()) {
                            db.collection("vehiculos").document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> { // En caso de que vaya bien
                                        // Elimamos de la lista en esa posición el vehículo
                                        vehiculosList.remove(position);
                                        // Notificamos el cambio de datos
                                        notifyItemRemoved(position);
                                        // Notificamos el cambio de tamaño de la lista de vehículos
                                        notifyItemRangeChanged(position, vehiculosList.size());
                                        // Lanzamos un toast indicando que se ha eliminado un vehículo
                                        Toast.makeText(context, "Vehículo eliminado", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> // En caso de que algo vaya mal
                                            // Lanzamos un toast indicando que ocurrió un error
                                            Toast.makeText(context, "Error al eliminar el vehículo", Toast.LENGTH_SHORT).show()
                                    );
                        }
                    } else { // En caso de que la tarea no haya resultado
                        // Lanzamos un toast indicando que no se pudó encontrar el vehículo
                        Toast.makeText(context, "No se encontró el vehículo", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> // En caso de que algo falle
                        // Lanzamos un toast indicando que ocurrio un error al conectar con la bd
                        Toast.makeText(context, "Error al conectar con la base de datos", Toast.LENGTH_SHORT).show()
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
            mensajeToast = Toast.makeText(context, mensaje, Toast.LENGTH_SHORT);
            // Mostramos dicho Toast
            mensajeToast.show();
        }
    }

    @Override
    public int getItemCount() {
        return vehiculosList != null ? vehiculosList.size() : 0;
    }

    public static class VehiculoViewHolder extends RecyclerView.ViewHolder {
        TextView textViewModeloYMarca, textViewDescripcion;
        ImageView imageViewTipoVehiculo, imageViewEdit, imageViewDelete, imageViewFavorito;
        ConstraintLayout fondo;

        public VehiculoViewHolder(View itemView) {
            super(itemView);
            textViewModeloYMarca = itemView.findViewById(R.id.textViewMarcaModelo);
            textViewDescripcion = itemView.findViewById(R.id.textViewDescripcion);
            imageViewTipoVehiculo = itemView.findViewById(R.id.imageViewTipoVehiculo);
            imageViewEdit = itemView.findViewById(R.id.imageViewEdit);
            imageViewDelete = itemView.findViewById(R.id.imageViewDelete);
            fondo = itemView.findViewById(R.id.fondoVeh);
            imageViewFavorito = itemView.findViewById(R.id.imageViewFavorito);
        }
    }
}
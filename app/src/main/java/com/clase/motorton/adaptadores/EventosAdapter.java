package com.clase.motorton.adaptadores;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.clase.motorton.R;
import com.clase.motorton.modelos.Evento;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventosAdapter extends RecyclerView.Adapter<EventosAdapter.ViewHolder> {
    // Variable para manejar la lista de los eventos
    private List<Evento> eventoList = new ArrayList<>();
    // Variable para manejar el contexto de la aplicación
    private Context context = null;
    // Variable para manejar el uid del usuario
    private String currentUserId = null;

    /**
     * @param context
     * @param eventos
     * @param currentUserId
     * Constructor en donde inicializamos la lista de eventos,
     * el uid del usuario y el contexto de la aplicación
     */
    public EventosAdapter(Context context, List<Evento> eventos, String currentUserId) {
        this.context = context;
        this.eventoList = eventos;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_evento, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtengo en un evento el objeto elegido
        Evento evento = eventoList.get(position);

        // Establezco en los textview los diferentes datos del evento
        holder.nombre.setText(evento.getNombre());
        holder.tipoEvento.setText("Tipo: " + evento.getTipoEvento());

        Date fechaEvento = evento.getFecha();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String fechaFormateada = sdf.format(fechaEvento);

        holder.fecha.setText("Fecha: " + fechaFormateada);

        //holder.fecha.setText("Fecha: " + evento.getFecha());

        // Inicializamos la instancia de la base de datos
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Obtenemos el uid del usuario
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Procedemos a comprobar si el uid del organizador y el del usuario autenticado son el mismo o no
        if (evento.getOrganizador() != null && evento.getOrganizador().equals(currentUserId)) {
            // En caso de ser el mismo activamos el botón de editar
            holder.botonEditar.setVisibility(View.VISIBLE);
            holder.botonBorrar.setVisibility(View.VISIBLE);
        } else { // En caso de ser otro
            // Ocultamos el botón de editar
            holder.botonEditar.setVisibility(View.GONE);
            holder.botonBorrar.setVisibility(View.GONE);
        }

        // Accedemos a la colección de eventos
        db.collection("eventos").whereEqualTo("id", evento.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> { // En caso de que vaya bien
                    // Comprobamos que lo que nos devuelve la query no sea nulo
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                        // Guardamos en una lista los participantes desde la base de datos
                        List<String> participantes = (List<String>) documentSnapshot.get("participantes");
                        if (participantes != null && participantes.contains(uid)) { // En caso de que ya estes en la lista de participantes
                            // Establecemos el texto como quitarme
                            holder.botonApuntarse.setText("Quitarme");
                        } else { // En caso de que no estes en la lista de participantes
                            // Establecemos el texto como apuntarse
                            holder.botonApuntarse.setText("Apuntarse");
                        }
                    }
                })
                .addOnFailureListener(e -> { // En caso de que algo falle
                    // Lanzamos un toast indicando que ocurrió un error al obtener el evento
                    Toast.makeText(context, "Error al obtener el evento", Toast.LENGTH_SHORT).show();
                });

        // Acción para cuando toquemos el botón de apuntarnos
        holder.botonApuntarse.setOnClickListener(v -> {
            // Ponemos visible el progressbar
            holder.progressBar.setVisibility(View.VISIBLE);

            // Accedemos a la colección de eventos filtrando por el id del evento
            db.collection("eventos").whereEqualTo("id", evento.getId())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) { // En caso de que lo obtenido tenga datos
                            // Obtenemos el documento de la query
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                            // Guardo en una variable el id del evento que es el id del documento
                            String eventoId = documentSnapshot.getId();
                            // Guardamos en una lista los participantes desde la base de datos
                            List<String> participantes = (List<String>) documentSnapshot.get("participantes");

                            // En caso de que la lista de participantes sea nula la inicializamos
                            if (participantes != null && participantes.contains(uid)) {
                                // Agregamos a la lista el uid del usuario
                                participantes.remove(uid);

                                // Accedemos a la colección de eventos por el id del evento
                                db.collection("eventos").document(eventoId)
                                        .update("participantes", participantes) // Actualizamos la lista de participantes
                                        .addOnSuccessListener(unused -> { // En caso de que todo vaya bien
                                            // Cambiamos el texto del botón
                                            holder.botonApuntarse.setText("Apuntarse");
                                            // Lanzamos un toast indicando que el usuario se quito del evento
                                            Toast.makeText(context, "Te has quitado del evento", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> { // En caso de que algo falle
                                            // Lanzamos un toast indicando que hubo un error al quitarte del evento
                                            Toast.makeText(context, "Error al quitarte", Toast.LENGTH_SHORT).show();
                                        });
                            } else { // En caso de que lo obtenido no tenga datos
                                // En caso de que la lista de participantes sea nula la inicializamos
                                if (participantes == null) participantes = new java.util.ArrayList<>();
                                // Agregamos a la lista el uid del usuario
                                participantes.add(uid);

                                // Accedemos a la colección de eventos y al evento por el id del mismo
                                db.collection("eventos").document(eventoId)
                                        .update("participantes", participantes) // Actualizamos los participantes
                                        .addOnSuccessListener(unused -> { // En caso de que vaya bien
                                            // Cambiamos el botón a quitarme
                                            holder.botonApuntarse.setText("Quitarme");
                                            // Lanzo un toast indicando que te has apuntado al evento
                                            Toast.makeText(context, "Te has apuntado al evento", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> { // En caso de que algo falle
                                            // Lanzo un toast indicando que ocurrió un error al apuntarse
                                            Toast.makeText(context, "Error al apuntarte", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(e -> { // En caso de que algo falle
                        // Lanzamos un toast indicando que ocurrió un error al obtener el evento
                        Toast.makeText(context, "Error al obtener el evento", Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task -> { // En caso de que se complete
                        // Ponemos invisible la barra de progreso
                        holder.progressBar.setVisibility(View.GONE);
                    });
        });

        holder.botonVerMas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("eventoId", evento.getId());

                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.navigation_info_evento, bundle);
            }
        });

        holder.botonEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("eventoId", evento.getId());

                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.navigation_editar_evento, bundle);
            }
        });

        holder.botonBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eliminarEvento(evento, position);
            }
        });
    }

    private void eliminarEvento(Evento evento, int position){
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar este evento?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("eventos")
                            .whereEqualTo("id", evento.getId())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                    String eventoId = documentSnapshot.getId();
                                    db.collection("eventos").document(eventoId)
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                eventoList.remove(position);
                                                notifyItemRemoved(position);
                                                notifyItemRangeChanged(position, eventoList.size());
                                                Toast.makeText(context, "Evento eliminado", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(context, "Error al eliminar el evento", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Error al buscar el evento para eliminar", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return eventoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, tipoEvento, fecha;
        ImageView imagenEvento, botonEditar, botonBorrar;
        Button botonApuntarse, botonVerMas;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
            imagenEvento = itemView.findViewById(R.id.imagenEvento);
            nombre = itemView.findViewById(R.id.tituloEvento);
            tipoEvento = itemView.findViewById(R.id.tipoEvento);
            fecha = itemView.findViewById(R.id.fechaEvento);
            botonApuntarse = itemView.findViewById(R.id.botonApuntarse);
            botonVerMas = itemView.findViewById(R.id.botonVerMas);
            botonEditar = itemView.findViewById(R.id.imageViewEditarEvento);
            botonBorrar = itemView.findViewById(R.id.imageViewEliminarEvento);
        }
    }
}
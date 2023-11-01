package com.example.cursosappmvc.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cursosappmvc.R;
import com.example.cursosappmvc.model.Leccion;
import com.example.cursosappmvc.util.SharedPrefManager;
import com.example.cursosappmvc.view.LeccionDetailActivity;

import java.util.List;

public class LeccionAdapter extends RecyclerView.Adapter<LeccionAdapter.LeccionViewHolder> {

    private List<Leccion> listaLecciones;
    private Context context;
    private DetalleLeccionController detalleLeccionController;

    private int cursoId = -1; // Añade esta línea


    // Constructor
    public LeccionAdapter(Context context, List<Leccion> listaLecciones) {
        this.context = context;
        this.listaLecciones = listaLecciones;
        this.detalleLeccionController = new DetalleLeccionController(context);
    }

    @NonNull
    @Override
    public LeccionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("LeccionAdapter", "Inflando vista de lección");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leccion_item, parent, false);
        return new LeccionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeccionViewHolder holder, int position) {
        final Leccion leccion = listaLecciones.get(position);
        holder.tituloLeccion.setText(leccion.getTitulo());

        Log.d("LeccionAdapter", "Vinculando lección: " + leccion.getTitulo());
        Log.d("LeccionAdapter", "Lección completada: " + leccion.isCompletada());
        Log.d("LeccionAdapter", "Lección accesible: " + leccion.isAccesible());

        // Si la lección está completada, muestra un icono de completado
        if (leccion.isCompletada()) {
            holder.leccionIcon.setImageResource(R.drawable.icono_completado);  // Cambia 'icono_completado' al nombre de tu recurso
        } else {
            holder.leccionIcon.setImageResource(R.drawable.icono_normal); // Cambia 'icono_normal' al nombre de tu recurso
        }

        // Si la lección no es accesible, cambia el aspecto para que parezca desactivada
        if (!leccion.isAccesible()) {
            holder.itemView.setAlpha(0.5f);  // Hace que el elemento parezca semi-transparente
            holder.itemView.setEnabled(false);  // Desactiva cualquier interacción con el elemento
        } else {
            holder.itemView.setAlpha(1.0f);  // Asegura que el elemento es completamente opaco
            holder.itemView.setEnabled(true);  // Asegura que el elemento es interactivo
        }

        // Configura el OnClickListener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //   if (!isUserLoggedIn()) {
                //    showLoginDialog();
                //  } else {
                int usuarioId = SharedPrefManager.getInstance(context).getUserId();
                detalleLeccionController.leccionYaIniciada(usuarioId, leccion.getId(), isStarted -> {
                    if (isStarted) {
                        // Si ya ha iniciado la lección, ir directamente a LeccionDetailActivity
                        Intent intent = new Intent(context, LeccionDetailActivity.class);
                        intent.putExtra("leccionId", leccion.getId());
                        intent.putExtra("cursoId", cursoId);
                        context.startActivity(intent);
                    } else {
                        // Mostrar diálogo para comenzar lección
                        new AlertDialog.Builder(context)
                                .setTitle("Comenzar Lección")
                                .setMessage("¿Quieres empezar la lección " + leccion.getTitulo() + "?")
                                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        detalleLeccionController.registrarInicioLeccion(usuarioId, leccion.getId(), success -> {
                                            if (success) {
                                                Intent intent = new Intent(context, LeccionDetailActivity.class);
                                                intent.putExtra("leccionId", leccion.getId());
                                                context.startActivity(intent);
                                            } else {
                                                Toast.makeText(context, "Error al registrar el inicio de la lección", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                });
            }
            // }
        });
    }

    @Override
    public int getItemCount() {
        int size = listaLecciones.size();
        Log.d("LeccionAdapter", "Número de lecciones en el adaptador: " + size);
        return size;
    }

    public void updateLecciones(List<Leccion> lecciones) {
        this.listaLecciones = lecciones;
    }

    public static class LeccionViewHolder extends RecyclerView.ViewHolder {
        TextView tituloLeccion;
        ImageView leccionIcon;  // Añade esta variable para el icono

        public LeccionViewHolder(@NonNull View itemView) {
            super(itemView);
            tituloLeccion = itemView.findViewById(R.id.tituloLeccion);
            leccionIcon = itemView.findViewById(R.id.leccion_icon);  // Inicializa el ImageView del icono
        }
    }

}
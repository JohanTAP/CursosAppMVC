package com.example.cursosappmvc.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cursosappmvc.R;
import com.example.cursosappmvc.model.Leccion;
import com.example.cursosappmvc.util.SharedPrefManager;
import com.example.cursosappmvc.view.LeccionDetailActivity;
import com.example.cursosappmvc.view.LoginActivity;

import java.util.List;

public class LeccionAdapter extends RecyclerView.Adapter<LeccionAdapter.LeccionViewHolder> {

    private List<Leccion> listaLecciones;
    private Context context;
    private DetalleLeccionController detalleLeccionController;

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
        Log.d("LeccionAdapter", "Mostrando lección: " + leccion.getTitulo());
        holder.tituloLeccion.setText(leccion.getTitulo());

        // Configura el OnClickListener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isUserLoggedIn()) {
                    showLoginDialog();
                } else {
                    int usuarioId = SharedPrefManager.getInstance(context).getUserId();
                    detalleLeccionController.leccionYaIniciada(usuarioId, leccion.getId(), isStarted -> {
                        if (isStarted) {
                            // Si ya ha iniciado la lección, ir directamente a LeccionDetailActivity
                            Intent intent = new Intent(context, LeccionDetailActivity.class);
                            intent.putExtra("leccionId", leccion.getId());
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
            }
        });
    }

    private boolean isUserLoggedIn() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }

    private void showLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Iniciar sesión requerido");
        builder.setMessage("Necesitas iniciar sesión para acceder a esta lección.");
        builder.setPositiveButton("Iniciar sesión", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Redirige al usuario al LoginActivity
                Intent loginIntent = new Intent(context, LoginActivity.class);
                context.startActivity(loginIntent);
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    @Override
    public int getItemCount() {
        int size = listaLecciones.size();
        Log.d("LeccionAdapter", "Número de lecciones en el adaptador: " + size);
        return size;
    }

    public static class LeccionViewHolder extends RecyclerView.ViewHolder {
        TextView tituloLeccion;

        public LeccionViewHolder(@NonNull View itemView) {
            super(itemView);
            tituloLeccion = itemView.findViewById(R.id.tituloLeccion);
        }
    }
}
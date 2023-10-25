package com.example.cursosappmvc.controller;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cursosappmvc.R;
import com.example.cursosappmvc.model.Leccion;
import com.example.cursosappmvc.view.LeccionDetailActivity; // Importa la Activity que mostrará el contenido de la lección

import java.util.List;

public class LeccionAdapter extends RecyclerView.Adapter<LeccionAdapter.LeccionViewHolder> {

    private List<Leccion> listaLecciones;
    private Context context;

    // Constructor
    public LeccionAdapter(Context context, List<Leccion> listaLecciones) {
        this.context = context;
        this.listaLecciones = listaLecciones;
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

        // Configura el OnClickListener para abrir ContenidoLeccionActivity al hacer clic
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, LeccionDetailActivity.class);
                intent.putExtra("leccionId", leccion.getId());
                context.startActivity(intent);
            }
        });

        // Espacio para más datos en del modelo de Leccion, se pueden vincular aquí.
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
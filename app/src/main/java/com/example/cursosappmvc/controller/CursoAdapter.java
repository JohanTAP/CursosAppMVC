package com.example.cursosappmvc.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cursosappmvc.R;
import com.example.cursosappmvc.model.Curso;

import java.util.List;

public class CursoAdapter extends RecyclerView.Adapter<CursoAdapter.CursoViewHolder> {

    private List<Curso> listaCursos;
    private Context context;

    // Constructor
    public CursoAdapter(Context context, List<Curso> listaCursos) {
        this.context = context;
        this.listaCursos = listaCursos;
    }

    @NonNull
    @Override
    public CursoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.curso_item, parent, false);
        return new CursoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CursoViewHolder holder, int position) {
        Curso curso = listaCursos.get(position);
        // Aquí vinculas los datos del curso a los elementos de la vista, como TextViews, ImageViews, etc.
        holder.nombreCurso.setText(curso.getNombre());
        holder.descripcionCurso.setText(curso.getDescripcion());
        // Si tienes una imagen, puedes usar Glide o Picasso para cargar la imagen aquí.

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí se maneja lo que sucede cuando se hace clic en el CardView
                Toast.makeText(context, "Has hecho clic en el curso " + curso.getNombre(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaCursos.size();
    }

    public static class CursoViewHolder extends RecyclerView.ViewHolder {
        // Aquí van tus vistas, como TextViews, ImageViews, etc.
        TextView nombreCurso;
        TextView descripcionCurso;

        public CursoViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreCurso = itemView.findViewById(R.id.nombreCurso);
            descripcionCurso = itemView.findViewById(R.id.descripcionCurso);
            // Inicialización de otras vistas
        }
    }
}

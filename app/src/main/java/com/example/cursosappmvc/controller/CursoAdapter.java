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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.cursosappmvc.R;
import com.example.cursosappmvc.model.Curso;
import com.example.cursosappmvc.view.LeccionActivity;
import com.example.cursosappmvc.view.LoginActivity;

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
        // Vincular los datos del curso a los elementos de la vista, como TextViews, ImageViews, etc.
        holder.nombreCurso.setText(curso.getNombre());
        holder.descripcionCurso.setText(curso.getDescripcion());

        // Carga la imagen del curso con Glide.
        Glide.with(context)
                .load(curso.getImagenUrl()) // Getter getImagenUrl() de clase Curso.
                .apply(new RequestOptions().placeholder(R.drawable.placeholder_image).error(R.drawable.placeholder_image))
                .into(holder.cursoImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isUserLoggedIn()) {
                    showLoginDialog();
                } else {
                    Intent intent = new Intent(context, LeccionActivity.class);
                    intent.putExtra("cursoId", curso.getId());
                    Log.d("CursoAdapter", "Curso seleccionado ID: " + curso.getId());
                    context.startActivity(intent);
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
        builder.setMessage("Necesitas iniciar sesión para acceder a este curso.");
        builder.setPositiveButton("Iniciar sesión", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent loginIntent = new Intent(context, LoginActivity.class);
                context.startActivity(loginIntent);
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }


    @Override
    public int getItemCount() {
        return listaCursos.size();
    }

    public static class CursoViewHolder extends RecyclerView.ViewHolder {
        // Aquí van las vistas, como TextViews, ImageViews, etc.
        TextView nombreCurso;
        TextView descripcionCurso;
        ImageView cursoImage; // Añade esto para la imagen

        public CursoViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreCurso = itemView.findViewById(R.id.nombreCurso);
            descripcionCurso = itemView.findViewById(R.id.descripcionCurso);
            cursoImage = itemView.findViewById(R.id.cursoImage); // Inicializa ImageView
        }

    }
}
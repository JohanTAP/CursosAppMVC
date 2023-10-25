package com.example.cursosappmvc.view;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cursosappmvc.R;
import com.example.cursosappmvc.controller.LeccionAdapter;
import com.example.cursosappmvc.model.Leccion;
import com.example.cursosappmvc.model.LeccionDAO;

import java.util.ArrayList;
import java.util.List;

public class LeccionActivity extends AppCompatActivity {

    private RecyclerView leccionesRecyclerView;
    private LeccionAdapter leccionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leccion);

        leccionesRecyclerView = findViewById(R.id.recyclerLecciones);
        leccionesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicializa el adaptador aquí, aunque esté vacío
        leccionAdapter = new LeccionAdapter(this, new ArrayList<>());
        leccionesRecyclerView.setAdapter(leccionAdapter);

        // Obtener el ID del curso desde el Intent
        Intent intent = getIntent();
        int cursoId = intent.getIntExtra("cursoId", -1);
        Log.d("LeccionActivity", "ID del curso recibido: " + cursoId);

        if (cursoId != -1) {
            // Iniciar la tarea asíncrona para cargar las lecciones
            new LoadLeccionesTask(cursoId).execute();
        } else {
            Log.e("LeccionActivity", "Error: no se recibió un ID de curso válido.");
            Toast.makeText(this, "Error al cargar las lecciones: no se recibió el ID del curso", Toast.LENGTH_LONG).show();
        }
    }

    private class LoadLeccionesTask extends AsyncTask<Void, Void, List<Leccion>> {
        private int cursoId;

        public LoadLeccionesTask(int cursoId) {
            this.cursoId = cursoId;
        }

        @Override
        protected List<Leccion> doInBackground(Void... voids) {
            try {
                // Obtener las lecciones utilizando LeccionDAO
                return new LeccionDAO().obtenerLeccionesPorCurso(getApplicationContext(), cursoId);
            } catch (Exception e) {
                Log.e("LeccionActivity", "Error al cargar las lecciones", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Leccion> lecciones) {
            super.onPostExecute(lecciones);
            if (lecciones != null && !lecciones.isEmpty()) {
                leccionAdapter = new LeccionAdapter(LeccionActivity.this, lecciones);
                leccionesRecyclerView.setAdapter(leccionAdapter);
                Log.d("LeccionActivity", "Lecciones actualizadas en el adaptador.");
            } else {
                Log.e("LeccionActivity", "No se encontraron lecciones o hubo un error al cargarlas.");
                Toast.makeText(LeccionActivity.this, "No se encontraron lecciones para este curso", Toast.LENGTH_LONG).show();
            }
        }

    }

}
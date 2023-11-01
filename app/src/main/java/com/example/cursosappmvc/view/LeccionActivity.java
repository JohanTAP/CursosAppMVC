package com.example.cursosappmvc.view;

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
import com.example.cursosappmvc.util.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class LeccionActivity extends AppCompatActivity {

    private RecyclerView leccionesRecyclerView;
    private LeccionAdapter leccionAdapter;
    private LeccionDAO leccionDAO; // Referencia al DAO
    private int usuarioId;
    private int cursoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. Configura el contenido de la vista
        setContentView(R.layout.activity_leccion);

        // 2. Inicializa todas las vistas
        leccionesRecyclerView = findViewById(R.id.recyclerLecciones);

        // 3. Inicializa todos los adaptadores y otros componentes
        leccionAdapter = new LeccionAdapter(this, new ArrayList<>());
        leccionDAO = new LeccionDAO(getApplicationContext());

        // 4. Configura las vistas con sus respectivos adaptadores o listeners
        leccionesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        leccionesRecyclerView.setAdapter(leccionAdapter);

        // 5. Recupera cualquier dato adicional necesario
        usuarioId = SharedPrefManager.getInstance(this).getUserId();
        cursoId = getIntent().getIntExtra("cursoId", -1); // Guarda el cursoId
        Log.d("LeccionActivity", "UsuarioId recuperado: " + usuarioId);
        Log.d("LeccionActivity", "ID del curso recibido: " + cursoId);

        // 6. Lanza cualquier tarea en segundo plano para recuperar datos
        if (cursoId != -1) {
            new LoadLeccionesTask(cursoId, usuarioId).execute();  // Pasa el usuarioId a la tarea
        } else {
            Log.e("LeccionActivity", "Error: no se recibió un ID de curso válido.");
            Toast.makeText(this, "Error al cargar las lecciones: no se recibió el ID del curso", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Recarga las lecciones para actualizar su estado
        new LoadLeccionesTask(cursoId, usuarioId).execute();
    }

    private class LoadLeccionesTask extends AsyncTask<Void, Void, List<Leccion>> {
        private int cursoId;
        private int usuarioId;  // Añade una variable para el usuarioId

        public LoadLeccionesTask(int cursoId, int usuarioId) {  // Modifica el constructor para recibir usuarioId
            this.cursoId = cursoId;
            this.usuarioId = usuarioId;
        }

        @Override
        protected List<Leccion> doInBackground(Void... voids) {
            try {
                List<Leccion> lecciones = leccionDAO.obtenerLeccionesPorCurso(cursoId);

                for (Leccion leccion : lecciones) {
                    // Usa el usuarioId aquí
                    boolean completada = leccionDAO.leccionCompletadaPorUsuario(usuarioId, leccion.getId());
                    leccion.setCompletada(completada);

                    boolean puedeAcceder = leccionDAO.puedeAccederALeccion(usuarioId, leccion.getId());
                    leccion.setAccesible(puedeAcceder);
                }

                return lecciones;
            } catch (Exception e) {
                Log.e("LeccionActivity", "Error al cargar las lecciones", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Leccion> lecciones) {
            if (lecciones != null && !lecciones.isEmpty()) {
                leccionAdapter.updateLecciones(lecciones);
                leccionAdapter.notifyDataSetChanged();

                Log.d("LeccionActivity", "Lecciones actualizadas en el adaptador.");
            } else {
                Log.e("LeccionActivity", "No se encontraron lecciones o hubo un error al cargarlas.");
                Toast.makeText(LeccionActivity.this, "No se encontraron lecciones para este curso", Toast.LENGTH_LONG).show();
            }
        }
    }
}
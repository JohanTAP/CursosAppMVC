package com.example.cursosappmvc.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import com.example.cursosappmvc.R;
import com.example.cursosappmvc.model.Leccion;
import com.example.cursosappmvc.model.LeccionDAO;

public class LeccionDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leccion_detail);

        // Obtiene el ID de la lección del Intent
        int leccionId = getIntent().getIntExtra("leccionId", -1);
        if (leccionId == -1) {
            // Error, ID no encontrado
            finish();  // Cierra la Activity
            return;
        }

        // Lanza la tarea para obtener la lección por ID
        new FetchLeccionTask(this).execute(leccionId);
    }

    // Clase interna para la tarea asíncrona
    private class FetchLeccionTask extends AsyncTask<Integer, Void, Leccion> {
        private Context context;

        public FetchLeccionTask(Context context) {
            this.context = context;
        }

        @Override
        protected Leccion doInBackground(Integer... leccionIds) {
            int leccionId = leccionIds[0];
            LeccionDAO leccionDAO = new LeccionDAO();
            return leccionDAO.obtenerLeccionPorId(context, leccionId);
        }

        @Override
        protected void onPostExecute(Leccion leccion) {
            // Configura la vista con el contenido de la lección
            if (leccion != null) {
                TextView contenidoTextView = findViewById(R.id.tvContenidoLeccion);
                contenidoTextView.setText(leccion.getContenido());
                setTitle(leccion.getTitulo());
                TextView tituloLeccionTextView = findViewById(R.id.tvTituloLeccion);
                tituloLeccionTextView.setText(leccion.getTitulo());
            } else {
                // Manejar el caso en que la lección es null (por ejemplo, mostrar un mensaje de error o un Toast)
            }
        }
    }

}
package com.example.cursosappmvc.view;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cursosappmvc.R;
import com.example.cursosappmvc.model.Leccion;
import com.example.cursosappmvc.model.LeccionDAO;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class LeccionDetailActivity extends AppCompatActivity {

    private int cursoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leccion_detail);

        cursoId = getIntent().getIntExtra("cursoId", -1);

        int leccionId = getIntent().getIntExtra("leccionId", -1);
        if (leccionId == -1) {
            finish();
            return;
        }

        new FetchLeccionTask(this).execute(leccionId);

        FloatingActionButton btnRealizarLeccion = findViewById(R.id.btnRealizarLeccion);
        btnRealizarLeccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LeccionDetailActivity.this, QuizActivity.class);
                intent.putExtra("leccionId", leccionId);
                intent.putExtra("cursoId", cursoId);  // Asegúrate de pasar el cursoId también
                startActivity(intent);
            }
        });
    }

    private class FetchLeccionTask extends AsyncTask<Integer, Void, Leccion> {
        private Context context;

        public FetchLeccionTask(Context context) {
            this.context = context;
        }

        @Override
        protected Leccion doInBackground(Integer... leccionIds) {
            int leccionId = leccionIds[0];
            LeccionDAO leccionDAO = new LeccionDAO(context);
            return leccionDAO.obtenerLeccionPorId(context, leccionId);
        }

        @Override
        protected void onPostExecute(Leccion leccion) {
            if (leccion != null) {
                TextView contenidoTextView = findViewById(R.id.tvContenidoLeccion);
                contenidoTextView.setText(leccion.getContenido());
                setTitle(leccion.getTitulo());
                TextView tituloLeccionTextView = findViewById(R.id.tvTituloLeccion);
                tituloLeccionTextView.setText(leccion.getTitulo());
            }
        }
    }
}
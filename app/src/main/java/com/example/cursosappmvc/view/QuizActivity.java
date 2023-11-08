package com.example.cursosappmvc.view;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cursosappmvc.MainActivity;
import com.example.cursosappmvc.R;
import com.example.cursosappmvc.model.DetalleLeccionDAO;
import com.example.cursosappmvc.model.Opcion;
import com.example.cursosappmvc.model.Pregunta;
import com.example.cursosappmvc.model.Quiz;
import com.example.cursosappmvc.model.QuizDAO;
import com.example.cursosappmvc.util.SharedPrefManager;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "QuizActivity";

    private TextView tvPregunta;
    private RadioGroup rgOpciones;
    private Button btnResponder;
    private ProgressBar progressBar;
    private List<Pregunta> todasLasPreguntas;
    private int preguntaActualIndex = 0;

    // Variables de instancia
    private int usuarioId = -1;
    private int leccionId = -1;
    private int cursoId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        Log.d(TAG, "onCreate: Iniciando la Activity");

        tvPregunta = findViewById(R.id.tvPregunta);
        rgOpciones = findViewById(R.id.rgOpciones);
        btnResponder = findViewById(R.id.btnResponder);
        progressBar = findViewById(R.id.progressBar);

        usuarioId = SharedPrefManager.getInstance(this).getUserId(); // Obtener el ID del usuario desde SharedPrefManager
        leccionId = getIntent().getIntExtra("leccionId", -1);
        cursoId = getIntent().getIntExtra("cursoId", -1);

        if (leccionId == -1) {
            Log.e(TAG, "onCreate: ID de lección no encontrado");
            finish();
            return;
        }

        cargarQuiz(leccionId);

        btnResponder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Botón responder clickeado");
                verificarRespuesta(v);
            }
        });
    }

    private void cargarQuiz(int leccionId) {
        new AsyncTask<Void, Void, Quiz>() {
            @Override
            protected Quiz doInBackground(Void... voids) {
                Log.d(TAG, "doInBackground: Cargando quiz desde la base de datos");
                QuizDAO quizDAO = new QuizDAO(QuizActivity.this);
                return quizDAO.getQuizByLeccionId(leccionId);
            }

            @Override
            protected void onPostExecute(Quiz quiz) {
                if (quiz != null && quiz.getPreguntas() != null && !quiz.getPreguntas().isEmpty()) {
                    todasLasPreguntas = quiz.getPreguntas();
                    mostrarPregunta(todasLasPreguntas.get(preguntaActualIndex));
                } else {
                    Log.e(TAG, "onPostExecute: Quiz vacío o sin preguntas.");
                }
            }
        }.execute();
    }

    private void limpiarColoresRadioButtons() {
        for (int i = 0; i < rgOpciones.getChildCount(); i++) {
            RadioButton rb = (RadioButton) rgOpciones.getChildAt(i);
            rb.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void verificarRespuesta(View v) {
        limpiarColoresRadioButtons();
        int selectedOptionId = rgOpciones.getCheckedRadioButtonId();
        RadioButton rbSeleccionado = findViewById(selectedOptionId);
        boolean esCorrecta = false;

        if (rbSeleccionado == null) {
            Snackbar.make(v, "Debes seleccionar una opción", Snackbar.LENGTH_SHORT).show();
            return;
        }

        for (Opcion opcion : todasLasPreguntas.get(preguntaActualIndex).getOpciones()) {
            if (opcion.getOpcionId() == selectedOptionId && opcion.isCorrecta()) {
                esCorrecta = true;
                break;
            }
        }

        if (esCorrecta) {
            rbSeleccionado.setBackgroundColor(Color.GREEN);
            Snackbar.make(v, "¡Correcto!", Snackbar.LENGTH_SHORT).show();
            reproducirSonido(R.raw.correct_answer); // Asegúrate de que el archivo correct_answer.mp3 esté en res/raw
            darFeedbackHaptico();

            rbSeleccionado.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressBar.setProgress(progressBar.getProgress() + (100 / todasLasPreguntas.size()));
                    preguntaActualIndex++;
                    if (preguntaActualIndex < todasLasPreguntas.size()) {
                        mostrarPregunta(todasLasPreguntas.get(preguntaActualIndex));
                    } else {
                        progressBar.setProgress(100);
                        Snackbar.make(v, "¡Felicidades! Completaste el Quiz.", Snackbar.LENGTH_LONG).show();
                        reproducirSonido(R.raw.success);
                        darFeedbackHaptico();
                        new MarcarLeccionComoCompletadaTask().execute();
                    }
                }
            }, 1500);
        } else {
            rbSeleccionado.setBackgroundColor(Color.RED);
            Snackbar.make(v, "Incorrecto, intenta de nuevo.", Snackbar.LENGTH_SHORT).show();
            reproducirSonido(R.raw.wrong_answer);
            darFeedbackHaptico();
        }
    }

    private void reproducirSonido(int soundResourceId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, soundResourceId);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mediaPlayer.start();
    }

    private void darFeedbackHaptico() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(100); // Vibrate for 100 milliseconds
        }
    }

    private void mostrarPregunta(Pregunta pregunta) {
        if (pregunta == null || pregunta.getOpciones() == null) {
            Log.e(TAG, "mostrarPregunta: Pregunta u opciones nulas");
            return;
        }

        Log.d(TAG, "mostrarPregunta: Mostrando pregunta y opciones");
        Animation slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);

        slideOutLeft.setDuration(500);
        slideInRight.setDuration(500);

        slideOutLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                for (int i = 0; i < rgOpciones.getChildCount(); i++) {
                    RadioButton rb = (RadioButton) rgOpciones.getChildAt(i);
                    rb.setBackgroundColor(Color.TRANSPARENT);
                }

                tvPregunta.setText(pregunta.getContenido());
                rgOpciones.removeAllViews();

                for (Opcion opcion : pregunta.getOpciones()) {
                    RadioButton rbOpcion = new RadioButton(QuizActivity.this);
                    rbOpcion.setText(opcion.getContenido());
                    rbOpcion.setId(opcion.getOpcionId());
                    rgOpciones.addView(rbOpcion);
                }

                rgOpciones.startAnimation(slideInRight);
                tvPregunta.startAnimation(slideInRight);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        rgOpciones.startAnimation(slideOutLeft);
        tvPregunta.startAnimation(slideOutLeft);
    }

    private class MarcarLeccionComoCompletadaTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            DetalleLeccionDAO quizDAO = new DetalleLeccionDAO(QuizActivity.this);
            quizDAO.marcarLeccionComoCompletada(usuarioId, leccionId);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent intent = new Intent(QuizActivity.this, MainActivity.class);
            intent.putExtra("leccionId", leccionId);
            startActivity(intent);
            finish();
        }
    }
}
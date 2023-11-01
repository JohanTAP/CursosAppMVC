package com.example.cursosappmvc.model;

import android.content.Context;
import android.util.Log;

import com.example.cursosappmvc.model.database.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuizDAO {

    private static final String TAG = "QuizDAO"; // Etiqueta para el registro
    private Context context;

    public QuizDAO(Context context) {
        this.context = context;
    }

    public Quiz getQuizByLeccionId(int leccionId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection(context);

            String query = "SELECT * FROM quiz WHERE lec_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, leccionId);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Quiz quiz = new Quiz();
                quiz.setId(resultSet.getInt("quiz_id"));
                quiz.setNombre(resultSet.getString("quiz_nombre"));
                quiz.setDescripcion(resultSet.getString("quiz_descripcion"));

                Log.d(TAG, "Quiz recuperado: " + quiz.getNombre());

                // Ahora obtén las preguntas y opciones para este quiz
                List<Pregunta> preguntas = getPreguntasByQuizId(quiz.getId());
                quiz.setPreguntas(preguntas);

                return quiz;
            } else {
                Log.d(TAG, "No se encontró quiz para la lección con ID: " + leccionId);
            }

        } catch (SQLException e) {
            Log.e(TAG, "Error al obtener el Quiz", e);
        } finally {
            // Asegúrate de cerrar todas las conexiones y recursos aquí
        }

        return null;
    }

    private List<Pregunta> getPreguntasByQuizId(int quizId) {
        List<Pregunta> preguntas = new ArrayList<>();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection(context);

            String query = "SELECT * FROM pregunta WHERE quiz_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, quizId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Pregunta pregunta = new Pregunta();
                pregunta.setPreguntaId(resultSet.getInt("preg_id"));
                pregunta.setContenido(resultSet.getString("preg_contenido"));

                Log.d(TAG, "Pregunta recuperada: " + pregunta.getContenido());

                // Ahora obtén las opciones para esta pregunta
                List<Opcion> opciones = getOpcionesByPreguntaId(pregunta.getPreguntaId());
                pregunta.setOpciones(opciones);

                preguntas.add(pregunta);
            }

        } catch (SQLException e) {
            Log.e(TAG, "Error al obtener las Preguntas", e);
        } finally {
            // Asegúrate de cerrar todas las conexiones y recursos aquí
        }

        return preguntas;
    }

    private List<Opcion> getOpcionesByPreguntaId(int preguntaId) {
        List<Opcion> opciones = new ArrayList<>();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection(context);

            String query = "SELECT * FROM opcion WHERE preg_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, preguntaId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Opcion opcion = new Opcion();
                opcion.setOpcionId(resultSet.getInt("op_id"));
                opcion.setContenido(resultSet.getString("op_contenido"));
                opcion.setCorrecta(resultSet.getString("op_correcta").equals("Y")); // Aquí es donde estableces si la opción es correcta

                Log.d(TAG, "Opción recuperada: " + opcion.getContenido());

                // Agrega cualquier otro campo necesario para la opción aquí
                opciones.add(opcion);
            }

        } catch (SQLException e) {
            Log.e(TAG, "Error al obtener las Opciones", e);
        } finally {
            // Asegúrate de cerrar todas las conexiones y recursos aquí
        }

        return opciones;
    }

}
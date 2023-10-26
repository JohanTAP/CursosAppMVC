package com.example.cursosappmvc.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.cursosappmvc.model.database.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DetalleLeccionDAO {

    private static Context context = null;

    public DetalleLeccionDAO(Context context) {
        this.context = context;
    }

    public void leccionYaIniciada(int usuarioId, int leccionId, OnLeccionIniciadaListener listener) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                Connection connection = null;
                PreparedStatement preparedStatement = null;
                try {
                    connection = DatabaseUtil.getConnection(context);

                    // Verificar si ya existe un registro para ese usuario y lección
                    String checkQuery = "SELECT 1 FROM DetalleLeccion WHERE DET_USU_ID = ? AND DET_LEC_ID = ?";
                    preparedStatement = connection.prepareStatement(checkQuery);
                    preparedStatement.setInt(1, usuarioId);
                    preparedStatement.setInt(2, leccionId);
                    ResultSet rs = preparedStatement.executeQuery();
                    return rs.next();  // Devuelve true si el usuario ya ha comenzado esta lección, false en caso contrario

                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    // Cierra las conexiones y declaraciones aquí
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                listener.onResult(result);
            }
        }.execute();
    }

    public boolean registrarInicioLeccion(int usuarioId, int leccionId) {
        Log.d("DetalleLeccionDAO", "Registrando inicio de lección para usuario ID: " + usuarioId + " y lección ID: " + leccionId);
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DatabaseUtil.getConnection(context);
            String query = "INSERT INTO DetalleLeccion (DET_ID, DET_USU_ID, DET_LEC_ID, DET_FechaInicio, DET_EstadoProgreso) VALUES (DETALLELECCION_SEQ.NEXTVAL, ?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, usuarioId);
            preparedStatement.setInt(2, leccionId);
            preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setString(4, "En Progreso");

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            Log.e("DetalleLeccionDAO", "Error al registrar el inicio de la lección", e);
            return false;
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                Log.e("DetalleLeccionDAO", "Error al cerrar recursos", e);
            }
        }

    }


    // Añadir esta interfaz al principio de tu clase
    public interface OnLeccionIniciadaListener {
        void onResult(boolean isStarted);
    }


}
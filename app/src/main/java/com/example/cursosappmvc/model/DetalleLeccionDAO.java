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

    public boolean marcarLeccionComoCompletada(int usuarioId, int leccionId) {
        Log.d("DetalleLeccionDAO", "Marcando lección como completada para usuario ID: " + usuarioId + " y lección ID: " + leccionId);
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DatabaseUtil.getConnection(context);
            String query = "UPDATE DetalleLeccion SET DET_EstadoProgreso = 'Completada' WHERE DET_USU_ID = ? AND DET_LEC_ID = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, usuarioId);
            preparedStatement.setInt(2, leccionId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            Log.e("DetalleLeccionDAO", "Error al marcar la lección como completada", e);
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

    public boolean leccionAnteriorCompletada(int usuarioId, int leccionIdActual, int ordenLeccionActual) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = DatabaseUtil.getConnection(context);

            // Primero, obtener el ID de la lección anterior basado en lec_ordenleccion
            String queryId = "SELECT lec_id FROM leccion WHERE lec_ordenleccion = ?";
            preparedStatement = connection.prepareStatement(queryId);
            preparedStatement.setInt(1, ordenLeccionActual - 1);
            ResultSet rsId = preparedStatement.executeQuery();
            if (!rsId.next()) {
                return false;  // No hay lección anterior
            }
            int leccionIdAnterior = rsId.getInt("lec_id");

            // Luego, verificar si esta lección anterior fue completada por el usuario
            String query = "SELECT DET_EstadoProgreso FROM DetalleLeccion WHERE DET_USU_ID = ? AND DET_LEC_ID = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, usuarioId);
            preparedStatement.setInt(2, leccionIdAnterior);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                String estado = rs.getString("DET_EstadoProgreso");
                return "Completada".equals(estado);
            }
        } catch (SQLException e) {
            Log.e("DetalleLeccionDAO", "Error al verificar si la lección anterior fue completada", e);
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
        return false;
    }

    // Añadir esta interfaz al principio de tu clase
    public interface OnLeccionIniciadaListener {
        void onResult(boolean isStarted);
    }

}
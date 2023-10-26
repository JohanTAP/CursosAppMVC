package com.example.cursosappmvc.model;

import android.content.Context;
import android.util.Log;

import com.example.cursosappmvc.model.database.DatabaseUtil;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class LeccionDAO {

    private Context context;

    public LeccionDAO(Context context) {
        this.context = context;
    }

    public List<Leccion> obtenerLeccionesPorCurso(int cursoId) {
        List<Leccion> lecciones = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection(context);
            String query = "SELECT * FROM leccion WHERE lec_curso_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, cursoId);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("lec_id");
                String titulo = resultSet.getString("lec_titulo");
                Clob clobContenido = resultSet.getClob("lec_contenido");
                String contenido = clobContenido.getSubString(1, (int) clobContenido.length());
                int ordenLeccion = resultSet.getInt("lec_ordenleccion");

                Leccion leccion = new Leccion();
                leccion.setId(id);
                leccion.setTitulo(titulo);
                leccion.setContenido(contenido);
                leccion.setCursoId(cursoId);
                leccion.setOrdenLeccion(ordenLeccion);

                lecciones.add(leccion);
            }

            if (lecciones.isEmpty()) {
                Log.d("LeccionDAO", "La consulta se ejecutó correctamente pero no devolvió ningún resultado.");
            } else {
                Log.d("LeccionDAO", "Número de lecciones recuperadas: " + lecciones.size());
            }
        } catch (Exception e) {
            Log.e("LeccionDAO", "Error al obtener lecciones", e); // Añade esta línea
            e.printStackTrace();
        } finally {

        }
        return lecciones;
    }

    public Leccion obtenerLeccionPorId(Context context, int leccionId) {
        Leccion leccion = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection(context);
            String query = "SELECT * FROM leccion WHERE lec_id = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, leccionId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("lec_id");
                String titulo = resultSet.getString("lec_titulo");
                Clob clobContenido = resultSet.getClob("lec_contenido");
                String contenido = clobContenido.getSubString(1, (int) clobContenido.length());
                int ordenLeccion = resultSet.getInt("lec_ordenleccion");

                leccion = new Leccion();
                leccion.setId(id);
                leccion.setTitulo(titulo);
                leccion.setContenido(contenido);
            }
        } catch (Exception e) {
            Log.e("LeccionDAO", "Error al obtener la lección por ID", e);
        } finally {
            // Cerrar recursos (connection, preparedStatement, resultSet)...
        }
        return leccion;
    }

    public boolean registrarInicioLeccion(int usuarioId, int leccionId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean success = false;

        try {
            connection = DatabaseUtil.getConnection(context);
            String query = "INSERT INTO detalleleccion (det_usu_id, det_lec_id, det_fechainicio, det_estadoprogreso) VALUES (?, ?, CURRENT_TIMESTAMP, 'En Progreso')";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, usuarioId);
            preparedStatement.setInt(2, leccionId);

            int affectedRows = preparedStatement.executeUpdate();
            success = (affectedRows > 0); // Si se afectó al menos una fila, entonces el registro fue exitoso.

        } catch (Exception e) {
            Log.e("DetalleLeccionDAO", "Error al registrar el inicio de la lección", e);
            e.printStackTrace();
        } finally {
            // Aquí deberías cerrar tus recursos (connection, preparedStatement)...
        }

        return success;
    }

}
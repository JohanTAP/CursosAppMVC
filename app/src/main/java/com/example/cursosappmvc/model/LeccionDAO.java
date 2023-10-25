package com.example.cursosappmvc.model;

import android.content.Context;
import android.util.Log;

import com.example.cursosappmvc.model.database.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.List;

public class LeccionDAO {

    public List<Leccion> obtenerLeccionesPorCurso(Context context, int cursoId) {
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

}
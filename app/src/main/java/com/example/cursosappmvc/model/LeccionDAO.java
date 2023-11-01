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

    public boolean leccionCompletadaPorUsuario(int usuarioId, int leccionId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = DatabaseUtil.getConnection(context);
            String checkQuery = "SELECT 1 FROM DetalleLeccion WHERE DET_USU_ID = ? AND DET_LEC_ID = ? AND DET_EstadoProgreso = 'Completada'";
            preparedStatement = connection.prepareStatement(checkQuery);
            preparedStatement.setInt(1, usuarioId);
            preparedStatement.setInt(2, leccionId);
            ResultSet rs = preparedStatement.executeQuery();
            return rs.next();  // Devuelve true si la lección ha sido completada por el usuario, false en caso contrario
        } catch (Exception e) {
            Log.e("LeccionDAO", "Error al verificar si la lección ha sido completada", e);
            return false;
        } finally {
            // Cierra las conexiones y declaraciones aquí
            // (connection, preparedStatement)
        }
    }

    public boolean puedeAccederALeccion(int usuarioId, int leccionId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = DatabaseUtil.getConnection(context);

            // Primero obtiene el orden de la lección actual
            String ordenQuery = "SELECT lec_ordenleccion FROM leccion WHERE lec_id = ?";
            preparedStatement = connection.prepareStatement(ordenQuery);
            preparedStatement.setInt(1, leccionId);
            ResultSet rsOrden = preparedStatement.executeQuery();
            if (rsOrden.next()) {
                int ordenActual = rsOrden.getInt("lec_ordenleccion");

                // Si es la primera lección, siempre se puede acceder
                if (ordenActual == 1) {
                    return true;
                }

                // Luego, verifica si la lección anterior ha sido completada
                String checkQuery = "SELECT 1 FROM leccion l JOIN DetalleLeccion d ON l.lec_id = d.DET_LEC_ID WHERE d.DET_USU_ID = ? AND l.lec_ordenleccion = ? AND d.DET_EstadoProgreso = 'Completada'";
                preparedStatement = connection.prepareStatement(checkQuery);
                preparedStatement.setInt(1, usuarioId);
                preparedStatement.setInt(2, ordenActual - 1); // lección anterior
                ResultSet rsCheck = preparedStatement.executeQuery();
                return rsCheck.next();
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.e("LeccionDAO", "Error al verificar el acceso a la lección", e);
            return false;
        } finally {
            // Cierra las conexiones y declaraciones aquí
            // (connection, preparedStatement)
        }
    }

}
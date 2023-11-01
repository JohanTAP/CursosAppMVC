package com.example.cursosappmvc.model;

import android.content.Context;
import android.util.Log;

import com.example.cursosappmvc.model.database.DatabaseUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CursoDAO {

    public List<Curso> obtenerCursos(Context context) {
        List<Curso> cursos = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection(context); // Obtener la conexión desde DatabaseUtil
            String query = "SELECT * FROM curso"; // Consulta SQL
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                int id = resultSet.getInt("CUR_ID");
                String nombre = resultSet.getString("CUR_Nombre");
                String descripcion = resultSet.getString("CUR_Descripcion");
                // Aquí se deberían agregar los campos restantes si se necesitan para el objeto Curso

                Curso curso = new Curso();
                curso.setId(id);
                curso.setNombre(nombre);
                curso.setDescripcion(descripcion);
                curso.setImagenUrl(resultSet.getString("cur_imagen_url"));
                // Setear los campos restantes al objeto curso

                cursos.add(curso);
            }

            if (cursos.isEmpty()) {
                Log.d("CursoDAO", "La consulta se ejecutó correctamente pero no devolvió ningún resultado.");
            } else {
                Log.d("CursoDAO", "Número de cursos recuperados: " + cursos.size());
            }

        } catch (SQLException e) {
            Log.e("CursoDAO", "Error SQL", e);
        } catch (Exception e) {
            Log.e("CursoDAO", "Error desconocido al recuperar cursos", e);
        } finally {
            // Finalmente, nos aseguramos de cerrar los recursos
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return cursos;
    }

}
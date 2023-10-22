package com.example.cursosappmvc.model;

import android.content.Context;

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
                int id = resultSet.getInt("cur_id");
                String nombre = resultSet.getString("cur_nombre");
                String descripcion = resultSet.getString("cur_descripcion");
                // Aquí se deberían agregar los campos restantes si se necesitan para el objeto Curso

                Curso curso = new Curso();
                curso.setId(id);
                curso.setNombre(nombre);
                curso.setDescripcion(descripcion);
                // Setear los campos restantes al objeto curso

                cursos.add(curso);
            }
        } catch (SQLException e) {
            // Manejar excepciones de SQL
            e.printStackTrace();
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
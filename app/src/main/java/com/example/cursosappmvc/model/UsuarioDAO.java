package com.example.cursosappmvc.model;

import android.content.Context;
import android.util.Log;

import com.example.cursosappmvc.model.database.DatabaseUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    private final Context context;

    public UsuarioDAO(Context context) {
        this.context = context;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());

            // Convertir el hash a una representación hexadecimal
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Verifica las credenciales del usuario en la base de datos
    public boolean verificarCredenciales(String correo, String contrasena) {
        boolean valid = false;

        // Lógica de conexión a la base de datos
        try (Connection connection = DatabaseUtil.getConnection(context);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT USU_Contrasena FROM Usuario WHERE USU_CorreoElectronico = ?")) {

            preparedStatement.setString(1, correo);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String storedPasswordHash = resultSet.getString("USU_Contrasena");

                // Asumiendo que tienes una función hashPassword
                String enteredPasswordHash = hashPassword(contrasena);

                if (storedPasswordHash.equals(enteredPasswordHash)) {
                    valid = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return valid;
    }

    public Usuario buscarPorCorreoYContrasena(String correoElectronico, String contrasena) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection(context);
            String query = "SELECT * FROM Usuario WHERE USU_CorreoElectronico = ? AND USU_Contrasena = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, correoElectronico);
            preparedStatement.setString(2, hashPassword(contrasena)); // Asegúrate de usar el hash

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Usuario usuario = new Usuario();
                usuario.setId(resultSet.getInt("USU_ID"));
                // ... llenar los demás campos del usuario
                return usuario;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Cierra las conexiones, resultados y declaraciones aquí
        }
        return null;
    }

    public boolean guardar(Usuario usuario) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DatabaseUtil.getConnection(context);
            String query = "INSERT INTO Usuario (nombre, apellido1, ...) VALUES (?, ?, ...)";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, usuario.getNombre());
            // ... establecer los demás campos del usuario

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Cierra las conexiones y declaraciones aquí
        }
        return false;
    }

    public boolean actualizarTokenRestablecimiento(String correoElectronico, String token) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DatabaseUtil.getConnection(context);
            String query = "UPDATE Usuario SET tokenReset = ? WHERE correoElectronico = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, token);
            preparedStatement.setString(2, correoElectronico);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Cierra las conexiones y declaraciones aquí
        }
        return false;
    }

    public Usuario buscarPorId(int userId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection(context);
            String query = "SELECT * FROM Usuario WHERE USU_ID = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userId);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Usuario usuario = new Usuario();
                usuario.setId(resultSet.getInt("USU_ID"));
                usuario.setNombre(resultSet.getString("USU_Nombre"));
                usuario.setApellido1(resultSet.getString("USU_Apellido1")); // Asegúrate de que estos nombres de columna coincidan
                usuario.setApellido2(resultSet.getString("USU_Apellido2")); // con los de tu base de datos
                usuario.setCorreoElectronico(resultSet.getString("USU_CorreoElectronico"));
                usuario.setCiudad(resultSet.getString("USU_Ciudad"));
                Log.d("UsuarioDAO", "Ciudad recuperada: " + usuario.getCiudad()); // Log para depuración
                usuario.setDireccion(resultSet.getString("USU_Direccion"));
                usuario.setTelefono(resultSet.getString("USU_Telefono"));
                usuario.setFechaNacimiento(resultSet.getDate("USU_Fecha_Nacimiento"));
                usuario.setContrasenaHash(resultSet.getString("USU_Contrasena"));
                // Llenar los demás campos del usuario
                Log.d("UsuarioDAO", "Usuario encontrado: ID = " + usuario.getId() + ", Nombre = " + usuario.getNombre());
                return usuario;
            } else {
                Log.d("UsuarioDAO", "No se encontraron resultados para el ID: " + userId);
            }
        } catch (SQLException e) {
            Log.e("UsuarioDAO", "Error SQL al buscar usuario con ID: " + userId, e);
        } finally {
            // Asegurarse de cerrar los recursos
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException ex) {
                Log.e("UsuarioDAO", "Error al cerrar recursos de SQL", ex);
            }
        }
        return null;
    }

    public boolean actualizarUsuario(Usuario usuario) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DatabaseUtil.getConnection(context);
            String query = "UPDATE Usuario SET USU_Nombre = ?, USU_Apellido1 = ?, USU_Apellido2 = ?, USU_CorreoElectronico = ?, USU_Ciudad = ?, USU_Direccion = ?, USU_Telefono = ?, USU_Fecha_Nacimiento = ? WHERE USU_ID = ?";
            preparedStatement = connection.prepareStatement(query);

            preparedStatement.setString(1, usuario.getNombre());
            preparedStatement.setString(2, usuario.getApellido1());
            preparedStatement.setString(3, usuario.getApellido2());
            preparedStatement.setString(4, usuario.getCorreoElectronico());
            preparedStatement.setString(5, usuario.getCiudad());
            preparedStatement.setString(6, usuario.getDireccion());
            preparedStatement.setString(7, usuario.getTelefono());
            preparedStatement.setDate(8, usuario.getFechaNacimiento());
            preparedStatement.setInt(9, usuario.getId());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Cerrar conexiones y declaraciones aquí
        }
        return false;
    }

}
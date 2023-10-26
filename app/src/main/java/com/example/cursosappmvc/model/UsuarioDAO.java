package com.example.cursosappmvc.model;

import android.content.Context;

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

}
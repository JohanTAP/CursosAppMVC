package com.example.cursosappmvc.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cursosappmvc.R;
import com.example.cursosappmvc.model.database.DatabaseUtil;
import com.google.android.material.textfield.TextInputLayout;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(threadPolicy);
    }

    public void registerUser(View view) {
        Connection connection = null;
        CallableStatement statement = null;

        try {
            TextInputLayout emailTextInputLayout = findViewById(R.id.emailTextInputLayout);
            TextInputLayout passwordTextInputLayout = findViewById(R.id.passwordTextInputLayout);
            TextInputLayout confirmPasswordTextInputLayout = findViewById(R.id.confirmPasswordTextInputLayout);

            String email = ((EditText) findViewById(R.id.emailEditText)).getText().toString();
            String password = ((EditText) findViewById(R.id.passwordEditText)).getText().toString();
            String confirmPassword = ((EditText) findViewById(R.id.confirmPasswordEditText)).getText().toString();

            // Validar longitud del correo
            if (email.length() < 6 || email.length() > 30) {
                emailTextInputLayout.setError("El correo debe tener entre 6 y 30 caracteres");
                return; // No continuar si el correo no cumple con la longitud
            } else {
                emailTextInputLayout.setError(null); // Borrar el mensaje de error
            }

            // Validar longitud de la contraseña
            if (password.equals("")) {
                passwordTextInputLayout.setError("Ingresar una contraseña");
                return; // No continuar si la contraseña está vacía
            } else if (password.length() < 8) {
                passwordTextInputLayout.setError("La contraseña debe tener al menos 8 caracteres");
                return; // No continuar si la contraseña no cumple con la longitud
            } else {
                passwordTextInputLayout.setError(null); // Borrar el mensaje de error
            }

            if (confirmPassword.equals("")) {
                confirmPasswordTextInputLayout.setError("Ingresa la contraseña nuevamente");
                return; // No continuar si la confirmación de contraseña está vacía
            }

            // Verificar si los campos de contraseña y confirmación coinciden
            if (!password.equals(confirmPassword)) {
                confirmPasswordTextInputLayout.setError("Las contraseñas no coinciden");
                return;
            }

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, ingrese correo, contraseña y la confirmación", Toast.LENGTH_LONG).show();
                return;
            }

            // Verificar si el correo ya existe en la base de datos
            if (isEmailExists(email)) {
                emailTextInputLayout.setError("Este nombre de usuario ya está en uso. Elige otro.");
                return;
            }

            // Sanitizar los datos de entrada para prevenir SQL injection
            email = email.replaceAll("[^a-zA-Z0-9@._-]", "");

            // Calcular el hash de la contraseña
            String hashedPassword = hashPassword(password);

            // Añadir registro de depuración para verificar la conexión
            connection = DatabaseUtil.getConnection(this);
            Log.d("Registro", "Conexión exitosa a la base de datos");

            // Añadir registro de depuración para verificar la ejecución del procedimiento almacenado
            statement = connection.prepareCall("{ call insert_usuario(?,?) }");
            statement.setString(1, email);
            statement.setString(2, hashedPassword);
            int rowsInserted = statement.executeUpdate();
            Log.d("Registro", "Filas insertadas: " + rowsInserted);

            if (rowsInserted > 0) {
                Toast.makeText(this, "Registro exitoso. Ya puedes iniciar sesión con tu cuenta.", Toast.LENGTH_LONG).show();
                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            } else {
                Toast.makeText(this, "Datos inválidos", Toast.LENGTH_LONG).show();
            }
        } catch (SQLException e) {
            Log.e("SQL_ERROR", "Error en la operación de BD: " + e.getMessage());
            Toast.makeText(this, "Error en el registro", Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                Toast.makeText(this, "Error al cerrar conexión", Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean isEmailExists(String email) {
        Connection connection = null;
        CallableStatement statement = null;

        try {
            connection = DatabaseUtil.getConnection(this);
            statement = connection.prepareCall("{ call check_email_exists(?, ?) }");
            statement.setString(1, email);
            statement.registerOutParameter(2, Types.INTEGER);

            statement.execute();

            int emailExists = statement.getInt(2);

            return emailExists == 1;
        } catch (SQLException e) {
            Log.e("SQL_ERROR", "Error al verificar el correo en BD: " + e.getMessage());
            return false;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                Log.e("SQL_ERROR", "Error al cerrar conexión: " + ex.getMessage());
            }
        }
    }

    public String hashPassword(String password) {
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
            Log.e("Hashing Error", "Error al calcular el hash de la contraseña: " + e.getMessage());
            return null;
        }
    }
}
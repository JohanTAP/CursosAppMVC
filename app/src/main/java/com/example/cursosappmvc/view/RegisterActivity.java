package com.example.cursosappmvc.view;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cursosappmvc.R;
import com.example.cursosappmvc.model.database.DatabaseUtil;
import com.google.android.material.textfield.TextInputLayout;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void registerUser(View view) {
        new UserRegistrationTask().execute();
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

    private boolean registerNewUser(String email, String hashedPassword) {
        Connection connection = null;
        CallableStatement statement = null;

        try {
            connection = DatabaseUtil.getConnection(this);
            statement = connection.prepareCall("{ call insert_usuario(?,?) }");
            statement.setString(1, email);
            statement.setString(2, hashedPassword);

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            Log.e("SQL_ERROR", "Error al insertar el usuario en la BD: " + e.getMessage());
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
                Log.e("SQL_ERROR", "Error al cerrar la conexión de BD: " + ex.getMessage());
            }
        }
    }

    private void sendEmail(final String email, final String subject, final String messageBody, EmailSendCallback callback) {
        ExecutorService emailExecutor = Executors.newSingleThreadExecutor();
        emailExecutor.submit(() -> {
            try {
                Message message = createEmailMessage(email, subject, messageBody);
                Transport.send(message);
                callback.onSuccess(); // Notificar éxito
            } catch (MessagingException e) {
                Log.e("EmailError", "Error al enviar correo electrónico: " + e.getMessage(), e);
                callback.onFailure(e.getMessage()); // Notificar fracaso
            } catch (Exception e) {
                Log.e("EmailError", "Error general al enviar correo electrónico: " + e.getMessage(), e);
                callback.onFailure(e.getMessage());
            }
        });
        emailExecutor.shutdown();
    }

    private Message createEmailMessage(String email, String subject, String messageBody) throws MessagingException {
        final String username = "cursosappmvc@gmail.com";
        final String password = "mxbpkunouafprqbh";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "*");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        // Activar el modo de depuración
        session.setDebug(true);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
        message.setSubject(subject);
        message.setText(messageBody);
        return message;
    }

    public interface EmailSendCallback {
        void onSuccess();

        void onFailure(String errorMessage);
    }

    private class UserRegistrationTask extends AsyncTask<Void, Void, String> {
        private String email;
        private String password;
        private String confirmPassword;
        private TextInputLayout emailTextInputLayout;
        private TextInputLayout passwordTextInputLayout;
        private TextInputLayout confirmPasswordTextInputLayout;

        @Override
        protected void onPreExecute() {
            emailTextInputLayout = findViewById(R.id.emailTextInputLayout);
            passwordTextInputLayout = findViewById(R.id.passwordTextInputLayout);
            confirmPasswordTextInputLayout = findViewById(R.id.confirmPasswordTextInputLayout);

            email = ((EditText) findViewById(R.id.emailEditText)).getText().toString();
            password = ((EditText) findViewById(R.id.passwordEditText)).getText().toString();
            confirmPassword = ((EditText) findViewById(R.id.confirmPasswordEditText)).getText().toString();

            // Clear previous errors
            emailTextInputLayout.setError(null);
            passwordTextInputLayout.setError(null);
            confirmPasswordTextInputLayout.setError(null);
        }

        @Override
        protected String doInBackground(Void... voids) {
            String validationError = validateInputs();
            if (validationError != null) {
                return validationError;
            }

            if (isEmailExists(email)) {
                return "Este nombre de usuario ya está en uso. Elige otro.";
            }

            String hashedPassword = hashPassword(password);
            boolean isRegistered = registerNewUser(email, hashedPassword);
            return isRegistered ? "success" : "Error al registrar el usuario.";
        }

        private String validateInputs() {
            // Validación del formato de correo
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return "El formato del correo electrónico no es válido.";
            }

            // Validar longitud del correo
            if (email.length() < 6 || email.length() > 30) {
                return "El correo debe tener entre 6 y 30 caracteres.";
            }

            // Validar longitud de la contraseña
            if (password.isEmpty()) {
                return "Ingresar una contraseña.";
            } else if (password.length() < 8) {
                return "La contraseña debe tener al menos 8 caracteres.";
            }

            // Verificar si los campos de contraseña y confirmación coinciden
            if (!password.equals(confirmPassword)) {
                return "Las contraseñas no coinciden.";
            }

            // No hay errores de validación
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if ("success".equals(result)) {
                Toast.makeText(RegisterActivity.this, "Registro exitoso. Ya puedes iniciar sesión con tu cuenta.", Toast.LENGTH_LONG).show();

                // Actualiza aquí con el callback
                sendEmail(email, "Confirmación de Registro", "Gracias por registrarte en nuestra app.", new EmailSendCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i("Email", "Correo enviado exitosamente.");
                        // Aquí podrías hacer algo más, como cerrar la actividad
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e("Email", "Error al enviar correo: " + errorMessage);
                        // Aquí podrías informar al usuario del fallo
                    }
                });

                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            } else {
                Toast.makeText(RegisterActivity.this, result, Toast.LENGTH_LONG).show();
            }
        }
    }
}
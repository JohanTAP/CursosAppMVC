package com.example.cursosappmvc.view;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cursosappmvc.R;
import com.example.cursosappmvc.model.database.DatabaseUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;
import java.util.Calendar;
import java.util.Locale;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TIMER_STARTED_AT = "timer_started_at";
    private static final String TIMER_TIME_LEFT = "timer_time_left";
    private static final String HAS_GENERATED_CODE = "has_generated_code";
    private static final String GENERATED_CODE = "generated_code";
    private final long blockRequestButtonUntil = 0;
    private boolean isRequestButtonEnabled = true;
    private EditText emailEditText;
    private Button requestResetButton;
    private EditText resetCodeEditText;
    private Button confirmCodeButton;
    private TextView timerTextView;
    private boolean isTimeExpired = false;
    private String generatedCode;
    private long timeLeftInMillis = 0;
    private boolean hasGeneratedCode = false;
    private boolean isCodeCopied = false;
    private CountDownTimer countDownTimer;
    private long timerStartedAt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        emailEditText = findViewById(R.id.emailEditText);
        requestResetButton = findViewById(R.id.requestResetButton);
        resetCodeEditText = findViewById(R.id.resetCodeEditText);
        confirmCodeButton = findViewById(R.id.confirmCodeButton);
        timerTextView = findViewById(R.id.countdownTextView);
        loadTimeLeftMillis();
        if (timeLeftInMillis > 0) {
            long currentTime = System.currentTimeMillis();
            if (timerStartedAt != 0) {
                long timePassed = currentTime - timerStartedAt;
                timeLeftInMillis -= timePassed;
            }

            if (timeLeftInMillis <= 0) {
                timeLeftInMillis = 0;
            } else {
                startCountdownTimer(timeLeftInMillis);
            }
        }

        requestResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRequestButtonEnabled) {
                    String emailAddress = emailEditText.getText().toString();
                    if (!isTimeExpired) {
                        if (!hasGeneratedCode) {
                            new VerificarCorreoTask().execute(emailAddress);
                        } else {
                            mostrarCodigoRestablecimiento();
                        }
                    } else {
                        showMessage("El tiempo ha expirado. Solicite un nuevo código de restablecimiento.");
                    }
                } else {
                    showMessage("El botón de solicitud está bloqueado temporalmente. Prueba de nuevo más tarde.");
                }
            }
        });

        confirmCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isTimeExpired) {
                    if (hasGeneratedCode) {
                        String enteredCode = resetCodeEditText.getText().toString();
                        if (enteredCode.equals(generatedCode)) {
                            mostrarDialogCambioContrasena();
                        } else {
                            showMessage("Código incorrecto. Por favor, verifíquelo e intente nuevamente.");
                        }
                    } else {
                        showMessage("Solicite un código de restablecimiento primero.");
                    }
                } else {
                    showMessage("El tiempo ha expirado. Solicite un nuevo código de restablecimiento.");
                }
            }
        });

        if (timeLeftInMillis <= 0) {
            timeLeftInMillis = 300000; // Establecer un tiempo predeterminado si no hay ninguno
        }

        if (savedInstanceState != null) {
            timerStartedAt = savedInstanceState.getLong(TIMER_STARTED_AT, 0);
            timeLeftInMillis = savedInstanceState.getLong(TIMER_TIME_LEFT, timeLeftInMillis);
        }

        if (timerStartedAt != 0) {
            long currentTime = System.currentTimeMillis();
            long timePassed = currentTime - timerStartedAt;
            timeLeftInMillis -= timePassed;
            if (timeLeftInMillis <= 0) {
                timeLeftInMillis = 0;
            } else {
                startCountdownTimer(timeLeftInMillis);
            }
        } else {
            startCountdownTimer(timeLeftInMillis);
        }
        // Verificar si ya hay un código generado
        if (getSavedGeneratedCodeState()) {
            hasGeneratedCode = true;
            generatedCode = getSavedGeneratedCode();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveTimeLeftMillis();
        // Guardar el estado del código generado
        saveGeneratedCodeState(hasGeneratedCode);
        if (hasGeneratedCode) {
            saveGeneratedCode(generatedCode);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTimeLeftMillis();
        if (timeLeftInMillis <= 0) {
            enableFunctionality();
        } else {
            if (timeLeftInMillis > 0) {
                startCountdownTimer(timeLeftInMillis);
            }
        }
    }

    private void loadTimeLeftMillis() {
        SharedPreferences preferences = getSharedPreferences("timer_prefs", MODE_PRIVATE);
        timeLeftInMillis = preferences.getLong("time_left_millis", 0);
    }

    private void enableFunctionality() {
        isRequestButtonEnabled = true;
    }

    private void saveTimeLeftMillis() {
        SharedPreferences preferences = getSharedPreferences("timer_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("time_left_millis", timeLeftInMillis);
        editor.apply();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(TIMER_STARTED_AT, timerStartedAt);
        outState.putLong(TIMER_TIME_LEFT, timeLeftInMillis);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences preferences = getSharedPreferences("timer_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(TIMER_STARTED_AT, timerStartedAt);
        editor.putLong(TIMER_TIME_LEFT, timeLeftInMillis);
        editor.apply();
    }

    private void startCountdownTimer(long durationInMillis) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timerStartedAt = System.currentTimeMillis();
        countDownTimer = new CountDownTimer(durationInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long currentTime = System.currentTimeMillis();
                if (currentTime >= blockRequestButtonUntil) {
                }
                updateTimerTextView(millisUntilFinished);
                timeLeftInMillis = millisUntilFinished;
                saveTimeLeftMillis();
            }

            @Override
            public void onFinish() {
                isTimeExpired = true;
                isRequestButtonEnabled = true;
                timerTextView.setTextColor(Color.RED);
                enableFunctionality();
                updateTimerTextView(0);
            }
        }.start();
    }

    private void updateTimerTextView(long millisUntilFinished) {
        long minutes = (millisUntilFinished / 1000) / 60;
        long seconds = (millisUntilFinished / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerTextView.setText(timeLeftFormatted);
    }

    private void mostrarCodigoRestablecimiento() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordActivity.this);
        builder.setTitle("Código de Restablecimiento");
        builder.setMessage("Tu código para restablecer es:\n\n" + generatedCode);
        builder.setPositiveButton("Copiar Código", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!isCodeCopied) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Código de Restablecimiento", generatedCode);
                    clipboard.setPrimaryClip(clip);
                    showMessage("Código copiado al portapapeles");
                    isCodeCopied = true;
                } else {
                    showMessage("El código ha sido copiado anteriormente.");
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showMessage("Proceso de restablecimiento cancelado");
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void mostrarDialogCambioContrasena() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordActivity.this);
        builder.setTitle("Cambio de Contraseña");
        builder.setMessage("Ingrese su nueva contraseña:");
        final EditText newPasswordEditText = new EditText(ForgotPasswordActivity.this);
        newPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(newPasswordEditText);
        builder.setPositiveButton("Siguiente", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String newPassword = newPasswordEditText.getText().toString();
                dialog.dismiss();
                mostrarDialogConfirmacionContrasena(newPassword);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showMessage("Cambio de contraseña cancelado");
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void mostrarDialogConfirmacionContrasena(final String newPassword) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordActivity.this);
        builder.setTitle("Confirmar Contraseña");
        builder.setMessage("Confirme su nueva contraseña:");
        final EditText confirmPasswordEditText = new EditText(ForgotPasswordActivity.this);
        confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(confirmPasswordEditText);
        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String confirmPassword = confirmPasswordEditText.getText().toString();
                if (newPassword.equals(confirmPassword)) {
                    // Inicia la tarea asincrónica para actualizar la contraseña
                    new UpdatePasswordTask().execute(newPassword);
                } else {
                    showMessage("Las contraseñas no coinciden. Por favor, inténtelo de nuevo.");
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showMessage("Cambio de contraseña cancelado");
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private boolean correoExisteEnBaseDeDatos(String email) {
        Connection connection = null;
        CallableStatement statement = null;
        try {
            connection = DatabaseUtil.getConnection(ForgotPasswordActivity.this);
            statement = connection.prepareCall("{ call check_email_exists(?, ?) }");
            statement.setString(1, email);
            statement.registerOutParameter(2, Types.INTEGER);
            statement.execute();
            int emailExists = statement.getInt(2);
            return emailExists == 1;
        } catch (SQLException | java.sql.SQLException e) {
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
            } catch (SQLException | java.sql.SQLException ex) {
                Log.e("SQL_ERROR", "Error al cerrar conexión: " + ex.getMessage());
            }
        }
    }

    private void guardarCodigoTemporalEnBaseDeDatos(String email, String codigoTemporal) {
        Connection connection = null;
        CallableStatement statement = null;
        try {
            connection = DatabaseUtil.getConnection(ForgotPasswordActivity.this);
            statement = connection.prepareCall("{ call guardar_codigo_temporal(?, ?, ?) }");
            statement.setString(1, email);
            statement.setString(2, codigoTemporal);
            statement.setTimestamp(3, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
            statement.execute();
        } catch (SQLException | java.sql.SQLException e) {
            Log.e("SQL_ERROR", "Error al guardar el código temporal en BD: " + e.getMessage());
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException | java.sql.SQLException ex) {
                Log.e("SQL_ERROR", "Error al cerrar conexión: " + ex.getMessage());
            }
        }
    }

    private void showMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String generarTokenTemporal() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[16];
        secureRandom.nextBytes(token);
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=<>?";
        StringBuilder tokenBuilder = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            int index = secureRandom.nextInt(caracteres.length());
            tokenBuilder.append(caracteres.charAt(index));
        }
        return tokenBuilder.toString();
    }

    private void saveGeneratedCodeState(boolean hasGeneratedCode) {
        SharedPreferences preferences = getSharedPreferences("timer_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(HAS_GENERATED_CODE, hasGeneratedCode);
        editor.apply();
    }

    private boolean getSavedGeneratedCodeState() {
        SharedPreferences preferences = getSharedPreferences("timer_prefs", MODE_PRIVATE);
        return preferences.getBoolean(HAS_GENERATED_CODE, false);
    }

    private void saveGeneratedCode(String code) {
        SharedPreferences preferences = getSharedPreferences("timer_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(GENERATED_CODE, code);
        editor.apply();
    }

    private String getSavedGeneratedCode() {
        SharedPreferences preferences = getSharedPreferences("timer_prefs", MODE_PRIVATE);
        return preferences.getString(GENERATED_CODE, "");
    }

    private Boolean actualizarContrasenaEnBD(final String newPassword) {
        final String hashedPassword = hashPassword(newPassword);
        if (hashedPassword != null) {
            // Debes realizar la conexión a la base de datos aquí y luego llamar a un procedimiento almacenado para actualizar la contraseña
            Connection connection = null;
            CallableStatement statement = null;
            try {
                connection = DatabaseUtil.getConnection(ForgotPasswordActivity.this);
                statement = connection.prepareCall("{ call actualizar_contrasena(?, ?) }");
                statement.setString(1, emailEditText.getText().toString());
                statement.setString(2, hashedPassword);
                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    showMessage("Contraseña actualizada con éxito");
                    isCodeCopied = false;
                    return true; // Actualización exitosa
                } else {
                    showMessage("Error al actualizar la contraseña");
                }
            } catch (SQLException | java.sql.SQLException e) {
                Log.e("SQL_ERROR", "Error al actualizar la contraseña en BD: " + e.getMessage());
                showMessage("Error al actualizar la contraseña");
            } finally {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException | java.sql.SQLException ex) {
                    Log.e("SQL_ERROR", "Error al cerrar conexión: " + ex.getMessage());
                }
            }
        } else {
            showMessage("Error al calcular el hash de la contraseña");
        }
        return false; // Actualización fallida
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

    private class VerificarCorreoTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String email = params[0];
            return correoExisteEnBaseDeDatos(email);
        }

        @Override
        protected void onPostExecute(Boolean correoExiste) {
            if (correoExiste) {
                generatedCode = generarTokenTemporal();
                mostrarCodigoRestablecimiento();
                new GuardarCodigoTemporalTask().execute(emailEditText.getText().toString(), generatedCode);
                hasGeneratedCode = true;
            } else {
                showMessage("Correo electrónico no encontrado en la base de datos");
            }
        }
    }

    private class GuardarCodigoTemporalTask extends AsyncTask<String, String, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String email = params[0];
            String codigoTemporal = params[1];
            guardarCodigoTemporalEnBaseDeDatos(email, codigoTemporal);
            return null;
        }
    }

    private class UpdatePasswordTask extends AsyncTask<String, String, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String newPassword = params[0];
            boolean success = false;  // Inicializa con un valor predeterminado
            if (newPassword != null) {
                success = actualizarContrasenaEnBD(newPassword);
            }
            // Llamar a publishProgress con el mensaje de progreso
            publishProgress(success ? "Contraseña cambiada con éxito" : "Error al cambiar la contraseña. Por favor, inténtelo de nuevo.");
            return success;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                // La contraseña se actualizó con éxito, muestra un Toast en el hilo principal.
                showMessage("Contraseña cambiada con éxito");
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Ocurrió un error al actualizar la contraseña, muestra un Toast con el mensaje de error.
                showMessage("Error al cambiar la contraseña. Por favor, inténtelo de nuevo.");
            }
        }
    }
}
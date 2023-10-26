package com.example.cursosappmvc.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cursosappmvc.MainActivity;
import com.example.cursosappmvc.R;
import com.example.cursosappmvc.controller.UsuarioController;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private final UsuarioController usuarioController = new UsuarioController(this);
    private EditText usernameEditText;
    private EditText passwordEditText;
    private TextInputLayout passwordTextInputLayout;
    private TextInputLayout usernameTextInputLayout;
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Se llama cuando cambia el texto en los EditText
            // Borra los errores al editar los campos
            usernameTextInputLayout.setError(null);
            passwordTextInputLayout.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordTextInputLayout = findViewById(R.id.passwordTextInputLayout);
        usernameTextInputLayout = findViewById(R.id.usernameTextInputLayout);

        TextView registerLinkTextView = findViewById(R.id.registerLinkTextView);
        registerLinkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inicia la actividad de registro
                Intent registerIntent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(registerIntent);
            }
        });

        // Agrega TextWatcher para borrar errores al editar los campos
        usernameEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(textWatcher);

        TextView forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Agrega aquí la lógica para abrir la actividad ForgotPasswordActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    public void loginUser(View view) {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (username.isEmpty()) {
            usernameTextInputLayout.setError("Ingresa un correo electrónico.");
            return;
        } else {
            usernameTextInputLayout.setError(null); // Borrar el mensaje de error
        }

        if (password.isEmpty()) {
            passwordTextInputLayout.setError("La contraseña no puede estar en blanco");
            return;
        } else {
            passwordTextInputLayout.setError(null); // Borrar el mensaje de error
        }

        if (username.isEmpty() || password.isEmpty()) {
            showToast("Por favor, completa todos los campos");
            return;
        }

        // Sanitizar los datos de entrada para prevenir SQL injection
        username = username.replaceAll("[^a-zA-Z0-9@._-]", "");

        Handler handler = new Handler(new Handler.Callback() {
            public boolean handleMessage(Message message) {
                if (message.what == 1) {
                    int userId = (int) message.obj;
                    showToast("Inicio de sesión exitoso");

                    // Guardar estado de inicio de sesión y el ID del usuario
                    SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.putInt("userId", userId);
                    editor.apply();

                    finish();
                } else if (message.what == 0) {
                    usernameTextInputLayout.setError("No pudimos encontrar la combinación de correo electrónico y contraseña.");
                    passwordTextInputLayout.setError("Verifica tus datos de inicio de sesión.");
                } else {
                    showToast(message.obj.toString());
                }
                return true;
            }
        });


        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new VerificarCredencialesTask(username, password, handler));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private class VerificarCredencialesTask implements Runnable {
        private final String username;
        private final String password;
        private final Handler handler;

        public VerificarCredencialesTask(String username, String password, Handler handler) {
            this.username = username;
            this.password = password;
            this.handler = handler;
        }

        public void run() {
            int userId = usuarioController.iniciarSesion(username, password);
            Message message;
            if (userId > 0) {
                message = handler.obtainMessage(1, userId);
            } else {
                message = handler.obtainMessage(0);
            }
            handler.sendMessage(message);
        }


    }

}
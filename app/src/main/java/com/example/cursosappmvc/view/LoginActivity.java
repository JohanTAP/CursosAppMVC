package com.example.cursosappmvc.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cursosappmvc.MainActivity;
import com.example.cursosappmvc.R;
import com.example.cursosappmvc.controller.UsuarioController;
import com.example.cursosappmvc.util.SharedPrefManager;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private final UsuarioController usuarioController = new UsuarioController(this);
    private EditText usernameEditText;
    private EditText passwordEditText;
    private TextInputLayout passwordTextInputLayout;
    private TextInputLayout usernameTextInputLayout;
    private int cursoId = -1;
    private int loginAttempts = 0; // Contador de intentos de inicio de sesión
    private Button loginButton;
    private TextView registerLinkTextView;
    private ProgressBar loginProgressBar;

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

        // Inicialización de los componentes de la UI
        loginButton = findViewById(R.id.loginButton);
        registerLinkTextView = findViewById(R.id.registerLinkTextView);
        loginProgressBar = findViewById(R.id.loginProgressBar);

        cursoId = getIntent().getIntExtra("cursoId", -1); // Guarda el cursoId

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordTextInputLayout = findViewById(R.id.passwordTextInputLayout);
        usernameTextInputLayout = findViewById(R.id.usernameTextInputLayout);

        TextView registerLinkTextView = findViewById(R.id.registerLinkTextView);
        registerLinkTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inicia la actividad de registro
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
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
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Recuperar el número de intentos fallidos
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(LoginActivity.this);
        loginAttempts = sharedPrefManager.getLoginAttempts();

    }

    public void loginUser(View view) {

        if (loginAttempts >= 3) {
            showToast("Demasiados intentos fallidos. Por favor, contactanos a soporte@adventistas.org.");
            return;
        }

        // Codigo sanitizado. Datos de entrada con expresiones regulares para prevenir inyeccion SQL
        String username = usernameEditText.getText().toString().replaceAll("[^a-zA-Z0-9@._-]", "");
        String password = passwordEditText.getText().toString();

        if (username.isEmpty()) {
            usernameTextInputLayout.setError("Ingresa un correo electrónico.");
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            usernameTextInputLayout.setError("El formato del correo electrónico no es válido.");
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

        // Desactivar botones y mostrar el indicador de carga
        setLoginUIState(false);

        Handler handler = new Handler(new Handler.Callback() {
            public boolean handleMessage(Message message) {

                if (message.what == 1) {

                    int userId = (int) message.obj;
                    showToast("Inicio de sesión exitoso");

                    // Usar SharedPrefManager para guardar el estado de inicio de sesión y el ID del usuario
                    SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(LoginActivity.this);
                    sharedPrefManager.setUserLoggedIn(true);
                    sharedPrefManager.setUserId(userId);
                    loginAttempts = 0; // Reiniciar el contador de intentos de inicio de sesión
                    Log.d("LoginActivity", "UsuarioId guardado: " + userId);

                    // Agregar un log adicional para verificar inmediatamente el valor guardado
                    int storedUserId = sharedPrefManager.getUserId();
                    Log.d("LoginActivity", "UsuarioId recuperado inmediatamente después de guardar: " + storedUserId);

                    // Suponiendo que el inicio de sesión fue exitoso...
                    sharedPrefManager.setShouldRefreshLecciones(true);

                    // Reactivar botones y ocultar el indicador de carga
                    setLoginUIState(true);

                    // Inicia LeccionActivity pasando el cursoId
                    if (cursoId != -1) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("cursoId", cursoId);
                        startActivity(intent);
                    }

                    finish();
                } else if (message.what == 0) {
                    usernameTextInputLayout.setError("No pudimos encontrar la combinación de correo electrónico y contraseña.");
                    passwordTextInputLayout.setError("Verifica tus datos de inicio de sesión.");
                    loginAttempts++;
                    SharedPrefManager.setLoginAttempts(loginAttempts);
                    if (loginAttempts >= 3) {
                        showToast("Demasiados intentos fallidos. Por favor, contactanos a soporte@adventistas.org.");
                        loginProgressBar.setVisibility(View.GONE);
                    } else {
                        usernameTextInputLayout.setError("No pudimos encontrar la combinación de correo electrónico y contraseña.");
                        passwordTextInputLayout.setError("Verifica tus datos de inicio de sesión.");
                        // Reactivar botones y ocultar el indicador de carga
                        setLoginUIState(true);
                    }
                } else {
                    showToast(message.obj.toString());
                }
                return true;
            }
        });

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new VerificarCredencialesTask(username, password, handler));
    }

    private void setLoginUIState(boolean enabled) {
        loginButton.setEnabled(enabled);
        registerLinkTextView.setEnabled(enabled);
        loginProgressBar.setVisibility(enabled ? View.GONE : View.VISIBLE);
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
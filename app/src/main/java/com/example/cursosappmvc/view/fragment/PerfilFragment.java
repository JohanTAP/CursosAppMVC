package com.example.cursosappmvc.view.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cursosappmvc.R;
import com.example.cursosappmvc.controller.UsuarioController;
import com.example.cursosappmvc.model.Usuario;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PerfilFragment extends Fragment {

    private EditText nombreEditText, apellido1EditText, apellido2EditText, emailEditText, direccionEditText, telefonoEditText, fechaNacimientoEditText;
    private AutoCompleteTextView autoCompleteCiudad;
    private UsuarioController usuarioController;
    private boolean isEditMode = false;
    private MaterialButton buttonEditarPerfil;
    private int userId; // Variable para almacenar el ID del usuario

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        // Inicialización de componentes
        nombreEditText = view.findViewById(R.id.editTextNombre);
        apellido1EditText = view.findViewById(R.id.editTextApellido1);
        apellido2EditText = view.findViewById(R.id.editTextApellido2);
        emailEditText = view.findViewById(R.id.editTextCorreoElectronico);
        direccionEditText = view.findViewById(R.id.editTextDireccion);
        telefonoEditText = view.findViewById(R.id.editTextTelefono);
        // Configuración del EditText para la fecha de nacimiento
        fechaNacimientoEditText = view.findViewById(R.id.editTextFechaNacimiento);
        fechaNacimientoEditText.setOnClickListener(v -> mostrarDatePickerDialog());

        // Configuración del AutoCompleteTextView para la ciudad
        autoCompleteCiudad = view.findViewById(R.id.autoCompleteCiudad);
        ArrayAdapter<CharSequence> adapterCiudad = ArrayAdapter.createFromResource(getContext(),
                R.array.ciudades_array, android.R.layout.simple_dropdown_item_1line);
        autoCompleteCiudad.setAdapter(adapterCiudad);
        autoCompleteCiudad.setEnabled(false); // Inicialmente desactivado

        buttonEditarPerfil = view.findViewById(R.id.buttonEditarPerfil);
        usuarioController = new UsuarioController(getContext());

        // Recuperar y usar el ID del usuario pasado como argumento
        Bundle bundle = getArguments();
        if (bundle != null) {
            userId = bundle.getInt("userId", -1); // Usar -1 como valor por defecto
            if (userId != -1) {
                cargarDatosUsuario(userId);
            } else {
                Log.e("PerfilFragment", "ID de usuario no válido o no proporcionado");
            }
        }

        buttonEditarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEditMode();
            }
        });

        return view;
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        setEditMode(isEditMode);
        if (isEditMode) {
            buttonEditarPerfil.setText("Guardar");
        } else {
            mostrarDialogoConfirmacion();
        }
    }

    private void mostrarDialogoConfirmacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Confirmar Cambios");

        // Campo de texto para ingresar la contraseña
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Introduce tu contraseña");
        builder.setView(input);

        // Botón para confirmar
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = input.getText().toString();
                validarContraseña(password);
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void validarContraseña(String password) {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            boolean isValid = usuarioController.verificarContraseñaPorId(userId, password);
            handler.post(() -> {
                if (isValid) {
                    guardarCambios();
                } else {
                    Toast.makeText(getContext(), "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setEditMode(boolean enabled) {
        nombreEditText.setEnabled(enabled);
        apellido1EditText.setEnabled(enabled);
        apellido2EditText.setEnabled(enabled);
        emailEditText.setEnabled(enabled);
        autoCompleteCiudad.setEnabled(enabled);
        direccionEditText.setEnabled(enabled);
        telefonoEditText.setEnabled(enabled);
        fechaNacimientoEditText.setEnabled(enabled);
    }

    private void guardarCambios() {
        // Recoger datos de los EditTexts
        String nombre = nombreEditText.getText().toString();
        String apellido1 = apellido1EditText.getText().toString();
        String apellido2 = apellido2EditText.getText().toString();
        String email = emailEditText.getText().toString();
        String ciudad = autoCompleteCiudad.getText().toString(); // Obtener la ciudad seleccionada
        String direccion = direccionEditText.getText().toString();
        String telefono = telefonoEditText.getText().toString();
        String fechaNacimiento = fechaNacimientoEditText.getText().toString(); // Obtener la fecha de nacimiento

        // Validación del formato de correo electrónico
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "El formato del correo electrónico no es válido.", Toast.LENGTH_SHORT).show();
            return; // Detener la ejecución si el formato del correo no es válido
        }

        // Convertir la fecha de nacimiento al formato adecuado
        java.sql.Date fechaNacimientoSQL = convertirFechaFormatoSQL(fechaNacimiento);
        if (fechaNacimientoSQL == null) {
            Toast.makeText(getContext(), "Formato de fecha de nacimiento no válido.", Toast.LENGTH_SHORT).show();
            return; // Detener si la conversión falló
        }

        // Crear un objeto Usuario con los datos actualizados
        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setId(userId);
        usuarioActualizado.setNombre(nombre);
        usuarioActualizado.setApellido1(apellido1);
        usuarioActualizado.setApellido2(apellido2);
        usuarioActualizado.setCorreoElectronico(email);
        usuarioActualizado.setCiudad(ciudad);
        usuarioActualizado.setDireccion(direccion);
        usuarioActualizado.setTelefono(telefono);
        usuarioActualizado.setFechaNacimiento(fechaNacimientoSQL); // Establecer la fecha de nacimiento

        // Llamar a un método en UsuarioController para actualizar la base de datos
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean resultado = usuarioController.actualizarUsuario(usuarioActualizado);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (resultado) {
                            Log.d("PerfilFragment", "Datos del usuario actualizados correctamente.");
                            Toast.makeText(getContext(), "Datos actualizados con éxito", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("PerfilFragment", "Error al actualizar los datos del usuario.");
                            Toast.makeText(getContext(), "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }


    private void cargarDatosUsuario(int userId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Usuario usuario = usuarioController.obtenerDetallesUsuario(userId);

                if (usuario != null) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            nombreEditText.setText(usuario.getNombre());
                            apellido1EditText.setText(usuario.getApellido1());
                            apellido2EditText.setText(usuario.getApellido2());
                            emailEditText.setText(usuario.getCorreoElectronico());
                            direccionEditText.setText(usuario.getDireccion());
                            telefonoEditText.setText(usuario.getTelefono());
                            autoCompleteCiudad.setText(usuario.getCiudad());

                            // Actualiza el AutoCompleteTextView de la ciudad
                           // seleccionarCiudadEnAutoComplete(usuario.getCiudad());

                            // Actualiza el EditText de la fecha de nacimiento si es que existe
                            // Asumiendo que tienes un getter para la fecha de nacimiento en tu clase Usuario
                            if (usuario.getFechaNacimiento() != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                fechaNacimientoEditText.setText(sdf.format(usuario.getFechaNacimiento()));
                            }
                        }
                    });
                } else {
                    Log.e("PerfilFragment", "No se encontró usuario con ID: " + userId);
                }
            }
        }).start();
    }

    private void mostrarDatePickerDialog() {
        Calendar calendarioActual = Calendar.getInstance();
        int añoActual = calendarioActual.get(Calendar.YEAR);
        int mesActual = calendarioActual.get(Calendar.MONTH);
        int diaActual = calendarioActual.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    Calendar fechaSeleccionada = Calendar.getInstance();
                    fechaSeleccionada.set(year, monthOfYear, dayOfMonth);

                    if (fechaSeleccionada.after(calendarioActual)) {
                        Toast.makeText(getContext(), "La fecha de nacimiento no puede ser futura", Toast.LENGTH_SHORT).show();
                    } else {
                        String fechaTexto = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        fechaNacimientoEditText.setText(fechaTexto);
                    }
                }, añoActual, mesActual, diaActual);

        datePickerDialog.getDatePicker().setMaxDate(calendarioActual.getTimeInMillis());
        datePickerDialog.show();
    }


    private java.sql.Date convertirFechaFormatoSQL(String fecha) {
        SimpleDateFormat formatoEntrada = new SimpleDateFormat("dd/MM/yyyy");
        try {
            java.util.Date fechaParsed = formatoEntrada.parse(fecha);
            return new java.sql.Date(fechaParsed.getTime());
        } catch (java.text.ParseException e) {
            Log.e("PerfilFragment", "Error al convertir la fecha: " + fecha, e);
            return null;
        }
    }

    // Método actualizado para seleccionar la ciudad en el AutoCompleteTextView
    private void seleccionarCiudadEnAutoComplete(String ciudad) {
        autoCompleteCiudad.post(() -> {
            int position = ((ArrayAdapter<String>) autoCompleteCiudad.getAdapter()).getPosition(ciudad);
            if (position >= 0) {
                autoCompleteCiudad.setText(ciudad, false);
            }
        });
    }
}
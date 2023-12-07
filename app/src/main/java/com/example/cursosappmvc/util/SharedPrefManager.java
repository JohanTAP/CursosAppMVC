package com.example.cursosappmvc.util;

import static com.example.cursosappmvc.model.DetalleLeccionDAO.context;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    private static final String SHARED_PREF_NAME = "MyAppPrefs";
    private static SharedPrefManager instance;
    private static SharedPreferences prefs;

    private SharedPrefManager(Context context) {
        // Inicializa las preferencias aquí para asegurarte de que el contexto no sea null.
        this.prefs = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context.getApplicationContext());
        }
        return instance;
    }

    public int getLoginAttempts() {
        return prefs.getInt("login_attempts", 0);
    }

    public static void setLoginAttempts(int attempts) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("login_attempts", attempts);
        editor.apply();
    }

    // Método para establecer el estado de inicio de sesión del usuario
    public void setUserLoggedIn(boolean isLoggedIn) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.apply();
    }

    public int getUserId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("userId", -1); // retorna -1 si no hay un ID almacenado
    }

    // Método para establecer el ID del usuario
    public void setUserId(int userId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("userId", userId);
        editor.apply();
    }

    // Método para establecer el indicador de actualización
    public void setShouldRefreshLecciones(boolean shouldRefresh) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("shouldRefreshLecciones", shouldRefresh);
        editor.apply();
    }

}
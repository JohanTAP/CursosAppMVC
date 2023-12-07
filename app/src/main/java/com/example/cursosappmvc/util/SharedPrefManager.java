package com.example.cursosappmvc.util;

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

    // Método revisado para obtener el ID del usuario
    public int getUserId() {
        return prefs.getInt("userId", -1); // Utiliza la instancia 'prefs'
    }

    // Método revisado para establecer el ID del usuario
    public void setUserId(int userId) {
        SharedPreferences.Editor editor = prefs.edit(); // Utiliza la instancia 'prefs'
        editor.putInt("userId", userId);
        editor.apply();
    }

    // Método revisado para establecer el estado de inicio de sesión del usuario
    public void setUserLoggedIn(boolean isLoggedIn) {
        SharedPreferences.Editor editor = prefs.edit(); // Utiliza la instancia 'prefs'
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.apply();
    }

    // Método revisado para establecer el indicador de actualización
    public void setShouldRefreshLecciones(boolean shouldRefresh) {
        SharedPreferences.Editor editor = prefs.edit(); // Utiliza la instancia 'prefs'
        editor.putBoolean("shouldRefreshLecciones", shouldRefresh);
        editor.apply();
    }

}
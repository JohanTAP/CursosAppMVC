package com.example.cursosappmvc.model.database;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseUtil {

    private static final String DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String TAG = "DatabaseUtil";  // Etiqueta para los logs
    private static Connection connection = null;

    public static synchronized Connection getConnection(Context context) throws SQLException {
        if (connection == null || connection.isClosed()) {
            AssetManager assetManager = context.getAssets();
            try (InputStream inputStream = assetManager.open("config.properties")) {
                Properties properties = new Properties();
                properties.load(inputStream);
                String url = properties.getProperty("url");
                String user = properties.getProperty("user");
                String password = properties.getProperty("password");

                inputStream.close();

                // Asegurarse de que el driver esté cargado
                try {
                    Class.forName(DRIVER);
                } catch (ClassNotFoundException e) {
                    Log.e(TAG, "No se pudo cargar el driver de Oracle", e);  // Error de log
                    throw new SQLException("No se encontró el driver de Oracle", e);
                }

                // Establecer la conexión
                Log.d(TAG, "Intentando conectar a la base de datos...");  // Log de depuración
                connection = DriverManager.getConnection(url, user, password);
                Log.i(TAG, "Conexión establecida.");  // Información de log

            } catch (IOException e) {
                Log.e(TAG, "Error al abrir config.properties", e);  // Error de log
                throw new SQLException("Error al leer el archivo de configuración", e);
            }
        }
        return connection;
    }

    public static synchronized void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            Log.d(TAG, "Cerrando la conexión a la base de datos...");  // Log de depuración
            connection.close();
            connection = null;
            Log.i(TAG, "Conexión cerrada.");  // Información de log
        }
    }

}
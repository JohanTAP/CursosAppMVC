package com.example.cursosappmvc.view.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cursosappmvc.R;
import com.example.cursosappmvc.model.database.DatabaseUtil;

import java.sql.Connection;
import java.sql.SQLException;

public class CursoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Iniciar la tarea asíncrona para probar la conexión a la base de datos
        new DatabaseConnectTask().execute();

        return inflater.inflate(R.layout.fragment_curso, container, false);
    }

    private class DatabaseConnectTask extends AsyncTask<Void, Void, Boolean> {
        private Exception exception = null;

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Connection connection = DatabaseUtil.getConnection(getContext());
                return (connection != null && !connection.isClosed());
            } catch (SQLException e) {
                exception = e;
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isConnected) {
            super.onPostExecute(isConnected);
            if (isConnected) {
                Toast.makeText(getActivity(), "Conexión exitosa", Toast.LENGTH_LONG).show();
            } else {
                String errorMessage = (exception != null) ? exception.getMessage() : "Error al conectar";
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }

}
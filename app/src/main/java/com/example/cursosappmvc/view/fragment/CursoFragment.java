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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cursosappmvc.R;
import com.example.cursosappmvc.controller.CursoAdapter;
import com.example.cursosappmvc.model.Curso;
import com.example.cursosappmvc.model.CursoDAO;
import com.example.cursosappmvc.model.database.DatabaseUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CursoFragment extends Fragment {

    private RecyclerView recyclerView;
    private CursoAdapter cursoAdapter;
    private List<Curso> listaCursos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_curso, container, false);

        recyclerView = view.findViewById(R.id.recycler_cursos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Iniciar la tarea asíncrona para cargar los cursos
        new LoadCursosTask().execute();

        return view;
    }

    private class LoadCursosTask extends AsyncTask<Void, Void, List<Curso>> {
        private Exception exception = null;

        @Override
        protected List<Curso> doInBackground(Void... voids) {
            try {
                return new CursoDAO().obtenerCursos(getContext());
            } catch (Exception e) {
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Curso> cursos) {
            super.onPostExecute(cursos);
            if (cursos != null) {
                cursoAdapter = new CursoAdapter(getContext(), cursos);
                recyclerView.setAdapter(cursoAdapter);
            } else {
                String errorMessage = (exception != null) ? exception.getMessage() : "Error al cargar cursos";
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }
}

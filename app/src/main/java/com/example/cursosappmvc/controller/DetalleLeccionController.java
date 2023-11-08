package com.example.cursosappmvc.controller;

import android.content.Context;
import android.os.AsyncTask;

import com.example.cursosappmvc.model.DetalleLeccionDAO;

public class DetalleLeccionController {

    private Context context;
    private DetalleLeccionDAO detalleLeccionDAO;


    public DetalleLeccionController(Context context) {
        this.context = context;
        this.detalleLeccionDAO = new DetalleLeccionDAO(context);
    }

    public void registrarInicioLeccion(int usuarioId, int leccionId, OnRegistroCompleteListener listener) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                return detalleLeccionDAO.registrarInicioLeccion(usuarioId, leccionId);
            }

            @Override
            protected void onPostExecute(Boolean success) {
                listener.onComplete(success);
            }
        }.execute();
    }

    public void leccionYaIniciada(int usuarioId, int leccionId, DetalleLeccionDAO.OnLeccionIniciadaListener listener) {
        detalleLeccionDAO.leccionYaIniciada(usuarioId, leccionId, listener);
    }

    public interface OnRegistroCompleteListener {
        void onComplete(boolean success);
    }

}
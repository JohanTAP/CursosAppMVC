package com.example.cursosappmvc.controller;

import android.content.Context;

import com.example.cursosappmvc.model.Usuario;
import com.example.cursosappmvc.model.UsuarioDAO;

public class UsuarioController {

    private final UsuarioDAO usuarioDAO;

    public UsuarioController(Context context) {
        this.usuarioDAO = new UsuarioDAO(context);
    }

    public boolean existeUsuarioConCredenciales(String correo, String contrasena) {
        return usuarioDAO.verificarCredenciales(correo, contrasena);
    }

    public int iniciarSesion(String correoElectronico, String contrasena) {
        Usuario usuario = usuarioDAO.buscarPorCorreoYContrasena(correoElectronico, contrasena);
        if (usuario != null) {
            return usuario.getId();
        }
        return -1;
    }

    public boolean registrarUsuario(Usuario usuario) {
        return usuarioDAO.guardar(usuario);
    }

    public boolean restablecerContrasena(String correoElectronico) {
        return usuarioDAO.actualizarTokenRestablecimiento(correoElectronico, generarToken());
    }

    private String generarToken() {
        return String.valueOf(System.currentTimeMillis());
    }
}
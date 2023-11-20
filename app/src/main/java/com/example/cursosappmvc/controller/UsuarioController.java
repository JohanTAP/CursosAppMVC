package com.example.cursosappmvc.controller;

import android.content.Context;
import android.util.Log;

import com.example.cursosappmvc.model.Usuario;
import com.example.cursosappmvc.model.UsuarioDAO;

public class UsuarioController {

    private final UsuarioDAO usuarioDAO;

    public UsuarioController(Context context) {
        this.usuarioDAO = new UsuarioDAO(context);
    }

    public boolean existeUsuarioConCredenciales(String correo, String contrasena) {
        // Convertir la contraseña ingresada a su hash
        String hashedPassword = UsuarioDAO.hashPassword(contrasena);
        return usuarioDAO.verificarCredenciales(correo, hashedPassword);
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

    public Usuario obtenerDetallesUsuario(int userId) {
        Usuario usuario = usuarioDAO.buscarPorId(userId);
        if (usuario == null) {
            Log.e("UsuarioController", "No se encontró usuario con ID: " + userId);
        }
        return usuario;
    }

    public boolean actualizarUsuario(Usuario usuario) {
        return usuarioDAO.actualizarUsuario(usuario);
    }

    public boolean verificarContraseñaPorId(int userId, String contrasena) {
        Usuario usuario = usuarioDAO.buscarPorId(userId);
        if (usuario != null) {
            String storedPasswordHash = usuario.getContrasenaHash();
            String enteredPasswordHash = UsuarioDAO.hashPassword(contrasena);
            return storedPasswordHash.equals(enteredPasswordHash);
        }
        return false;
    }


}
package com.syncup.syncup_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.syncup.syncup_api.domain.Usuario;
import com.syncup.syncup_api.dto.UserRegistrationRequest;
import com.syncup.syncup_api.service.UsuarioService;

import com.syncup.syncup_api.dto.LoginRequest;
import com.syncup.syncup_api.dto.LoginResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    // registrar nuevo usuario
    @PostMapping("/register")
    public ResponseEntity<Usuario> registerUser(@RequestBody UserRegistrationRequest request) {
        Usuario usuarioRegistrado = usuarioService.registrarUsuario(request);
        return new ResponseEntity<>(usuarioRegistrado, HttpStatus.CREATED);
    }

    // endpoint para Iniciar sesión.
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginRequest request) {
        // Llama al servicio para hacer el trabajo
        LoginResponse response = usuarioService.loginUsuario(request);

        // Devuelve el token con un código 200 (OK)
        return ResponseEntity.ok(response);
    }

    // endpoint para verificar si un username (email) ya existe
    @GetMapping("/check-username/{username}")
    public ResponseEntity<Map<String, Boolean>> checkUsernameExists(@PathVariable String username) {
        boolean exists = usuarioService.usernameExists(username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}
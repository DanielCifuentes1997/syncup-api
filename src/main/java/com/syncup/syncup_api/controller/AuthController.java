package com.syncup.syncup_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.syncup.syncup_api.domain.Usuario;
import com.syncup.syncup_api.dto.UserRegistrationRequest;
import com.syncup.syncup_api.service.UsuarioService;

// --- ESTAS SON LAS IMPORTACIONES NUEVAS QUE NECESITAMOS ---
import com.syncup.syncup_api.dto.LoginRequest;
import com.syncup.syncup_api.dto.LoginResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Endpoint para RF-001: Registrar un nuevo usuario.
     * (Este ya lo teníamos)
     */
    @PostMapping("/register")
    public ResponseEntity<Usuario> registerUser(@RequestBody UserRegistrationRequest request) {
        Usuario usuarioRegistrado = usuarioService.registrarUsuario(request);
        return new ResponseEntity<>(usuarioRegistrado, HttpStatus.CREATED);
    }

    // --- ESTE ES EL NUEVO MÉTODO QUE AÑADIMOS ---

    /**
     * Endpoint para RF-001: Iniciar sesión.
     * Se accede vía: POST http://localhost:8080/api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginRequest request) {
        // Llama al servicio para hacer el trabajo
        LoginResponse response = usuarioService.loginUsuario(request);

        // Devuelve el token con un código 200 (OK)
        return ResponseEntity.ok(response);
    }
}
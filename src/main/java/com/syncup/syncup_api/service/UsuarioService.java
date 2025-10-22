package com.syncup.syncup_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.syncup.syncup_api.domain.Usuario;
import com.syncup.syncup_api.dto.UserRegistrationRequest;
import com.syncup.syncup_api.repository.UsuarioRepository;
import com.syncup.syncup_api.dto.LoginRequest;
import com.syncup.syncup_api.dto.LoginResponse;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio para gestionar la lógica de negocio relacionada con los Usuarios,
 * incluyendo autenticación y gestión de tokens.
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrafoSocialService grafoSocialService;

    // Mapa en memoria para almacenar tokens de sesión activos
    private Map<String, String> activeTokens = new HashMap<>();

    /**
     * Registra un nuevo usuario en el sistema.
     */
    public Usuario registrarUsuario(UserRegistrationRequest request) {

        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya existe: " + request.getUsername());
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(request.getUsername());
        nuevoUsuario.setNombre(request.getNombre());
        nuevoUsuario.setPassword(request.getPassword());
        nuevoUsuario.setListaFavoritos(new LinkedList<>());

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        grafoSocialService.agregarUsuario(usuarioGuardado);

        return usuarioGuardado;
    }

    //Autentica a un usuario y genera un token de sesión.
    public LoginResponse loginUsuario(LoginRequest request) {

        Optional<Usuario> usuarioOptional = usuarioRepository.findByUsername(request.getUsername());

        if (usuarioOptional.isEmpty() || !usuarioOptional.get().getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        String token = UUID.randomUUID().toString();
        activeTokens.put(token, request.getUsername());
        return new LoginResponse(token);
    }

    /**
     * Obtiene el nombre de usuario (username) asociado a un token de sesión.
     * @param token El token de sesión.
     * @return El username si el token es válido, o null si no lo es.
     */
    public String getUsernameFromToken(String token) {
        // El token real viene con "Bearer " al inicio, lo limpiamos.
        String cleanToken = token.replace("Bearer ", "");
        return activeTokens.get(cleanToken);
    }

    /**
     * Obtiene la entidad Usuario completa a partir de un token de sesión.
     * @param token El token de sesión.
     * @return El objeto Usuario completo.
     * @throws RuntimeException si el token no es válido o el usuario no se encuentra.
     */
    public Usuario getUserFromToken(String token) {
        String username = getUsernameFromToken(token);
        if (username == null) {
            throw new RuntimeException("Token inválido o sesión expirada");
        }

        return usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado para el token: " + username));
    }
}
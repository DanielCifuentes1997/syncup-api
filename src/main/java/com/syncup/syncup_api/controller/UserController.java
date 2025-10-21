package com.syncup.syncup_api.controller;

import com.syncup.syncup_api.domain.Usuario;
import com.syncup.syncup_api.dto.UserDto;
import com.syncup.syncup_api.repository.UsuarioRepository;
import com.syncup.syncup_api.service.GrafoSocialService;
import com.syncup.syncup_api.service.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controlador para gestionar las acciones del usuario
 * (perfil, conexiones sociales, etc.).
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrafoSocialService grafoSocialService;

    /**
     * Endpoint para seguir a otro usuario.
     * Cumple con RF-007.
     *
     * @param token            El token de autorización del usuario que *realiza* la
     *                         acción.
     * @param usernameToFollow El username del usuario *a ser seguido*.
     * @return Una respuesta HTTP 200 (OK) si la operación es exitosa.
     */
    @PostMapping("/follow/{usernameToFollow}")
    public ResponseEntity<String> followUser(
            @RequestHeader("Authorization") String token,
            @PathVariable String usernameToFollow) {

        // 1. Identificar al usuario "seguidor" (quién hace la petición)
        Usuario seguidor = usuarioService.getUserFromToken(token);

        // 2. Identificar al usuario "seguido" (a quién se quiere seguir)
        Usuario seguido = usuarioRepository.findByUsername(usernameToFollow)
                .orElseThrow(() -> new RuntimeException("Usuario a seguir no encontrado: " + usernameToFollow));

        // 3. Añadir la conexión en la entidad
        seguidor.getSeguidos().add(seguido);

        // 4. Guardar el cambio en la Base de Datos
        usuarioRepository.save(seguidor);

        // 5. Actualizar el grafo en memoria
        grafoSocialService.agregarConexion(seguidor, seguido);

        return ResponseEntity.ok(seguidor.getUsername() + " ahora sigue a " + seguido.getUsername());
    }

    /**
     * Endpoint para obtener sugerencias de "amigos de amigos".
     * Cumple con RF-008.
     *
     * @param token El token de autorización del usuario que pide las sugerencias.
     * @return Una lista de UserDto con las sugerencias.
     */
    @GetMapping("/suggestions")
    public ResponseEntity<List<UserDto>> getSuggestions(
            @RequestHeader("Authorization") String token) {

        // 1. Identificar al usuario actual
        Usuario usuarioActual = usuarioService.getUserFromToken(token);

        // 2. Pedir las sugerencias al servicio del grafo (que usa BFS)
        Set<Usuario> sugerencias = grafoSocialService.obtenerSugerencias(usuarioActual);

        // 3. Convertir la lista de entidades Usuario a una lista de UserDto
        // Esto es crucial para no exponer contraseñas.
        List<UserDto> sugerenciasDto = sugerencias.stream()
                .map(usuario -> new UserDto(usuario)) // Llama al constructor que creamos
                .collect(Collectors.toList());

        return ResponseEntity.ok(sugerenciasDto);
    }
}
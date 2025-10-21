package com.syncup.syncup_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.syncup.syncup_api.domain.Usuario;
import com.syncup.syncup_api.dto.UserRegistrationRequest;
import com.syncup.syncup_api.repository.UsuarioRepository;
import com.syncup.syncup_api.dto.LoginRequest;
import com.syncup.syncup_api.dto.LoginResponse;

import java.util.HashMap;
import java.util.LinkedList; // <-- Importación necesaria
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service // Le dice a Spring que esto es un Servicio (lógica de negocio)
public class UsuarioService {

    // Inyecta el repositorio...
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Mapa en memoria para almacenar tokens activos (llaves de hotel)
    // La llave del mapa es el Token (String), el valor es el username (String)
    private Map<String, String> activeTokens = new HashMap<>();

    
    public Usuario registrarUsuario(UserRegistrationRequest request) {
        
        // 1. Opcional: Validar si el usuario ya existe
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            // Manejar el error, por ahora lanzamos una excepción simple
            throw new RuntimeException("El nombre de usuario ya existe: " + request.getUsername());
        }

        // 2. Crear un nuevo objeto Usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(request.getUsername());
        nuevoUsuario.setNombre(request.getNombre());
        nuevoUsuario.setPassword(request.getPassword());

        nuevoUsuario.setListaFavoritos(new LinkedList<>());

        // 4. Guardar el usuario en la base de datos
        return usuarioRepository.save(nuevoUsuario);
    }

    
    public LoginResponse loginUsuario(LoginRequest request) {
        
        // 1. Buscar al usuario por su username
        Optional<Usuario> usuarioOptional = usuarioRepository.findByUsername(request.getUsername());

        // 2. Si no se encuentra O si la contraseña no coincide
        if (usuarioOptional.isEmpty() || !usuarioOptional.get().getPassword().equals(request.getPassword())) {
            // Lanzamos un error (más adelante lo haremos más seguro)
            throw new RuntimeException("Credenciales inválidas");
        }

        // 3. Si las credenciales son correctas, generamos un token
        // Usamos UUID para generar un String aleatorio y único
        String token = UUID.randomUUID().toString();

        // 4. Guardamos el token en nuestro mapa de tokens activos
        // Lo asociamos con el username de quien inició sesión
        activeTokens.put(token, request.getUsername());

        // 5. Devolvemos el token al usuario
        return new LoginResponse(token);
    }

}
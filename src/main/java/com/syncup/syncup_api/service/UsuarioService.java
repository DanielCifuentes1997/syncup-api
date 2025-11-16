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

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrafoSocialService grafoSocialService;

    private Map<String, String> activeTokens = new HashMap<>();

    public Usuario registrarUsuario(UserRegistrationRequest request) {

        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya existe: " + request.getUsername());
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(request.getUsername());
        nuevoUsuario.setNombre(request.getNombre());
        nuevoUsuario.setPassword(request.getPassword());
        nuevoUsuario.setFechaNacimiento(request.getFechaNacimiento());
        nuevoUsuario.setGenero(request.getGenero());
        nuevoUsuario.setListaFavoritos(new LinkedList<>());

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        grafoSocialService.agregarUsuario(usuarioGuardado);

        return usuarioGuardado;
    }

    public LoginResponse loginUsuario(LoginRequest request) {

        Optional<Usuario> usuarioOptional = usuarioRepository.findByUsername(request.getUsername());

        if (usuarioOptional.isEmpty() || !usuarioOptional.get().getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }
        
        Usuario usuario = usuarioOptional.get();
        return generateTokenForUser(usuario);
    }
    
    public LoginResponse processGoogleLogin(String username, String nombre) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByUsername(username);
        
        Usuario usuario;
        if (usuarioOptional.isPresent()) {
            usuario = usuarioOptional.get();
        } else {
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setUsername(username);
            nuevoUsuario.setNombre(nombre);
            nuevoUsuario.setPassword(null);
            nuevoUsuario.setListaFavoritos(new LinkedList<>());
            
            usuario = usuarioRepository.save(nuevoUsuario);
            grafoSocialService.agregarUsuario(usuario);
        }
        
        return generateTokenForUser(usuario);
    }

    private LoginResponse generateTokenForUser(Usuario usuario) {
        String token = UUID.randomUUID().toString();
        activeTokens.put(token, usuario.getUsername());
        return new LoginResponse(token, usuario.getNombre(), usuario.isHaCompletadoOnboarding());
    }

    public String getUsernameFromToken(String token) {
        String cleanToken = token.replace("Bearer ", "");
        return activeTokens.get(cleanToken);
    }

    public Usuario getUserFromToken(String token) {
        String username = getUsernameFromToken(token);
        if (username == null) {
            throw new RuntimeException("Token inválido o sesión expirada");
        }

        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado para el token: " + username));
    }

    public boolean usernameExists(String username) {
        return usuarioRepository.findByUsername(username).isPresent();
    }
}
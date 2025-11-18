package com.syncup.syncup_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.syncup.syncup_api.domain.Cancion;
import com.syncup.syncup_api.domain.Usuario;
import com.syncup.syncup_api.dto.OnboardingRequest;
import com.syncup.syncup_api.dto.UserRegistrationRequest;
import com.syncup.syncup_api.dto.UserUpdateDto;
import com.syncup.syncup_api.repository.CancionRepository;
import com.syncup.syncup_api.repository.UsuarioRepository;
import com.syncup.syncup_api.dto.LoginRequest;
import com.syncup.syncup_api.dto.LoginResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrafoSocialService grafoSocialService;

    @Autowired
    private CancionRepository cancionRepository;

    @Value("${jwt.secret:default-secret-key-for-syncup-project}")
    private String jwtSecret;

    private SecretKey key;

    @Transactional
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
        nuevoUsuario.setRol("USER");

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        grafoSocialService.agregarUsuario(usuarioGuardado);

        return usuarioGuardado;
    }

    public LoginResponse loginUsuario(LoginRequest request) {

        Optional<Usuario> usuarioOptional = usuarioRepository.findByUsername(request.getUsername());

        if (usuarioOptional.isEmpty()) {
            throw new RuntimeException("Credenciales inválidas");
        }

        Usuario usuario = usuarioOptional.get();

        if (usuario.getPassword() == null) {
            throw new RuntimeException("Este usuario fue registrado con Google. Por favor, use 'Iniciar con Google'.");
        }

        if (!usuario.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }
        
        return generateTokenForUser(usuario);
    }
    
    @Transactional
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
            nuevoUsuario.setRol("USER");
            
            usuario = usuarioRepository.save(nuevoUsuario);
            grafoSocialService.agregarUsuario(usuario);
        }
        
        return generateTokenForUser(usuario);
    }

    private LoginResponse generateTokenForUser(Usuario usuario) {
        if (key == null) {
            this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        }
        
        long now = System.currentTimeMillis();
        long expirationTime = 3600000;
        
        String jwtToken = Jwts.builder()
                .setSubject(usuario.getUsername())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationTime))
                .claim("nombre", usuario.getNombre())
                .claim("rol", usuario.getRol())
                .signWith(key)
                .compact();

        return new LoginResponse(jwtToken, usuario.getNombre(), usuario.isHaCompletadoOnboarding());
    }

    private Claims getClaims(String token) {
        if (key == null) {
            this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        }
        
        String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(cleanToken)
                .getBody();
    }

    public String getUsernameFromToken(String token) {
        try {
            return getClaims(token).getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public String getRoleFromToken(String token) {
         try {
            return getClaims(token).get("rol", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public Usuario getUserFromToken(String token) {
        String username = getUsernameFromToken(token);
        if (username == null) {
            throw new RuntimeException("Token inválido o sesión expirada");
        }

        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado para el token: " + username));
    }

    @Transactional
    public void completeOnboarding(String username, OnboardingRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));

        List<String> artistas = request.getArtistas();
        List<String> generos = request.getGeneros();

        if (artistas != null && !artistas.isEmpty()) {
            List<Cancion> cancionesPorArtista = cancionRepository.findByArtistaIn(artistas);
            cancionesPorArtista.forEach(cancion -> {
                if (!usuario.getListaFavoritos().contains(cancion)) {
                    usuario.getListaFavoritos().add(cancion);
                }
            });
        }

        if (generos != null && !generos.isEmpty()) {
            List<Cancion> cancionesPorGenero = cancionRepository.findByGeneroIn(generos);
            cancionesPorGenero.forEach(cancion -> {
                if (!usuario.getListaFavoritos().contains(cancion)) {
                    usuario.getListaFavoritos().add(cancion);
                }
            });
        }

        usuario.setHaCompletadoOnboarding(true);
        usuarioRepository.save(usuario);
    }

    public boolean usernameExists(String username) {
        return usuarioRepository.findByUsername(username).isPresent();
    }

    @Transactional
    public List<Cancion> addFavorite(String username, Long songId) {
        Usuario usuarioActual = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        
        Cancion cancion = cancionRepository.findById(songId)
            .orElseThrow(() -> new RuntimeException("Canción no encontrada: " + songId));

        if (!usuarioActual.getListaFavoritos().contains(cancion)) {
            usuarioActual.getListaFavoritos().add(cancion);
        }
        
        return usuarioActual.getListaFavoritos();
    }

    @Transactional
    public List<Cancion> removeFavorite(String username, Long songId) {
        Usuario usuarioActual = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));

        Cancion cancion = cancionRepository.findById(songId)
            .orElseThrow(() -> new RuntimeException("Canción no encontrada: " + songId));

        usuarioActual.getListaFavoritos().remove(cancion);
        
        return usuarioActual.getListaFavoritos();
    }
    
    @Bean
    CommandLineRunner initAdminUser() {
        return args -> {
            if (usuarioRepository.findByUsername("admin").isEmpty()) {
                System.out.println("--- Creando usuario ADMIN por defecto ---");
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setNombre("Admin SyncUp");
                admin.setPassword("admin123");
                admin.setRol("ADMIN");
                admin.setListaFavoritos(new LinkedList<>());
                admin.setHaCompletadoOnboarding(true);
                
                Usuario adminGuardado = usuarioRepository.save(admin);
                grafoSocialService.agregarUsuario(adminGuardado);
                System.out.println("--- Usuario ADMIN creado: admin / admin123 ---");
            }
        };
    }

    public List<Usuario> getAllUsers() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public void deleteUser(Long userId) {
        Usuario usuarioAEliminar = usuarioRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado para eliminar: " + userId));

        if ("ADMIN".equals(usuarioAEliminar.getRol())) {
            throw new RuntimeException("No se puede eliminar a un usuario administrador.");
        }

        grafoSocialService.eliminarUsuario(usuarioAEliminar);

        List<Usuario> todos = usuarioRepository.findAll();
        for (Usuario u : todos) {
            u.getSeguidos().remove(usuarioAEliminar);
        }

        usuarioAEliminar.setListaFavoritos(null);
        usuarioAEliminar.setSeguidos(null);
        
        usuarioRepository.delete(usuarioAEliminar);
    }

    @Transactional
    public Usuario updateUser(String username, UserUpdateDto updateDto) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (updateDto.getNombre() != null && !updateDto.getNombre().trim().isEmpty()) {
            usuario.setNombre(updateDto.getNombre());
        }

        if (updateDto.getNewPassword() != null && !updateDto.getNewPassword().trim().isEmpty()) {
            if (updateDto.getCurrentPassword() == null || !updateDto.getCurrentPassword().equals(usuario.getPassword())) {
                throw new RuntimeException("La contraseña actual es incorrecta.");
            }
            usuario.setPassword(updateDto.getNewPassword());
        }

        return usuarioRepository.save(usuario);
    }
    
    public List<Usuario> searchUsers(String query) {
        String queryLower = query.toLowerCase();
        
        return usuarioRepository.findAll().stream()
            .filter(u -> u.getUsername().toLowerCase().contains(queryLower) || 
                         (u.getNombre() != null && u.getNombre().toLowerCase().contains(queryLower)))
            .collect(Collectors.toList());
    }

    public Usuario getUserByUsername(String username) {
        return usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }

    @Transactional
    public Set<Usuario> getFollowedUsers(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return usuario.getSeguidos();
    }

    @Transactional
    public Set<Usuario> unfollowUser(String followerUsername, String followedUsername) {
        Usuario seguidor = usuarioRepository.findByUsername(followerUsername)
            .orElseThrow(() -> new RuntimeException("Usuario seguidor no encontrado"));
        Usuario seguido = usuarioRepository.findByUsername(followedUsername)
            .orElseThrow(() -> new RuntimeException("Usuario seguido no encontrado"));

        boolean removed = seguidor.getSeguidos().remove(seguido);
        
        if (removed) {
            usuarioRepository.save(seguidor);
            grafoSocialService.eliminarConexion(seguidor, seguido);
        }

        return seguidor.getSeguidos();
    }
}
package com.syncup.syncup_api.controller;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.syncup.syncup_api.domain.Cancion;
import com.syncup.syncup_api.domain.Usuario;
import com.syncup.syncup_api.dto.OnboardingRequest;
import com.syncup.syncup_api.dto.SongDto;
import com.syncup.syncup_api.dto.UserDto;
import com.syncup.syncup_api.dto.UserUpdateDto;
import com.syncup.syncup_api.repository.CancionRepository;
import com.syncup.syncup_api.repository.UsuarioRepository;
import com.syncup.syncup_api.service.GrafoSocialService;
import com.syncup.syncup_api.service.UsuarioService;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrafoSocialService grafoSocialService;

    @Autowired
    private CancionRepository cancionRepository;

    @PostMapping("/follow/{usernameToFollow}")
    public ResponseEntity<String> followUser(
            @RequestHeader("Authorization") String token,
            @PathVariable String usernameToFollow) {

        Usuario seguidor = usuarioService.getUserFromToken(token);
        Usuario seguido = usuarioRepository.findByUsername(usernameToFollow)
            .orElseThrow(() -> new RuntimeException("Usuario a seguir no encontrado: " + usernameToFollow));

        if(seguidor.equals(seguido)) {
             throw new RuntimeException("No puedes seguirte a ti mismo.");
        }

        seguidor.getSeguidos().add(seguido);
        usuarioRepository.save(seguidor);
        grafoSocialService.agregarConexion(seguidor, seguido);

        return ResponseEntity.ok(seguidor.getUsername() + " ahora sigue a " + seguido.getUsername());
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<UserDto>> getSuggestions(
            @RequestHeader("Authorization") String token) {

        Usuario usuarioActual = usuarioService.getUserFromToken(token);
        Set<Usuario> sugerencias = grafoSocialService.obtenerSugerencias(usuarioActual);

        List<UserDto> sugerenciasDto = sugerencias.stream()
            .map(UserDto::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok(sugerenciasDto);
    }

    @PostMapping("/me/onboarding")
    public ResponseEntity<Void> completeOnboarding(
            @RequestHeader("Authorization") String token,
            @RequestBody OnboardingRequest request) {
        
        String username = usuarioService.getUsernameFromToken(token);
        usuarioService.completeOnboarding(username, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/favorites/{songId}")
    public ResponseEntity<List<SongDto>> addFavorite(
            @RequestHeader("Authorization") String token,
            @PathVariable Long songId) {

        String username = usuarioService.getUsernameFromToken(token);
        List<Cancion> favoritas = usuarioService.addFavorite(username, songId);

        List<SongDto> favoritasDto = favoritas.stream()
                .map(SongDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(favoritasDto);
    }

    @DeleteMapping("/me/favorites/{songId}")
    public ResponseEntity<List<SongDto>> removeFavorite(
            @RequestHeader("Authorization") String token,
            @PathVariable Long songId) {

        String username = usuarioService.getUsernameFromToken(token);
        List<Cancion> favoritas = usuarioService.removeFavorite(username, songId);

        List<SongDto> favoritasDto = favoritas.stream()
                .map(SongDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(favoritasDto);
    }

    @GetMapping("/me/favorites")
    public ResponseEntity<List<SongDto>> getFavorites(
            @RequestHeader("Authorization") String token) {

        Usuario usuarioActual = usuarioService.getUserFromToken(token);
        
        List<SongDto> favoritasDto = usuarioActual.getListaFavoritos().stream()
                .map(SongDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(favoritasDto);
    }

    @GetMapping("/me/favorites/export")
    public void exportFavorites(
            @RequestHeader("Authorization") String token,
            HttpServletResponse response) {

        Usuario usuarioActual = usuarioService.getUserFromToken(token);

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"favoritos_" + usuarioActual.getUsername() + ".csv\"");

        try {
            List<Cancion> favoritas = usuarioActual.getListaFavoritos();
            List<SongDto> favoritasDto = favoritas.stream()
                    .map(SongDto::new)
                    .collect(Collectors.toList());

            StatefulBeanToCsv<SongDto> writer = new StatefulBeanToCsvBuilder<SongDto>(response.getWriter())
                    .withQuotechar(com.opencsv.ICSVWriter.NO_QUOTE_CHARACTER)
                    .withSeparator(',')
                    .withOrderedResults(false) 
                    .build();

            writer.write(favoritasDto);

        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            System.err.println("Error al exportar CSV de favoritos: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UserUpdateDto updateDto) {
        
        String username = usuarioService.getUsernameFromToken(token);
        Usuario usuarioActualizado = usuarioService.updateUser(username, updateDto);
        
        return ResponseEntity.ok(new UserDto(usuarioActualizado));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String query) {
        List<Usuario> usuarios = usuarioService.searchUsers(query);
        
        List<UserDto> dtos = usuarios.stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
    @GetMapping("/profile/{username}")
    public ResponseEntity<UserDto> getPublicProfile(@PathVariable String username) {
        Usuario usuario = usuarioService.getUserByUsername(username);
        return ResponseEntity.ok(new UserDto(usuario));
    }

    @GetMapping("/profile/{username}/favorites")
    public ResponseEntity<List<SongDto>> getPublicFavorites(@PathVariable String username) {
        Usuario usuario = usuarioService.getUserByUsername(username);
        
        List<SongDto> favoritasDto = usuario.getListaFavoritos().stream()
                .map(SongDto::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(favoritasDto);
    }
}
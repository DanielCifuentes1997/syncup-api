package com.syncup.syncup_api.controller;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.syncup.syncup_api.domain.Cancion;
import com.syncup.syncup_api.domain.Usuario;
import com.syncup.syncup_api.dto.SongDto;
import com.syncup.syncup_api.dto.UserDto;
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

// Controlador para gestionar las acciones del usuario autenticado
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

    // Endpoint para seguir a otro usuario
    @PostMapping("/follow/{usernameToFollow}")
    public ResponseEntity<String> followUser(
            @RequestHeader("Authorization") String token,
            @PathVariable String usernameToFollow) {

        Usuario seguidor = usuarioService.getUserFromToken(token);
        Usuario seguido = usuarioRepository.findByUsername(usernameToFollow)
            .orElseThrow(() -> new RuntimeException("Usuario a seguir no encontrado: " + usernameToFollow));

        // Evitar seguirse a sí mismo
        if(seguidor.equals(seguido)) {
             throw new RuntimeException("No puedes seguirte a ti mismo.");
        }

        seguidor.getSeguidos().add(seguido);
        usuarioRepository.save(seguidor);
        grafoSocialService.agregarConexion(seguidor, seguido);

        return ResponseEntity.ok(seguidor.getUsername() + " ahora sigue a " + seguido.getUsername());
    }

    // Endpoint para obtener sugerencias de usuarios a seguir
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

    // Endpoint para añadir una canción a los favoritos del usuario actual
    @PostMapping("/me/favorites/{songId}")
    public ResponseEntity<List<SongDto>> addFavorite(
            @RequestHeader("Authorization") String token,
            @PathVariable Long songId) {

        Usuario usuarioActual = usuarioService.getUserFromToken(token);
        Cancion cancion = cancionRepository.findById(songId)
            .orElseThrow(() -> new RuntimeException("Canción no encontrada: " + songId));

        // Añadir a favoritos si no está ya
        if (!usuarioActual.getListaFavoritos().contains(cancion)) {
            usuarioActual.getListaFavoritos().add(cancion);
            usuarioRepository.save(usuarioActual);
        }

        // Devolver la lista actualizada de favoritos
        List<SongDto> favoritasDto = usuarioActual.getListaFavoritos().stream()
                .map(SongDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(favoritasDto);
    }

    // Endpoint para eliminar una canción de los favoritos del usuario actual
    @DeleteMapping("/me/favorites/{songId}")
    public ResponseEntity<List<SongDto>> removeFavorite(
            @RequestHeader("Authorization") String token,
            @PathVariable Long songId) {

        Usuario usuarioActual = usuarioService.getUserFromToken(token);
        Cancion cancion = cancionRepository.findById(songId)
            .orElseThrow(() -> new RuntimeException("Canción no encontrada: " + songId));

        // Quitar de favoritos si existe
        boolean removed = usuarioActual.getListaFavoritos().remove(cancion);
        if (removed) {
            usuarioRepository.save(usuarioActual);
        }

        // Devolver la lista actualizada de favoritos
        List<SongDto> favoritasDto = usuarioActual.getListaFavoritos().stream()
                .map(SongDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(favoritasDto);
    }


    // Endpoint para exportar las canciones favoritas del usuario actual a CSV
    @GetMapping("/me/favorites/export")
    public void exportFavorites(
            @RequestHeader("Authorization") String token,
            HttpServletResponse response) {

        Usuario usuarioActual = usuarioService.getUserFromToken(token);

        // Configurar cabeceras HTTP para descarga
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"favoritos_" + usuarioActual.getUsername() + ".csv\"");

        try {
            List<Cancion> favoritas = usuarioActual.getListaFavoritos();
            List<SongDto> favoritasDto = favoritas.stream()
                    .map(SongDto::new)
                    .collect(Collectors.toList());

            // Escribir DTOs a la respuesta usando OpenCSV
            StatefulBeanToCsv<SongDto> writer = new StatefulBeanToCsvBuilder<SongDto>(response.getWriter())
                    .withQuotechar(com.opencsv.ICSVWriter.NO_QUOTE_CHARACTER)
                    .withSeparator(',')
                    .withOrderedResults(false) // No forzar orden alfabético de columnas
                    .build();

            writer.write(favoritasDto);

        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            // Manejo básico de errores
            System.err.println("Error al exportar CSV de favoritos: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
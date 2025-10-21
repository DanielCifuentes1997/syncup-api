package com.syncup.syncup_api.controller;

import com.syncup.syncup_api.domain.Cancion; // Necesario para buscar la semilla
import com.syncup.syncup_api.dto.SongDto; // Nuestro nuevo DTO
import com.syncup.syncup_api.repository.CancionRepository; // Para buscar la semilla
import com.syncup.syncup_api.service.RecomendacionService; // El nuevo servicio
import com.syncup.syncup_api.service.TrieService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestionar las operaciones relacionadas con las canciones.
 */
@RestController
@RequestMapping("/api") // Cambiamos el prefijo base para que coincida con la arquitectura
public class CancionController {

    @Autowired
    private TrieService trieService;

    // Inyectamos los nuevos servicios y repositorios
    @Autowired
    private RecomendacionService recomendacionService;

    @Autowired
    private CancionRepository cancionRepository;

    /**
     * Endpoint para autocompletar títulos de canciones.
     * Cumple con RF-003.
     * Se accede vía: GET /api/songs/autocomplete?prefix=...
     */
    @GetMapping("/songs/autocomplete")
    public ResponseEntity<List<String>> autocompleteCancion(
            @RequestParam("prefix") String prefix) {

        List<String> suggestions = trieService.autocomplete(prefix);
        return ResponseEntity.ok(suggestions);
    }

    // --- NUEVOS ENDPOINTS DE RECOMENDACIÓN ---

    /**
     * Endpoint para generar la playlist "Descubrimiento Semanal".
     * Cumple con RF-005.
     * Se accede vía: GET /api/recommendations/discover-weekly
     */
    @GetMapping("/recommendations/discover-weekly")
    public ResponseEntity<List<SongDto>> getDescubrimientoSemanal() {

        // Llama al servicio para obtener la lista (límite de 20 por ej.)
        List<Cancion> canciones = recomendacionService.generarDescubrimientoSemanal(20);

        // Convierte la lista de Entidades a una lista de DTOs
        List<SongDto> dtos = canciones.stream()
            .map(cancion -> new SongDto(cancion))
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Endpoint para generar una "Radio" a partir de una canción semilla.
     * Cumple con RF-006.
     * Se accede vía: GET /api/recommendations/radio/{songId}
     */
    @GetMapping("/recommendations/radio/{songId}")
    public ResponseEntity<List<SongDto>> getRadio(
            @PathVariable Long songId) {

        // 1. Buscar la canción semilla en la BD
        Cancion cancionSemilla = cancionRepository.findById(songId)
            .orElseThrow(() -> new RuntimeException("Canción no encontrada: " + songId));

        // 2. Llama al servicio para generar la radio (límite de 10 por ej.)
        List<Cancion> radio = recomendacionService.generarRadio(cancionSemilla, 10);

        // 3. Convertir a DTOs
        List<SongDto> dtos = radio.stream()
            .map(cancion -> new SongDto(cancion))
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
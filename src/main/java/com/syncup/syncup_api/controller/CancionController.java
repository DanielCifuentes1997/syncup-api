package com.syncup.syncup_api.controller;

import com.syncup.syncup_api.domain.Cancion;
import com.syncup.syncup_api.dto.SongDto;
import com.syncup.syncup_api.repository.CancionRepository;
import com.syncup.syncup_api.service.CancionService; // Importamos CancionService
import com.syncup.syncup_api.service.RecomendacionService;
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
@RequestMapping("/api")
public class CancionController {

    @Autowired
    private TrieService trieService;

    @Autowired
    private RecomendacionService recomendacionService;

    @Autowired
    private CancionRepository cancionRepository;

    // Inyectamos el nuevo CancionService
    @Autowired
    private CancionService cancionService;

    /**
     * Endpoint para autocompletar títulos de canciones.
     * Cumple con RF-003.
     */
    @GetMapping("/songs/autocomplete")
    public ResponseEntity<List<String>> autocompleteCancion(
            @RequestParam("prefix") String prefix) {

        List<String> suggestions = trieService.autocomplete(prefix);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Endpoint para generar la playlist "Descubrimiento Semanal".
     * Cumple con RF-005.
     */
    @GetMapping("/recommendations/discover-weekly")
    public ResponseEntity<List<SongDto>> getDescubrimientoSemanal() {

        List<Cancion> canciones = recomendacionService.generarDescubrimientoSemanal(20);
        List<SongDto> dtos = canciones.stream()
            .map(SongDto::new) // Forma corta de llamar al constructor
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Endpoint para generar una "Radio" a partir de una canción semilla.
     * Cumple con RF-006.
     */
    @GetMapping("/recommendations/radio/{songId}")
    public ResponseEntity<List<SongDto>> getRadio(
            @PathVariable Long songId) {

        Cancion cancionSemilla = cancionRepository.findById(songId)
            .orElseThrow(() -> new RuntimeException("Canción no encontrada: " + songId));
        List<Cancion> radio = recomendacionService.generarRadio(cancionSemilla, 10);
        List<SongDto> dtos = radio.stream()
            .map(SongDto::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // --- NUEVO ENDPOINT DE BÚSQUEDA AVANZADA ---

    /**
     * Endpoint para realizar búsquedas avanzadas de canciones.
     * Cumple con RF-004. La concurrencia (RF-030) se implementará en el servicio.
     * Se accede vía: GET /api/songs/search?query=...
     * Ejemplo query: "artista:Taylor Swift AND anio:2008"
     *
     * @param query La cadena de consulta con los criterios.
     * @return Una lista de SongDto que coinciden con la búsqueda.
     */
    @GetMapping("/songs/search")
    public ResponseEntity<List<SongDto>> searchSongs(
            @RequestParam("query") String query) {

        // Llama al servicio para realizar la búsqueda (que por ahora solo maneja AND)
        List<Cancion> cancionesEncontradas = cancionService.buscarCancionesAvanzado(query);

        // Convierte las entidades a DTOs
        List<SongDto> dtos = cancionesEncontradas.stream()
            .map(SongDto::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
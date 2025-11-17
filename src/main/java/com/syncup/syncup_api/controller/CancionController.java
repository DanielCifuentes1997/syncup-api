package com.syncup.syncup_api.controller;

import com.syncup.syncup_api.domain.Cancion;
import com.syncup.syncup_api.dto.SongDto;
import com.syncup.syncup_api.repository.CancionRepository;
import com.syncup.syncup_api.service.CancionService; 
import com.syncup.syncup_api.service.RecomendacionService;
import com.syncup.syncup_api.service.TrieService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

//Controlador REST para gestionar las operaciones relacionadas con las canciones.
@RestController
@RequestMapping("/api")
public class CancionController {

    @Autowired
    private TrieService trieService;

    @Autowired
    private RecomendacionService recomendacionService;

    @Autowired
    private CancionRepository cancionRepository;

    @Autowired
    private CancionService cancionService;

    //Endpoint para autocompletar títulos de canciones.
    @GetMapping("/songs/autocomplete")
    public ResponseEntity<List<String>> autocompleteCancion(
            @RequestParam("prefix") String prefix) {

        List<String> suggestions = trieService.autocomplete(prefix);
        return ResponseEntity.ok(suggestions);
    }

     //Endpoint para generar la playlist "Descubrimiento Semanal".
    @GetMapping("/recommendations/discover-weekly")
    public ResponseEntity<List<SongDto>> getDescubrimientoSemanal() {

        List<Cancion> canciones = recomendacionService.generarDescubrimientoSemanal(20);
        List<SongDto> dtos = canciones.stream()
                .map(SongDto::new) 
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    //Endpoint para generar una "Radio" a partir de una canción semilla.
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

    // Endpoint para realizar búsquedas avanzadas de canciones.
    @GetMapping("/songs/search")
    public ResponseEntity<List<SongDto>> searchSongs(
            @RequestParam("query") String query) {

        List<Cancion> cancionesEncontradas = cancionService.buscarCancionesAvanzado(query);

        List<SongDto> dtos = cancionesEncontradas.stream()
                .map(SongDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // Endpoint para obtener todos los artistas únicos
    @GetMapping("/songs/artists")
    public ResponseEntity<List<String>> getAllArtists() {
        return ResponseEntity.ok(cancionService.getAvailableArtists());
    }

    // Endpoint para obtener todos los géneros únicos (del catálogo de canciones)
    @GetMapping("/songs/genres")
    public ResponseEntity<List<String>> getAllGenres() {
        return ResponseEntity.ok(cancionService.getAvailableGenres());
    }

    // Endpoint para obtener la lista maestra de géneros (para el asistente)
    @GetMapping("/genres/master")
    public ResponseEntity<List<String>> getMasterGenres() {
        return ResponseEntity.ok(cancionService.getMasterGenres());
    }
}
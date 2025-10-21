package com.syncup.syncup_api.controller;

import com.syncup.syncup_api.service.TrieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para gestionar las operaciones relacionadas con las canciones.
 */
@RestController
@RequestMapping("/api/songs") // Prefijo base para todos los endpoints de este controlador
public class CancionController {

    // Inyectamos el servicio del Trie que contiene la lógica de autocompletado
    @Autowired
    private TrieService trieService;

    /**
     * Endpoint para autocompletar títulos de canciones basado en un prefijo.
     * Cumple con el requisito funcional RF-003.
     *
     * Se accede vía: GET http://localhost:8080/api/songs/autocomplete?prefix=...
     *
     * @param prefix El prefijo de búsqueda (ej. "lov") proporcionado como parámetro en la URL.
     * @return Una respuesta HTTP 200 (OK) con la lista de sugerencias.
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocompleteCancion(
            @RequestParam("prefix") String prefix) {

        // Llama al servicio para que realice la búsqueda en el Trie
        List<String> suggestions = trieService.autocomplete(prefix);

        // Devuelve la lista de sugerencias
        return ResponseEntity.ok(suggestions);
    }
}
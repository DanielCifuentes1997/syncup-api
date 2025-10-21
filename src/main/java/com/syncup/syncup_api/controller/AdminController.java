package com.syncup.syncup_api.controller;

import com.syncup.syncup_api.domain.Cancion;
import com.syncup.syncup_api.dto.SongCreateDto;
import com.syncup.syncup_api.dto.SongDto; // Usaremos el DTO para la respuesta
import com.syncup.syncup_api.service.CancionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para gestionar las operaciones administrativas,
 * como la gestión del catálogo de canciones y usuarios.
 */
@RestController
@RequestMapping("/api/admin") // Prefijo base para todas las rutas de admin
public class AdminController {

    @Autowired
    private CancionService cancionService;

    /**
     * Endpoint para (Admin) añadir una nueva canción al catálogo.
     * Cumple con el requisito funcional RF-010.
     *
     * @param songDto El DTO con los datos de la canción a crear.
     * @return Un ResponseEntity con el DTO de la canción creada y un estado 201 (Created).
     */
    @PostMapping("/songs")
    public ResponseEntity<SongDto> addSong(@RequestBody SongCreateDto songDto) {

        // 1. Llama al servicio para crear la canción y guardarla en la BD
        Cancion cancionGuardada = cancionService.crearCancion(songDto);

        // 2. Convierte la entidad guardada a un DTO para la respuesta
        SongDto respuestaDto = new SongDto(cancionGuardada);

        // 3. Devuelve el DTO con el estado HTTP 201 (Created)
        return new ResponseEntity<>(respuestaDto, HttpStatus.CREATED);
    }
}
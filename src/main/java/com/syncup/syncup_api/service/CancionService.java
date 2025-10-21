package com.syncup.syncup_api.service;

import com.syncup.syncup_api.domain.Cancion;
import com.syncup.syncup_api.dto.SongCreateDto;
import com.syncup.syncup_api.repository.CancionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestionar la lógica de negocio relacionada con las Canciones,
 * como la creación, búsqueda y eliminación.
 */
@Service
public class CancionService {

    @Autowired
    private CancionRepository cancionRepository;

    @Autowired
    private TrieService trieService;
    // Por ahora, el grafo de similitud solo se calcula al inicio.
    @Autowired
    private RecomendacionService recomendacionService;

    /**
     * Crea y guarda una nueva canción en el sistema.
     * Cumple con RF-010.
     *
     * @param songDto El DTO con la información de la nueva canción.
     * @return La entidad Cancion que fue guardada en la BD.
     */
    @Transactional // Asegura que la operación sea atómica
    public Cancion crearCancion(SongCreateDto songDto) {

        // 1. Convertir el DTO a una entidad
        Cancion nuevaCancion = new Cancion();
        nuevaCancion.setTitulo(songDto.getTitulo());
        nuevaCancion.setArtista(songDto.getArtista());
        nuevaCancion.setGenero(songDto.getGenero());
        nuevaCancion.setAnio(songDto.getAnio());
        nuevaCancion.setDuracion(songDto.getDuracion());

        // 2. Guardar la entidad en la base de datos
        Cancion cancionGuardada = cancionRepository.save(nuevaCancion);

        // 3. Actualizar el Trie de autocompletado en memoria
        trieService.agregarCancionAlTrie(cancionGuardada);


        // 5. Devolver la entidad guardada
        return cancionGuardada;
    }
}
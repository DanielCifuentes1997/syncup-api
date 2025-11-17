package com.syncup.syncup_api.service;

import com.syncup.syncup_api.domain.Cancion;
import com.syncup.syncup_api.dto.SongCreateDto;
import com.syncup.syncup_api.repository.CancionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CancionService {

    @Autowired
    private CancionRepository cancionRepository;

    @Autowired
    private TrieService trieService;

    @Autowired
    private RecomendacionService recomendacionService;

    @Autowired
    private ExecutorService taskExecutor;

    @Value("${syncup.genres.master-list}")
    private String masterGenresList;

    @Transactional
    public Cancion crearCancion(SongCreateDto songDto) {
        Cancion nuevaCancion = new Cancion();
        nuevaCancion.setTitulo(songDto.getTitulo());
        nuevaCancion.setArtista(songDto.getArtista());
        nuevaCancion.setGenero(songDto.getGenero());
        nuevaCancion.setAnio(songDto.getAnio());
        nuevaCancion.setDuracion(songDto.getDuracion());

        Cancion cancionGuardada = cancionRepository.save(nuevaCancion);
        trieService.agregarCancionAlTrie(cancionGuardada);
        return cancionGuardada;
    }

    //Realiza búsqueda avanzada, manejando AND y OR (concurrente).
    public List<Cancion> buscarCancionesAvanzado(String query) {
        List<Cancion> todasLasCanciones = cancionRepository.findAll();

        String[] orParts = query.split("(?i)\\s+OR\\s+");

        if (orParts.length > 1) {
            // Lógica CONCURRENTE para OR
            System.out.println("--- [CancionService] Ejecutando búsqueda con OR concurrente.");

            List<Future<List<Cancion>>> futures = new ArrayList<>();

            for (String orPart : orParts) {
                Callable<List<Cancion>> task = () -> {
                    Map<String, String> criteria = parseQuery(orPart);
                    return todasLasCanciones.stream()
                            .filter(cancion -> matchesCriteria(cancion, criteria))
                            .collect(Collectors.toList());
                };
                futures.add(taskExecutor.submit(task));
            }

            Set<Cancion> combinedResults = new HashSet<>();
            for (Future<List<Cancion>> future : futures) {
                try {
                    combinedResults.addAll(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Error ejecutando tarea concurrente: " + e.getMessage());
                    Thread.currentThread().interrupt();
                    return new ArrayList<>();
                }
            }
            return new ArrayList<>(combinedResults);

        } else {
            // Lógica SECUENCIAL para AND (o consulta simple)
            System.out.println("--- [CancionService] Ejecutando búsqueda simple (AND/única).");
            Map<String, String> criteria = parseQuery(query);
            return todasLasCanciones.stream()
                    .filter(cancion -> matchesCriteria(cancion, criteria))
                    .collect(Collectors.toList());
        }
    }
    
    // Devuelve una lista de todos los artistas únicos en la base de datos.
    public List<String> getAvailableArtists() {
        return cancionRepository.findAll().stream()
            .map(Cancion::getArtista)
            .distinct()
            .collect(Collectors.toList());
    }

    // Devuelve una lista de todos los géneros únicos en la base de datos.
    public List<String> getAvailableGenres() {
        return cancionRepository.findAll().stream()
            .map(Cancion::getGenero)
            .distinct()
            .collect(Collectors.toList());
    }
    
    // Devuelve la lista maestra de géneros desde application.properties
    public List<String> getMasterGenres() {
        return Arrays.asList(masterGenresList.split(","));
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> criteria = new HashMap<>();
        if (query == null || query.trim().isEmpty()) {
            return criteria;
        }
        String[] parts = query.split("(?i)\\s+AND\\s+");
        for (String part : parts) {
            String[] keyValue = part.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().toLowerCase();
                String value = keyValue[1].trim();
                criteria.put(key, value);
            } else {
                System.err.println("Skipping malformed query part: " + part);
            }
        }
        return criteria;
    }

    private boolean matchesCriteria(Cancion cancion, Map<String, String> criteria) {
        for (Map.Entry<String, String> entry : criteria.entrySet()) {
            String key = entry.getKey();
            String expectedValue = entry.getValue();
            boolean match = switch (key) {
                case "artista" ->
                    cancion.getArtista() != null && cancion.getArtista().equalsIgnoreCase(expectedValue);
                case "genero" ->
                    cancion.getGenero() != null && cancion.getGenero().equalsIgnoreCase(expectedValue);
                case "anio" -> String.valueOf(cancion.getAnio()).equals(expectedValue);
                default -> true;
            };
            if (!match) {
                return false;
            }
        }
        return true;
    }
}
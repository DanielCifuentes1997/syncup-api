package com.syncup.syncup_api.service;

import com.syncup.syncup_api.domain.Cancion;
import com.syncup.syncup_api.domain.Usuario;
import com.syncup.syncup_api.dto.SongCreateDto;
import com.syncup.syncup_api.repository.CancionRepository;
import com.syncup.syncup_api.repository.UsuarioRepository;

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
    private UsuarioRepository usuarioRepository;

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
        nuevaCancion.setFilename(songDto.getFilename());

        Cancion cancionGuardada = cancionRepository.save(nuevaCancion);
        trieService.agregarCancionAlTrie(cancionGuardada);
        return cancionGuardada;
    }

    public List<Cancion> buscarCancionesAvanzado(String query) {
        List<Cancion> todasLasCanciones = cancionRepository.findAll();

        boolean isSimpleQuery = !query.contains(":") && !query.matches("(?i).*\\s+(OR|AND)\\s+.*");

        if (isSimpleQuery) {
            String lowerQuery = query.toLowerCase().trim();
            return todasLasCanciones.stream()
                .filter(cancion -> cancion.getTitulo() != null && 
                                   cancion.getTitulo().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
        }

        String[] orParts = query.split("(?i)\\s+OR\\s+");

        if (orParts.length > 1) {
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
            Map<String, String> criteria = parseQuery(query);
            return todasLasCanciones.stream()
                    .filter(cancion -> matchesCriteria(cancion, criteria))
                    .collect(Collectors.toList());
        }
    }
    
    public List<String> getAvailableArtists() {
        return cancionRepository.findAll().stream()
            .map(Cancion::getArtista)
            .distinct()
            .collect(Collectors.toList());
    }

    public List<String> getAvailableGenres() {
        return cancionRepository.findAll().stream()
            .map(Cancion::getGenero)
            .distinct()
            .collect(Collectors.toList());
    }
    
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
            }
        }
        return criteria;
    }

    private boolean matchesCriteria(Cancion cancion, Map<String, String> criteria) {
        if (criteria.isEmpty()) {
            return false;
        }

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

    public List<Cancion> getAllSongs() {
        return cancionRepository.findAll();
    }

    @Transactional
    public Cancion updateSong(Long songId, SongCreateDto songDetails) {
        Cancion cancion = cancionRepository.findById(songId)
            .orElseThrow(() -> new RuntimeException("Canción no encontrada: " + songId));

        cancion.setTitulo(songDetails.getTitulo());
        cancion.setArtista(songDetails.getArtista());
        cancion.setGenero(songDetails.getGenero());
        cancion.setAnio(songDetails.getAnio());
        cancion.setDuracion(songDetails.getDuracion());
        cancion.setFilename(songDetails.getFilename());
        
        return cancionRepository.save(cancion);
    }

    @Transactional
    public void deleteSong(Long songId) {
        Cancion cancion = cancionRepository.findById(songId)
            .orElseThrow(() -> new RuntimeException("Canción no encontrada: " + songId));

        List<Usuario> usuarios = usuarioRepository.findAll();
        for (Usuario usuario : usuarios) {
            usuario.getListaFavoritos().remove(cancion);
        }
        
        trieService.eliminarCancionDelTrie(cancion);
        
        cancionRepository.delete(cancion);
    }
}
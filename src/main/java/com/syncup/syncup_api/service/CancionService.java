package com.syncup.syncup_api.service;

import com.syncup.syncup_api.domain.Cancion;
import com.syncup.syncup_api.dto.SongCreateDto;
import com.syncup.syncup_api.repository.CancionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet; // Para quitar duplicados
import java.util.List;
import java.util.Map;
import java.util.Set; // Para quitar duplicados
import java.util.concurrent.Callable; // Para las tareas concurrentes
import java.util.concurrent.ExecutionException; // Para manejar errores de hilos
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future; // Para obtener resultados de hilos
import java.util.stream.Collectors;
import java.util.stream.Stream; // Para combinar resultados

@Service
public class CancionService {

    @Autowired
    private CancionRepository cancionRepository;

    @Autowired
    private TrieService trieService;

    @Autowired
    private RecomendacionService recomendacionService;

    @Autowired
    private ExecutorService taskExecutor; // Nuestro pool de hilos

    @Transactional
    public Cancion crearCancion(SongCreateDto songDto) {
        // ... (código existente sin cambios)
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

        // Detectar si hay OR (ignorando mayúsculas/minúsculas)
        String[] orParts = query.split("(?i)\\s+OR\\s+");

        if (orParts.length > 1) {
            // --- Lógica CONCURRENTE para OR ---
            System.out.println("--- [CancionService] Ejecutando búsqueda con OR concurrente.");

            List<Future<List<Cancion>>> futures = new ArrayList<>();

            // Crear una tarea separada para cada parte del OR
            for (String orPart : orParts) {
                Callable<List<Cancion>> task = () -> {
                    Map<String, String> criteria = parseQuery(orPart); // Parsea solo esta parte
                    return todasLasCanciones.stream()
                            .filter(cancion -> matchesCriteria(cancion, criteria))
                            .collect(Collectors.toList());
                };
                futures.add(taskExecutor.submit(task)); // Enviar tarea al pool de hilos
            }

            // Recolectar resultados de todas las tareas
            Set<Cancion> combinedResults = new HashSet<>(); // Usar Set para eliminar duplicados automáticamente
            for (Future<List<Cancion>> future : futures) {
                try {
                    combinedResults.addAll(future.get()); // .get() espera a que el hilo termine
                } catch (InterruptedException | ExecutionException e) {
                    // Manejo básico de errores de concurrencia
                    System.err.println("Error ejecutando tarea concurrente: " + e.getMessage());
                    Thread.currentThread().interrupt(); // Reestablecer estado interrumpido
                    // Podríamos devolver lista vacía o lanzar excepción específica
                    return new ArrayList<>();
                }
            }
            return new ArrayList<>(combinedResults); // Convertir Set a List para devolver

        } else {
            // Lógica SECUENCIAL para AND (o consulta simple)
            System.out.println("--- [CancionService] Ejecutando búsqueda simple (AND/única).");
            Map<String, String> criteria = parseQuery(query);
            return todasLasCanciones.stream()
                    .filter(cancion -> matchesCriteria(cancion, criteria))
                    .collect(Collectors.toList());
        }
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
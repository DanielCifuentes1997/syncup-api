package com.syncup.syncup_api.service;

import com.syncup.syncup_api.core.GrafoDeSimilitud;
import com.syncup.syncup_api.domain.Cancion;
import com.syncup.syncup_api.repository.CancionRepository;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio Singleton que gestiona el motor de recomendaciones.
 * Es propietario del Grafo de Similitud de Canciones.
 */
@Service
public class RecomendacionService {

    private final GrafoDeSimilitud grafo = new GrafoDeSimilitud();

    @Autowired
    private CancionRepository cancionRepository;

    // Umbral máximo de peso para considerar una conexión
    private static final double UMBRAL_MAXIMO_PESO = 3.0;
    // Peso mínimo para evitar problemas con Dijkstra y filtros
    private static final double PESO_MINIMO = 0.01;

    /**
     * Inicializa y construye el Grafo de Similitud al arrancar.
     * Proceso O(N^2) necesario para las recomendaciones offline.
     */
    @PostConstruct
    public void init() {
        System.out.println("--- [RecomendacionService] Iniciando construcción del Grafo de Similitud...");
        List<Cancion> todasLasCanciones = cancionRepository.findAll();

        if (todasLasCanciones.isEmpty()) {
            System.out.println("--- [RecomendacionService] No hay canciones en la BD para construir el grafo.");
            return;
        }

        for (Cancion cancion : todasLasCanciones) {
            grafo.agregarCancion(cancion);
        }

        int aristasAgregadas = 0;
        for (int i = 0; i < todasLasCanciones.size(); i++) {
            Cancion c1 = todasLasCanciones.get(i);
            for (int j = i + 1; j < todasLasCanciones.size(); j++) {
                Cancion c2 = todasLasCanciones.get(j);
                double peso = calcularPeso(c1, c2);
                if (peso <= UMBRAL_MAXIMO_PESO) {
                    grafo.agregarConexionPonderada(c1, c2, peso);
                    aristasAgregadas++;
                }
            }
        }
        System.out.println("--- [RecomendacionService] Grafo de Similitud construido. Aristas añadidas: " + aristasAgregadas);
    }

    //Genera una "Radio" basada en similitud usando Dijkstra.
    public List<Cancion> generarRadio(Cancion cancionSemilla, int limite) {
        Map<Cancion, Double> distancias = grafo.dijkstra(cancionSemilla);

        return distancias.entrySet().stream()
            // Ordenar por distancia ascendente
            .sorted(Map.Entry.comparingByValue())
            // Excluir la canción semilla
            .filter(entry -> !entry.getKey().getId().equals(cancionSemilla.getId()))
            // FILTRO CLAVE: Excluir canciones inalcanzables (distancia infinita)
            .filter(entry -> entry.getValue() != Double.POSITIVE_INFINITY)
            // Obtener solo la entidad Cancion
            .map(Map.Entry::getKey)
            // Limitar al número solicitado
            .limit(limite)
            // Recolectar en lista
            .collect(Collectors.toList());
    }

    //Calcula la disimilitud (peso) entre dos canciones.
    private double calcularPeso(Cancion c1, Cancion c2) {
        double peso = 0.0;

        // Comparar género (ignorar nulos)
        if (c1.getGenero() != null && c2.getGenero() != null && !c1.getGenero().equalsIgnoreCase(c2.getGenero())) {
            peso += 1.5;
        } else if ((c1.getGenero() == null) != (c2.getGenero() == null)) { // Uno es nulo, el otro no
             peso += 1.5;
        }

        // Comparar artista (ignorar nulos)
        if (c1.getArtista() != null && c2.getArtista() != null && !c1.getArtista().equalsIgnoreCase(c2.getArtista())) {
            peso += 1.0;
        } else if ((c1.getArtista() == null) != (c2.getArtista() == null)) {
             peso += 1.0;
        }

        // Comparar año (asumiendo que anio nunca es nulo/cero por ser int primitivo)
        peso += Math.abs(c1.getAnio() - c2.getAnio()) / 10.0;

        // Asegurar peso mínimo
        return Math.max(peso, PESO_MINIMO);
    }

    //Genera playlist "Descubrimiento Semanal" (implementación simple).
    public List<Cancion> generarDescubrimientoSemanal(int limite) {
        List<Cancion> todas = cancionRepository.findAll();
        Collections.shuffle(todas);
        return todas.stream().limit(limite).collect(Collectors.toList());
    }
}
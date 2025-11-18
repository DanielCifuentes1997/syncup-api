package com.syncup.syncup_api.service;

import com.syncup.syncup_api.core.GrafoDeSimilitud;
import com.syncup.syncup_api.domain.Cancion;
import com.syncup.syncup_api.domain.Usuario;
import com.syncup.syncup_api.repository.CancionRepository;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RecomendacionService {

    private final GrafoDeSimilitud grafo = new GrafoDeSimilitud();

    @Autowired
    private CancionRepository cancionRepository;

    private static final double UMBRAL_MAXIMO_PESO = 3.0;
    private static final double PESO_MINIMO = 0.01;

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

    public List<Cancion> generarRadio(Cancion cancionSemilla, int limite) {
        Map<Cancion, Double> distancias = grafo.dijkstra(cancionSemilla);

        return distancias.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .filter(entry -> !entry.getKey().getId().equals(cancionSemilla.getId()))
            .filter(entry -> entry.getValue() != Double.POSITIVE_INFINITY)
            .map(Map.Entry::getKey)
            .limit(limite)
            .collect(Collectors.toList());
    }

    private double calcularPeso(Cancion c1, Cancion c2) {
        double peso = 0.0;

        if (c1.getGenero() != null && c2.getGenero() != null && !c1.getGenero().equalsIgnoreCase(c2.getGenero())) {
            peso += 1.5;
        } else if ((c1.getGenero() == null) != (c2.getGenero() == null)) {
             peso += 1.5;
        }

        if (c1.getArtista() != null && c2.getArtista() != null && !c1.getArtista().equalsIgnoreCase(c2.getArtista())) {
            peso += 1.0;
        } else if ((c1.getArtista() == null) != (c2.getArtista() == null)) {
             peso += 1.0;
        }

        peso += Math.abs(c1.getAnio() - c2.getAnio()) / 10.0;

        return Math.max(peso, PESO_MINIMO);
    }

    public List<Cancion> generarDescubrimientoSemanal(Usuario usuario, int limite) {
        List<Cancion> favoritas = usuario.getListaFavoritos();
        
        if (favoritas.isEmpty()) {
            List<Cancion> todas = cancionRepository.findAll();
            Collections.shuffle(todas);
            return todas.stream().limit(limite).collect(Collectors.toList());
        }

        Random random = new Random();
        Cancion cancionSemilla = favoritas.get(random.nextInt(favoritas.size()));

        List<Cancion> similares = generarRadio(cancionSemilla, limite);
        
        return similares;
    }
}
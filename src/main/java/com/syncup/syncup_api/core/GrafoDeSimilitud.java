package com.syncup.syncup_api.core;

import com.syncup.syncup_api.domain.Cancion;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.HashSet;

/**
 * Implementación de un Grafo Ponderado No Dirigido.
 * Modela la "similitud" entre canciones. Un peso bajo significa alta similitud.
 */
public class GrafoDeSimilitud {

    /**
     * El núcleo del grafo. Es un mapa donde cada Cancion (nodo) está
     * asociada con una lista de Aristas Ponderadas (conexiones a otras canciones).
     */
    private final Map<Cancion, List<AristaPonderada>> listaDeAdyacencia;

    public GrafoDeSimilitud() {
        this.listaDeAdyacencia = new HashMap<>();
    }

    //Añade una nueva canción (nodo) al grafo.
    public void agregarCancion(Cancion cancion) {
        listaDeAdyacencia.computeIfAbsent(cancion, k -> new ArrayList<>());
    }

     //Añade una conexión ponderada y no dirigida entre dos canciones.
    public void agregarConexionPonderada(Cancion cancion1, Cancion cancion2, double peso) {
        // Asegurarse de que ambos nodos existan
        agregarCancion(cancion1);
        agregarCancion(cancion2);

        // Crear la arista en ambas direcciones
        listaDeAdyacencia.get(cancion1).add(new AristaPonderada(cancion2, peso));
        listaDeAdyacencia.get(cancion2).add(new AristaPonderada(cancion1, peso));
    }

    /**
     * Implementa el algoritmo de Dijkstra para encontrar los caminos más cortos
     * (rutas de mayor similitud) desde una canción semilla.
     * @param cancionInicio La canción desde la cual se genera la "radio".
     * @return Un Map donde la clave es la Cancion destino y el valor es el
     *         "costo" total (disimilitud acumulada) para llegar a ella.
     */
    public Map<Cancion, Double> dijkstra(Cancion cancionInicio) {

        // 1. Mapa de Distancias: Almacena el "costo" más corto encontrado
        // hasta ahora para llegar a cada canción.
        Map<Cancion, Double> distancias = new HashMap<>();

        // 2. Cola de Prioridad: Almacena los nodos por visitar, ordenados
        // por la distancia más corta primero. Es la clave de la eficiencia de Dijkstra.
        // Se ordena por el valor (Double) de la entrada del mapa.
        PriorityQueue<Map.Entry<Cancion, Double>> pq = new PriorityQueue<>(
                Map.Entry.comparingByValue());

        // 3. Set de Visitados: Rastrea los nodos que ya hemos procesado.
        Set<Cancion> visitados = new HashSet<>();

        // 4. Inicialización:
        // Ponemos todas las distancias en "Infinito", excepto la de inicio.
        for (Cancion cancion : listaDeAdyacencia.keySet()) {
            distancias.put(cancion, Double.POSITIVE_INFINITY);
        }

        // La distancia a la canción de inicio es 0
        distancias.put(cancionInicio, 0.0);
        pq.add(Map.entry(cancionInicio, 0.0));

        // 5. Bucle principal de Dijkstra
        while (!pq.isEmpty()) {

            // 5a. Obtener el nodo más cercano (con menor distancia) de la cola
            Cancion actual = pq.poll().getKey();

            // 5b. Si ya hemos visitado este nodo, lo saltamos.
            if (visitados.contains(actual)) {
                continue;
            }

            // 5c. Marcarlo como visitado
            visitados.add(actual);

            // 5d. Explorar todos los vecinos (aristas) del nodo actual
            for (AristaPonderada arista : listaDeAdyacencia.getOrDefault(actual, new ArrayList<>())) {
                Cancion vecino = arista.getDestino();
                double pesoArista = arista.getPeso();

                if (!visitados.contains(vecino)) {

                    // 5e. Calcular la nueva distancia (distancia_actual + peso_arista)
                    double nuevaDistancia = distancias.get(actual) + pesoArista;

                    // 5f. Relajación de la arista:
                    // Si esta nueva ruta es más corta que la que teníamos...
                    if (nuevaDistancia < distancias.get(vecino)) {
                        // ...actualizamos la distancia
                        distancias.put(vecino, nuevaDistancia);

                        // ...y añadimos al vecino a la cola de prioridad
                        // con su nueva (y mejor) distancia.
                        pq.add(Map.entry(vecino, nuevaDistancia));
                    }
                }
            }
        }

        // 6. Devolver el mapa con todas las distancias más cortas calculadas
        return distancias;
    }
}
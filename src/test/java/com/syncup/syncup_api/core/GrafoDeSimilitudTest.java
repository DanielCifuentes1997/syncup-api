package com.syncup.syncup_api.core;

import com.syncup.syncup_api.domain.Cancion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba Unitaria para la clase GrafoDeSimilitud.
 * Verifica el algoritmo de Dijkstra para encontrar el camino más corto.
 * Cumple con parte del requisito RF-031.
 */
class GrafoDeSimilitudTest {

    private GrafoDeSimilitud grafo;

    // Nodos (Canciones)
    private Cancion cancionA = new Cancion();
    private Cancion cancionB = new Cancion();
    private Cancion cancionC = new Cancion();
    private Cancion cancionD = new Cancion();
    private Cancion cancionE = new Cancion();

    /**
     * Este método se ejecuta antes de cada prueba.
     * Construye nuestro grafo de canciones.
     *
     * Estructura del Grafo (Pesos = Disimilitud):
     *
     * (B) --5-- (D)
     * / |         |
     * 1  3         1
     * /   |         |
     * (A) --2-- (C) --4-- (E)
     *
     */
    @BeforeEach
    void setUp() {
        // Es crucial setear un ID (o lo que use el hashCode/equals)
        // para que el HashMap del grafo pueda distinguirlos.
        // Usaremos el ID que definimos en RF-020.
        cancionA.setId(1L);
        cancionB.setId(2L);
        cancionC.setId(3L);
        cancionD.setId(4L);
        cancionE.setId(5L);

        grafo = new GrafoDeSimilitud();

        // Añadir conexiones
        grafo.agregarConexionPonderada(cancionA, cancionB, 1.0); // A-B = 1
        grafo.agregarConexionPonderada(cancionA, cancionC, 2.0); // A-C = 2
        grafo.agregarConexionPonderada(cancionB, cancionC, 3.0); // B-C = 3
        grafo.agregarConexionPonderada(cancionB, cancionD, 5.0); // B-D = 5
        grafo.agregarConexionPonderada(cancionD, cancionE, 1.0); // D-E = 1
        grafo.agregarConexionPonderada(cancionC, cancionE, 4.0); // C-E = 4
    }

    /**
     * Prueba el algoritmo de Dijkstra desde el nodo A.
     */
    @Test
    void testDijkstraDesdeA() {
        // Ejecutamos Dijkstra comenzando desde la Canción A
        Map<Cancion, Double> distancias = grafo.dijkstra(cancionA);

        // Verificamos las distancias (caminos más cortos) desde A

        // Distancia A -> A debería ser 0
        assertEquals(0.0, distancias.get(cancionA));

        // Distancia A -> B (Directa) es 1
        assertEquals(1.0, distancias.get(cancionB));

        // Distancia A -> C (Directa) es 2
        assertEquals(2.0, distancias.get(cancionC));

        // Distancia A -> D:
        // Ruta A-B-D = 1.0 + 5.0 = 6.0
        // Ruta A-C-E-D = 2.0 + 4.0 + 1.0 = 7.0
        // Ruta A-C-B-D = 2.0 + 3.0 + 5.0 = 10.0
        // Camino más corto es A-B-D
        assertEquals(6.0, distancias.get(cancionD));

        // Distancia A -> E:
        // Ruta A-C-E = 2.0 + 4.0 = 6.0
        // Ruta A-B-D-E = 1.0 + 5.0 + 1.0 = 7.0
        // Camino más corto es A-C-E
        assertEquals(6.0, distancias.get(cancionE));
    }
}
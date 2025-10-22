package com.syncup.syncup_api.core;

import com.syncup.syncup_api.domain.Usuario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Implementación de un Grafo Social No Dirigido.
 * Utiliza una lista de adyacencia (Map) para almacenar las conexiones (aristas)
 * entre los nodos (Usuarios).
 */
public class GrafoSocial {

    /**
     * El núcleo del grafo. Es un mapa donde cada Usuario (nodo) está
     * asociado con una lista de otros Usuarios (sus "vecinos" o amigos).
     */
    private final Map<Usuario, List<Usuario>> listaDeAdyacencia;

    //Constructor que inicializa el grafo.
    public GrafoSocial() {
        this.listaDeAdyacencia = new HashMap<>();
    }

    /**
     * Añade un nuevo usuario (nodo) al grafo.
     * Si el usuario ya existe, no hace nada.
     */
    public void agregarUsuario(Usuario usuario) {
        // computeIfAbsent asegura que solo se añada el usuario si no existe ya
        listaDeAdyacencia.computeIfAbsent(usuario, k -> new ArrayList<>());
    }

    /**
     * Añade una conexión no dirigida (amistad) entre dos usuarios.
     * Al ser no dirigido, si U1 sigue a U2, U2 también sigue a U1.
     */
    public void agregarConexion(Usuario usuario1, Usuario usuario2) {
        // Asegurarse de que ambos usuarios existan como nodos
        agregarUsuario(usuario1);
        agregarUsuario(usuario2);

        // Añadir la conexión en ambas direcciones, evitando duplicados
        if (!listaDeAdyacencia.get(usuario1).contains(usuario2)) {
            listaDeAdyacencia.get(usuario1).add(usuario2);
        }
        if (!listaDeAdyacencia.get(usuario2).contains(usuario1)) {
            listaDeAdyacencia.get(usuario2).add(usuario1);
        }
    }

    /**
     * Implementa el algoritmo BFS (Búsqueda en Anchura) para encontrar
     * "amigos de amigos" (nodos a 2 niveles de distancia).
     * Cumple con el requisito RF-024.
     * @param usuarioInicio El usuario desde el que se inicia la búsqueda (el usuario actual).
     * @return Un Set de Usuarios que son "amigos de amigos" (nivel 2).
     */
    public Set<Usuario> bfsAmigosDeAmigos(Usuario usuarioInicio) {
        // Set para guardar los resultados (amigos de nivel 2)
        Set<Usuario> amigosDeAmigos = new HashSet<>();

        // Cola para el algoritmo BFS, almacena los nodos a visitar
        Queue<Usuario> cola = new LinkedList<>();

        // Set para rastrear nodos ya visitados y evitar ciclos infinitos
        Set<Usuario> visitados = new HashSet<>();

        // Mapa para rastrear la "distancia" o "nivel" desde el nodo de inicio
        Map<Usuario, Integer> distancia = new HashMap<>();

        // 1. Configuración inicial
        cola.add(usuarioInicio);
        visitados.add(usuarioInicio);
        distancia.put(usuarioInicio, 0); // La distancia a sí mismo es 0

        // 2. Bucle principal de BFS
        while (!cola.isEmpty()) {
            // Sacar el siguiente usuario de la cola
            Usuario actual = cola.poll();
            int distActual = distancia.get(actual);

            // 3. Explorar los vecinos del usuario actual
            for (Usuario vecino : listaDeAdyacencia.getOrDefault(actual, new ArrayList<>())) {

                // Si no hemos visitado a este vecino...
                if (!visitados.contains(vecino)) {
                    // Marcarlo como visitado
                    visitados.add(vecino);

                    // Establecer su distancia (un nivel más que el nodo actual)
                    distancia.put(vecino, distActual + 1);

                    // Añadirlo a la cola para explorar a sus vecinos después
                    cola.add(vecino);

                    // 4. Lógica de "Amigos de Amigos"
                    // Si la distancia de este vecino es 2...
                    if (distActual + 1 == 2) {
                        // ¡Es un amigo de un amigo! Lo añadimos al resultado.
                        amigosDeAmigos.add(vecino);
                    }
                }
            }
        }

        // 5. Filtrado final
        // Quitamos a los amigos que ya seguimos (nivel 1) del resultado.
        // (Aunque el BFS de nivel 2 no debería incluirlos, es una doble seguridad)
        amigosDeAmigos.removeAll(listaDeAdyacencia.get(usuarioInicio));

        return amigosDeAmigos;
    }
}
package com.syncup.syncup_api.service;

import com.syncup.syncup_api.core.Trie;
import com.syncup.syncup_api.domain.Cancion;
import com.syncup.syncup_api.repository.CancionRepository;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // Marca esto como un Servicio de Spring (será un Singleton)
public class TrieService {

    // 1. El servicio es "dueño" de la instancia única del Trie
    private final Trie trie = new Trie();

    // 2. Inyectamos el repositorio para poder leer la BD
    @Autowired
    private CancionRepository cancionRepository;

    /**
     * Este método se ejecuta automáticamente UNA SOLA VEZ
     * cuando la aplicación Spring Boot arranca. [cite: 111]
     * Su trabajo es poblar el Trie con los datos de la BD.
     */
    @PostConstruct
    public void init() {
        System.out.println("--- [TrieService] Poblando el Trie con canciones...");

        // Obtenemos TODAS las canciones de la base de datos
        List<Cancion> todasLasCanciones = cancionRepository.findAll();

        // Insertamos cada título en el Trie
        for (Cancion cancion : todasLasCanciones) {
            trie.insert(cancion.getTitulo());
        }

        System.out.println("--- [TrieService] Trie poblado con " + todasLasCanciones.size() + " canciones.");
    }

    /**
     * Método público que el Controlador usará para obtener sugerencias.
     * Simplemente delega la llamada a nuestro objeto Trie. [cite: 120]
     */
    public List<String> autocomplete(String prefix) {
        return trie.autocomplete(prefix);
    }

    /**
     * Método para añadir una nueva canción al Trie "en caliente"
     * (cuando se añade una nueva canción después de que la app ya arrancó).
     */
    public void agregarCancionAlTrie(Cancion cancion) {
        trie.insert(cancion.getTitulo());
    }
}
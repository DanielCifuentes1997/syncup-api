package com.syncup.syncup_api.service;

import com.syncup.syncup_api.core.Trie;
import com.syncup.syncup_api.domain.Cancion;
import com.syncup.syncup_api.repository.CancionRepository;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrieService {

    private final Trie trie = new Trie();

    @Autowired
    private CancionRepository cancionRepository;

    @PostConstruct
    public void init() {
        System.out.println("--- [TrieService] Poblando el Trie con canciones...");

        List<Cancion> todasLasCanciones = cancionRepository.findAll();

        for (Cancion cancion : todasLasCanciones) {
            trie.insert(cancion.getTitulo());
        }

        System.out.println("--- [TrieService] Trie poblado con " + todasLasCanciones.size() + " canciones.");
    }

    public List<String> autocomplete(String prefix) {
        return trie.autocomplete(prefix);
    }

    public void agregarCancionAlTrie(Cancion cancion) {
        trie.insert(cancion.getTitulo());
    }

    public void eliminarCancionDelTrie(Cancion cancion) {
        trie.delete(cancion.getTitulo());
    }
}
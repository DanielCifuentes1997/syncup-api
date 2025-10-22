package com.syncup.syncup_api.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * [cite_start]Implementación del Árbol de Prefijos (Trie)[cite: 225].
 * Esta estructura almacena una colección de strings (títulos de canciones)
 * y permite búsquedas de autocompletado (prefijo) de manera muy eficiente.
 */
public class Trie {

    // El nodo raíz del árbol. Es un nodo vacío que sirve como punto de partida.
    private final TrieNode root;

    /**
     * Constructor: Inicializa el árbol creando un nodo raíz vacío.
     */
    public Trie() {
        root = new TrieNode();
    }

    /**
     * Inserta una palabra (título de canción) en el Trie.
     * Recorre la palabra letra por letra, creando nodos hijos según sea necesario.
     */
    public void insert(String word) {
        // Empezamos siempre desde la raíz
        TrieNode current = root;

        // Convertimos la palabra a minúsculas para que las búsquedas no distingan
        // entre "Love" y "love".
        for (char ch : word.toLowerCase().toCharArray()) {
            // Obtenemos los hijos del nodo actual
            Map<Character, TrieNode> children = current.getChildren();

            // Verificamos si ya existe un nodo hijo para esta letra ('ch')
            // Si no existe, 'computeIfAbsent' crea un nuevo TrieNode y lo agrega al mapa.
            // Luego, 'current' se actualiza para apuntar a ese nodo hijo (sea nuevo o existente).
            current = children.computeIfAbsent(ch, c -> new TrieNode());
        }

        // Al final del bucle, 'current' apunta al último nodo de la palabra.
        // Marcamos este nodo como "fin de palabra".
        current.setEndOfWord(true);
    }

    /**
     * Busca todas las palabras en el Trie que comienzan con un prefijo dado.
     * @param prefix El prefijo (ej. "lov") para autocompletar.
     * @return Una lista de strings (ej. ["love", "lovely"]) que comienzan con el prefijo.
     */
    public List<String> autocomplete(String prefix) {
        List<String> suggestions = new ArrayList<>();
        TrieNode current = root;
        String prefixLower = prefix.toLowerCase();

        // 1. Navegar hasta el final del prefijo
        // Recorremos el árbol letra por letra siguiendo el prefijo
        for (char ch : prefixLower.toCharArray()) {
            TrieNode node = current.getChildren().get(ch);
            if (node == null) {
                // Si en algún punto el prefijo no existe en el árbol
                // (ej. buscar "xyz" y la 'x' no existe),
                // devolvemos una lista vacía. No hay sugerencias.
                return suggestions;
            }
            current = node;
        }

        // 2. Si llegamos aquí, 'current' es el nodo que representa el final del prefijo
        // (ej. el nodo 'v' en "lov").
        // Ahora, recolectamos todas las palabras que descienden de este nodo.
        // 'prefixLower' se usa para reconstruir las palabras completas.
        collectWords(current, prefixLower, suggestions);

        return suggestions;
    }

    /**
     * Método auxiliar recursivo (DFS - Búsqueda en Profundidad).
     * Recorre el árbol desde un nodo dado, recolectando todas las palabras
     * que marca como 'isEndOfWord'.
     *
     * @param node El nodo desde donde empezar a buscar (ej. el nodo 'v' en "lov").
     * @param currentWord La palabra formada hasta ahora (ej. "lov").
     * @param suggestions La lista donde se agregarán las palabras completas encontradas.
     */
    private void collectWords(TrieNode node, String currentWord, List<String> suggestions) {
        // Caso base 1: Si este nodo es el final de una palabra,
        // la palabra formada 'currentWord' es una sugerencia válida.
        if (node.isEndOfWord()) {
            suggestions.add(currentWord);
        }

        // Caso recursivo: Explorar todos los hijos de este nodo
        // Para cada hijo (letra), llamamos recursivamente a esta función,
        // añadiendo la letra del hijo a la 'currentWord'.
        for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
            char nextChar = entry.getKey();
            TrieNode nextNode = entry.getValue();
            collectWords(nextNode, currentWord + nextChar, suggestions);
        }
    }
}
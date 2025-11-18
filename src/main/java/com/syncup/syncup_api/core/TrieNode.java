package com.syncup.syncup_api.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa un nodo individual en el árbol Trie.
 * Cada nodo tiene dos componentes principales:
 * 1. Un mapa de 'hijos' (Map Character, TrieNode) que almacena la
 * siguiente letra posible en la palabra.
 * 2. Un booleano 'isEndOfWord' que marca si este nodo es el
 * final de una palabra completa.
 */
public class TrieNode {

    // Almacena los nodos hijos (las siguientes letras)
    // Ejemplo: Si este nodo es 'c', un hijo podría ser ('a', Nodo 'a') para "ca..."
    private final Map<Character, TrieNode> children = new HashMap<>();

    // Marca si este nodo representa el final de una palabra insertada
    // Ejemplo: En la palabra "casa", el nodo 'a' final tendría isEndOfWord = true
    private boolean isEndOfWord;

    // --- Métodos generados (Getters y Setters) ---
    // (Lombok no se suele usar en clases de nodos puros
    // para mantener la claridad de la estructura)

    public Map<Character, TrieNode> getChildren() {
        return children;
    }

    public boolean isEndOfWord() {
        return isEndOfWord;
    }

    public void setEndOfWord(boolean endOfWord) {
        isEndOfWord = endOfWord;
    }
}
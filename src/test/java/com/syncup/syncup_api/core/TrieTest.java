package com.syncup.syncup_api.core;

import org.junit.jupiter.api.BeforeEach; // Para configurar la prueba
import org.junit.jupiter.api.Test; // Para marcar un método como una prueba

import java.util.List;

import static org.junit.jupiter.api.Assertions.*; // Para verificar resultados


class TrieTest {

    // Declara una variable para nuestro árbol Trie
    private Trie trie;

   
    @BeforeEach
    void setUp() {
        // Crea una nueva instancia del Trie
        trie = new Trie();

        // Inserta algunas palabras de ejemplo
        trie.insert("Love Story");
        trie.insert("Lover");
        trie.insert("Lovely");
        trie.insert("Amor Prohibido");
        trie.insert("Amante");
    }

   
    @Test
    void testAutocompleteSimple() {
        // Busca todas las palabras que empiezan con "lov"
        List<String> suggestions = trie.autocomplete("lov");

        // Verificamos que encontró 3 sugerencias
        assertEquals(3, suggestions.size());

        // Verificamos que contiene las palabras correctas
        assertTrue(suggestions.contains("love story"));
        assertTrue(suggestions.contains("lover"));
        assertTrue(suggestions.contains("lovely"));
    }

 
    @Test
    void testAutocompleteCaseInsensitive() {
        // Busca usando "LOV" (mayúsculas)
        List<String> suggestions = trie.autocomplete("LOV");

        // Debería dar el mismo resultado que "lov"
        assertEquals(3, suggestions.size());
        assertTrue(suggestions.contains("love story"));
    }

    
    @Test
    void testAutocompleteAnotherPrefix() {
        // Busca palabras que empiezan con "am"
        List<String> suggestions = trie.autocomplete("am");

        // Verificamos que encontró 2 sugerencias
        assertEquals(2, suggestions.size());
        assertTrue(suggestions.contains("amor prohibido"));
        assertTrue(suggestions.contains("amante"));
    }


    @Test
    void testAutocompleteNoMatch() {
        // Busca un prefijo que no insertamos
        List<String> suggestions = trie.autocomplete("xyz");

        // La lista de sugerencias debería estar vacía
        assertEquals(0, suggestions.size());
    }

   
    @Test
    void testAutocompleteFullWord() {
        // Busca "lover" (que es una palabra completa y un prefijo)
        List<String> suggestions = trie.autocomplete("lover");

        // Solo debería encontrar "lover"
        assertEquals(1, suggestions.size());
        assertTrue(suggestions.contains("lover"));
    }
}
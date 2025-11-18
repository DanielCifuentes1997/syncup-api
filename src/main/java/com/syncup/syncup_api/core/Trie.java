package com.syncup.syncup_api.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Trie {

    private final TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    public void insert(String word) {
        TrieNode current = root;

        for (char ch : word.toLowerCase().toCharArray()) {
            Map<Character, TrieNode> children = current.getChildren();
            current = children.computeIfAbsent(ch, c -> new TrieNode());
        }

        current.setEndOfWord(true);
    }

    public List<String> autocomplete(String prefix) {
        List<String> suggestions = new ArrayList<>();
        TrieNode current = root;
        String prefixLower = prefix.toLowerCase();

        for (char ch : prefixLower.toCharArray()) {
            TrieNode node = current.getChildren().get(ch);
            if (node == null) {
                return suggestions;
            }
            current = node;
        }

        collectWords(current, prefixLower, suggestions);

        return suggestions;
    }

    private void collectWords(TrieNode node, String currentWord, List<String> suggestions) {
        if (node.isEndOfWord()) {
            suggestions.add(currentWord);
        }

        for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
            char nextChar = entry.getKey();
            TrieNode nextNode = entry.getValue();
            collectWords(nextNode, currentWord + nextChar, suggestions);
        }
    }

    public void delete(String word) {
        delete(root, word.toLowerCase(), 0);
    }

    private boolean delete(TrieNode current, String word, int index) {
        if (index == word.length()) {
            if (!current.isEndOfWord()) {
                return false;
            }
            current.setEndOfWord(false);
            return current.getChildren().isEmpty();
        }

        char ch = word.charAt(index);
        TrieNode node = current.getChildren().get(ch);
        if (node == null) {
            return false;
        }

        boolean shouldDeleteChild = delete(node, word, index + 1);

        if (shouldDeleteChild) {
            current.getChildren().remove(ch);
            return current.getChildren().isEmpty() && !current.isEndOfWord();
        }
        
        return false;
    }
}
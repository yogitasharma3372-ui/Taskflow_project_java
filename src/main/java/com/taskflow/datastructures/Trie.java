package com.taskflow.datastructures;

import com.taskflow.model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Custom Trie (Prefix Tree) implementation for fast autocomplete and prefix-based search of Task titles.
 * 
 * Big-O Tradeoffs:
 * - Insert: O(L) where L is the length of the title string.
 * - Prefix Search / Autocomplete: O(L + K) where L is prefix length and K is the number of matching tasks in subtree.
 * - Space Complexity: O(N * L) where N is total tasks and L is average title length.
 */
public class Trie {

    private static class TrieNode {
        final Map<Character, TrieNode> children;
        final Set<Task> tasks; // Tasks associated with words ending at or matching this node
        boolean isEndOfWord;

        TrieNode() {
            this.children = new HashMap<>();
            this.tasks = new HashSet<>();
            this.isEndOfWord = false;
        }
    }

    private final TrieNode root;

    public Trie() {
        this.root = new TrieNode();
    }

    /**
     * Inserts a task into the Trie indexed by its title.
     * Case-insensitive indexing for smooth user search.
     * 
     * Big-O: O(L) where L is title length.
     * 
     * @param task Task to index in Trie
     */
    public void insert(Task task) {
        if (task == null || task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            return;
        }

        String title = task.getTitle().trim().toLowerCase();
        TrieNode current = root;

        for (int i = 0; i < title.length(); i++) {
            char ch = title.charAt(i);
            current = current.children.computeIfAbsent(ch, c -> new TrieNode());
        }
        current.isEndOfWord = true;
        current.tasks.add(task);
    }

    /**
     * Removes a task reference from the Trie.
     * 
     * @param task Task to remove
     */
    public void remove(Task task) {
        if (task == null || task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            return;
        }
        String title = task.getTitle().trim().toLowerCase();
        removeHelper(root, title, 0, task);
    }

    private boolean removeHelper(TrieNode current, String title, int index, Task task) {
        if (index == title.length()) {
            current.tasks.remove(task);
            if (current.tasks.isEmpty()) {
                current.isEndOfWord = false;
            }
            return current.children.isEmpty() && !current.isEndOfWord;
        }

        char ch = title.charAt(index);
        TrieNode node = current.children.get(ch);
        if (node == null) {
            return false;
        }

        boolean shouldDeleteChild = removeHelper(node, title, index + 1, task);

        if (shouldDeleteChild) {
            current.children.remove(ch);
            return current.children.isEmpty() && !current.isEndOfWord && current.tasks.isEmpty();
        }

        return false;
    }

    /**
     * Finds all tasks whose title begins with the given prefix.
     * 
     * Big-O: O(L + K) where L is prefix length, K is number of matched results.
     * 
     * @param prefix Search prefix
     * @return List of matching tasks (distinct)
     */
    public List<Task> autocomplete(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String searchPrefix = prefix.trim().toLowerCase();
        TrieNode current = root;

        // Traverse down to the prefix node
        for (int i = 0; i < searchPrefix.length(); i++) {
            char ch = searchPrefix.charAt(i);
            current = current.children.get(ch);
            if (current == null) {
                return Collections.emptyList(); // Prefix not found
            }
        }

        // Collect all tasks under subtree of current node
        Set<Task> resultSet = new HashSet<>();
        collectAllTasks(current, resultSet);
        return new ArrayList<>(resultSet);
    }

    /**
     * Recursively traverses subtree to collect all tasks.
     */
    private void collectAllTasks(TrieNode node, Set<Task> resultSet) {
        if (node == null) return;

        if (node.isEndOfWord) {
            resultSet.addAll(node.tasks);
        }

        for (TrieNode child : node.children.values()) {
            collectAllTasks(child, resultSet);
        }
    }

    /**
     * Clears all nodes from the Trie.
     */
    public void clear() {
        root.children.clear();
        root.tasks.clear();
        root.isEndOfWord = false;
    }
}

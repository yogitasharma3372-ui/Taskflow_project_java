package com.taskflow.datastructures;

import com.taskflow.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Trie Custom Data Structure Tests")
class TrieTest {

    private Trie trie;

    @BeforeEach
    void setUp() {
        trie = new Trie();
    }

    @Test
    @DisplayName("Insert and prefix autocomplete search")
    void testPrefixAutocomplete() {
        Task t1 = new Task("Setup Maven", "Desc", 1, LocalDateTime.now());
        Task t2 = new Task("Setup Database", "Desc", 2, LocalDateTime.now());
        Task t3 = new Task("Build UI", "Desc", 3, LocalDateTime.now());

        trie.insert(t1);
        trie.insert(t2);
        trie.insert(t3);

        List<Task> setupResults = trie.autocomplete("setup");
        assertEquals(2, setupResults.size());
        assertTrue(setupResults.contains(t1));
        assertTrue(setupResults.contains(t2));

        List<Task> buildResults = trie.autocomplete("build");
        assertEquals(1, buildResults.size());
        assertTrue(buildResults.contains(t3));

        List<Task> nonExistent = trie.autocomplete("xyz");
        assertTrue(nonExistent.isEmpty());
    }

    @Test
    @DisplayName("Case-insensitive title matching")
    void testCaseInsensitivity() {
        Task task = new Task("Fix Critical Bug", "Desc", 1, LocalDateTime.now());
        trie.insert(task);

        List<Task> lower = trie.autocomplete("fix");
        List<Task> upper = trie.autocomplete("FIX");
        List<Task> mixed = trie.autocomplete("FiX cRiT");

        assertEquals(1, lower.size());
        assertEquals(1, upper.size());
        assertEquals(1, mixed.size());
    }

    @Test
    @DisplayName("Task removal from Trie")
    void testRemoveFromTrie() {
        Task task = new Task("Deploy App", "Desc", 1, LocalDateTime.now());
        trie.insert(task);

        assertFalse(trie.autocomplete("deploy").isEmpty());

        trie.remove(task);
        assertTrue(trie.autocomplete("deploy").isEmpty());
    }
}

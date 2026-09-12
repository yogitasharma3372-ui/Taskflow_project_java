package com.taskflow.datastructures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Graph Custom Data Structure Tests")
class GraphTest {

    private Graph graph;

    @BeforeEach
    void setUp() {
        graph = new Graph();
    }

    @Test
    @DisplayName("Add vertices and directed dependency edges")
    void testAddVerticesAndDependencies() {
        graph.addDependency("T2", "T1"); // T2 depends on T1 (T1 -> T2)

        assertTrue(graph.containsVertex("T1"));
        assertTrue(graph.containsVertex("T2"));
        assertEquals(2, graph.getVertices().size());

        Map<String, Integer> inDegrees = graph.calculateInDegrees();
        assertEquals(0, inDegrees.get("T1"), "Prerequisite T1 should have in-degree 0");
        assertEquals(1, inDegrees.get("T2"), "Dependent T2 should have in-degree 1");
    }

    @Test
    @DisplayName("Remove vertex clears edges")
    void testRemoveVertex() {
        graph.addDependency("T2", "T1");
        graph.removeVertex("T1");

        assertFalse(graph.containsVertex("T1"));
        assertTrue(graph.containsVertex("T2"));
        assertEquals(0, graph.calculateInDegrees().get("T2"));
    }
}

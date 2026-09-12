package com.taskflow.algorithms;

import com.taskflow.datastructures.Graph;
import com.taskflow.exception.CircularDependencyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TopologicalSort & Cycle Detection Tests")
class TopologicalSortTest {

    private Graph graph;

    @BeforeEach
    void setUp() {
        graph = new Graph();
    }

    @Test
    @DisplayName("Acyclic DAG returns valid topological execution order")
    void testAcyclicGraph() {
        // T2 depends on T1, T3 depends on T1, T4 depends on T2 and T3
        graph.addDependency("T2", "T1");
        graph.addDependency("T3", "T1");
        graph.addDependency("T4", "T2");
        graph.addDependency("T4", "T3");

        List<String> order = TopologicalSort.sort(graph);
        assertEquals(4, order.size());
        assertEquals("T1", order.get(0), "T1 must come first as it has 0 prerequisites");
        assertTrue(order.indexOf("T2") > order.indexOf("T1"));
        assertTrue(order.indexOf("T3") > order.indexOf("T1"));
        assertTrue(order.indexOf("T4") > order.indexOf("T2"));
        assertTrue(order.indexOf("T4") > order.indexOf("T3"));
    }

    @Test
    @DisplayName("Direct circular dependency (A -> B -> A) throws CircularDependencyException")
    void testDirectCycleDetection() {
        graph.addDependency("B", "A"); // B depends on A
        graph.addDependency("A", "B"); // A depends on B (Cycle!)

        assertThrows(CircularDependencyException.class, () -> TopologicalSort.sort(graph));
    }

    @Test
    @DisplayName("Indirect circular dependency (A -> B -> C -> A) throws CircularDependencyException")
    void testIndirectCycleDetection() {
        graph.addDependency("B", "A");
        graph.addDependency("C", "B");
        graph.addDependency("A", "C"); // Cycle A -> B -> C -> A

        assertThrows(CircularDependencyException.class, () -> TopologicalSort.sort(graph));
    }

    @Test
    @DisplayName("Empty or single node graph behavior")
    void testEmptyAndSingleNodeGraph() {
        assertTrue(TopologicalSort.sort(new Graph()).isEmpty());

        Graph single = new Graph();
        single.addVertex("T1");
        List<String> order = TopologicalSort.sort(single);
        assertEquals(1, order.size());
        assertEquals("T1", order.get(0));
    }
}

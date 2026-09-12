package com.taskflow.algorithms;

import com.taskflow.datastructures.Graph;
import com.taskflow.datastructures.Queue;
import com.taskflow.exception.CircularDependencyException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Custom Topological Sort implementation using Kahn's Algorithm (BFS-based).
 * 
 * Algorithm Overview:
 * - Computes in-degrees for all vertices in the Task dependency graph.
 * - Enqueues vertices with an in-degree of 0 (no prerequisites remaining).
 * - Iteratively dequeues a vertex, appends it to execution order, and decrements in-degrees of dependents.
 * - Detects circular dependencies (cycles): If the sorted result count is less than total vertices,
 *   a dependency cycle exists and a {@link CircularDependencyException} is thrown.
 * 
 * Big-O Complexity Analysis:
 * - Time Complexity:  O(V + E) where V is vertices (tasks) and E is edges (dependencies).
 * - Space Complexity: O(V) for in-degree map, queue, and result list.
 */
public class TopologicalSort {

    /**
     * Performs topological sort on task dependency graph using Kahn's BFS Algorithm.
     * Uses custom {@link Queue} for zero in-degree processing.
     * 
     * @param graph Task dependency graph
     * @return List of task IDs in valid execution order (prerequisites first)
     * @throws CircularDependencyException if a dependency cycle is detected
     */
    public static List<String> sort(Graph graph) {
        if (graph == null || graph.getVertices().isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> vertices = graph.getVertices();
        Map<String, List<String>> adjList = graph.getAdjacencyList();
        Map<String, Integer> inDegrees = graph.calculateInDegrees();

        // Custom Queue for Kahn's BFS
        Queue<String> zeroInDegreeQueue = new Queue<>();

        // 1. Initialize queue with nodes having 0 in-degree (no prerequisites)
        for (String v : vertices) {
            if (inDegrees.getOrDefault(v, 0) == 0) {
                zeroInDegreeQueue.enqueue(v);
            }
        }

        List<String> executionOrder = new ArrayList<>();

        // 2. BFS Traversal
        while (!zeroInDegreeQueue.isEmpty()) {
            String u = zeroInDegreeQueue.dequeue();
            executionOrder.add(u);

            // Decrement in-degree for all dependent neighbors (u -> v)
            List<String> dependents = adjList.getOrDefault(u, new ArrayList<>());
            for (String v : dependents) {
                int updatedInDegree = inDegrees.get(v) - 1;
                inDegrees.put(v, updatedInDegree);

                if (updatedInDegree == 0) {
                    zeroInDegreeQueue.enqueue(v);
                }
            }
        }

        // 3. Cycle Detection Check
        if (executionOrder.size() != vertices.size()) {
            List<String> uncompletedVertices = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : inDegrees.entrySet()) {
                if (entry.getValue() > 0) {
                    uncompletedVertices.add(entry.getKey());
                }
            }
            throw new CircularDependencyException(
                    "Circular dependency detected! Cycle involves task IDs: " + uncompletedVertices);
        }

        return executionOrder;
    }
}

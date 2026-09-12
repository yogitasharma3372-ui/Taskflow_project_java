package com.taskflow.datastructures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Custom Directed Graph implementation using Adjacency Lists for task dependency management.
 * 
 * Dependency Model:
 * - A directed edge from V to U (V -> U) signifies that U depends on V (V must be completed before U).
 * - Adjacency List: key = prerequisite task ID (V), value = list of dependent task IDs (U).
 * 
 * Big-O Tradeoffs:
 * - Add Vertex: O(1)
 * - Add Edge:   O(1)
 * - Space Complexity: O(V + E) where V is vertices (tasks) and E is dependency edges.
 */
public class Graph {
    // Map of vertex ID -> list of outgoing adjacent vertex IDs (prerequisite -> dependents)
    private final Map<String, List<String>> adjList;
    private final Set<String> vertices;

    public Graph() {
        this.adjList = new HashMap<>();
        this.vertices = new HashSet<>();
    }

    /**
     * Adds a vertex to the graph.
     * Big-O: O(1).
     * 
     * @param vertexId Unique identifier for the vertex
     */
    public void addVertex(String vertexId) {
        if (vertexId != null && !vertexId.trim().isEmpty()) {
            vertices.add(vertexId);
            adjList.putIfAbsent(vertexId, new ArrayList<>());
        }
    }

    /**
     * Adds a directed dependency edge: prereqId -> taskId (meaning prerequisite task must complete before task).
     * Big-O: O(1).
     * 
     * @param taskId ID of task that depends on prereqId
     * @param prereqId ID of prerequisite task
     */
    public void addDependency(String taskId, String prereqId) {
        if (taskId == null || prereqId == null || taskId.equals(prereqId)) {
            return;
        }

        addVertex(taskId);
        addVertex(prereqId);

        List<String> dependents = adjList.get(prereqId);
        if (!dependents.contains(taskId)) {
            dependents.add(taskId);
        }
    }

    /**
     * Removes a directed dependency edge.
     */
    public void removeDependency(String taskId, String prereqId) {
        if (adjList.containsKey(prereqId)) {
            adjList.get(prereqId).remove(taskId);
        }
    }

    /**
     * Removes a vertex and all connected edges from the graph.
     * Big-O: O(V + E).
     */
    public void removeVertex(String vertexId) {
        if (!vertices.contains(vertexId)) return;

        vertices.remove(vertexId);
        adjList.remove(vertexId);

        // Remove incoming edges to this vertex
        for (List<String> dependents : adjList.values()) {
            dependents.remove(vertexId);
        }
    }

    /**
     * Calculates in-degree (number of prerequisites) for every vertex in the graph.
     * Big-O: O(V + E).
     * 
     * @return Map of vertex ID -> in-degree count
     */
    public Map<String, Integer> calculateInDegrees() {
        Map<String, Integer> inDegreeMap = new HashMap<>();
        for (String v : vertices) {
            inDegreeMap.put(v, 0);
        }

        for (Map.Entry<String, List<String>> entry : adjList.entrySet()) {
            for (String neighbor : entry.getValue()) {
                inDegreeMap.put(neighbor, inDegreeMap.getOrDefault(neighbor, 0) + 1);
            }
        }

        return inDegreeMap;
    }

    public Set<String> getVertices() {
        return Collections.unmodifiableSet(vertices);
    }

    public Map<String, List<String>> getAdjacencyList() {
        return Collections.unmodifiableMap(adjList);
    }

    public boolean containsVertex(String vertexId) {
        return vertices.contains(vertexId);
    }

    public void clear() {
        adjList.clear();
        vertices.clear();
    }
}

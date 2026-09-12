package com.taskflow.dto;

/**
 * Request body for adding a task dependency edge.
 * Represents: taskId depends on dependsOnId (prereqId).
 */
public class DependencyRequest {
    private String taskId;
    private String dependsOnId;

    public DependencyRequest() {}

    public DependencyRequest(String taskId, String dependsOnId) {
        this.taskId = taskId;
        this.dependsOnId = dependsOnId;
    }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getDependsOnId() { return dependsOnId; }
    public void setDependsOnId(String dependsOnId) { this.dependsOnId = dependsOnId; }
}

package com.taskflow.model;

import java.time.LocalDateTime;

/**
 * Model class representing an action performed in the system.
 * Stored in the undo Stack to support full state reversal.
 */
public class Action {
    private final ActionType type;
    private final Task task;
    private final String secondaryId; // Optional secondary identifier (e.g. for dependency edges)
    private final LocalDateTime timestamp;

    public Action(ActionType type, Task task) {
        this(type, task, null);
    }

    public Action(ActionType type, Task task, String secondaryId) {
        this.type = type;
        this.task = (task != null) ? task.copy() : null;
        this.secondaryId = secondaryId;
        this.timestamp = LocalDateTime.now();
    }

    public ActionType getType() { return type; }
    public Task getTask() { return (task != null) ? task.copy() : null; }
    public String getSecondaryId() { return secondaryId; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("Action[Type=%s, TaskID=%s, SecondaryID=%s, Time=%s]",
                type, (task != null ? task.getId() : "N/A"), secondaryId, timestamp);
    }
}

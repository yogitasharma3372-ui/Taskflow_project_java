package com.taskflow.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Domain model representing a Task in the TaskFlow system.
 * Implements {@link Comparable} to define natural priority ordering for the MinHeap.
 * 
 * Priority Rules:
 * 1. Lower numeric priority value = higher priority (1 is Urgent/Critical, 5 is Low).
 * 2. Tie-breaker 1: Earlier deadline comes first.
 * 3. Tie-breaker 2: Alphabetical order by title.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Task implements Comparable<Task> {
    private String id;
    private String title;
    private String description;
    private int priority; // 1 (highest) to 5 (lowest)
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime deadline;
    
    private Set<String> tags;
    private List<String> dependencies; // Task IDs that this task depends on
    private boolean completed;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime completedAt;

    /**
     * Default constructor for Jackson JSON deserialization.
     */
    public Task() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.tags = new HashSet<>();
        this.dependencies = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.completed = false;
    }

    /**
     * Constructs a new Task with the specified details.
     * 
     * @param title Task title
     * @param description Short task description
     * @param priority Priority value between 1 (highest) and 5 (lowest)
     * @param deadline Target deadline timestamp
     */
    public Task(String title, String description, int priority, LocalDateTime deadline) {
        this();
        this.title = title;
        this.description = description;
        this.priority = validatePriority(priority);
        this.deadline = deadline;
    }

    /**
     * Constructs a full Task with explicit ID, tags, and dependencies.
     */
    public Task(String id, String title, String description, int priority, LocalDateTime deadline, Set<String> tags, List<String> dependencies) {
        this.id = (id != null && !id.trim().isEmpty()) ? id : UUID.randomUUID().toString().substring(0, 8);
        this.title = title;
        this.description = description;
        this.priority = validatePriority(priority);
        this.deadline = deadline;
        this.tags = (tags != null) ? new HashSet<>(tags) : new HashSet<>();
        this.dependencies = (dependencies != null) ? new ArrayList<>(dependencies) : new ArrayList<>();
        this.completed = false;
        this.createdAt = LocalDateTime.now();
    }

    private int validatePriority(int priority) {
        if (priority < 1 || priority > 5) {
            throw new IllegalArgumentException("Priority must be between 1 (highest) and 5 (lowest). Given: " + priority);
        }
        return priority;
    }

    /**
     * Compares two tasks for priority ordering in the MinHeap.
     * Higher priority tasks (lower numeric priority) rank first.
     * 
     * Big-O: O(1) comparison time.
     */
    @Override
    public int compareTo(Task other) {
        if (other == null) return -1;
        
        // 1. Primary: Priority numeric value (1 is highest)
        int priorityComp = Integer.compare(this.priority, other.priority);
        if (priorityComp != 0) {
            return priorityComp;
        }

        // 2. Secondary: Deadline (earlier deadline is higher priority)
        if (this.deadline != null && other.deadline != null) {
            int deadlineComp = this.deadline.compareTo(other.deadline);
            if (deadlineComp != 0) {
                return deadlineComp;
            }
        } else if (this.deadline != null) {
            return -1;
        } else if (other.deadline != null) {
            return 1;
        }

        // 3. Fallback: Alphabetical title
        if (this.title != null && other.title != null) {
            int titleComp = this.title.compareToIgnoreCase(other.title);
            if (titleComp != 0) {
                return titleComp;
            }
        }

        // 4. Final tie-breaker: ID comparison to ensure consistency
        return this.id.compareTo(other.id);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = validatePriority(priority); }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = (tags != null) ? tags : new HashSet<>(); }

    public List<String> getDependencies() { return dependencies; }
    public void setDependencies(List<String> dependencies) { this.dependencies = (dependencies != null) ? dependencies : new ArrayList<>(); }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            this.tags.add(tag.trim().toLowerCase());
        }
    }

    public void addDependency(String dependsOnTaskId) {
        if (dependsOnTaskId != null && !dependsOnTaskId.trim().isEmpty() && !this.dependencies.contains(dependsOnTaskId)) {
            this.dependencies.add(dependsOnTaskId);
        }
    }

    /**
     * Creates a deep copy of this Task instance for snapshot history and undo stack safety.
     */
    public Task copy() {
        Task copy = new Task();
        copy.id = this.id;
        copy.title = this.title;
        copy.description = this.description;
        copy.priority = this.priority;
        copy.deadline = this.deadline;
        copy.tags = new HashSet<>(this.tags);
        copy.dependencies = new ArrayList<>(this.dependencies);
        copy.completed = this.completed;
        copy.createdAt = this.createdAt;
        copy.completedAt = this.completedAt;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Task[ID=%s, Title='%s', Priority=%d, Deadline=%s, Completed=%b, Tags=%s, Deps=%s]",
                id, title, priority, (deadline != null ? deadline.toString() : "None"), completed, tags, dependencies);
    }
}

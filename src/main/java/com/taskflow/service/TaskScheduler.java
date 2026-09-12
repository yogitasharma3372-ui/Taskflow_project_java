package com.taskflow.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskflow.algorithms.MergeSort;
import com.taskflow.algorithms.QuickSort;
import com.taskflow.algorithms.TopologicalSort;
import com.taskflow.datastructures.Graph;
import com.taskflow.datastructures.MinHeap;
import com.taskflow.datastructures.Queue;
import com.taskflow.datastructures.Stack;
import com.taskflow.datastructures.Trie;
import com.taskflow.exception.CircularDependencyException;
import com.taskflow.exception.InvalidTaskDataException;
import com.taskflow.exception.TaskNotFoundException;
import com.taskflow.model.Action;
import com.taskflow.model.ActionType;
import com.taskflow.model.Task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service Layer orchestrator uniting all core custom data structures and algorithms:
 * 
 * 1. MinHeap: Priority-based task retrieval.
 * 2. Stack: Multi-level Undo operation stack.
 * 3. Queue: Activity log / task completion history.
 * 4. Trie: Prefix-based task autocomplete search.
 * 5. Graph & TopologicalSort: Directed dependency graph with cycle detection.
 * 6. HashMaps: O(1) task lookups by ID and by Tag.
 * 7. Custom MergeSort & QuickSort: Benchmarking and flexible sorting.
 */
public class TaskScheduler {

    private final MinHeap<Task> minHeap;
    private final Stack<Action> undoStack;
    private final Queue<Task> historyQueue;
    private final Trie trie;
    private final Graph dependencyGraph;

    // O(1) Index Lookups
    private final Map<String, Task> taskById;
    private final Map<String, Set<Task>> tasksByTag;

    private final ObjectMapper objectMapper;

    public TaskScheduler() {
        this.minHeap = new MinHeap<>();
        this.undoStack = new Stack<>();
        this.historyQueue = new Queue<>();
        this.trie = new Trie();
        this.dependencyGraph = new Graph();
        this.taskById = new HashMap<>();
        this.tasksByTag = new HashMap<>();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Adds a new task to the scheduler and indexes it across all data structures.
     * 
     * Big-O Time Complexity:
     * - HashMap ID & Tag index: O(1)
     * - MinHeap insert: O(log N)
     * - Trie insert: O(L) where L is title length
     * - Graph add vertex: O(1)
     * - Stack push: O(1)
     * Overall: O(log N + L)
     * 
     * @param task Task to add
     */
    public synchronized void addTask(Task task) {
        if (task == null) {
            throw new InvalidTaskDataException("Cannot add null task");
        }
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new InvalidTaskDataException("Task title cannot be empty");
        }
        if (taskById.containsKey(task.getId())) {
            throw new InvalidTaskDataException("Task with ID '" + task.getId() + "' already exists");
        }

        // 1. Add to HashMap index
        taskById.put(task.getId(), task);

        // 2. Add to Tag index
        if (task.getTags() != null) {
            for (String tag : task.getTags()) {
                tasksByTag.computeIfAbsent(tag.toLowerCase(), k -> new HashSet<>()).add(task);
            }
        }

        // 3. Add to MinHeap (if not completed)
        if (!task.isCompleted()) {
            minHeap.insert(task);
        }

        // 4. Add to Trie
        trie.insert(task);

        // 5. Add to Graph
        dependencyGraph.addVertex(task.getId());
        if (task.getDependencies() != null) {
            for (String prereqId : task.getDependencies()) {
                if (taskById.containsKey(prereqId)) {
                    dependencyGraph.addDependency(task.getId(), prereqId);
                }
            }
        }

        // 6. Push to Undo Stack
        undoStack.push(new Action(ActionType.ADD, task));
    }

    /**
     * Retrieves the next highest-priority pending task from the MinHeap.
     * Big-O: O(1) peek time.
     * 
     * @return Highest priority task, or null if no pending tasks exist.
     */
    public synchronized Task getNextTask() {
        if (minHeap.isEmpty()) {
            return null;
        }
        return minHeap.peek();
    }

    /**
     * Completes the highest priority task currently in the MinHeap.
     * Big-O: O(log N) extractMin + O(1) Queue enqueue + O(1) Stack push = O(log N).
     * 
     * @return The completed Task
     * @throws TaskNotFoundException if no pending tasks exist
     */
    public synchronized Task completeNextTask() {
        if (minHeap.isEmpty()) {
            throw new TaskNotFoundException("No pending tasks available to complete");
        }
        Task completedTask = minHeap.extractMin();
        return processTaskCompletion(completedTask);
    }

    /**
     * Completes a task by its specific ID.
     */
    public synchronized Task completeTaskById(String taskId) {
        Task task = taskById.get(taskId);
        if (task == null) {
            throw new TaskNotFoundException("Task with ID '" + taskId + "' not found");
        }
        if (task.isCompleted()) {
            throw new InvalidTaskDataException("Task '" + taskId + "' is already completed");
        }

        minHeap.removeIf(t -> t.getId().equals(taskId));
        return processTaskCompletion(task);
    }

    private Task processTaskCompletion(Task task) {
        task.setCompleted(true);
        task.setCompletedAt(java.time.LocalDateTime.now());
        historyQueue.enqueue(task.copy());
        undoStack.push(new Action(ActionType.COMPLETE, task));
        return task;
    }

    /**
     * Undoes the last action performed in the system.
     * Pops from the Stack and reverses the operation.
     * 
     * Big-O: O(log N) depending on action type.
     * 
     * @return Summary string of undone action
     */
    public synchronized String undoLastAction() {
        if (undoStack.isEmpty()) {
            return "Nothing to undo.";
        }

        Action action = undoStack.pop();
        ActionType type = action.getType();
        Task targetTask = action.getTask();

        switch (type) {
            case ADD:
                // Reverse ADD: Delete the added task
                taskById.remove(targetTask.getId());
                minHeap.removeIf(t -> t.getId().equals(targetTask.getId()));
                trie.remove(targetTask);
                dependencyGraph.removeVertex(targetTask.getId());
                if (targetTask.getTags() != null) {
                    for (String tag : targetTask.getTags()) {
                        Set<Task> tagSet = tasksByTag.get(tag.toLowerCase());
                        if (tagSet != null) {
                            tagSet.remove(targetTask);
                        }
                    }
                }
                return "Undone: Added task '" + targetTask.getTitle() + "' (ID: " + targetTask.getId() + ") removed.";

            case COMPLETE:
                // Reverse COMPLETE: Restore task to active state
                Task activeTask = taskById.get(targetTask.getId());
                if (activeTask != null) {
                    activeTask.setCompleted(false);
                    activeTask.setCompletedAt(null);
                    minHeap.insert(activeTask);
                    historyQueue.removeIf(t -> t.getId().equals(targetTask.getId()));
                }
                return "Undone: Completion of task '" + targetTask.getTitle() + "' restored to pending state.";

            case ADD_DEPENDENCY:
                // Reverse ADD_DEPENDENCY: Remove edge
                String taskId = targetTask.getId();
                String prereqId = action.getSecondaryId();
                dependencyGraph.removeDependency(taskId, prereqId);
                Task mainTask = taskById.get(taskId);
                if (mainTask != null) {
                    mainTask.getDependencies().remove(prereqId);
                }
                return "Undone: Dependency edge (" + taskId + " depends on " + prereqId + ") removed.";

            default:
                return "Undone action: " + type;
        }
    }

    /**
     * Searches tasks by title prefix using Trie autocomplete.
     * Big-O: O(L + K) where L is prefix length, K is match count.
     */
    public List<Task> searchTasksByPrefix(String prefix) {
        return trie.autocomplete(prefix);
    }

    /**
     * Searches tasks by tag using HashMap index.
     * Big-O: O(1) lookup.
     */
    public List<Task> searchTasksByTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return Collections.emptyList();
        }
        Set<Task> set = tasksByTag.get(tag.trim().toLowerCase());
        return (set != null) ? new ArrayList<>(set) : Collections.emptyList();
    }

    /**
     * Container holding sorted tasks and execution benchmark duration.
     */
    public record SortBenchmarkResult(List<Task> sortedTasks, long durationNanos, String algorithm, String sortKey) {}

    /**
     * Sorts all pending tasks by chosen sort key and sorting algorithm, benchmarking execution time.
     * 
     * @param sortKey "priority", "deadline", or "title"
     * @param algorithm "mergesort" or "quicksort"
     * @return SortBenchmarkResult containing sorted tasks and execution time in nanoseconds
     */
    public SortBenchmarkResult sortTasks(String sortKey, String algorithm) {
        List<Task> allTasks = getAllTasks();
        Comparator<Task> comparator;

        switch (sortKey.toLowerCase()) {
            case "deadline":
                comparator = Comparator.comparing(Task::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "title":
            case "alphabetical":
                comparator = Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER);
                break;
            case "priority":
            default:
                comparator = Task::compareTo;
                break;
        }

        long startTime = System.nanoTime();
        if ("quicksort".equalsIgnoreCase(algorithm)) {
            QuickSort.sort(allTasks, comparator);
        } else {
            MergeSort.sort(allTasks, comparator);
        }
        long duration = System.nanoTime() - startTime;

        return new SortBenchmarkResult(allTasks, duration, algorithm.toUpperCase(), sortKey.toUpperCase());
    }

    /**
     * Adds a dependency edge between two tasks and validates acyclic property via Topological Sort.
     * Edge: taskId depends on prereqId.
     */
    public synchronized void addDependency(String taskId, String prereqId) {
        Task task = taskById.get(taskId);
        Task prereq = taskById.get(prereqId);

        if (task == null) {
            throw new TaskNotFoundException("Task ID '" + taskId + "' does not exist");
        }
        if (prereq == null) {
            throw new TaskNotFoundException("Prerequisite Task ID '" + prereqId + "' does not exist");
        }
        if (taskId.equals(prereqId)) {
            throw new InvalidTaskDataException("A task cannot depend on itself");
        }

        // Add edge
        dependencyGraph.addDependency(taskId, prereqId);
        task.addDependency(prereqId);

        // Cycle check validation using Topological Sort
        try {
            TopologicalSort.sort(dependencyGraph);
        } catch (CircularDependencyException e) {
            // Revert edge addition
            dependencyGraph.removeDependency(taskId, prereqId);
            task.getDependencies().remove(prereqId);
            throw new CircularDependencyException("Cannot add dependency: Creates a circular dependency loop! (" + taskId + " -> " + prereqId + ")");
        }

        undoStack.push(new Action(ActionType.ADD_DEPENDENCY, task, prereqId));
    }

    /**
     * Computes valid topological execution order of all tasks based on dependency graph.
     * Big-O: O(V + E).
     * 
     * @return List of tasks in topological execution order
     */
    public List<Task> getValidExecutionOrder() {
        List<String> orderedIds = TopologicalSort.sort(dependencyGraph);
        List<Task> orderedTasks = new ArrayList<>();
        for (String id : orderedIds) {
            Task task = taskById.get(id);
            if (task != null) {
                orderedTasks.add(task);
            }
        }
        return orderedTasks;
    }

    /**
     * Returns history log of completed tasks in FIFO order.
     * Big-O: O(N).
     */
    public List<Task> getTaskHistoryLog() {
        return historyQueue.toList();
    }

    /**
     * Returns all pending (uncompleted) tasks.
     */
    public List<Task> getPendingTasks() {
        List<Task> pending = new ArrayList<>();
        for (Task t : taskById.values()) {
            if (!t.isCompleted()) {
                pending.add(t);
            }
        }
        return pending;
    }

    public Task getTaskById(String id) {
        return taskById.get(id);
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(taskById.values());
    }

    public Graph getDependencyGraph() {
        return dependencyGraph;
    }

    /**
     * Saves all system tasks to a JSON file.
     */
    public synchronized void saveToFile(String filePath) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), getAllTasks());
    }

    /**
     * Loads tasks from a JSON file and rebuilds all data structures.
     */
    public synchronized void loadFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new TaskNotFoundException("File not found: " + filePath);
        }

        List<Task> loadedTasks = objectMapper.readValue(file, new TypeReference<List<Task>>() {});
        
        // Reset all internal structures
        minHeap.clear();
        undoStack.clear();
        historyQueue.clear();
        trie.clear();
        dependencyGraph.clear();
        taskById.clear();
        tasksByTag.clear();

        // Populate with loaded tasks
        for (Task task : loadedTasks) {
            taskById.put(task.getId(), task);

            if (!task.isCompleted()) {
                minHeap.insert(task);
            } else {
                historyQueue.enqueue(task);
            }

            trie.insert(task);
            dependencyGraph.addVertex(task.getId());

            if (task.getTags() != null) {
                for (String tag : task.getTags()) {
                    tasksByTag.computeIfAbsent(tag.toLowerCase(), k -> new HashSet<>()).add(task);
                }
            }
        }

        // Rebuild dependency graph edges
        for (Task task : loadedTasks) {
            if (task.getDependencies() != null) {
                for (String prereqId : task.getDependencies()) {
                    if (taskById.containsKey(prereqId)) {
                        dependencyGraph.addDependency(task.getId(), prereqId);
                    }
                }
            }
        }
    }
}

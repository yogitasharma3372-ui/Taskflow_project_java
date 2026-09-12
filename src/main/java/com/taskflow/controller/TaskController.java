package com.taskflow.controller;

import com.taskflow.dto.ApiResponse;
import com.taskflow.dto.DependencyRequest;
import com.taskflow.dto.SortRaceResponse;
import com.taskflow.model.Task;
import com.taskflow.service.TaskScheduler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring REST Controller exposing TaskFlow scheduling operations via HTTP.
 * Keeps business logic decoupled by delegating to {@link TaskScheduler}.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskScheduler scheduler;

    public TaskController(TaskScheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * POST /api/tasks - Add a new task to the scheduler.
     */
    @PostMapping("/tasks")
    public ResponseEntity<ApiResponse<Task>> addTask(@RequestBody Task task) {
        scheduler.addTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(task));
    }

    /**
     * GET /api/tasks - List all tasks, optionally sorted using custom MergeSort or QuickSort algorithms.
     * Query Params: ?sortBy=priority|deadline|title & ?algorithm=merge|quick
     */
    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<List<Task>>> getAllTasks(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "merge") String algorithm) {
        
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            TaskScheduler.SortBenchmarkResult result = scheduler.sortTasks(sortBy, algorithm);
            return ResponseEntity.ok(ApiResponse.ok(result.sortedTasks()));
        }
        return ResponseEntity.ok(ApiResponse.ok(scheduler.getAllTasks()));
    }

    /**
     * GET /api/tasks/next - Peek the highest priority task from the MinHeap root.
     */
    @GetMapping("/tasks/next")
    public ResponseEntity<ApiResponse<Task>> getNextTask() {
        Task next = scheduler.getNextTask();
        return ResponseEntity.ok(ApiResponse.ok(next));
    }

    /**
     * POST /api/tasks/{id}/complete - Complete a task by ID.
     */
    @PostMapping("/tasks/{id}/complete")
    public ResponseEntity<ApiResponse<Task>> completeTask(@PathVariable String id) {
        Task completed = scheduler.completeTaskById(id);
        return ResponseEntity.ok(ApiResponse.ok(completed));
    }

    /**
     * POST /api/undo - Undo the last action performed in the system.
     */
    @PostMapping("/undo")
    public ResponseEntity<ApiResponse<Map<String, String>>> undoLastAction() {
        String resultMessage = scheduler.undoLastAction();
        Map<String, String> payload = new HashMap<>();
        payload.put("message", resultMessage);
        return ResponseEntity.ok(ApiResponse.ok(payload));
    }

    /**
     * GET /api/tasks/search?prefix= - Trie-based autocomplete search by title prefix.
     */
    @GetMapping("/tasks/search")
    public ResponseEntity<ApiResponse<List<Task>>> searchTasksByPrefix(@RequestParam String prefix) {
        List<Task> results = scheduler.searchTasksByPrefix(prefix);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    /**
     * GET /api/tasks/tag/{tag} - HashMap index lookup by tag.
     */
    @GetMapping("/tasks/tag/{tag}")
    public ResponseEntity<ApiResponse<List<Task>>> searchTasksByTag(@PathVariable String tag) {
        List<Task> results = scheduler.searchTasksByTag(tag);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    /**
     * POST /api/dependencies - Add a directed dependency edge (taskId depends on dependsOnId).
     */
    @PostMapping("/dependencies")
    public ResponseEntity<ApiResponse<Map<String, String>>> addDependency(@RequestBody DependencyRequest request) {
        scheduler.addDependency(request.getTaskId(), request.getDependsOnId());
        Map<String, String> payload = new HashMap<>();
        payload.put("message", "Dependency edge (" + request.getTaskId() + " depends on " + request.getDependsOnId() + ") added successfully.");
        return ResponseEntity.ok(ApiResponse.ok(payload));
    }

    /**
     * GET /api/tasks/execution-order - Topological sort execution sequence (Kahn's BFS).
     */
    @GetMapping("/tasks/execution-order")
    public ResponseEntity<ApiResponse<List<Task>>> getExecutionOrder() {
        List<Task> order = scheduler.getValidExecutionOrder();
        return ResponseEntity.ok(ApiResponse.ok(order));
    }

    /**
     * GET /api/history - Task history log (Queue FIFO order).
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<Task>>> getTaskHistory() {
        List<Task> history = scheduler.getTaskHistoryLog();
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    /**
     * GET /api/tasks/benchmark - Run Sorting Algorithm Race (MergeSort vs QuickSort).
     */
    @GetMapping("/tasks/benchmark")
    public ResponseEntity<ApiResponse<SortRaceResponse>> benchmarkSorts() {
        TaskScheduler.SortBenchmarkResult mergeRes = scheduler.sortTasks("priority", "mergesort");
        TaskScheduler.SortBenchmarkResult quickRes = scheduler.sortTasks("priority", "quicksort");

        SortRaceResponse.AlgorithmStats mergeStats = new SortRaceResponse.AlgorithmStats(
                "MERGESORT", "O(N log N)", "O(N)", true, mergeRes.durationNanos(), mergeRes.sortedTasks().size()
        );

        SortRaceResponse.AlgorithmStats quickStats = new SortRaceResponse.AlgorithmStats(
                "QUICKSORT", "O(N log N)", "O(log N)", false, quickRes.durationNanos(), quickRes.sortedTasks().size()
        );

        SortRaceResponse response = new SortRaceResponse(mergeStats, quickStats, mergeRes.sortedTasks());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * GET /api/health - Observability endpoint returning server uptime & total task count.
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealth() {
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("uptimeSeconds", uptimeMillis / 1000);
        healthInfo.put("totalTasksTracked", scheduler.getAllTasks().size());
        healthInfo.put("pendingTasksCount", scheduler.getPendingTasks().size());
        healthInfo.put("completedTasksCount", scheduler.getTaskHistoryLog().size());
        return ResponseEntity.ok(ApiResponse.ok(healthInfo));
    }
}

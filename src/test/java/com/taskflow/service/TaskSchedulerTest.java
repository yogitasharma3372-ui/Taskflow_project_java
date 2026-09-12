package com.taskflow.service;

import com.taskflow.exception.CircularDependencyException;
import com.taskflow.exception.TaskNotFoundException;
import com.taskflow.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TaskScheduler Integration Tests")
class TaskSchedulerTest {

    private TaskScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new TaskScheduler();
    }

    @Test
    @DisplayName("AddTask and GetNextTask priority order")
    void testAddTaskAndPriorityPeek() {
        Task t1 = new Task("T1", "Desc", 3, LocalDateTime.now());
        Task t2 = new Task("T2", "Desc", 1, LocalDateTime.now());

        scheduler.addTask(t1);
        scheduler.addTask(t2);

        assertEquals(t2, scheduler.getNextTask(), "Highest priority task (1) should be returned first");
    }

    @Test
    @DisplayName("CompleteTask and Undo functionality")
    void testCompleteAndUndo() {
        Task t1 = new Task("Task 1", "Desc", 1, LocalDateTime.now());
        scheduler.addTask(t1);

        Task completed = scheduler.completeNextTask();
        assertTrue(completed.isCompleted());
        assertNotNull(completed.getCompletedAt(), "completedAt timestamp should be populated");
        assertEquals(1, scheduler.getTaskHistoryLog().size());
        assertNull(scheduler.getNextTask(), "MinHeap should be empty after completing only task");

        // Undo completion
        String undoMsg = scheduler.undoLastAction();
        assertTrue(undoMsg.contains("restored"));
        assertNotNull(scheduler.getNextTask());
        assertFalse(scheduler.getNextTask().isCompleted());
        assertNull(scheduler.getNextTask().getCompletedAt(), "completedAt timestamp should be reset on undo");
    }

    @Test
    @DisplayName("Search by Prefix and Tag")
    void testSearching() {
        Task t1 = new Task("Refactor Authentication Module", "Desc", 1, LocalDateTime.now());
        t1.addTag("security");
        Task t2 = new Task("Refactor Database Queries", "Desc", 2, LocalDateTime.now());
        t2.addTag("database");

        scheduler.addTask(t1);
        scheduler.addTask(t2);

        List<Task> refactorTasks = scheduler.searchTasksByPrefix("refactor");
        assertEquals(2, refactorTasks.size());

        List<Task> secTasks = scheduler.searchTasksByTag("security");
        assertEquals(1, secTasks.size());
        assertEquals(t1, secTasks.get(0));
    }

    @Test
    @DisplayName("Add dependency with cycle detection prevention")
    void testDependencyAndCyclePrevention() {
        Task t1 = new Task("T1", "Desc", 1, LocalDateTime.now());
        Task t2 = new Task("T2", "Desc", 2, LocalDateTime.now());
        t1.setId("T1");
        t2.setId("T2");

        scheduler.addTask(t1);
        scheduler.addTask(t2);

        scheduler.addDependency("T2", "T1"); // T2 depends on T1

        assertThrows(CircularDependencyException.class, () -> scheduler.addDependency("T1", "T2"),
                "Adding T1 depends on T2 should throw CircularDependencyException");
    }

    @Test
    @DisplayName("JSON File Save and Load Persistence")
    void testJSONSaveAndLoad(@TempDir Path tempDir) throws IOException {
        File jsonFile = tempDir.resolve("test_tasks.json").toFile();

        Task t1 = new Task("Persistent Task 1", "Desc 1", 1, LocalDateTime.now().plusDays(1));
        t1.addTag("persist");
        scheduler.addTask(t1);

        scheduler.saveToFile(jsonFile.getAbsolutePath());
        assertTrue(jsonFile.exists());

        // Create new scheduler and load
        TaskScheduler loadedScheduler = new TaskScheduler();
        loadedScheduler.loadFromFile(jsonFile.getAbsolutePath());

        assertEquals(1, loadedScheduler.getAllTasks().size());
        Task loadedTask = loadedScheduler.getTaskById(t1.getId());
        assertNotNull(loadedTask);
        assertEquals("Persistent Task 1", loadedTask.getTitle());
    }
}

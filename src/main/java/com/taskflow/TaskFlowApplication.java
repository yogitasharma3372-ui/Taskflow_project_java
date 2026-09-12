package com.taskflow;

import com.taskflow.model.Task;
import com.taskflow.service.TaskScheduler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Spring Boot Application Entry Point for TaskFlow.
 * Exposes REST API backend and serves the Glassmorphism Web Frontend.
 */
@SpringBootApplication
public class TaskFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskFlowApplication.class, args);
    }

    /**
     * Bean supplier for TaskScheduler, pre-loaded with demo data for immediate web visualizer interaction.
     */
    @Bean
    public TaskScheduler taskScheduler() {
        TaskScheduler scheduler = new TaskScheduler();
        loadDemoData(scheduler);
        return scheduler;
    }

    private static void loadDemoData(TaskScheduler scheduler) {
        Task t1 = new Task("T1", "Setup Project Architecture", "Initialize Maven pom.xml and directory layout", 1, LocalDateTime.now().plusHours(4), new HashSet<>(Arrays.asList("architecture", "maven")), null);
        Task t2 = new Task("T2", "Implement MinHeap Data Structure", "Create custom array-based min binary heap", 2, LocalDateTime.now().plusHours(8), new HashSet<>(Arrays.asList("dsa", "core")), Arrays.asList("T1"));
        Task t3 = new Task("T3", "Implement Graph & Topological Sort", "Build adjacency list directed graph & Kahn BFS algorithm", 2, LocalDateTime.now().plusHours(12), new HashSet<>(Arrays.asList("dsa", "graph")), Arrays.asList("T1"));
        Task t4 = new Task("T4", "Build TaskScheduler Service Layer", "Integrate MinHeap, Trie, Stack, Queue, HashMaps", 1, LocalDateTime.now().plusHours(16), new HashSet<>(Arrays.asList("service", "core")), Arrays.asList("T2", "T3"));
        Task t5 = new Task("T5", "Implement CLI Menu & Web Dashboard", "Interactive console loop & Glassmorphism UI", 3, LocalDateTime.now().plusHours(24), new HashSet<>(Arrays.asList("ui", "web")), Arrays.asList("T4"));
        Task t6 = new Task("T6", "Write Comprehensive JUnit 5 Tests", "Test custom data structures, sorting, edge cases", 2, LocalDateTime.now().plusHours(36), new HashSet<>(Arrays.asList("testing", "qa")), Arrays.asList("T4"));

        t1.setId("T1");
        t2.setId("T2");
        t3.setId("T3");
        t4.setId("T4");
        t5.setId("T5");
        t6.setId("T6");

        scheduler.addTask(t1);
        scheduler.addTask(t2);
        scheduler.addTask(t3);
        scheduler.addTask(t4);
        scheduler.addTask(t5);
        scheduler.addTask(t6);
    }
}

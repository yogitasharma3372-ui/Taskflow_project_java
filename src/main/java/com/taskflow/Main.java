package com.taskflow;

import com.taskflow.gui.TaskFlowGUI;
import com.taskflow.model.Task;
import com.taskflow.service.TaskScheduler;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

/**
 * CLI Entry Point for TaskFlow application.
 * Provides an interactive menu loop for priority scheduling, searching, sorting, graph dependency resolution, and benchmarking.
 */
public class Main {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {
        // Check if GUI flag is set
        if (args.length > 0 && "--gui".equalsIgnoreCase(args[0])) {
            launchGUI(new TaskScheduler());
            return;
        }

        TaskScheduler scheduler = new TaskScheduler();
        Scanner scanner = new Scanner(System.in);

        printBanner();

        // Ask user if they want demo sample tasks pre-loaded
        System.out.print("Pre-load demo software project tasks & dependencies? (Y/n): ");
        String demoChoice = scanner.nextLine().trim();
        if (demoChoice.isEmpty() || demoChoice.equalsIgnoreCase("y")) {
            loadDemoData(scheduler);
            System.out.println("✅ Pre-loaded 6 sample tasks with dependencies and tags.\n");
        }

        boolean exit = false;
        while (!exit) {
            printMenu();
            System.out.print("Enter choice (1-16): ");
            String input = scanner.nextLine().trim();

            System.out.println("\n------------------------------------------------------------");
            try {
                switch (input) {
                    case "1" -> addTaskUI(scheduler, scanner);
                    case "2" -> viewNextTaskUI(scheduler);
                    case "3" -> completeNextTaskUI(scheduler);
                    case "4" -> completeTaskByIdUI(scheduler, scanner);
                    case "5" -> undoUI(scheduler);
                    case "6" -> searchPrefixUI(scheduler, scanner);
                    case "7" -> searchTagUI(scheduler, scanner);
                    case "8" -> sortTasksUI(scheduler, scanner);
                    case "9" -> addDependencyUI(scheduler, scanner);
                    case "10" -> viewTopologicalOrderUI(scheduler);
                    case "11" -> viewHistoryLogUI(scheduler);
                    case "12" -> saveToFileUI(scheduler, scanner);
                    case "13" -> loadFromFileUI(scheduler, scanner);
                    case "14" -> benchmarkSortsUI(scheduler);
                    case "15" -> launchGUI(scheduler);
                    case "16", "exit", "quit" -> {
                        exit = true;
                        System.out.println("Thank you for using TaskFlow! Exiting application.");
                    }
                    default -> System.out.println("❌ Invalid choice. Please enter a number between 1 and 16.");
                }
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
            System.out.println("------------------------------------------------------------\n");
        }
    }

    private static void printBanner() {
        System.out.println("""
                ============================================================
                  ████████╗█████╗ ███████╗██╗██╗      ██████╗ ██╗    ██╗
                  ╚══██╔══╝██╔══██╗██╔════╝██║██║     ██╔═══██╗██║    ██║
                     ██║   ███████║███████╗██║██║     ██║   ██║██║ █╗ ██║
                     ██║   ██╔══██║╚════██║██║██║     ██║   ██║██║███╗██║
                     ██║   ██║  ██║███████║██║███████╗╚██████╔╝╚███╔███╔╝
                     ╚═╝   ╚═╝  ╚═╝╚══════╝╚═╝╚══════╝ ╚═════╝  ╚══╝╚══╝ 
                  Priority Task Scheduler & Data Structures Portfolio
                ============================================================
                """);
    }

    private static void printMenu() {
        System.out.println("""
                --- CLI MAIN MENU ---
                 1.  Add New Task
                 2.  View Next Priority Task (MinHeap Peek)
                 3.  Complete Next Priority Task (MinHeap ExtractMin)
                 4.  Complete Task by ID
                 5.  Undo Last Action (Stack Pop)
                 6.  Search Tasks by Title Prefix (Trie Autocomplete)
                 7.  Search Tasks by Tag (HashMap Index)
                 8.  Sort All Tasks (Choose Algorithm + Sort Key)
                 9.  Add Task Dependency (Graph Edge)
                 10. View Execution Order (Topological Sort / Kahn's BFS)
                 11. View Task History Log (Queue FIFO)
                 12. Save Tasks to File (JSON)
                 13. Load Tasks from File (JSON)
                 14. Benchmark MergeSort vs QuickSort Performance
                 15. Launch Swing GUI Visualizer
                 16. Exit Application
                """);
    }

    private static void addTaskUI(TaskScheduler scheduler, Scanner scanner) {
        System.out.println("--- Add New Task ---");
        System.out.print("Enter Title: ");
        String title = scanner.nextLine().trim();

        System.out.print("Enter Description: ");
        String description = scanner.nextLine().trim();

        System.out.print("Enter Priority (1 = Urgent/Highest, 5 = Lowest): ");
        int priority = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Enter Deadline (yyyy-MM-dd HH:mm) or press Enter for default (+2 days): ");
        String deadlineStr = scanner.nextLine().trim();
        LocalDateTime deadline = deadlineStr.isEmpty() ? LocalDateTime.now().plusDays(2) : LocalDateTime.parse(deadlineStr, DATE_FORMATTER);

        System.out.print("Enter Tags (comma separated, e.g. dev,urgent): ");
        String tagsStr = scanner.nextLine().trim();

        Task task = new Task(title, description, priority, deadline);
        if (!tagsStr.isEmpty()) {
            for (String tag : tagsStr.split(",")) {
                task.addTag(tag);
            }
        }

        scheduler.addTask(task);
        System.out.println("✅ Task added successfully! Generated ID: " + task.getId());
    }

    private static void viewNextTaskUI(TaskScheduler scheduler) {
        Task next = scheduler.getNextTask();
        if (next == null) {
            System.out.println("ℹ️ No pending tasks in MinHeap.");
        } else {
            System.out.println("⭐ NEXT HIGHEST PRIORITY TASK (MinHeap Root):");
            displayTask(next);
        }
    }

    private static void completeNextTaskUI(TaskScheduler scheduler) {
        Task completed = scheduler.completeNextTask();
        System.out.println("✅ Completed highest priority task: " + completed.getTitle() + " (ID: " + completed.getId() + ")");
    }

    private static void completeTaskByIdUI(TaskScheduler scheduler, Scanner scanner) {
        System.out.print("Enter Task ID to complete: ");
        String id = scanner.nextLine().trim();
        Task completed = scheduler.completeTaskById(id);
        System.out.println("✅ Completed task: " + completed.getTitle() + " (ID: " + completed.getId() + ")");
    }

    private static void undoUI(TaskScheduler scheduler) {
        String msg = scheduler.undoLastAction();
        System.out.println("↩ " + msg);
    }

    private static void searchPrefixUI(TaskScheduler scheduler, Scanner scanner) {
        System.out.print("Enter Title Prefix to Search (Trie Autocomplete): ");
        String prefix = scanner.nextLine().trim();
        List<Task> results = scheduler.searchTasksByPrefix(prefix);
        System.out.println("🔍 Found " + results.size() + " task(s) matching prefix '" + prefix + "':");
        for (Task t : results) {
            displayTask(t);
        }
    }

    private static void searchTagUI(TaskScheduler scheduler, Scanner scanner) {
        System.out.print("Enter Tag to Search (HashMap Index): ");
        String tag = scanner.nextLine().trim();
        List<Task> results = scheduler.searchTasksByTag(tag);
        System.out.println("🏷️ Found " + results.size() + " task(s) with tag '" + tag + "':");
        for (Task t : results) {
            displayTask(t);
        }
    }

    private static void sortTasksUI(TaskScheduler scheduler, Scanner scanner) {
        System.out.println("Choose Sort Key: 1) Priority  2) Deadline  3) Title/Alphabetical");
        System.out.print("Choice: ");
        String keyChoice = scanner.nextLine().trim();
        String key = switch (keyChoice) {
            case "2" -> "deadline";
            case "3" -> "title";
            default -> "priority";
        };

        System.out.println("Choose Sorting Algorithm: 1) MergeSort (Stable, O(N log N))  2) QuickSort (In-Place, Median-of-Three)");
        System.out.print("Choice: ");
        String algoChoice = scanner.nextLine().trim();
        String algo = "2".equals(algoChoice) ? "quicksort" : "mergesort";

        TaskScheduler.SortBenchmarkResult result = scheduler.sortTasks(key, algo);
        System.out.printf("📊 Sorted %d tasks using %s by %s in %d ns (%.3f ms):\n",
                result.sortedTasks().size(), result.algorithm(), result.sortKey(), result.durationNanos(), result.durationNanos() / 1_000_000.0);

        for (Task t : result.sortedTasks()) {
            displayTask(t);
        }
    }

    private static void addDependencyUI(TaskScheduler scheduler, Scanner scanner) {
        System.out.print("Enter Dependent Task ID (Task that depends on another): ");
        String taskId = scanner.nextLine().trim();

        System.out.print("Enter Prerequisite Task ID (Task that must be finished first): ");
        String prereqId = scanner.nextLine().trim();

        scheduler.addDependency(taskId, prereqId);
        System.out.println("🔗 Dependency added: Task '" + taskId + "' now depends on prerequisite '" + prereqId + "'. Cycle check passed!");
    }

    private static void viewTopologicalOrderUI(TaskScheduler scheduler) {
        System.out.println("🔗 Computing Valid Execution Order (Kahn's Topological Sort Algorithm)...");
        List<Task> order = scheduler.getValidExecutionOrder();
        System.out.println("✅ Valid Topological Order (" + order.size() + " tasks):");
        for (int i = 0; i < order.size(); i++) {
            System.out.printf("  %d. [%s] %s (Priority %d, Deps: %s)\n",
                    i + 1, order.get(i).getId(), order.get(i).getTitle(), order.get(i).getPriority(), order.get(i).getDependencies());
        }
    }

    private static void viewHistoryLogUI(TaskScheduler scheduler) {
        List<Task> history = scheduler.getTaskHistoryLog();
        System.out.println("📜 Task Activity Log / History (Queue FIFO Order - " + history.size() + " completed tasks):");
        if (history.isEmpty()) {
            System.out.println("  (No completed tasks in history queue)");
        } else {
            for (int i = 0; i < history.size(); i++) {
                System.out.printf("  %d. Completed: [%s] %s\n", i + 1, history.get(i).getId(), history.get(i).getTitle());
            }
        }
    }

    private static void saveToFileUI(TaskScheduler scheduler, Scanner scanner) throws Exception {
        System.out.print("Enter JSON File Path to Save (default: tasks.json): ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) path = "tasks.json";
        scheduler.saveToFile(path);
        System.out.println("💾 Saved " + scheduler.getAllTasks().size() + " tasks to '" + path + "'.");
    }

    private static void loadFromFileUI(TaskScheduler scheduler, Scanner scanner) throws Exception {
        System.out.print("Enter JSON File Path to Load (default: tasks.json): ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) path = "tasks.json";
        scheduler.loadFromFile(path);
        System.out.println("📂 Loaded " + scheduler.getAllTasks().size() + " tasks from '" + path + "'. Data structures rebuilt!");
    }

    private static void benchmarkSortsUI(TaskScheduler scheduler) {
        System.out.println("⚡ BENCHMARKING MERGESORT vs QUICKSORT ⚡");
        TaskScheduler.SortBenchmarkResult mergeRes = scheduler.sortTasks("priority", "mergesort");
        TaskScheduler.SortBenchmarkResult quickRes = scheduler.sortTasks("priority", "quicksort");

        System.out.printf("MergeSort Time: %10d ns (%.4f ms) | Space: O(N) | Stable: YES\n",
                mergeRes.durationNanos(), mergeRes.durationNanos() / 1_000_000.0);
        System.out.printf("QuickSort Time: %10d ns (%.4f ms) | Space: O(log N) | Stable: NO\n",
                quickRes.durationNanos(), quickRes.durationNanos() / 1_000_000.0);
    }

    private static void launchGUI(TaskScheduler scheduler) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> {
            TaskFlowGUI gui = new TaskFlowGUI(scheduler);
            gui.setVisible(true);
        });
        System.out.println("🖥️ Swing GUI Window launched!");
    }

    private static void displayTask(Task t) {
        System.out.printf("  • [%s] Priority: %d | Title: %-25s | Deadline: %-16s | Tags: %s | Completed: %b\n",
                t.getId(), t.getPriority(), t.getTitle(),
                (t.getDeadline() != null ? t.getDeadline().format(DATE_FORMATTER) : "None"),
                t.getTags(), t.isCompleted());
    }

    private static void loadDemoData(TaskScheduler scheduler) {
        Task t1 = new Task("T1", "Setup Project Architecture", "Initialize Maven pom.xml and directory layout", 1, LocalDateTime.now().plusHours(4), new HashSet<>(Arrays.asList("architecture", "maven")), null);
        Task t2 = new Task("T2", "Implement MinHeap Data Structure", "Create custom array-based min binary heap", 2, LocalDateTime.now().plusHours(8), new HashSet<>(Arrays.asList("dsa", "core")), Arrays.asList("T1"));
        Task t3 = new Task("T3", "Implement Graph & Topological Sort", "Build adjacency list directed graph & Kahn BFS algorithm", 2, LocalDateTime.now().plusHours(12), new HashSet<>(Arrays.asList("dsa", "graph")), Arrays.asList("T1"));
        Task t4 = new Task("T4", "Build TaskScheduler Service Layer", "Integrate MinHeap, Trie, Stack, Queue, HashMaps", 1, LocalDateTime.now().plusHours(16), new HashSet<>(Arrays.asList("service", "core")), Arrays.asList("T2", "T3"));
        Task t5 = new Task("T5", "Implement CLI Menu Interface", "Interactive console loop with full menu features", 3, LocalDateTime.now().plusHours(24), new HashSet<>(Arrays.asList("ui", "cli")), Arrays.asList("T4"));
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

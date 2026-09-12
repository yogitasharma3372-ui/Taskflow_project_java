# 🚀 TaskFlow — Priority Task Scheduler, Spring REST API & Web Visualizer

**TaskFlow** is a high-performance, priority-based task scheduling system written in Java. Built as a software engineering portfolio and interview project, TaskFlow demonstrates **from-scratch implementations of fundamental computer science data structures and algorithms** wrapped with a modern **Spring Boot REST API** and an interactive **Glassmorphism Web Dashboard**.

---

## 🏗️ System Architecture

```
                  +----------------------------------------------+
                  |  Single Page Web Dashboard (Vanilla JS + CSS)|
                  +----------------------+-----------------------+
                                         |
                                  HTTP / JSON (REST)
                                         |
                  +----------------------v-----------------------+
                  |    Spring Boot REST Controller Layer         |
                  |    (TaskController / GlobalExceptionHandler) |
                  +----------------------+-----------------------+
                                         |
                  +----------------------v-----------------------+
                  |         TaskScheduler Service                |
                  +----------------------+-----------------------+
                                         |
         +-------------------------------+-------------------------------+
         |                               |                               |
  +------v------+                 +------v------+                 +------v------+
  |   MinHeap   |                 |    Stack    |                 |    Queue    |
  | (Priority)  |                 |   (Undo)    |                 |  (History)  |
  +-------------+                 +-------------+                 +-------------+
         |                               |                               |
  +------v------+                 +------v------+                 +------v------+
  |    Trie     |                 |    Graph    |                 |  HashMaps   |
  |(Autocomplete|                 |(Dependencies|                 |  (O(1) ID/  |
  |   Search)   |                 | & TopoSort) |                 |    Tags)    |
  +-------------+                 +-------------+                 +-------------+
```

---

## 🌐 REST API Endpoints

| HTTP Method | Endpoint | Description | Status Codes |
|---|---|---|---|
| `GET` | `/api/health` | System health, server uptime, and task metrics | 200 OK |
| `POST` | `/api/tasks` | Create a new task | 201 Created, 400 Bad Request |
| `GET` | `/api/tasks` | List all tasks (`?sortBy=priority\|deadline\|title` & `?algorithm=merge\|quick`) | 200 OK |
| `GET` | `/api/tasks/next` | Peek highest priority task from MinHeap root | 200 OK |
| `POST` | `/api/tasks/{id}/complete` | Complete a task (extracts from MinHeap, enqueues to history Queue, pushes to undo Stack) | 200 OK, 404 Not Found |
| `POST` | `/api/undo` | Undo last action (pops from Stack and reverses state) | 200 OK |
| `GET` | `/api/tasks/search?prefix=` | Trie-based prefix autocomplete search | 200 OK |
| `GET` | `/api/tasks/tag/{tag}` | HashMap index lookup by tag | 200 OK |
| `POST` | `/api/dependencies` | Add directed dependency edge between two tasks | 200 OK, 409 Conflict (Cycle Detected) |
| `GET` | `/api/tasks/execution-order` | Topological sort valid execution sequence (Kahn's BFS) | 200 OK, 409 Conflict (Cycle Detected) |
| `GET` | `/api/history` | Task completion history log in FIFO order (Queue) | 200 OK |
| `GET` | `/api/tasks/benchmark` | Run Sorting Algorithm Race (MergeSort vs QuickSort performance metrics) | 200 OK |

---

## 📊 Core Data Structures & Big-O Complexity

| Data Structure | Implementation | Primary Use Case | Time Complexity (Best/Avg/Worst) | Space Complexity | Interview Rationale / Why Chosen |
|---|---|---|---|---|---|
| **MinHeap** | Custom Array Binary Min Heap | Priority-based task extraction (`peek`, `extractMin`) | Insert: \(O(\log N)\)<br>ExtractMin: \(O(\log N)\)<br>Peek: \(O(1)\) | \(O(N)\) | Guarantees top-priority task is always instantly accessible in \(O(1)\) time, with efficient \(O(\log N)\) updates as new tasks arrive. |
| **Stack** | Custom Singly-Linked Nodes | Multi-level Undo functionality (LIFO) | Push: \(O(1)\)<br>Pop: \(O(1)\)<br>Peek: \(O(1)\) | \(O(N)\) | Perfect for state reversal. Replaces complex global state management by pushing inverse `Action` snapshots. |
| **Queue** | Custom Linked Nodes (Head & Tail) | Task Activity History Log (FIFO) | Enqueue: \(O(1)\)<br>Dequeue: \(O(1)\)<br>Peek: \(O(1)\) | \(O(N)\) | Preserves chronological execution history where oldest completed tasks stay at the front. |
| **Trie** | Prefix Tree with HashMap Children | Task title prefix autocomplete & search | Insert: \(O(L)\)<br>Search Prefix: \(O(L + K)\)<br>*(L = prefix length, K = match count)* | \(O(N \cdot L)\) | Sub-linear prefix lookups independent of total tasks \(N\). Ideal for search-as-you-type interfaces. |
| **Graph** | Adjacency List Directed Graph | Dependency modeling between tasks | Add Vertex/Edge: \(O(1)\)<br>In-Degree Calculation: \(O(V + E)\) | \(O(V + E)\) | Represents task prerequisites cleanly. Directed edge \(V \to U\) means Task \(U\) depends on prerequisite \(V\). |
| **HashMaps** | Key-Value Indexing | Instant lookup by ID and Tag search | Lookup: \(O(1)\) avg<br>Insert: \(O(1)\) avg | \(O(N)\) | Eliminates \(O(N)\) linear scans when querying specific task IDs or tag sets. |

---

## ⚡ Custom Sorting Algorithms & Benchmarking

TaskFlow implements custom **Merge Sort** and **Quick Sort** without using `java.util.Collections.sort` or `Arrays.sort`.

### 1. Merge Sort (`MergeSort.java`)
- **Type**: Divide-and-Conquer, **Stable** Sort.
- **Time Complexity**: Guaranteed \(O(N \log N)\) across Best, Average, and Worst cases.
- **Space Complexity**: \(O(N)\) auxiliary array buffer.

### 2. Quick Sort (`QuickSort.java`)
- **Type**: In-Place Partitioning, **Unstable** Sort.
- **Pivot Strategy**: **Median-of-Three** (first, middle, last) to protect against worst-case \(O(N^2)\) on pre-sorted data.
- **Time Complexity**: Average \(O(N \log N)\), Worst \(O(N^2)\).
- **Space Complexity**: \(O(\log N)\) call stack space.

---

## 🛠️ Build & Run Instructions

### 1. Run Spring Boot Web Application & REST API
```bash
.\maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
```
Once started, open **`http://localhost:8080`** in your browser to view the **Glassmorphism Web Dashboard** featuring live MinHeap binary tree visualizer, dependency graph, and sort race panel!

### 2. Run All JUnit 5 Tests
```bash
.\maven\apache-maven-3.9.6\bin\mvn.cmd test
```

### 3. Run Interactive CLI Mode
```bash
.\maven\apache-maven-3.9.6\bin\mvn.cmd exec:java
```

---

## 📄 License
MIT License. Created for technical interview and portfolio demonstration.

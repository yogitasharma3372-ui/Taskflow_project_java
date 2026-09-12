package com.taskflow.datastructures;

import com.taskflow.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MinHeap Custom Data Structure Tests")
class MinHeapTest {

    private MinHeap<Task> heap;

    @BeforeEach
    void setUp() {
        heap = new MinHeap<>();
    }

    @Test
    @DisplayName("Empty heap extraction and peek should throw NoSuchElementException")
    void testEmptyHeapExceptions() {
        assertTrue(heap.isEmpty());
        assertEquals(0, heap.size());
        assertThrows(NoSuchElementException.class, () -> heap.extractMin());
        assertThrows(NoSuchElementException.class, () -> heap.peek());
    }

    @Test
    @DisplayName("Single element insertion and extraction")
    void testSingleElement() {
        Task task = new Task("Task 1", "Desc", 3, LocalDateTime.now());
        heap.insert(task);

        assertFalse(heap.isEmpty());
        assertEquals(1, heap.size());
        assertEquals(task, heap.peek());
        assertEquals(task, heap.extractMin());
        assertTrue(heap.isEmpty());
    }

    @Test
    @DisplayName("Multiple elements with varying priorities extracted in ascending priority order")
    void testPriorityOrdering() {
        Task low = new Task("Low Priority", "Desc", 5, LocalDateTime.now());
        Task high = new Task("High Priority", "Desc", 1, LocalDateTime.now());
        Task med = new Task("Medium Priority", "Desc", 3, LocalDateTime.now());

        heap.insert(low);
        heap.insert(high);
        heap.insert(med);

        assertEquals(3, heap.size());
        assertEquals(high, heap.extractMin());
        assertEquals(med, heap.extractMin());
        assertEquals(low, heap.extractMin());
        assertTrue(heap.isEmpty());
    }

    @Test
    @DisplayName("Duplicate priorities should tie-break via deadline or title")
    void testDuplicatePriorities() {
        LocalDateTime now = LocalDateTime.now();
        Task t1 = new Task("A Title", "Desc", 2, now.plusHours(5));
        Task t2 = new Task("B Title", "Desc", 2, now.plusHours(1)); // Earlier deadline

        heap.insert(t1);
        heap.insert(t2);

        assertEquals(t2, heap.extractMin(), "Earlier deadline should extract first when priorities match");
        assertEquals(t1, heap.extractMin());
    }

    @Test
    @DisplayName("Dynamic array resizing when inserting beyond initial capacity")
    void testDynamicResizing() {
        MinHeap<Integer> intHeap = new MinHeap<>(2);
        for (int i = 100; i >= 1; i--) {
            intHeap.insert(i);
        }

        assertEquals(100, intHeap.size());
        for (int i = 1; i <= 100; i++) {
            assertEquals(i, intHeap.extractMin());
        }
        assertTrue(intHeap.isEmpty());
    }
}

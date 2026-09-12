package com.taskflow.datastructures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Queue Custom Data Structure Tests")
class QueueTest {

    private Queue<Integer> queue;

    @BeforeEach
    void setUp() {
        queue = new Queue<>();
    }

    @Test
    @DisplayName("Empty queue operations throw exceptions")
    void testEmptyQueue() {
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        assertThrows(NoSuchElementException.class, () -> queue.dequeue());
        assertThrows(NoSuchElementException.class, () -> queue.peek());
    }

    @Test
    @DisplayName("Enqueue and Dequeue FIFO ordering")
    void testFifoOrder() {
        queue.enqueue(10);
        queue.enqueue(20);
        queue.enqueue(30);

        assertEquals(3, queue.size());
        assertEquals(10, queue.peek());
        assertEquals(10, queue.dequeue());
        assertEquals(20, queue.dequeue());
        assertEquals(30, queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    @Test
    @DisplayName("Queue toList snapshot preservation")
    void testToList() {
        queue.enqueue(1);
        queue.enqueue(2);
        queue.enqueue(3);

        List<Integer> list = queue.toList();
        assertEquals(List.of(1, 2, 3), list);
        assertEquals(3, queue.size()); // Queue size should remain unchanged
    }

    @Test
    @DisplayName("Null enqueue exception")
    void testNullEnqueue() {
        assertThrows(IllegalArgumentException.class, () -> queue.enqueue(null));
    }
}

package com.taskflow.datastructures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EmptyStackException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Stack Custom Data Structure Tests")
class StackTest {

    private Stack<String> stack;

    @BeforeEach
    void setUp() {
        stack = new Stack<>();
    }

    @Test
    @DisplayName("Empty stack behavior")
    void testEmptyStack() {
        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
        assertThrows(EmptyStackException.class, () -> stack.pop());
        assertThrows(EmptyStackException.class, () -> stack.peek());
    }

    @Test
    @DisplayName("Push and Pop LIFO order")
    void testLifoOrder() {
        stack.push("First");
        stack.push("Second");
        stack.push("Third");

        assertEquals(3, stack.size());
        assertEquals("Third", stack.peek());
        assertEquals("Third", stack.pop());
        assertEquals("Second", stack.pop());
        assertEquals("First", stack.pop());
        assertTrue(stack.isEmpty());
    }

    @Test
    @DisplayName("Single element push pop")
    void testSingleElement() {
        stack.push("Single");
        assertEquals("Single", stack.peek());
        assertEquals("Single", stack.pop());
        assertTrue(stack.isEmpty());
    }

    @Test
    @DisplayName("Null push exception")
    void testNullPush() {
        assertThrows(IllegalArgumentException.class, () -> stack.push(null));
    }
}

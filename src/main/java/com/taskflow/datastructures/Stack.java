package com.taskflow.datastructures;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

/**
 * Custom Generic Stack implementation using a singly-linked node structure.
 * Used for undo functionality (LIFO - Last In, First Out).
 * 
 * Big-O Analysis:
 * - Push:  O(1)
 * - Pop:   O(1)
 * - Peek:  O(1)
 * - Size:  O(1)
 * - Space: O(N)
 * 
 * @param <T> Element type stored in stack
 */
public class Stack<T> {
    private static class Node<E> {
        final E data;
        final Node<E> next;

        Node(E data, Node<E> next) {
            this.data = data;
            this.next = next;
        }
    }

    private Node<T> top;
    private int size;

    public Stack() {
        this.top = null;
        this.size = 0;
    }

    /**
     * Pushes an element onto the top of the stack.
     * Big-O: O(1).
     * 
     * @param element Non-null element to push
     */
    public void push(T element) {
        if (element == null) {
            throw new IllegalArgumentException("Cannot push null element onto Stack");
        }
        top = new Node<>(element, top);
        size++;
    }

    /**
     * Removes and returns the top element of the stack.
     * Big-O: O(1).
     * 
     * @return Top element
     * @throws EmptyStackException if stack is empty
     */
    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        T data = top.data;
        top = top.next;
        size--;
        return data;
    }

    /**
     * Returns the top element without removing it.
     * Big-O: O(1).
     * 
     * @return Top element
     * @throws EmptyStackException if stack is empty
     */
    public T peek() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return top.data;
    }

    /**
     * Returns the number of elements in the stack.
     * Big-O: O(1).
     */
    public int size() {
        return size;
    }

    /**
     * Checks if stack is empty.
     * Big-O: O(1).
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Clears all elements from the stack.
     */
    public void clear() {
        top = null;
        size = 0;
    }

    /**
     * Returns elements in stack from top to bottom as a List.
     */
    public List<T> toList() {
        List<T> list = new ArrayList<>(size);
        Node<T> current = top;
        while (current != null) {
            list.add(current.data);
            current = current.next;
        }
        return list;
    }
}

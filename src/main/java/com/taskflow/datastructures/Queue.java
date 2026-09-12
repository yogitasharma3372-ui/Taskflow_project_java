package com.taskflow.datastructures;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Custom Generic Queue implementation using a linked node structure with head and tail pointers.
 * Used for task activity history logging (FIFO - First In, First Out).
 * 
 * Big-O Analysis:
 * - Enqueue: O(1)
 * - Dequeue: O(1)
 * - Peek:    O(1)
 * - Size:    O(1)
 * - Space:   O(N)
 * 
 * @param <T> Element type stored in queue
 */
public class Queue<T> {
    private static class Node<E> {
        final E data;
        Node<E> next;

        Node(E data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> head; // Front of queue (removal point)
    private Node<T> tail; // Back of queue (insertion point)
    private int size;

    public Queue() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Inserts an element at the back of the queue.
     * Big-O: O(1).
     * 
     * @param element Non-null element to enqueue
     */
    public void enqueue(T element) {
        if (element == null) {
            throw new IllegalArgumentException("Cannot enqueue null element into Queue");
        }
        Node<T> newNode = new Node<>(element);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    /**
     * Removes and returns the element at the front of the queue.
     * Big-O: O(1).
     * 
     * @return Front element
     * @throws NoSuchElementException if queue is empty
     */
    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Cannot dequeue from an empty Queue");
        }
        T data = head.data;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        size--;
        return data;
    }

    /**
     * Returns the front element without removing it.
     * Big-O: O(1).
     * 
     * @return Front element
     * @throws NoSuchElementException if queue is empty
     */
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Cannot peek on an empty Queue");
        }
        return head.data;
    }

    /**
     * Returns the number of elements in the queue.
     * Big-O: O(1).
     */
    public int size() {
        return size;
    }

    /**
     * Checks if queue is empty.
     * Big-O: O(1).
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Removes elements matching the predicate from the queue.
     * 
     * @param predicate Predicate matching elements to remove
     * @return true if any element was removed
     */
    public boolean removeIf(java.util.function.Predicate<T> predicate) {
        if (predicate == null || isEmpty()) return false;
        boolean removed = false;
        while (head != null && predicate.test(head.data)) {
            head = head.next;
            size--;
            removed = true;
        }
        if (head == null) {
            tail = null;
            return removed;
        }
        Node<T> current = head;
        while (current.next != null) {
            if (predicate.test(current.next.data)) {
                if (current.next == tail) {
                    tail = current;
                }
                current.next = current.next.next;
                size--;
                removed = true;
            } else {
                current = current.next;
            }
        }
        return removed;
    }

    /**
     * Clears all elements from the queue.
     */
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Returns a snapshot list of elements in FIFO order (front to back).
     */
    public List<T> toList() {
        List<T> list = new ArrayList<>(size);
        Node<T> current = head;
        while (current != null) {
            list.add(current.data);
            current = current.next;
        }
        return list;
    }
}

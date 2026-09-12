package com.taskflow.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Custom Generic Min Binary Heap implementation.
 * 
 * Concept & Big-O Tradeoffs:
 * - A MinHeap is a complete binary tree where every parent node is less than or equal to its children.
 * - Stored in a contiguous array where for any node at index i:
 *     - Left Child:  2 * i + 1
 *     - Right Child: 2 * i + 2
 *     - Parent:      (i - 1) / 2
 * 
 * Time Complexities:
 * - Peek: O(1)
 * - Insert: O(log N) amortized (with dynamic array resizing)
 * - ExtractMin: O(log N)
 * - Search/Remove by element: O(N)
 * - Space Complexity: O(N)
 *
 * @param <T> Element type that implements Comparable<T>
 */
public class MinHeap<T extends Comparable<T>> {
    private static final int DEFAULT_CAPACITY = 16;
    private Object[] heap;
    private int size;

    /**
     * Initializes an empty MinHeap with default capacity (16).
     */
    public MinHeap() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Initializes an empty MinHeap with specified initial capacity.
     */
    public MinHeap(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive. Given: " + initialCapacity);
        }
        this.heap = new Object[initialCapacity];
        this.size = 0;
    }

    /**
     * Inserts a new element into the min heap.
     * Restores heap invariant using heapifyUp (sift-up).
     * 
     * Big-O: O(log N) amortized time complexity.
     * 
     * @param element Non-null element to insert
     */
    public void insert(T element) {
        if (element == null) {
            throw new IllegalArgumentException("Cannot insert null element into MinHeap");
        }
        ensureCapacity();
        heap[size] = element;
        heapifyUp(size);
        size++;
    }

    /**
     * Retrieves and removes the minimum element (root) of the heap.
     * Restores heap invariant using heapifyDown (sift-down).
     * 
     * Big-O: O(log N) time complexity.
     * 
     * @return Minimum element in the heap
     * @throws NoSuchElementException if the heap is empty
     */
    @SuppressWarnings("unchecked")
    public T extractMin() {
        if (isEmpty()) {
            throw new NoSuchElementException("Cannot extract min from an empty MinHeap");
        }
        T min = (T) heap[0];
        heap[0] = heap[size - 1];
        heap[size - 1] = null; // Prevent memory leak
        size--;

        if (size > 0) {
            heapifyDown(0);
        }
        return min;
    }

    /**
     * Returns the minimum element without removing it.
     * 
     * Big-O: O(1) time complexity.
     * 
     * @return Minimum element in the heap
     * @throws NoSuchElementException if the heap is empty
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Cannot peek on an empty MinHeap");
        }
        return (T) heap[0];
    }

    /**
     * Removes a specific element matching the predicate from the heap.
     * 
     * Big-O: O(N) search + O(log N) heap readjustment = O(N).
     * 
     * @param predicate Predicate matching element to remove
     * @return true if an element was found and removed, false otherwise
     */
    @SuppressWarnings("unchecked")
    public boolean removeIf(Predicate<T> predicate) {
        if (predicate == null || isEmpty()) return false;
        for (int i = 0; i < size; i++) {
            if (predicate.test((T) heap[i])) {
                removeAtIndex(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes an element at a specific index and restores heap ordering.
     */
    @SuppressWarnings("unchecked")
    private void removeAtIndex(int index) {
        if (index < 0 || index >= size) return;
        
        heap[index] = heap[size - 1];
        heap[size - 1] = null;
        size--;

        if (index < size) {
            heapifyUp(index);
            heapifyDown(index);
        }
    }

    /**
     * Sifts node at index up to maintain MinHeap invariant.
     */
    @SuppressWarnings("unchecked")
    private void heapifyUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            T current = (T) heap[index];
            T parent = (T) heap[parentIndex];

            if (current.compareTo(parent) < 0) {
                swap(index, parentIndex);
                index = parentIndex;
            } else {
                break;
            }
        }
    }

    /**
     * Sifts node at index down to maintain MinHeap invariant.
     */
    @SuppressWarnings("unchecked")
    private void heapifyDown(int index) {
        while (true) {
            int leftChild = 2 * index + 1;
            int rightChild = 2 * index + 2;
            int smallest = index;

            if (leftChild < size && ((T) heap[leftChild]).compareTo((T) heap[smallest]) < 0) {
                smallest = leftChild;
            }

            if (rightChild < size && ((T) heap[rightChild]).compareTo((T) heap[smallest]) < 0) {
                smallest = rightChild;
            }

            if (smallest != index) {
                swap(index, smallest);
                index = smallest;
            } else {
                break;
            }
        }
    }

    private void swap(int i, int j) {
        Object temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    private void ensureCapacity() {
        if (size >= heap.length) {
            int newCapacity = heap.length * 2;
            heap = Arrays.copyOf(heap, newCapacity);
        }
    }

    /**
     * Returns current element count in heap.
     * Big-O: O(1).
     */
    public int size() {
        return size;
    }

    /**
     * Checks if heap is empty.
     * Big-O: O(1).
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Clears all elements from the heap.
     */
    public void clear() {
        Arrays.fill(heap, 0, size, null);
        size = 0;
    }

    /**
     * Converts heap elements to an unsorted list snapshot.
     */
    @SuppressWarnings("unchecked")
    public List<T> toList() {
        List<T> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add((T) heap[i]);
        }
        return result;
    }
}

package com.taskflow.algorithms;

import java.util.Comparator;
import java.util.List;

/**
 * Custom Quick Sort implementation.
 * 
 * Algorithm Overview:
 * - In-place Divide-and-Conquer partitioning algorithm.
 * - Uses Median-of-Three pivot selection (first, middle, last) to eliminate worst-case O(N^2) behavior
 *   on pre-sorted inputs.
 * 
 * Big-O Complexity Analysis:
 * - Best Case Time:    O(N log N)
 * - Average Case Time: O(N log N)
 * - Worst Case Time:   O(N^2) (highly unlikely with median-of-three pivot selection)
 * - Auxiliary Space:   O(log N) recursion call stack
 * - Stability:         UNSTABLE (may swap relative position of equal elements)
 */
public class QuickSort {

    /**
     * Sorts the provided list in-place using Quick Sort according to the supplied comparator.
     * 
     * @param <T> Element type
     * @param list List of elements to sort
     * @param comparator Comparator defining element ordering
     */
    public static <T> void sort(List<T> list, Comparator<T> comparator) {
        if (list == null || list.size() <= 1 || comparator == null) {
            return;
        }
        quickSort(list, 0, list.size() - 1, comparator);
    }

    private static <T> void quickSort(List<T> list, int low, int high, Comparator<T> comparator) {
        if (low < high) {
            int pivotIndex = partition(list, low, high, comparator);
            quickSort(list, low, pivotIndex - 1, comparator);
            quickSort(list, pivotIndex + 1, high, comparator);
        }
    }

    /**
     * Partitions list around median-of-three chosen pivot.
     */
    private static <T> int partition(List<T> list, int low, int high, Comparator<T> comparator) {
        // Median-of-three pivot selection
        int mid = low + (high - low) / 2;
        selectMedianOfThree(list, low, mid, high, comparator);
        
        // Move median pivot to high - 1 for partitioning
        swap(list, mid, high);
        T pivot = list.get(high);

        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (comparator.compare(list.get(j), pivot) <= 0) {
                i++;
                swap(list, i, j);
            }
        }

        swap(list, i + 1, high);
        return i + 1;
    }

    /**
     * Sorts low, mid, and high elements to find median.
     */
    private static <T> void selectMedianOfThree(List<T> list, int low, int mid, int high, Comparator<T> comparator) {
        if (comparator.compare(list.get(low), list.get(mid)) > 0) {
            swap(list, low, mid);
        }
        if (comparator.compare(list.get(low), list.get(high)) > 0) {
            swap(list, low, high);
        }
        if (comparator.compare(list.get(mid), list.get(high)) > 0) {
            swap(list, mid, high);
        }
    }

    private static <T> void swap(List<T> list, int i, int j) {
        if (i != j) {
            T temp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, temp);
        }
    }
}

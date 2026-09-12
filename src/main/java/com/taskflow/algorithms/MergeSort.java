package com.taskflow.algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Custom Merge Sort implementation.
 * 
 * Algorithm Overview:
 * - Divide-and-Conquer stable sorting algorithm.
 * - Recursively divides list into two halves until single elements remain,
 *   then merges sorted halves.
 * 
 * Big-O Complexity Analysis:
 * - Best Case Time:    O(N log N)
 * - Average Case Time: O(N log N)
 * - Worst Case Time:   O(N log N)
 * - Auxiliary Space:   O(N) (temporary merge buffer array)
 * - Stability:         STABLE (maintains relative order of equal elements)
 */
public class MergeSort {

    /**
     * Sorts the provided list in-place using Merge Sort according to the supplied comparator.
     * 
     * @param <T> Element type
     * @param list List of elements to sort
     * @param comparator Comparator defining element ordering
     */
    public static <T> void sort(List<T> list, Comparator<T> comparator) {
        if (list == null || list.size() <= 1 || comparator == null) {
            return;
        }

        List<T> aux = new ArrayList<>(list);
        mergeSort(list, aux, 0, list.size() - 1, comparator);
    }

    private static <T> void mergeSort(List<T> list, List<T> aux, int low, int high, Comparator<T> comparator) {
        if (low >= high) {
            return;
        }

        int mid = low + (high - low) / 2;
        mergeSort(list, aux, low, mid, comparator);
        mergeSort(list, aux, mid + 1, high, comparator);

        // Optimization: Skip merge if already sorted across sub-lists
        if (comparator.compare(list.get(mid), list.get(mid + 1)) <= 0) {
            return;
        }

        merge(list, aux, low, mid, high, comparator);
    }

    private static <T> void merge(List<T> list, List<T> aux, int low, int mid, int high, Comparator<T> comparator) {
        // Copy elements to aux buffer
        for (int k = low; k <= high; k++) {
            aux.set(k, list.get(k));
        }

        int i = low;      // Left pointer
        int j = mid + 1;  // Right pointer

        for (int k = low; k <= high; k++) {
            if (i > mid) {
                list.set(k, aux.get(j++));
            } else if (j > high) {
                list.set(k, aux.get(i++));
            } else if (comparator.compare(aux.get(i), aux.get(j)) <= 0) { // <= preserves stability
                list.set(k, aux.get(i++));
            } else {
                list.set(k, aux.get(j++));
            }
        }
    }
}

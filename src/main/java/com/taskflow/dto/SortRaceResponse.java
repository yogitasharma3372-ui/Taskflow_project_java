package com.taskflow.dto;

import com.taskflow.model.Task;
import java.util.List;

/**
 * DTO for the Sorting Algorithm Race benchmark endpoint.
 * Contains detailed performance metrics comparing MergeSort vs QuickSort.
 */
public class SortRaceResponse {

    public static class AlgorithmStats {
        private String name;
        private String timeComplexity;
        private String spaceComplexity;
        private boolean isStable;
        private long durationNanos;
        private double durationMillis;
        private int totalElementsSorted;
        private long estimatedComparisons;
        private long estimatedSwaps;

        public AlgorithmStats(String name, String timeComplexity, String spaceComplexity, boolean isStable, long durationNanos, int totalElementsSorted) {
            this.name = name;
            this.timeComplexity = timeComplexity;
            this.spaceComplexity = spaceComplexity;
            this.isStable = isStable;
            this.durationNanos = durationNanos;
            this.durationMillis = durationNanos / 1_000_000.0;
            this.totalElementsSorted = totalElementsSorted;

            // Theoretical N log2(N) metric calculations for visualization dashboard
            double logN = (totalElementsSorted > 0) ? (Math.log(totalElementsSorted) / Math.log(2)) : 0;
            this.estimatedComparisons = Math.round(totalElementsSorted * logN);
            this.estimatedSwaps = name.equalsIgnoreCase("MERGESORT") ? Math.round(totalElementsSorted * logN) : Math.round((totalElementsSorted * logN) / 2);
        }

        public String getName() { return name; }
        public String getTimeComplexity() { return timeComplexity; }
        public String getSpaceComplexity() { return spaceComplexity; }
        public boolean isStable() { return isStable; }
        public long getDurationNanos() { return durationNanos; }
        public double getDurationMillis() { return durationMillis; }
        public int getTotalElementsSorted() { return totalElementsSorted; }
        public long getEstimatedComparisons() { return estimatedComparisons; }
        public long getEstimatedSwaps() { return estimatedSwaps; }
    }

    private AlgorithmStats mergeSort;
    private AlgorithmStats quickSort;
    private List<Task> sortedTasks;
    private String winner;

    public SortRaceResponse(AlgorithmStats mergeSort, AlgorithmStats quickSort, List<Task> sortedTasks) {
        this.mergeSort = mergeSort;
        this.quickSort = quickSort;
        this.sortedTasks = sortedTasks;
        this.winner = (mergeSort.getDurationNanos() <= quickSort.getDurationNanos()) ? "MERGESORT" : "QUICKSORT";
    }

    public AlgorithmStats getMergeSort() { return mergeSort; }
    public AlgorithmStats getQuickSort() { return quickSort; }
    public List<Task> getSortedTasks() { return sortedTasks; }
    public String getWinner() { return winner; }
}

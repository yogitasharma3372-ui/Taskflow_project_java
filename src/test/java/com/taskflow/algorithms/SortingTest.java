package com.taskflow.algorithms;

import com.taskflow.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MergeSort and QuickSort Algorithm Tests")
class SortingTest {

    private List<Task> taskList;

    @BeforeEach
    void setUp() {
        taskList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        taskList.add(new Task("C Task", "Desc", 4, now.plusHours(10)));
        taskList.add(new Task("A Task", "Desc", 1, now.plusHours(2)));
        taskList.add(new Task("B Task", "Desc", 3, now.plusHours(5)));
        taskList.add(new Task("D Task", "Desc", 2, now.plusHours(1)));
    }

    @Test
    @DisplayName("MergeSort by Priority")
    void testMergeSortByPriority() {
        MergeSort.sort(taskList, Task::compareTo);

        assertEquals(1, taskList.get(0).getPriority());
        assertEquals(2, taskList.get(1).getPriority());
        assertEquals(3, taskList.get(2).getPriority());
        assertEquals(4, taskList.get(3).getPriority());
    }

    @Test
    @DisplayName("QuickSort by Priority")
    void testQuickSortByPriority() {
        QuickSort.sort(taskList, Task::compareTo);

        assertEquals(1, taskList.get(0).getPriority());
        assertEquals(2, taskList.get(1).getPriority());
        assertEquals(3, taskList.get(2).getPriority());
        assertEquals(4, taskList.get(3).getPriority());
    }

    @Test
    @DisplayName("Sort by Title Alphabetical")
    void testSortByTitle() {
        Comparator<Task> titleComp = Comparator.comparing(Task::getTitle);

        List<Task> mergeList = new ArrayList<>(taskList);
        MergeSort.sort(mergeList, titleComp);
        assertEquals("A Task", mergeList.get(0).getTitle());
        assertEquals("D Task", mergeList.get(3).getTitle());

        List<Task> quickList = new ArrayList<>(taskList);
        QuickSort.sort(quickList, titleComp);
        assertEquals("A Task", quickList.get(0).getTitle());
        assertEquals("D Task", quickList.get(3).getTitle());
    }

    @Test
    @DisplayName("Sort empty and single element lists cleanly")
    void testEdgeCases() {
        List<Task> empty = new ArrayList<>();
        MergeSort.sort(empty, Task::compareTo);
        QuickSort.sort(empty, Task::compareTo);
        assertTrue(empty.isEmpty());

        List<Task> single = new ArrayList<>();
        single.add(new Task("One", "D", 1, LocalDateTime.now()));
        MergeSort.sort(single, Task::compareTo);
        QuickSort.sort(single, Task::compareTo);
        assertEquals(1, single.size());
    }
}

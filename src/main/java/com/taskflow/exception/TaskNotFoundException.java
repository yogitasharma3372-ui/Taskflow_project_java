package com.taskflow.exception;

/**
 * Exception thrown when a requested task ID is not found in the index.
 */
public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String message) {
        super(message);
    }
}

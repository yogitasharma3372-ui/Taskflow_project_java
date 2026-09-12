package com.taskflow.exception;

/**
 * Exception thrown when invalid parameters are provided for task creation or updates.
 */
public class InvalidTaskDataException extends RuntimeException {
    public InvalidTaskDataException(String message) {
        super(message);
    }
}

package com.taskflow.model;

/**
 * Enumeration representing the types of actions supported by the TaskFlow scheduler.
 * Used for undo tracking and activity logging.
 */
public enum ActionType {
    /** Task creation action */
    ADD,
    /** Task completion action */
    COMPLETE,
    /** Task deletion action */
    DELETE,
    /** Task modification action */
    UPDATE,
    /** Dependency addition action */
    ADD_DEPENDENCY
}

package com.matchylabs.matchy;

/**
 * Exception thrown by matchy operations.
 * 
 * This is the primary exception type for all matchy-java operations,
 * including database opening, querying, and building.
 */
public class MatchyException extends Exception {
    
    /**
     * Constructs a new MatchyException with the specified detail message.
     *
     * @param message the detail message
     */
    public MatchyException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new MatchyException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public MatchyException(String message, Throwable cause) {
        super(message, cause);
    }
}

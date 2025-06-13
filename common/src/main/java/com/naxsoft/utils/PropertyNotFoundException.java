package com.naxsoft.utils;

/**
 * Runtime exception thrown when a required configuration property is not found.
 * Extends RuntimeException for unchecked exception handling in configuration scenarios.
 */
public class PropertyNotFoundException extends RuntimeException {

    /**
     * Constructs a new PropertyNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining which property was not found
     */
    public PropertyNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new PropertyNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message explaining which property was not found
     * @param cause   the cause of this exception
     */
    public PropertyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

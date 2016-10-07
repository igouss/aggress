package com.naxsoft.utils;

/**
 * @param <T>
 */
public class Property<T> {
    private final T value;

    /**
     * Set property value
     * @param value Property value
     */
    Property(T value) {
        this.value = value;
    }

    /**
     * Retrieve property value
     * @return property value
     */
    public T getValue() {
        return value;
    }
}

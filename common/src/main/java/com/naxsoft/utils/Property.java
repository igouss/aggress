package com.naxsoft.utils;

/**
 * @param <T>
 */
public class Property<T> {
    private T value;

    /**
     * @param value
     */
    public Property(T value) {
        setValue(value);
    }

    /**
     *
     * @return
     */
    public T getValue() {
        return value;
    }

    /**
     *
     * @param value
     */
    public void setValue(T value) {
        this.value = value;
    }
}

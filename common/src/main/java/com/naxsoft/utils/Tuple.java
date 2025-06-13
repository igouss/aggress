package com.naxsoft.utils;

import lombok.Value;

/**
 * Immutable tuple utility class for holding pairs of related values.
 * Uses Lombok @Value for automatic generation of getters, equals, hashCode, and toString.
 */
@Value
public class Tuple<V1, V2> {
    V1 v1;
    V2 v2;

    /**
     * Static factory method for creating tuples
     *
     * @param v1 First value
     * @param v2 Second value
     * @return New tuple instance
     */
    public static <T1, T2> Tuple<T1, T2> of(T1 v1, T2 v2) {
        return new Tuple<>(v1, v2);
    }
}

package com.naxsoft.utils;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * @param <T>
 */
public class Property<T> {
    private T value;
    private PublishSubject<T> property;

    /**
     *
     */
    private Property() {
        property = PublishSubject.create();
    }

    /**
     * @param value
     */
    public Property(T value) {
        this();
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
        property.onNext(value);
    }

    /**
     *
     * @return
     */
    public Observable<T> observe() {
        return property.asObservable();
    }
}

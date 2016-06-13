package com.naxsoft.utils;

import rx.Observable;
import rx.subjects.PublishSubject;

public class Property<T> {
    private T value;
    private PublishSubject<T> property;

    public Property() {
        property = PublishSubject.create();
    }

    public Property(T value) {
        super();
        setValue(value);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
        property.onNext(value);
    }

    public Observable<T> observe() {
        return property.asObservable();
    }

}

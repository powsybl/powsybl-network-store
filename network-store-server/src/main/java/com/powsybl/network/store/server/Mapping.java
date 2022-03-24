package com.powsybl.network.store.server;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class Mapping<T, R, U, K, O> {
    private int index;  // not used
    private Class<R> classR;
    private Function<T, R> getter;
    private BiConsumer<T, U> setter;
    private Class<K> classMapKey;
    private Class<O> classMapValue;

    Mapping(int index, Class<R> classR, Function<T, R> getter, BiConsumer<T, U> setter) {
        this(index, classR, getter, setter, null, null);
    }

    Mapping(int index, Class<R> classR, Function<T, R> getter, BiConsumer<T, U> setter, Class<K> classMapKey, Class<O> classMapValue) {
        this.index = index;
        this.classR = classR;
        this.getter = getter;
        this.setter = setter;
        this.classMapKey = classMapKey;
        this.classMapValue = classMapValue;
    }

    R get(T obj) {
        return getter.apply(obj);
    }

    void set(T obj, U value) {
        setter.accept(obj, value);
    }

    Class<R> getClassR() {
        return classR;
    }

    Class<K> getClassMapKey() {
        return classMapKey;
    }

    Class<O> getClassMapValue() {
        return classMapValue;
    }
}

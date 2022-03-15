package com.powsybl.network.store.server;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class Mapping<T, R, U> {
    private int index;  // not used
    private Class<R> classR;
    private Function<T, R> getter;
    private BiConsumer<T, U> setter;

    Mapping(int index, Class<R> classR, Function<T, R> getter, BiConsumer<T, U> setter) {
        this.index = index;
        this.classR = classR;
        this.getter = getter;
        this.setter = setter;
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
}

/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import lombok.NonNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Column mapping.
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ColumnMapping<T, R, U, K, O> {
    private Class<R> classR;
    private Function<T, R> getter;
    private BiConsumer<T, U> setter;
    private Class<K> classMapKey;
    private Class<O> classMapValue;

    ColumnMapping(@NonNull Class<R> classR, @NonNull Function<T, R> getter, @NonNull BiConsumer<T, U> setter) {
        this(classR, getter, setter, null, null);
    }

    ColumnMapping(Class<R> classR, @NonNull Function<T, R> getter, @NonNull BiConsumer<T, U> setter, Class<K> classMapKey, Class<O> classMapValue) {
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

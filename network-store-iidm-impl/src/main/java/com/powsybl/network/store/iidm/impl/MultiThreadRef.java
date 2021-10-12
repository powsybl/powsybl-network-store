/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MultiThreadRef<T> implements Ref<T> {

    private final ThreadLocal<T> ref = new ThreadLocal<>();

    public MultiThreadRef(T ref) {
        this.ref.set(ref);
    }

    @Override
    public void set(T ref) {
        this.ref.set(ref);
    }

    @Override
    public T get() {
        return ref.get();
    }
}

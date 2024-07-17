/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.util;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@FunctionalInterface
public interface PentaFunction<T, U, V, W, X, R> {

    R apply(T t, U u, V v, W w, X x);
}

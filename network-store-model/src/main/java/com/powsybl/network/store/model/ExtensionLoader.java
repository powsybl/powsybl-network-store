/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public interface ExtensionLoader<T extends Extendable<T>, E extends Extension<T>, K extends ExtensionAttributes> {

    Extension<T> load(T extendable);

    String getName();

    Class<? super E> getType();

    Class<? extends ExtensionAttributes > getAttributesType();
}

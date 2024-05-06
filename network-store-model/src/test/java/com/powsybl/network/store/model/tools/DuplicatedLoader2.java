/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model.tools;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Injection;
import com.powsybl.network.store.model.ExtensionAttributes;
import com.powsybl.network.store.model.ExtensionLoader;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@AutoService(ExtensionLoader.class)
public class DuplicatedLoader2<I extends Injection<I>> implements ExtensionLoader<I, Extension<I>, ExtensionAttributes> {
    @Override
    public Extension<I> load(I injection) {
        return null;
    }

    @Override
    public String getName() {
        return "loader";
    }

    @Override
    public Class<Extension<I>> getType() {
        return null;
    }

    @Override
    public Class<ExtensionAttributes> getAttributesType() {
        return null;
    }
}

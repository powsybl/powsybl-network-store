/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.DynamicModelInfo;
import com.powsybl.network.store.model.DynamicModelInfoAttributes;
import com.powsybl.network.store.model.ExtensionLoader;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@AutoService(ExtensionLoader.class)
public class DynamicModelInfoLoader<I extends Identifiable<I>> implements ExtensionLoader<I, DynamicModelInfo<I>, DynamicModelInfoAttributes> {
    @Override
    public Extension<I> load(I identifiable) {
        return new DynamicModelInfoImpl<>(identifiable);
    }

    @Override
    public String getName() {
        return DynamicModelInfo.NAME;
    }

    @Override
    public Class<DynamicModelInfo> getType() {
        return DynamicModelInfo.class;
    }

    @Override
    public Class<DynamicModelInfoAttributes> getAttributesType() {
        return DynamicModelInfoAttributes.class;
    }
}

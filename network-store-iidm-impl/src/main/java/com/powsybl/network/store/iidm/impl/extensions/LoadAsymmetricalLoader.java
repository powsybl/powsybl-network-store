/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.extensions.LoadAsymmetrical;
import com.powsybl.network.store.model.ExtensionLoader;
import com.powsybl.network.store.model.LoadAsymmetricalAttributes;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@AutoService(ExtensionLoader.class)
public class LoadAsymmetricalLoader implements ExtensionLoader<Load, LoadAsymmetrical, LoadAsymmetricalAttributes> {
    @Override
    public Extension<Load> load(Load load) {
        return new LoadAsymmetricalImpl(load);
    }

    @Override
    public String getName() {
        return LoadAsymmetrical.NAME;
    }

    @Override
    public Class<LoadAsymmetrical> getType() {
        return LoadAsymmetrical.class;
    }

    @Override
    public Class<LoadAsymmetricalAttributes> getAttributesType() {
        return LoadAsymmetricalAttributes.class;
    }
}

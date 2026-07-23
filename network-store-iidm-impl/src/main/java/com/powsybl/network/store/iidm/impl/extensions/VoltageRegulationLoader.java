/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.extensions.VoltageRegulation;
import com.powsybl.network.store.model.ExtensionLoader;
import com.powsybl.network.store.model.VoltageRegulationAttributes;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@AutoService(ExtensionLoader.class)
public class VoltageRegulationLoader implements ExtensionLoader<Battery, VoltageRegulation, VoltageRegulationAttributes> {
    @Override
    public Extension<Battery> load(Battery battery) {
        return new VoltageRegulationImpl(battery);
    }

    @Override
    public String getName() {
        return VoltageRegulation.NAME;
    }

    @Override
    public Class<VoltageRegulation> getType() {
        return VoltageRegulation.class;
    }

    @Override
    public Class<VoltageRegulationAttributes> getAttributesType() {
        return VoltageRegulationAttributes.class;
    }
}

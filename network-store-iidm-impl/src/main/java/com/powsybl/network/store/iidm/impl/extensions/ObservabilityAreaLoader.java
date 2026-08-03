/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.ObservabilityArea;
import com.powsybl.network.store.model.ExtensionLoader;
import com.powsybl.network.store.model.ObservabilityAreaAttributes;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
@AutoService(ExtensionLoader.class)
public class ObservabilityAreaLoader implements ExtensionLoader<VoltageLevel, ObservabilityArea, ObservabilityAreaAttributes> {

    @Override
    public Extension<VoltageLevel> load(VoltageLevel voltageLevel) {
        if (TopologyKind.BUS_BREAKER.equals(voltageLevel.getTopologyKind())) {
            return new BusBreakerObservabilityArea(voltageLevel);
        } else if (TopologyKind.NODE_BREAKER.equals(voltageLevel.getTopologyKind())) {
            return new NodeBreakerObservabilityArea(voltageLevel);
        }
        throw new UnsupportedOperationException("Topology kind " + voltageLevel.getTopologyKind() + " not supported");
    }

    @Override
    public String getName() {
        return ObservabilityArea.NAME;
    }

    @Override
    public Class<ObservabilityArea> getType() {
        return ObservabilityArea.class;
    }

    @Override
    public Class<ObservabilityAreaAttributes> getAttributesType() {
        return ObservabilityAreaAttributes.class;
    }
}

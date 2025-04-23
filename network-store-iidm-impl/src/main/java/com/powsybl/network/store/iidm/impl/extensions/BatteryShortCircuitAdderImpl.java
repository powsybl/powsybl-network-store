/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.extensions.BatteryShortCircuit;
import com.powsybl.iidm.network.extensions.BatteryShortCircuitAdder;
import com.powsybl.network.store.iidm.impl.BatteryImpl;
import com.powsybl.network.store.model.ShortCircuitAttributes;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class BatteryShortCircuitAdderImpl extends AbstractShortCircuitAdderImpl<Battery, BatteryShortCircuit, BatteryShortCircuitAdder> implements BatteryShortCircuitAdder {
    protected BatteryShortCircuitAdderImpl(Battery extendable) {
        super(extendable);
    }

    @Override
    protected BatteryShortCircuitAdder self() {
        return this;
    }

    @Override
    protected BatteryShortCircuit createExtension(Battery battery) {
        ShortCircuitAttributes attributes = ShortCircuitAttributes.builder()
            .directSubtransX(directSubtransX)
            .directTransX(directTransX)
            .stepUpTransformerX(stepUpTransformerX)
            .build();
        ((BatteryImpl) battery).updateResourceWithoutNotification(res -> res.getAttributes().setBatteryShortCircuitAttributes(attributes));
        return new BatteryShortCircuitImpl((BatteryImpl) battery);
    }

}

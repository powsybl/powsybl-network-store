/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.extensions.BatteryShortCircuit;
import com.powsybl.iidm.network.extensions.BatteryShortCircuitAdder;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */

@AutoService(ExtensionAdderProvider.class)
public class BatteryShortCircuitAdderImplProvider implements ExtensionAdderProvider<Battery, BatteryShortCircuit, BatteryShortCircuitAdder> {

    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public String getExtensionName() {
        return BatteryShortCircuit.NAME;
    }

    public Class<BatteryShortCircuitAdder> getAdderClass() {
        return BatteryShortCircuitAdder.class;
    }

    public BatteryShortCircuitAdder newAdder(Battery extendable) {
        return new BatteryShortCircuitAdderImpl(extendable);
    }
}

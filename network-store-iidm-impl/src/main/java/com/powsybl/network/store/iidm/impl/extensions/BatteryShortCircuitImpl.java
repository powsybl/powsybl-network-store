/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.extensions.BatteryShortCircuit;
import com.powsybl.network.store.iidm.impl.BatteryImpl;
import com.powsybl.network.store.model.ShortCircuitAttributes;

import java.util.function.Consumer;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class BatteryShortCircuitImpl extends AbstractShortCircuitExtensionImpl<Battery, BatteryShortCircuitImpl> implements BatteryShortCircuit {

    public BatteryShortCircuitImpl(BatteryImpl battery) {
        super(battery, battery.getResource().getAttributes().getBatteryShortCircuitAttributes());
    }

    private BatteryImpl getBattery() {
        return (BatteryImpl) getExtendable();
    }

    @Override
    protected BatteryShortCircuitImpl self() {
        return this;
    }

    @Override
    protected void updateAttributes(double newValue, double oldValue, String name, Consumer<ShortCircuitAttributes> modifier) {
        getBattery().updateResourceExtension(this, res ->
            modifier.accept(res.getAttributes().getBatteryShortCircuitAttributes()), name, oldValue, newValue);
    }
}

/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.extensions.BatteryShortCircuit;
import com.powsybl.network.store.iidm.impl.BatteryImpl;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class BatteryShortCircuitImpl extends AbstractExtension<Battery> implements BatteryShortCircuit {

    public BatteryShortCircuitImpl(BatteryImpl battery) {
        super(battery);
    }

    private BatteryImpl getBattery() {
        return (BatteryImpl) getExtendable();
    }

    @Override
    public double getDirectSubtransX() {
        return getBattery().getResource().getAttributes().getBatteryShortCircuitAttributes().getDirectSubtransX();
    }

    @Override
    public BatteryShortCircuit setDirectSubtransX(double directSubtransX) {
        double oldValue = getDirectSubtransX();
        if (oldValue != directSubtransX) {
            getBattery().updateResourceExtension(this, res -> res.getAttributes().getBatteryShortCircuitAttributes().setDirectSubtransX(directSubtransX), "directSubtransX", oldValue, directSubtransX);
        }
        return this;
    }

    @Override
    public double getDirectTransX() {
        return getBattery().getResource().getAttributes().getBatteryShortCircuitAttributes().getDirectTransX();
    }

    @Override
    public BatteryShortCircuit setDirectTransX(double directTransX) {
        if (Double.isNaN(directTransX)) {
            throw new PowsyblException("Undefined directTransX");
        }
        double oldValue = getDirectTransX();
        if (oldValue != directTransX) {
            getBattery().updateResourceExtension(this, res -> res.getAttributes().getBatteryShortCircuitAttributes().setDirectTransX(directTransX), "directTransX", oldValue, directTransX);
        }
        return this;
    }

    @Override
    public double getStepUpTransformerX() {
        return getBattery().getResource().getAttributes().getBatteryShortCircuitAttributes()
                .getStepUpTransformerX();
    }

    @Override
    public BatteryShortCircuit setStepUpTransformerX(double stepUpTransformerX) {
        double oldValue = getStepUpTransformerX();
        if (oldValue != stepUpTransformerX) {
            getBattery().updateResourceExtension(this, res -> res.getAttributes().getBatteryShortCircuitAttributes().setStepUpTransformerX(stepUpTransformerX), "stepUpTransformerX", oldValue, stepUpTransformerX);
        }
        return this;
    }
}

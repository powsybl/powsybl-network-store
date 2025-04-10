/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.extensions.BatteryShortCircuit;
import com.powsybl.iidm.network.extensions.BatteryShortCircuitAdder;
import com.powsybl.network.store.iidm.impl.BatteryImpl;
import com.powsybl.network.store.model.ShortCircuitAttributes;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */

public class BatteryShortCircuitAdderImpl extends AbstractIidmExtensionAdder<Battery, BatteryShortCircuit> implements BatteryShortCircuitAdder {

    private double directTransX = 0.0D;
    private double directSubtransX = Double.NaN;
    private double stepUpTransformerX = Double.NaN;

    protected BatteryShortCircuitAdderImpl(Battery extendable) {
        super(extendable);
    }

    protected BatteryShortCircuit createExtension(Battery battery) {
        var attributes = ShortCircuitAttributes.builder()
                .directSubtransX(directSubtransX)
                .directTransX(directTransX)
                .stepUpTransformerX(stepUpTransformerX)
                .build();
        ((BatteryImpl) battery).updateResourceWithoutNotification(res -> res.getAttributes().setBatteryShortCircuitAttributes(attributes));
        return new BatteryShortCircuitImpl((BatteryImpl) battery);
    }

    public BatteryShortCircuitAdder withDirectTransX(double directTransX) {
        this.directTransX = directTransX;
        return this;
    }

    public BatteryShortCircuitAdder withDirectSubtransX(double directSubtransX) {
        this.directSubtransX = directSubtransX;
        return this;
    }

    public BatteryShortCircuitAdder withStepUpTransformerX(double stepUpTransformerX) {
        this.stepUpTransformerX = stepUpTransformerX;
        return this;
    }

    @Override
    public BatteryShortCircuit add() {
        if (Double.isNaN(this.directTransX)) {
            throw new PowsyblException("Undefined directTransX");
        }
        return super.add();
    }
}

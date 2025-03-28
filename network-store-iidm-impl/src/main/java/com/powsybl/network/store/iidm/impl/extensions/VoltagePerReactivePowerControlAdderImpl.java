/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControlAdder;
import com.powsybl.network.store.iidm.impl.StaticVarCompensatorImpl;
import com.powsybl.network.store.model.VoltagePerReactivePowerControlAttributes;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class VoltagePerReactivePowerControlAdderImpl extends AbstractIidmExtensionAdder<StaticVarCompensator, VoltagePerReactivePowerControl> implements VoltagePerReactivePowerControlAdder {

    private double slope;

    public VoltagePerReactivePowerControlAdderImpl(StaticVarCompensator extendable) {
        super(extendable);
    }

    @Override
    protected VoltagePerReactivePowerControl createExtension(StaticVarCompensator staticVarCompensator) {
        checkSlope();
        VoltagePerReactivePowerControlAttributes oldValue = ((StaticVarCompensatorImpl) staticVarCompensator).getResource().getAttributes().getVoltagePerReactiveControl();
        VoltagePerReactivePowerControlAttributes attributes = VoltagePerReactivePowerControlAttributes.builder()
                .slope(slope)
                .build();
        ((StaticVarCompensatorImpl) staticVarCompensator).updateResource(res -> res.getAttributes().setVoltagePerReactiveControl(attributes),
            "voltagePerReactiveControl", oldValue, attributes);
        return new VoltagePerReactivePowerControlImpl((StaticVarCompensatorImpl) staticVarCompensator);
    }

    @Override
    public VoltagePerReactivePowerControlAdder withSlope(double slope) {
        this.slope = slope;
        return this;
    }

    private void checkSlope() {
        if (Double.isNaN(slope)) {
            throw new PowsyblException("Undefined value for slope");
        }
        if (slope < 0) {
            throw new PowsyblException("Slope value of SVC " + extendable.getId() + " must be positive: " + slope);
        }
    }
}

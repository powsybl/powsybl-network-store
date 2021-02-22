/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControlAdder;
import com.powsybl.network.store.iidm.impl.StaticVarCompensatorImpl;
import com.powsybl.network.store.model.ActivePowerControlAttributes;
import com.powsybl.network.store.model.VoltagePerReactivePowerControlAttributes;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class VoltagePerReactivePowerControlImpl implements VoltagePerReactivePowerControl {

    private StaticVarCompensatorImpl staticVarCompensator;

    public VoltagePerReactivePowerControlImpl(StaticVarCompensatorImpl staticVarCompensator) {
        this.staticVarCompensator = staticVarCompensator;
    }

    public VoltagePerReactivePowerControlImpl(StaticVarCompensatorImpl staticVarCompensator, double slope) {
        this(staticVarCompensator);
        staticVarCompensator.getResource().getAttributes().setVoltagePerReactiveControl(VoltagePerReactivePowerControlAttributes.builder()
                .slope(slope)
                .build());
    }

    @Override
    public double getSlope() {
        return staticVarCompensator.getResource().getAttributes().getVoltagePerReactiveControl().getSlope();
    }

    @Override
    public VoltagePerReactivePowerControl setSlope(double slope) {
        staticVarCompensator.getResource().getAttributes().getVoltagePerReactiveControl().setSlope(slope);
        return this;
    }

    @Override
    public StaticVarCompensator getExtendable() {
        return staticVarCompensator;
    }

    @Override
    public void setExtendable(StaticVarCompensator staticVarCompensator) {
        this.staticVarCompensator = (StaticVarCompensatorImpl) staticVarCompensator;
    }

}

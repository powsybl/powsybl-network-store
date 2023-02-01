/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControlAdder;
import com.powsybl.network.store.iidm.impl.StaticVarCompensatorImpl;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class VoltagePerReactivePowerControlAdderImpl extends AbstractExtensionAdder<StaticVarCompensator, VoltagePerReactivePowerControl> implements VoltagePerReactivePowerControlAdder {

    private double slope;

    public VoltagePerReactivePowerControlAdderImpl(StaticVarCompensator extendable) {
        super(extendable);
    }

    @Override
    protected VoltagePerReactivePowerControl createExtension(StaticVarCompensator staticVarCompensator) {
        return new VoltagePerReactivePowerControlImpl((StaticVarCompensatorImpl) staticVarCompensator, slope);
    }

    @Override
    public VoltagePerReactivePowerControlAdder withSlope(double slope) {
        this.slope = slope;
        return this;
    }
}

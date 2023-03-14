/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;
import com.powsybl.network.store.iidm.impl.StaticVarCompensatorImpl;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class VoltagePerReactivePowerControlImpl extends AbstractExtension<StaticVarCompensator> implements VoltagePerReactivePowerControl {

    public VoltagePerReactivePowerControlImpl(StaticVarCompensatorImpl staticVarCompensator) {
        super(staticVarCompensator);
    }

    private StaticVarCompensatorImpl getSvc() {
        return (StaticVarCompensatorImpl) getExtendable();
    }

    @Override
    public double getSlope() {
        return getSvc().checkResource().getAttributes().getVoltagePerReactiveControl().getSlope();
    }

    @Override
    public VoltagePerReactivePowerControl setSlope(double slope) {
        getSvc().updateResource(res -> res.getAttributes().getVoltagePerReactiveControl().setSlope(slope));
        return this;
    }
}

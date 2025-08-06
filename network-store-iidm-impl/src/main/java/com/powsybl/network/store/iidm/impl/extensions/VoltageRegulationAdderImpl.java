/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.VoltageRegulation;
import com.powsybl.iidm.network.extensions.VoltageRegulationAdder;
import com.powsybl.network.store.iidm.impl.BatteryImpl;
import com.powsybl.network.store.iidm.impl.TerminalRefUtils;
import com.powsybl.network.store.model.TerminalRefAttributes;
import com.powsybl.network.store.model.VoltageRegulationAttributes;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class VoltageRegulationAdderImpl extends AbstractIidmExtensionAdder<Battery, VoltageRegulation> implements VoltageRegulationAdder {

    private boolean voltageRegulatorOn;

    private double targetV;

    private TerminalRefAttributes terminalRefAttributes = null;

    public VoltageRegulationAdderImpl(Battery battery) {
        super(battery);
    }

    @Override
    protected VoltageRegulation createExtension(Battery battery) {
        TerminalRefAttributes finalTerminalRefAttributes = terminalRefAttributes;
        if (terminalRefAttributes == null) {
            finalTerminalRefAttributes = TerminalRefUtils.getTerminalRefAttributes(battery.getTerminal());
        }
        VoltageRegulationAttributes voltageRegulationAttributes = VoltageRegulationAttributes.builder()
            .voltageRegulatorOn(voltageRegulatorOn)
            .targetV(targetV)
            .regulatingTerminal(finalTerminalRefAttributes)
            .build();
        ((BatteryImpl) battery).updateResourceWithoutNotification(
            res -> res.getAttributes().getExtensionAttributes()
                .put(VoltageRegulation.NAME, voltageRegulationAttributes));
        return new VoltageRegulationImpl(battery);
    }

    @Override
    public VoltageRegulationAdder withVoltageRegulatorOn(boolean voltageRegulatorOn) {
        this.voltageRegulatorOn = voltageRegulatorOn;
        return this;
    }

    @Override
    public VoltageRegulationAdder withTargetV(double targetV) {
        this.targetV = targetV;
        return this;
    }

    @Override
    public VoltageRegulationAdder withRegulatingTerminal(Terminal terminal) {
        if (terminal != null) {
            this.terminalRefAttributes = TerminalRefUtils.getTerminalRefAttributes(terminal);
        } else {
            this.terminalRefAttributes = null;
        }
        return this;
    }
}

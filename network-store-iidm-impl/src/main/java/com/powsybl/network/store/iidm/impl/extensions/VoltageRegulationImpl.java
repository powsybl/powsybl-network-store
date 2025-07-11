/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.VoltageRegulation;
import com.powsybl.network.store.iidm.impl.BatteryImpl;
import com.powsybl.network.store.iidm.impl.TerminalRefUtils;
import com.powsybl.network.store.model.TerminalRefAttributes;
import com.powsybl.network.store.model.VoltageRegulationAttributes;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class VoltageRegulationImpl extends AbstractExtension<Battery> implements VoltageRegulation {

    public VoltageRegulationImpl(Battery battery) {
        super(battery);
    }

    private BatteryImpl getBattery() {
        return (BatteryImpl) getExtendable();
    }

    private VoltageRegulationAttributes getVoltageRegulationAttributes() {
        return (VoltageRegulationAttributes) getBattery().getResource().getAttributes().getExtensionAttributes().get(VoltageRegulation.NAME);
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return getVoltageRegulationAttributes().isVoltageRegulatorOn();
    }

    @Override
    public void setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        boolean oldValue = isVoltageRegulatorOn();
        if (oldValue != voltageRegulatorOn) {
            getBattery().updateResourceExtension(this,
                res -> getVoltageRegulationAttributes().setVoltageRegulatorOn(voltageRegulatorOn),
                "voltageRegulatorOn", oldValue, voltageRegulatorOn);
        }
    }

    @Override
    public double getTargetV() {
        return getVoltageRegulationAttributes().getTargetV();
    }

    @Override
    public void setTargetV(double targetV) {
        double oldValue = getTargetV();
        if (oldValue != targetV) {
            getBattery().updateResourceExtension(this,
                res -> getVoltageRegulationAttributes().setTargetV(targetV),
                "targetV", oldValue, targetV);
        }
    }

    @Override
    public Terminal getRegulatingTerminal() {
        return TerminalRefUtils.getTerminal(getBattery().getIndex(), getVoltageRegulationAttributes().getRegulatingTerminal());
    }

    @Override
    public void setRegulatingTerminal(Terminal terminal) {
        Terminal oldValue = getRegulatingTerminal();
        if (oldValue != terminal) {
            TerminalRefAttributes finalTerminal;
            if (terminal != null) {
                finalTerminal = TerminalRefUtils.getTerminalRefAttributes(terminal);
            } else {
                finalTerminal = null;
            }
            getBattery().updateResourceExtension(this,
                res -> getVoltageRegulationAttributes().setRegulatingTerminal(finalTerminal),
                "targetV", oldValue, finalTerminal);
        }

    }
}

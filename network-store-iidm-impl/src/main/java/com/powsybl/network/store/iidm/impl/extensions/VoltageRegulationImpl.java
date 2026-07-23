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
import com.powsybl.network.store.iidm.impl.BatteryImpl;
import com.powsybl.network.store.iidm.impl.TerminalImpl;
import com.powsybl.network.store.iidm.impl.TerminalRefUtils;
import com.powsybl.network.store.model.TerminalRefAttributes;
import com.powsybl.network.store.model.VoltageRegulationAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class VoltageRegulationImpl extends AbstractIidmExtension<Battery> implements VoltageRegulation {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoltageRegulationImpl.class);

    public VoltageRegulationImpl(Battery battery) {
        super(battery);
        TerminalImpl<?> terminal = (TerminalImpl<?>) getRegulatingTerminal();
        if (terminal != null) {
            terminal.getReferrerManager().register(this);
        }
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
    public void setRegulatingTerminal(Terminal regulatingTerminal) {
        Terminal oldValue = getRegulatingTerminal();
        Terminal newRegulatingTerminal = regulatingTerminal != null ? regulatingTerminal : getExtendable().getTerminal();
        if (oldValue != newRegulatingTerminal) {
            TerminalRefAttributes finalTerminal = TerminalRefUtils.getTerminalRefAttributes(newRegulatingTerminal);
            getBattery().updateResourceExtension(this,
                res -> getVoltageRegulationAttributes().setRegulatingTerminal(finalTerminal),
                "regulatingTerminal", oldValue, finalTerminal);
        }

    }

    @Override
    public void onReferencedRemoval(Terminal removedTerminal) {
        LOGGER.warn("Change 'VoltageRegulation' regulatingTerminal to local for battery '{}', because its regulating terminal has been removed", getExtendable().getId());
        if (removedTerminal != null && removedTerminal.getConnectable().getId().equals(getRegulatingTerminal().getConnectable().getId())) {
            setRegulatingTerminal(null);
        }
    }

    @Override
    public void onReferencedReplacement(Terminal oldReferenced, Terminal newReferenced) {
        Terminal oldTerminal = getRegulatingTerminal();
        if (oldTerminal == null || oldTerminal.getConnectable().getId().equals(oldReferenced.getConnectable().getId())) {
            setRegulatingTerminal(newReferenced);
        }
    }

    @Override
    public void cleanup() {
        if (getRegulatingTerminal() != null) {
            ((TerminalImpl<?>) getRegulatingTerminal()).getReferrerManager().unregister(this);
        }
    }
}

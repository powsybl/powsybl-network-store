/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ShuntCompensatorAttributes;
import com.powsybl.network.store.model.TerminalRefAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ShuntCompensatorImpl extends AbstractInjectionImpl<ShuntCompensator, ShuntCompensatorAttributes> implements ShuntCompensator {

    public ShuntCompensatorImpl(NetworkObjectIndex index, Resource<ShuntCompensatorAttributes> resource) {
        super(index, resource);
    }

    static ShuntCompensatorImpl create(NetworkObjectIndex index, Resource<ShuntCompensatorAttributes> resource) {
        return new ShuntCompensatorImpl(index, resource);
    }

    @Override
    protected ShuntCompensator getInjection() {
        return this;
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.SHUNT_COMPENSATOR;
    }

    @Override
    public double getbPerSection() {
        return resource.getAttributes().getBPerSection();
    }

    @Override
    public ShuntCompensator setbPerSection(double bPerSection) {
        resource.getAttributes().setBPerSection(bPerSection);
        return this;
    }

    @Override
    public int getMaximumSectionCount() {
        return resource.getAttributes().getMaximumSectionCount();
    }

    @Override
    public ShuntCompensator setMaximumSectionCount(int maximumSectionCount) {
        resource.getAttributes().setMaximumSectionCount(maximumSectionCount);
        return this;
    }

    @Override
    public int getCurrentSectionCount() {
        return resource.getAttributes().getCurrentSectionCount();
    }

    @Override
    public ShuntCompensator setCurrentSectionCount(int currentSectionCount) {
        resource.getAttributes().setCurrentSectionCount(currentSectionCount);
        return this;
    }

    @Override
    public double getCurrentB() {
        return getbPerSection() * getCurrentSectionCount();
    }

    @Override
    public double getMaximumB() {
        return getbPerSection() * getMaximumSectionCount();
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        // TODO
        return false;
    }

    @Override
    public ShuntCompensator setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        // TODO
        return this;
    }

    @Override
    public Terminal getRegulatingTerminal() {
        TerminalRefAttributes terminalRefAttributes = resource.getAttributes().getRegulatingTerminal();
        return TerminalRefUtils.getTerminal(index, terminalRefAttributes);
    }

    @Override
    public ShuntCompensator setRegulatingTerminal(Terminal regulatingTerminal) {
        resource.getAttributes().setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal));
        return this;
    }

    @Override
    public double getTargetV() {
        // TODO
        return Double.NaN;
    }

    @Override
    public ShuntCompensator setTargetV(double targetV) {
        // TODO
        return this;
    }

    @Override
    public double getTargetDeadband() {
        // TODO
        return Double.NaN;
    }

    @Override
    public ShuntCompensator setTargetDeadband(double targetDeadband) {
        // TODO
        return this;
    }

    protected String getTypeDescription() {
        return "Shunt compensator";
    }
}

/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ShuntCompensatorAttributes;
import com.powsybl.network.store.model.TerminalRefAttributes;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ShuntCompensatorAdderImpl extends AbstractInjectionAdder<ShuntCompensatorAdderImpl> implements ShuntCompensatorAdder {

    private double bPerSection;

    private int maximumSectionCount;

    private int currentSectionCount;

    private Terminal regulatingTerminal;

    private boolean voltageRegulatorOn = false;

    private double targetV = Double.NaN;

    private double targetDeadband = Double.NaN;

    ShuntCompensatorAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(voltageLevelResource, index);
    }

    @Override
    public ShuntCompensatorAdder setbPerSection(double bPerSection) {
        this.bPerSection = bPerSection;
        return this;
    }

    @Override
    public ShuntCompensatorAdder setMaximumSectionCount(int maximumSectionCount) {
        this.maximumSectionCount = maximumSectionCount;
        return this;
    }

    @Override
    public ShuntCompensatorAdder setCurrentSectionCount(int currentSectionCount) {
        this.currentSectionCount = currentSectionCount;
        return this;
    }

    @Override
    public ShuntCompensatorAdderImpl setRegulatingTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = regulatingTerminal;
        return this;
    }

    @Override
    public ShuntCompensatorAdder setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        this.voltageRegulatorOn = voltageRegulatorOn;
        return this;
    }

    @Override
    public ShuntCompensatorAdder setTargetV(double targetV) {
        this.targetV = targetV;
        return this;
    }

    @Override
    public ShuntCompensatorAdder setTargetDeadband(double targetDeadband) {
        this.targetDeadband = targetDeadband;
        return this;
    }

    @Override
    public ShuntCompensator add() {
        String id = checkAndGetUniqueId();
        checkNodeBus();
        ValidationUtil.checkbPerSection(this, bPerSection);
        ValidationUtil.checkSections(this, currentSectionCount, maximumSectionCount);
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        TerminalRefAttributes terminalRefAttributes = TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal);
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, targetV);
        ValidationUtil.checkTargetDeadband(this, "shunt compensator", voltageRegulatorOn, targetDeadband);

        Resource<ShuntCompensatorAttributes> resource = Resource.shuntCompensatorBuilder(index.getNetwork().getUuid(), index.getResourceUpdater())
                .id(id)
                .attributes(ShuntCompensatorAttributes.builder()
                        .voltageLevelId(getVoltageLevelResource().getId())
                        .name(getName())
                        .node(getNode())
                        .bus(getBus())
                        .connectableBus(getConnectableBus())
                        .bPerSection(bPerSection)
                        .maximumSectionCount(maximumSectionCount)
                        .currentSectionCount(currentSectionCount)
                        .regulatingTerminal(terminalRefAttributes)
                        .voltageRegulatorOn(voltageRegulatorOn)
                        .targetV(targetV)
                        .targetDeadband(targetDeadband)
                        .build())
                .build();
        return getIndex().createShuntCompensator(resource);
    }

    @Override
    protected String getTypeDescription() {
        return "Shunt compensator";
    }
}

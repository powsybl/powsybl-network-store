/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.*;
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
    public ShuntCompensator add() {
        String id = checkAndGetUniqueId();
        checkNodeBus();
        ValidationUtil.checkbPerSection(this, bPerSection);
        ValidationUtil.checkSections(this, currentSectionCount, maximumSectionCount);
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());

        TerminalRefAttributes terminalRefAttributes = regulatingTerminal == null ? null :
                TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal);

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
                        .build())
                .build();
        return getIndex().createShuntCompensator(resource);
    }

    @Override
    protected String getTypeDescription() {
        return "Shunt compensator";
    }
}

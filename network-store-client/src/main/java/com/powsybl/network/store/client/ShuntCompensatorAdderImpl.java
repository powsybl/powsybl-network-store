/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorAdder;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ShuntCompensatorAttributes;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ShuntCompensatorAdderImpl implements ShuntCompensatorAdder {

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    private final NetworkObjectIndex index;

    private String id;

    private String name;

    private Integer node;

    private String bus;

    private String connectableBus;

    private double bPerSection;

    private int maximumSectionCount;

    private int currentSectionCount;

    ShuntCompensatorAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        this.voltageLevelResource = voltageLevelResource;
        this.index = index;
    }

    @Override
    public ShuntCompensatorAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public ShuntCompensatorAdder setEnsureIdUnicity(boolean ensureIdUnicity) {
        // TODO
        return this;
    }

    @Override
    public ShuntCompensatorAdder setName(String name) {
        this.name = name;
        return this;

    }

    @Override
    public ShuntCompensatorAdder setNode(int node) {
        this.node = node;
        return this;
    }

    @Override
    public ShuntCompensatorAdder setBus(String bus) {
        this.bus = bus;
        return this;
    }

    @Override
    public ShuntCompensatorAdder setConnectableBus(String connectableBus) {
        this.connectableBus = connectableBus;
        return this;
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
    public ShuntCompensator add() {
        Resource<ShuntCompensatorAttributes> resource = Resource.shuntCompensatorBuilder()
                .id(id)
                .attributes(ShuntCompensatorAttributes.builder()
                        .voltageLevelId(voltageLevelResource.getId())
                        .name(name)
                        .node(node)
                        .bus(bus)
                        .connectableBus(connectableBus)
                        .bPerSection(bPerSection)
                        .maximumSectionCount(maximumSectionCount)
                        .currentSectionCount(currentSectionCount)
                        .build())
                .build();
        return index.createShuntCompensator(resource);
    }
}

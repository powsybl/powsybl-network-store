/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.LoadAdder;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.network.store.model.LoadAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LoadAdderImpl implements LoadAdder {

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    private final NetworkObjectIndex index;

    private String id;

    private String name;

    private Integer node = -1;

    private String bus;

    private String connectableBus;

    private LoadType loadType = LoadType.UNDEFINED;

    private double p0 = Double.NaN;

    private double q0 = Double.NaN;

    LoadAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        this.voltageLevelResource = voltageLevelResource;
        this.index = index;
    }

    @Override
    public LoadAdder setBus(String bus) {
        this.bus = bus;
        return this;

    }

    @Override
    public LoadAdder setConnectableBus(String connectableBus) {
        this.connectableBus = connectableBus;
        return this;

    }

    @Override
    public LoadAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public LoadAdder setEnsureIdUnicity(boolean ensureIdUnicity) {
        // TODO
        return this;

    }

    @Override
    public LoadAdder setName(String name) {
        this.name = name;
        return this;

    }

    @Override
    public LoadAdder setNode(int node) {
        this.node = node;
        return this;
    }

    @Override
    public LoadAdder setLoadType(LoadType loadType) {
        this.loadType = loadType;
        return this;

    }

    @Override
    public LoadAdder setP0(double p0) {
        this.p0 = p0;
        return this;

    }

    @Override
    public LoadAdder setQ0(double q0) {
        this.q0 = q0;
        return this;
    }

    @Override
    public Load add() {
        Resource<LoadAttributes> resource = Resource.loadBuilder()
                .id(id)
                .attributes(LoadAttributes.builder()
                                          .voltageLevelId(voltageLevelResource.getId())
                                          .name(name)
                                          .node(node)
                                          .bus(bus)
                                          .connectableBus(connectableBus)
                                          .loadType(loadType)
                                          .p0(p0)
                                          .q0(q0)
                                          .build())
                .build();
        return index.createLoad(resource);
    }
}

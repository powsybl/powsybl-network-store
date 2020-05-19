/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.DanglingLineAdder;
import com.powsybl.network.store.model.DanglingLineAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class DanglingLineAdderImpl implements DanglingLineAdder {

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    private final NetworkObjectIndex index;

    private String id;

    private String name;

    private Integer node;

    private double p0 = Double.NaN;

    private double q0 = Double.NaN;

    private double r = Double.NaN;

    private double x = Double.NaN;

    private double g = Double.NaN;

    private double b = Double.NaN;

    private String ucteXNodeCode = null;

    private String bus;

    private String connectableBus;

    DanglingLineAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        this.voltageLevelResource = voltageLevelResource;
        this.index = index;
    }

    @Override
    public DanglingLineAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public DanglingLineAdder setEnsureIdUnicity(boolean b) {
        // TODO
        return this;
    }

    @Override
    public DanglingLineAdder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public DanglingLineAdder setNode(int node) {
        this.node = node;
        return this;
    }

    @Override
    public DanglingLineAdder setP0(double p0) {
        this.p0 = p0;
        return this;
    }

    @Override
    public DanglingLineAdder setQ0(double q0) {
        this.q0 = q0;
        return this;
    }

    @Override
    public DanglingLineAdder setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public DanglingLineAdder setX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public DanglingLineAdder setG(double g) {
        this.g = g;
        return this;
    }

    @Override
    public DanglingLineAdder setB(double b) {
        this.b = b;
        return this;
    }

    @Override
    public DanglingLineAdder setUcteXnodeCode(String ucteXNodeCode) {
        this.ucteXNodeCode = ucteXNodeCode;
        return this;
    }

    @Override
    public DanglingLineAdder setBus(String bus) {
        this.bus = bus;
        return this;
    }

    @Override
    public DanglingLineAdder setConnectableBus(String connectableBus) {
        this.connectableBus = connectableBus;
        return this;
    }

    @Override
    public DanglingLine add() {
        Resource<DanglingLineAttributes> resource = Resource.danglingLineBuilder(index.getNetwork().getUuid(), index.getStoreClient())
                .id(id)
                .attributes(DanglingLineAttributes.builder()
                        .voltageLevelId(voltageLevelResource.getId())
                        .name(name)
                        .node(node)
                        .bus(bus)
                        .connectableBus(connectableBus)
                        .p0(p0)
                        .q0(q0)
                        .r(r)
                        .x(x)
                        .g(g)
                        .b(b)
                        .ucteXnodeCode(ucteXNodeCode)
                        .build())
                .build();
        return index.createDanglingLine(resource);
    }

}

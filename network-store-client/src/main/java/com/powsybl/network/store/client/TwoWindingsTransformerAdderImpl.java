/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformerAdder;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TwoWindingsTransformerAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TwoWindingsTransformerAdderImpl implements TwoWindingsTransformerAdder {

    private final NetworkObjectIndex index;

    private String id;

    private String name;

    private String voltageLevelId1;

    private Integer node1 = -1;

    private String bus1;

    private String connectableBus1;

    private String voltageLevelId2;

    private Integer node2 = -1;

    private String bus2;

    private String connectableBus2;

    private double r = Double.NaN;

    private double x = Double.NaN;

    private double g = Double.NaN;

    private double b = Double.NaN;

    private double ratedU1 = Double.NaN;

    private double ratedU2 = Double.NaN;

    TwoWindingsTransformerAdderImpl(NetworkObjectIndex index) {
        this.index = index;
    }

    @Override
    public TwoWindingsTransformerAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setEnsureIdUnicity(boolean ensureIdUnicity) {
        // TODO
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setVoltageLevel1(String voltageLevelId1) {
        this.voltageLevelId1 = voltageLevelId1;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setNode1(int node1) {
        this.node1 = node1;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setBus1(String bus1) {
        this.bus1 = bus1;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setConnectableBus1(String connectableBus1) {
        this.connectableBus1 = connectableBus1;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setVoltageLevel2(String voltageLevelId2) {
        this.voltageLevelId2 = voltageLevelId2;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setNode2(int node2) {
        this.node2 = node2;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setBus2(String bus2) {
        this.bus2 = bus2;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setConnectableBus2(String connectableBus2) {
        this.connectableBus2 = connectableBus2;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setB(double b) {
        this.b = b;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setG(double g) {
        this.g = g;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setRatedU1(double ratedU1) {
        this.ratedU1 = ratedU1;
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setRatedU2(double ratedU2) {
        this.ratedU2 = ratedU2;
        return this;
    }

    @Override
    public TwoWindingsTransformer add() {
        Resource<TwoWindingsTransformerAttributes> resource = Resource.twoWindingsTransformerBuilder()
                .id(id)
                .attributes(TwoWindingsTransformerAttributes.builder()
                        .voltageLevelId1(voltageLevelId1)
                        .voltageLevelId2(voltageLevelId2)
                        .name(name)
                        .node1(node1)
                        .node2(node2)
                        .bus1(bus1)
                        .bus2(bus2)
                        .connectableBus1(connectableBus1)
                        .connectableBus2(connectableBus2)
                        .r(r)
                        .x(x)
                        .b(b)
                        .g(g)
                        .ratedU1(ratedU1)
                        .ratedU2(ratedU2)
                        .build())
                .build();
        return index.createTwoWindingsTransformer(resource);
    }
}

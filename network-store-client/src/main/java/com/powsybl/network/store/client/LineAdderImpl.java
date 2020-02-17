/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.network.store.model.LineAttributes;
import com.powsybl.network.store.model.Resource;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LineAdderImpl implements LineAdder {

    private final NetworkObjectIndex index;

    private String id;

    private String name;

    private String voltageLevelId1;

    private int node1 = -1;

    private String voltageLevelId2;

    private int node2 = -1;

    private double r = Double.NaN;

    private double x = Double.NaN;

    private double g1 = Double.NaN;

    private double b1 = Double.NaN;

    private double g2 = Double.NaN;

    private double b2 = Double.NaN;

    private String bus1;

    private String bus2;

    private String connectableBus1;

    private String connectableBus2;

    LineAdderImpl(NetworkObjectIndex index) {
        this.index = index;
    }

    @Override
    public LineAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public LineAdder setEnsureIdUnicity(boolean ensureIdUnicity) {
        return this;
    }

    @Override
    public LineAdder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public LineAdder setVoltageLevel1(String voltageLevelId1) {
        this.voltageLevelId1 = voltageLevelId1;
        return this;
    }

    @Override
    public LineAdder setNode1(int node1) {
        this.node1 = node1;
        return this;
    }

    @Override
    public LineAdder setBus1(String bus1) {
        this.bus1 = bus1;
        return this;
    }

    @Override
    public LineAdder setConnectableBus1(String connectableBus1) {
        this.connectableBus1 = connectableBus1;
        return this;
    }

    @Override
    public LineAdder setVoltageLevel2(String voltageLevelId2) {
        this.voltageLevelId2 = voltageLevelId2;
        return this;
    }

    @Override
    public LineAdder setNode2(int node2) {
        this.node2 = node2;
        return this;
    }

    @Override
    public LineAdder setBus2(String bus2) {
        this.bus2 = bus2;
        return this;
    }

    @Override
    public LineAdder setConnectableBus2(String connectableBus2) {
        this.connectableBus2 = connectableBus2;
        return this;
    }

    @Override
    public LineAdder setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public LineAdder setX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public LineAdder setG1(double g1) {
        this.g1 = g1;
        return this;
    }

    @Override
    public LineAdder setB1(double b1) {
        this.b1 = b1;
        return this;
    }

    @Override
    public LineAdder setG2(double g2) {
        this.g2 = g2;
        return this;
    }

    @Override
    public LineAdder setB2(double b2) {
        this.b2 = b2;
        return this;
    }

    @Override
    public Line add() {
        Resource<LineAttributes> resource = Resource.lineBuilder()
                .id(id)
                .attributes(LineAttributes.builder()
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
                        .g1(g1)
                        .b1(b1)
                        .g2(g2)
                        .b2(b2)
                        .build())
                .build();
        return index.createLine(resource);
    }
}

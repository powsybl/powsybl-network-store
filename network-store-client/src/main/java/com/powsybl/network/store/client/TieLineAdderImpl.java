/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.TieLineAdder;
import com.powsybl.network.store.model.LineAttributes;
import com.powsybl.network.store.model.MergedXnodeAttributes;
import com.powsybl.network.store.model.Resource;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

public class TieLineAdderImpl implements TieLineAdder {

    private NetworkObjectIndex index;
    private String id;
    private String name;
    private double r;
    private double x;
    private double g1;
    private double g2;
    private double b1;
    private double b2;
    private float rdp;
    private float xdp;
    private double xnodeP;
    private double xnodeQ;
    private String ucteXnodeCode;
    private String line1Name;
    private String line2Name;
    private Integer node1;
    private Integer node2;
    private String bus1;
    private String bus2;
    private String connectableBus1;
    private String connectableBus2;
    private String voltageLevelId1;
    private String voltageLevelId2;

    public TieLineAdderImpl(NetworkObjectIndex index) {
        this.index = index;
    }

    @Override
    public TieLineAdder setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public TieLineAdder setX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public TieLineAdder setG1(double g1) {
        this.g1 = g1;
        return this;
    }

    @Override
    public TieLineAdder setB1(double b1) {
        this.b1 = b1;
        return this;
    }

    @Override
    public TieLineAdder setG2(double g2) {
        this.g2 = g2;
        return this;
    }

    @Override
    public TieLineAdder setB2(double b2) {
        this.b2 = b2;
        return this;
    }

    @Override
    public TieLineAdder setXnodeP(double xnodeP) {
        this.xnodeP = xnodeP;
        return this;
    }

    @Override
    public TieLineAdder setXnodeQ(double xnodeQ) {
        this.xnodeQ = xnodeQ;
        return this;
    }

    @Override
    public TieLineAdder setUcteXnodeCode(String ucteXnodeCode) {
        this.ucteXnodeCode = ucteXnodeCode;
        return this;
    }

    @Override
    public TieLineAdder line1() {
        return this;
    }

    @Override
    public TieLineAdder line2() {
        return this;
    }

    @Override
    public TieLineAdder setVoltageLevel1(String voltageLevelId1) {
        this.voltageLevelId1 = voltageLevelId1;
        return this;
    }

    @Override
    public TieLineAdder setVoltageLevel2(String voltageLevelId2) {
        this.voltageLevelId2 = voltageLevelId2;
        return this;
    }

    @Override
    public TieLineAdder setNode1(int node1) {
        this.node1 = node1;
        return this;
    }

    @Override
    public TieLineAdder setNode2(int node2) {
        this.node2 = node2;
        return this;
    }

    @Override
    public TieLineAdder setBus1(String bus1) {
        this.bus1 = bus1;
        return this;
    }

    @Override
    public TieLineAdder setBus2(String bus2) {
        this.bus2 = bus2;
        return this;
    }

    @Override
    public TieLineAdder setConnectableBus1(String connectableBus1) {
        this.connectableBus1 = connectableBus1;
        return this;
    }

    @Override
    public TieLineAdder setConnectableBus2(String connectableBus2) {
        this.connectableBus2 = connectableBus2;
        return this;
    }

    @Override
    public TieLineAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public TieLineAdder setEnsureIdUnicity(boolean ensureIdUnicity) {
        return this;
    }

    @Override
    public TieLineAdder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public TieLine add() {
        //TODO how to set these values
        rdp = 0;
        xdp = 0;
        line1Name = "";
        line2Name = "";
        Resource<LineAttributes> resource = Resource.lineBuilder(index.getNetwork().getUuid(), index.getResourceUpdater())
                .id(id)
                .attributes(LineAttributes.builder()
                        .name(name)
                        .b1(b1)
                        .b2(b2)
                        .g1(g1)
                        .g2(g2)
                        .r(r)
                        .x(x)
                        .voltageLevelId1(voltageLevelId1)
                        .voltageLevelId2(voltageLevelId2)
                        .node1(node1)
                        .node2(node2)
                        .bus1(bus1)
                        .bus2(bus2)
                        .connectableBus1(connectableBus1)
                        .connectableBus2(connectableBus2)
                        .mergedXnode(
                                MergedXnodeAttributes.builder()
                                        .rdp(rdp)
                                        .xdp(xdp)
                                        .xnodeP1(xnodeP)
                                        .xnodeP2(xnodeP)
                                        .xnodeQ1(xnodeQ)
                                        .xnodeQ2(xnodeQ)
                                        .line1Name(line1Name)
                                        .line2Name(line2Name)
                                        .code(ucteXnodeCode)
                                        .build())
                        .build()).build();
        index.createLine(resource);
        return new TieLineImpl(index, resource);
    }
}

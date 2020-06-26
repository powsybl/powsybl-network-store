/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.TieLineAdder;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.network.store.model.LineAttributes;
import com.powsybl.network.store.model.MergedXnodeAttributes;
import com.powsybl.network.store.model.Resource;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

public class TieLineAdderImpl extends AbstractBranchAdder<TieLineAdderImpl> implements TieLineAdder {

    private double r = Double.NaN;

    private double x = Double.NaN;

    private double g1 = Double.NaN;

    private double g2 = Double.NaN;

    private double b1 = Double.NaN;

    private double b2 = Double.NaN;

    private float rdp;

    private float xdp;

    private double xnodeP = Double.NaN;

    private double xnodeQ = Double.NaN;

    private String ucteXnodeCode;

    private String line1Name;

    private String line2Name;

    public TieLineAdderImpl(NetworkObjectIndex index) {
        super(index);
    }

    @Override
    public TieLineAdderImpl setId(String id) {
        if (getId() == null) {
            super.setId(id);
        }
        return this;
    }

    @Override
    public TieLineAdderImpl setName(String name) {
        if (getName() == null) {
            super.setName(name);
        }
        return this;
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
    public TieLine add() {
        //TODO how to set these values
        rdp = 0;
        xdp = 0;
        line1Name = "";
        line2Name = "";

        String id = checkAndGetUniqueId();
        checkVoltageLevel1();
        checkVoltageLevel2();
        checkNodeBus1();
        checkNodeBus2();

        validate();

        Resource<LineAttributes> resource = Resource.lineBuilder(index.getResourceUpdater())
                .id(id)
                .attributes(LineAttributes.builder()
                        .name(getName())
                        .b1(b1)
                        .b2(b2)
                        .g1(g1)
                        .g2(g2)
                        .r(r)
                        .x(x)
                        .voltageLevelId1(getVoltageLevelId1())
                        .voltageLevelId2(getVoltageLevelId2())
                        .node1(getNode1())
                        .node2(getNode2())
                        .bus1(getBus1())
                        .bus2(getBus2())
                        .connectableBus1(getConnectableBus1())
                        .connectableBus2(getConnectableBus2())
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
        getIndex().createLine(resource);
        return new TieLineImpl(getIndex(), resource);
    }

    private void validate() {
        if (ucteXnodeCode == null) {
            throw new ValidationException(this, "ucteXnodeCode is not set");
        }
        if (Double.isNaN(r)) {
            throw new ValidationException(this, "r is not set");
        }
        if (Double.isNaN(x)) {
            throw new ValidationException(this, "x is not set");
        }
        if (Double.isNaN(g1)) {
            throw new ValidationException(this, "g1 is not set");
        }
        if (Double.isNaN(b1)) {
            throw new ValidationException(this, "b1 is not set");
        }
        if (Double.isNaN(g2)) {
            throw new ValidationException(this, "g2 is not set");
        }
        if (Double.isNaN(b2)) {
            throw new ValidationException(this, "b2 is not set");
        }
        if (Double.isNaN(xnodeP)) {
            throw new ValidationException(this, "xnodeP is not set");
        }
        if (Double.isNaN(xnodeQ)) {
            throw new ValidationException(this, "xnodeQ is not set");
        }
    }

    @Override
    protected String getTypeDescription() {
        return "AC tie Line";
    }
}

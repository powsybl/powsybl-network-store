/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.google.common.base.Strings;
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

    private final class HalfLineAdderImpl implements HalfLineAdder {

        private final boolean one;

        private String id;

        private String name;

        private double r = Double.NaN;

        private double x = Double.NaN;

        private double g1 = Double.NaN;

        private double g2 = Double.NaN;

        private double b1 = Double.NaN;

        private double b2 = Double.NaN;

        private double xnodeP = Double.NaN;

        private double xnodeQ = Double.NaN;

        private boolean fictitious = false;

        private HalfLineAdderImpl(boolean one) {
            this.one = one;
        }

        public String getId() {
            return id;
        }

        public HalfLineAdderImpl setId(String id) {
            this.id = id;
            return this;
        }

        public String getName() {
            return name;
        }

        public HalfLineAdderImpl setName(String name) {
            this.name = name;
            return this;
        }

        public boolean isFictitious() {
            return fictitious;
        }

        @Override
        public HalfLineAdder setFictitious(boolean fictitious) {
            this.fictitious = fictitious;
            return this;
        }

        public double getR() {
            return r;
        }

        public HalfLineAdderImpl setR(double r) {
            this.r = r;
            return this;
        }

        public double getX() {
            return x;
        }

        public HalfLineAdderImpl setX(double x) {
            this.x = x;
            return this;
        }

        public double getG1() {
            return g1;
        }

        public HalfLineAdderImpl setG1(double g1) {
            this.g1 = g1;
            return this;
        }

        public double getG2() {
            return g2;
        }

        public HalfLineAdderImpl setG2(double g2) {
            this.g2 = g2;
            return this;
        }

        public double getB1() {
            return b1;
        }

        public HalfLineAdderImpl setB1(double b1) {
            this.b1 = b1;
            return this;
        }

        public double getB2() {
            return b2;
        }

        public HalfLineAdderImpl setB2(double b2) {
            this.b2 = b2;
            return this;
        }

        public double getXnodeP() {
            return xnodeP;
        }

        public HalfLineAdderImpl setXnodeP(double xnodeP) {
            this.xnodeP = xnodeP;
            return this;
        }

        public double getXnodeQ() {
            return xnodeQ;
        }

        public HalfLineAdderImpl setXnodeQ(double xnodeQ) {
            this.xnodeQ = xnodeQ;
            return this;
        }

        private void validate() {
            int num = one ? 1 : 2;
            if (Strings.isNullOrEmpty(id)) {
                throw new ValidationException(TieLineAdderImpl.this, "id is not set for half line " + num);
            }
            if (Double.isNaN(r)) {
                throw new ValidationException(TieLineAdderImpl.this, "r is not set for half line " + num);
            }
            if (Double.isNaN(x)) {
                throw new ValidationException(TieLineAdderImpl.this, "x is not set for half line " + num);
            }
            if (Double.isNaN(g1)) {
                throw new ValidationException(TieLineAdderImpl.this, "g1 is not set for half line " + num);
            }
            if (Double.isNaN(b1)) {
                throw new ValidationException(TieLineAdderImpl.this, "b1 is not set for half line " + num);
            }
            if (Double.isNaN(g2)) {
                throw new ValidationException(TieLineAdderImpl.this, "g2 is not set for half line " + num);
            }
            if (Double.isNaN(b2)) {
                throw new ValidationException(TieLineAdderImpl.this, "b2 is not set for half line " + num);
            }
            if (Double.isNaN(xnodeP)) {
                throw new ValidationException(TieLineAdderImpl.this, "xnodeP is not set for half line " + num);
            }
            if (Double.isNaN(xnodeQ)) {
                throw new ValidationException(TieLineAdderImpl.this, "xnodeQ is not set for half line " + num);
            }
        }

        @Override
        public TieLineAdder add() {
            return TieLineAdderImpl.this;
        }
    }

    private HalfLineAdderImpl halfLine1Adder;

    private HalfLineAdderImpl halfLine2Adder;

    private String ucteXnodeCode;

    public TieLineAdderImpl(NetworkObjectIndex index) {
        super(index);
    }

    @Override
    public TieLineAdder setUcteXnodeCode(String ucteXnodeCode) {
        this.ucteXnodeCode = ucteXnodeCode;
        return this;
    }

    @Override
    public HalfLineAdder newHalfLine1() {
        halfLine1Adder = new HalfLineAdderImpl(true);
        return halfLine1Adder;
    }

    @Override
    public HalfLineAdder newHalfLine2() {
        halfLine2Adder = new HalfLineAdderImpl(false);
        return halfLine2Adder;
    }

    @Override
    public TieLine add() {
        String id = checkAndGetUniqueId();
        checkVoltageLevel1();
        checkVoltageLevel2();
        checkNodeBus1();
        checkNodeBus2();

        validate();

        double r = halfLine1Adder.getR() + halfLine2Adder.getR();
        double x = halfLine1Adder.getX() + halfLine2Adder.getX();
        double b1 = halfLine1Adder.getB1() + halfLine1Adder.getB2();
        double b2 = halfLine2Adder.getB1() + halfLine2Adder.getB2();
        double g1 = halfLine1Adder.getG1() + halfLine1Adder.getG2();
        double g2 = halfLine2Adder.getG1() + halfLine2Adder.getG2();
        double rdp = r == 0 ? 0.5 : halfLine1Adder.getR() / r;
        double xdp = x == 0 ? 0.5 : halfLine1Adder.getX() / x;
        Resource<LineAttributes> resource = Resource.lineBuilder(index.getNetwork().getUuid(), index.getResourceUpdater())
                .id(id)
                .attributes(LineAttributes.builder()
                        .name(getName())
                        .fictitious(isFictitious())
                        .r(r)
                        .x(x)
                        .b1(b1)
                        .b2(b2)
                        .g1(g1)
                        .g2(g2)
                        .voltageLevelId1(getVoltageLevelId1())
                        .voltageLevelId2(getVoltageLevelId2())
                        .node1(getNode1())
                        .node2(getNode2())
                        .bus1(getBus1())
                        .bus2(getBus2())
                        .connectableBus1(getConnectableBus1() != null ? getConnectableBus1() : getBus1())
                        .connectableBus2(getConnectableBus2() != null ? getConnectableBus2() : getBus2())
                        .mergedXnode(
                                MergedXnodeAttributes.builder()
                                        .rdp((float) rdp)
                                        .xdp((float) xdp)
                                        .xnodeP1(halfLine1Adder.getXnodeP())
                                        .xnodeP2(halfLine2Adder.getXnodeP())
                                        .xnodeQ1(halfLine1Adder.getXnodeQ())
                                        .xnodeQ2(halfLine2Adder.getXnodeQ())
                                        .line1Id(halfLine1Adder.getId())
                                        .line2Id(halfLine2Adder.getId())
                                        .line1Name(halfLine1Adder.getName())
                                        .line2Name(halfLine2Adder.getName())
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

        if (halfLine1Adder == null) {
            throw new ValidationException(this, "half line 1 is not set");
        }

        if (halfLine2Adder == null) {
            throw new ValidationException(this, "half line 2 is not set");
        }

        halfLine1Adder.validate();
        halfLine2Adder.validate();
    }

    @Override
    protected String getTypeDescription() {
        return "AC tie Line";
    }
}

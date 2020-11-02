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

    private static class HalfLine {

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

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getR() {
            return r;
        }

        public void setR(double r) {
            this.r = r;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getG1() {
            return g1;
        }

        public void setG1(double g1) {
            this.g1 = g1;
        }

        public double getG2() {
            return g2;
        }

        public void setG2(double g2) {
            this.g2 = g2;
        }

        public double getB1() {
            return b1;
        }

        public void setB1(double b1) {
            this.b1 = b1;
        }

        public double getB2() {
            return b2;
        }

        public void setB2(double b2) {
            this.b2 = b2;
        }

        public double getXnodeP() {
            return xnodeP;
        }

        public void setXnodeP(double xnodeP) {
            this.xnodeP = xnodeP;
        }

        public double getXnodeQ() {
            return xnodeQ;
        }

        public void setXnodeQ(double xnodeQ) {
            this.xnodeQ = xnodeQ;
        }
    }

    private HalfLine half1 = new HalfLine();

    private HalfLine half2 = new HalfLine();

    private HalfLine activeHalf;

    private String ucteXnodeCode;

    public TieLineAdderImpl(NetworkObjectIndex index) {
        super(index);
    }

    @Override
    public TieLineAdder setUcteXnodeCode(String ucteXnodeCode) {
        this.ucteXnodeCode = ucteXnodeCode;
        return this;
    }

    public TieLineAdderImpl line1() {
        activeHalf = half1;
        return this;
    }

    public TieLineAdderImpl line2() {
        activeHalf = half2;
        return this;
    }

    private HalfLine getActiveHalf() {
        if (activeHalf == null) {
            throw new ValidationException(this, "No active half of the line");
        } else {
            return activeHalf;
        }
    }

    public TieLineAdderImpl setId(String id) {
        if (activeHalf == null) {
            return super.setId(id);
        } else {
            activeHalf.setId(id);
            return this;
        }
    }

    public TieLineAdderImpl setName(String name) {
        if (activeHalf == null) {
            return super.setName(name);
        } else {
            activeHalf.setName(name);
            return this;
        }
    }

    public TieLineAdderImpl setR(double r) {
        getActiveHalf().setR(r);
        return this;
    }

    public TieLineAdderImpl setX(double x) {
        getActiveHalf().setX(x);
        return this;
    }

    public TieLineAdderImpl setG1(double g1) {
        getActiveHalf().setG1(g1);
        return this;
    }

    public TieLineAdderImpl setG2(double g2) {
        getActiveHalf().setG2(g2);
        return this;
    }

    public TieLineAdderImpl setB1(double b1) {
        getActiveHalf().setB1(b1);
        return this;
    }

    public TieLineAdderImpl setB2(double b2) {
        getActiveHalf().setB2(b2);
        return this;
    }

    public TieLineAdderImpl setXnodeP(double xnodeP) {
        getActiveHalf().setXnodeP(xnodeP);
        return this;
    }

    public TieLineAdderImpl setXnodeQ(double xnodeQ) {
        getActiveHalf().setXnodeQ(xnodeQ);
        return this;
    }

    private void checkHalf(HalfLine half, int num) {
        if (half.id == null) {
            throw new ValidationException(this, "id is not set for half line " + num);
        } else if (Double.isNaN(half.r)) {
            throw new ValidationException(this, "r is not set for half line " + num);
        } else if (Double.isNaN(half.x)) {
            throw new ValidationException(this, "x is not set for half line " + num);
        } else if (Double.isNaN(half.g1)) {
            throw new ValidationException(this, "g1 is not set for half line " + num);
        } else if (Double.isNaN(half.b1)) {
            throw new ValidationException(this, "b1 is not set for half line " + num);
        } else if (Double.isNaN(half.g2)) {
            throw new ValidationException(this, "g2 is not set for half line " + num);
        } else if (Double.isNaN(half.b2)) {
            throw new ValidationException(this, "b2 is not set for half line " + num);
        } else if (Double.isNaN(half.xnodeP)) {
            throw new ValidationException(this, "xnodeP is not set for half line " + num);
        } else if (Double.isNaN(half.xnodeQ)) {
            throw new ValidationException(this, "xnodeQ is not set for half line " + num);
        }
    }

    @Override
    public TieLine add() {
        String id = checkAndGetUniqueId();
        checkVoltageLevel1();
        checkVoltageLevel2();
        checkNodeBus1();
        checkNodeBus2();

        validate();

        double r = half1.getR() + half2.getR();
        double x = half1.getX() + half2.getX();
        double b1 = half1.getB1() + half1.getB2();
        double b2 = half2.getB1() + half2.getB2();
        double g1 = half1.getG1() + half1.getG2();
        double g2 = half2.getG1() + half2.getG2();
        double rdp = r == 0 ? 0.5 : half1.getR() / r;
        double xdp = x == 0 ? 0.5 : half1.getX() / x;
        Resource<LineAttributes> resource = Resource.lineBuilder(index.getNetwork().getUuid(), index.getResourceUpdater())
                .id(id)
                .attributes(LineAttributes.builder()
                        .name(getName())
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
                                        .xnodeP1(half1.getXnodeP())
                                        .xnodeP2(half2.getXnodeP())
                                        .xnodeQ1(half1.getXnodeQ())
                                        .xnodeQ2(half2.getXnodeQ())
                                        .line1Name(half1.getId())
                                        .line2Name(half2.getId())
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
        checkHalf(half1, 1);
        checkHalf(half2, 2);
    }

    @Override
    protected String getTypeDescription() {
        return "AC tie Line";
    }
}

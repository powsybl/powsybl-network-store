/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.LineAttributes;
import com.powsybl.network.store.model.Resource;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

public class TieLineImpl extends LineImpl implements TieLine {

    private final HalfLineImpl half1;
    private final HalfLineImpl half2;

    public TieLineImpl(NetworkObjectIndex index, Resource<LineAttributes> resource) {
        super(index, resource);
        half1 = new HalfLineImpl(this);
        half2 = new HalfLineImpl(this);
    }

    static class HalfLineImpl implements HalfLine {

        TieLineImpl parent;

        public HalfLineImpl(TieLineImpl parent) {
            this.parent = parent;
        }

        @Override
        public String getId() {
            return parent.getId();
        }

        @Override
        public String getName() {
            return parent.getName() == null ? parent.getId() : parent.getName();
        }

        @Override
        public double getXnodeP() {
            if (getHalfLineAttribute().equals("half1")) {
                return parent.resource.getAttributes().getMergedXnode().getXnodeP1();
            }
            return parent.resource.getAttributes().getMergedXnode().getXnodeP2();
        }

        @Override
        public HalfLineImpl setXnodeP(double xnodeP) {
            if (getHalfLineAttribute().equals("half1")) {
                parent.resource.getAttributes().getMergedXnode().setXnodeP1(xnodeP);
                return this;
            }
            parent.resource.getAttributes().getMergedXnode().setXnodeP2(xnodeP);
            return this;
        }

        @Override
        public double getXnodeQ() {
            if (getHalfLineAttribute().equals("half1")) {
                return parent.resource.getAttributes().getMergedXnode().getXnodeQ1();
            }
            return parent.resource.getAttributes().getMergedXnode().getXnodeQ2();
        }

        @Override
        public HalfLineImpl setXnodeQ(double xnodeQ) {
            if (getHalfLineAttribute().equals("half1")) {
                parent.resource.getAttributes().getMergedXnode().setXnodeQ1(xnodeQ);
                return this;
            }
            parent.resource.getAttributes().getMergedXnode().setXnodeQ2(xnodeQ);
            return this;
        }

        @Override
        public double getR() {
            return parent.getR();
        }

        @Override
        public HalfLineImpl setR(double r) {
            parent.setR(r);
            return this;
        }

        @Override
        public double getX() {
            return parent.getX();
        }

        @Override
        public HalfLineImpl setX(double x) {
            parent.setX(x);
            return this;
        }

        @Override
        public double getG1() {
            return parent.getG1();
        }

        @Override
        public HalfLineImpl setG1(double g1) {
            parent.setG1(g1);
            return this;
        }

        @Override
        public double getG2() {
            return parent.getG2();
        }

        @Override
        public HalfLineImpl setG2(double g2) {
            parent.setG2(g2);
            return this;
        }

        @Override
        public double getB1() {
            return parent.getB1();
        }

        @Override
        public HalfLineImpl setB1(double b1) {
            parent.setB1(b1);
            return this;
        }

        @Override
        public double getB2() {
            return parent.getB2();
        }

        @Override
        public HalfLineImpl setB2(double b2) {
            parent.setB2(b2);
            return this;
        }

        private String getHalfLineAttribute() {
            return this == parent.half1 ? "half1" : "half2";
        }
    }

    @Override
    public String getUcteXnodeCode() {
        return resource.getAttributes().getMergedXnode().getCode();
    }

    @Override
    public HalfLine getHalf1() {
        return half1;
    }

    @Override
    public HalfLine getHalf2() {
        return half2;
    }

    @Override
    public boolean isTieLine() {
        return true;
    }
}

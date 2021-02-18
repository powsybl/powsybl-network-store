/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.LineAttributes;
import com.powsybl.network.store.model.Resource;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

public class TieLineImpl extends LineImpl implements TieLine {

    private final HalfLineImpl half1 = new HalfLineImpl(true);

    private final HalfLineImpl half2 = new HalfLineImpl(false);

    public TieLineImpl(NetworkObjectIndex index, Resource<LineAttributes> resource) {
        super(index, resource);
        if (resource.getAttributes().getMergedXnode() == null) {
            throw new PowsyblException("A tie line must have MergedXnode extension");
        }
    }

    class HalfLineImpl implements HalfLine {

        private final boolean one;

        public HalfLineImpl(boolean one) {
            this.one = one;
        }

        @Override
        public String getId() {
            return one ? resource.getAttributes().getMergedXnode().getLine1Name()
                       : resource.getAttributes().getMergedXnode().getLine2Name();
        }

        @Override
        public String getName() {
            return getId();
        }

        @Override
        public double getXnodeP() {
            return one ? resource.getAttributes().getMergedXnode().getXnodeP1()
                       : resource.getAttributes().getMergedXnode().getXnodeP2();
        }

        @Override
        public HalfLineImpl setXnodeP(double xnodeP) {
            if (one) {
                resource.getAttributes().getMergedXnode().setXnodeP1(xnodeP);
            } else {
                resource.getAttributes().getMergedXnode().setXnodeP2(xnodeP);
            }
            return this;
        }

        @Override
        public double getXnodeQ() {
            return one ? resource.getAttributes().getMergedXnode().getXnodeQ1()
                       : resource.getAttributes().getMergedXnode().getXnodeQ2();
        }

        @Override
        public HalfLineImpl setXnodeQ(double xnodeQ) {
            throw new UnsupportedOperationException("TODO");
        }

        @Override
        public double getR() {
            return TieLineImpl.this.getR() * (one ? resource.getAttributes().getMergedXnode().getRdp() : 1 - resource.getAttributes().getMergedXnode().getRdp());
        }

        @Override
        public HalfLineImpl setR(double r) {
            throw new UnsupportedOperationException("TODO");
        }

        @Override
        public double getX() {
            return TieLineImpl.this.getX() * (one ? resource.getAttributes().getMergedXnode().getXdp() : 1 - resource.getAttributes().getMergedXnode().getXdp());
        }

        @Override
        public HalfLineImpl setX(double x) {
            throw new UnsupportedOperationException("TODO");
        }

        @Override
        public double getG1() {
            return one ? TieLineImpl.this.getG1() / 2 : TieLineImpl.this.getG2() / 2;
        }

        @Override
        public HalfLineImpl setG1(double g1) {
            throw new UnsupportedOperationException("TODO");
        }

        @Override
        public double getG2() {
            return one ? TieLineImpl.this.getG1() / 2 : TieLineImpl.this.getG2() / 2;
        }

        @Override
        public HalfLineImpl setG2(double g2) {
            throw new UnsupportedOperationException("TODO");
        }

        @Override
        public double getB1() {
            return one ? TieLineImpl.this.getB1() / 2 : TieLineImpl.this.getB2() / 2;
        }

        @Override
        public HalfLineImpl setB1(double b1) {
            throw new UnsupportedOperationException("TODO");
        }

        @Override
        public double getB2() {
            return one ? TieLineImpl.this.getB1() / 2 : TieLineImpl.this.getB2() / 2;
        }

        @Override
        public HalfLineImpl setB2(double b2) {
            throw new UnsupportedOperationException("TODO");
        }
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits1() {
        //TODO
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits1() {
        //TODO
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits2() {
        //TODO
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits2() {
        //TODO
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
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
    public HalfLine getHalf(Side side) {
        switch (side) {
            case ONE:
                return half1;
            case TWO:
                return half2;
            default:
                throw new AssertionError("Unknown branch side " + side);
        }
    }

    @Override
    public boolean isTieLine() {
        return true;
    }
}

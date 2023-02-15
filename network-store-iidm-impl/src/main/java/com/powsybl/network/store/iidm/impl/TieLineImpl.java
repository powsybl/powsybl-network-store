/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.SV;
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

        class BoundaryImpl implements Boundary {

            private Terminal getTerminal() {
                return one ? getTerminal1() : getTerminal2();
            }

            @Override
            public double getV() {
                Terminal t = getTerminal();
                Bus b = t.getBusView().getBus();
                return new SV(t.getP(), t.getQ(), BaseBus.getV(b), BaseBus.getAngle(b), getSide()).otherSideU(HalfLineImpl.this);
            }

            @Override
            public double getAngle() {
                Terminal t = getTerminal();
                Bus b = t.getBusView().getBus();
                return new SV(t.getP(), t.getQ(), BaseBus.getV(b), BaseBus.getAngle(b), getSide()).otherSideA(HalfLineImpl.this);
            }

            @Override
            public double getP() {
                Terminal t = getTerminal();
                Bus b = t.getBusView().getBus();
                return new SV(t.getP(), t.getQ(), BaseBus.getV(b), BaseBus.getAngle(b), getSide()).otherSideP(HalfLineImpl.this);
            }

            @Override
            public double getQ() {
                Terminal t = getTerminal();
                Bus b = t.getBusView().getBus();
                return new SV(t.getP(), t.getQ(), BaseBus.getV(b), BaseBus.getAngle(b), getSide()).otherSideQ(HalfLineImpl.this);
            }

            @Override
            public Branch.Side getSide() {
                return one ? Side.ONE : Side.TWO;
            }

            @Override
            public Connectable getConnectable() {
                return TieLineImpl.this;
            }
        }

        private final BoundaryImpl boundary = new BoundaryImpl();

        public HalfLineImpl(boolean one) {
            this.one = one;
        }

        @Override
        public String getId() {
            var resource = checkResource();
            return one ? resource.getAttributes().getMergedXnode().getLine1Name()
                       : resource.getAttributes().getMergedXnode().getLine2Name();
        }

        @Override
        public String getName() {
            return getId();
        }

        @Override
        public double getR() {
            var resource = checkResource();
            return TieLineImpl.this.getR() * (one ? resource.getAttributes().getMergedXnode().getRdp() : 1 - resource.getAttributes().getMergedXnode().getRdp());
        }

        @Override
        public HalfLineImpl setR(double r) {
            throw new UnsupportedOperationException("TODO");
        }

        @Override
        public double getX() {
            var resource = checkResource();
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

        @Override
        public Boundary getBoundary() {
            return boundary;
        }
    }

    @Override
    public String getUcteXnodeCode() {
        return checkResource().getAttributes().getMergedXnode().getCode();
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

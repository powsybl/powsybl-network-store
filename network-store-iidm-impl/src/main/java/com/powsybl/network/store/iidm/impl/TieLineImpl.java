/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.LineAttributes;
import com.powsybl.network.store.model.Resource;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 * @author Slimane Amar <slimane.amar at rte-france.com>
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

    private final class PositionRatio {
        private double sum;
        private float ratio;

        private PositionRatio(double sum, float ratio) {
            this.sum = sum;
            this.ratio = ratio;
        }

        private PositionRatio setSideValue(double sideValue, boolean sideOne) {
            double newSide1Value = sideOne ? sideValue : sum * ratio;
            double newSide2Value = !sideOne ? sideValue : sum * (1 - ratio);
            sum = newSide1Value + newSide2Value;
            ratio = (float) (sum == 0 ? 0.5 : newSide1Value / sum);
            return this;
        }
    }

    class HalfLineImpl implements HalfLine, Validable {

        private final boolean one;

        public HalfLineImpl(boolean one) {
            this.one = one;
        }

        @Override
        public String getId() {
            return one ? resource.getAttributes().getMergedXnode().getLine1Id()
                    : resource.getAttributes().getMergedXnode().getLine2Id();
        }

        @Override
        public String getName() {
            String name = one ? resource.getAttributes().getMergedXnode().getLine1Name()
                    : resource.getAttributes().getMergedXnode().getLine2Name();
            return name != null ? name : getId();
        }

        @Override
        public boolean isFictitious() {
            return one ? resource.getAttributes().getMergedXnode().isLine1Fictitious()
                    : resource.getAttributes().getMergedXnode().isLine2Fictitious();
        }

        @Override
        public double getXnodeP() {
            return one ? resource.getAttributes().getMergedXnode().getXnodeP1()
                    : resource.getAttributes().getMergedXnode().getXnodeP2();
        }

        @Override
        public HalfLineImpl setXnodeP(double xnodeP) {
            checkValidationXnodeP(xnodeP);
            double oldValue = getXnodeP();
            if (one) {
                resource.getAttributes().getMergedXnode().setXnodeP1(xnodeP);
            } else {
                resource.getAttributes().getMergedXnode().setXnodeP2(xnodeP);
            }
            index.notifyUpdate(TieLineImpl.this, getHalfLineAttribute() + ".xnodeP", oldValue, xnodeP);
            return this;
        }

        @Override
        public double getXnodeQ() {
            return one ? resource.getAttributes().getMergedXnode().getXnodeQ1()
                    : resource.getAttributes().getMergedXnode().getXnodeQ2();
        }

        @Override
        public HalfLineImpl setXnodeQ(double xnodeQ) {
            checkValidationXnodeQ(xnodeQ);
            double oldValue = getXnodeQ();
            if (one) {
                resource.getAttributes().getMergedXnode().setXnodeQ1(xnodeQ);
            } else {
                resource.getAttributes().getMergedXnode().setXnodeQ2(xnodeQ);
            }
            index.notifyUpdate(TieLineImpl.this, getHalfLineAttribute() + ".xnodeQ", oldValue, xnodeQ);
            return this;
        }

        @Override
        public double getR() {
            return TieLineImpl.this.getR() * (one ? resource.getAttributes().getMergedXnode().getRdp() : 1 - resource.getAttributes().getMergedXnode().getRdp());
        }

        @Override
        public HalfLineImpl setR(double r) {
            ValidationUtil.checkR(this, r);
            double oldValue = getR();
            PositionRatio positionRatio = new PositionRatio(resource.getAttributes().getR(), resource.getAttributes().getMergedXnode().getRdp()).setSideValue(r, one);
            resource.getAttributes().setR(positionRatio.sum);
            resource.getAttributes().getMergedXnode().setRdp(positionRatio.ratio);
            index.notifyUpdate(TieLineImpl.this, getHalfLineAttribute() + ".r", oldValue, r);
            return this;
        }

        @Override
        public double getX() {
            return TieLineImpl.this.getX() * (one ? resource.getAttributes().getMergedXnode().getXdp() : 1 - resource.getAttributes().getMergedXnode().getXdp());
        }

        @Override
        public HalfLineImpl setX(double x) {
            ValidationUtil.checkX(this, x);
            double oldValue = getX();
            PositionRatio positionRatio = new PositionRatio(resource.getAttributes().getX(), resource.getAttributes().getMergedXnode().getXdp()).setSideValue(x, one);
            resource.getAttributes().setX(positionRatio.sum);
            resource.getAttributes().getMergedXnode().setXdp(positionRatio.ratio);
            index.notifyUpdate(TieLineImpl.this, getHalfLineAttribute() + ".x", oldValue, x);
            return this;
        }

        @Override
        public double getG1() {
            return one ?
                    TieLineImpl.this.getG1() * resource.getAttributes().getMergedXnode().getG1dp() :
                    TieLineImpl.this.getG2() * resource.getAttributes().getMergedXnode().getG2dp();
        }

        private void setSideG(double g, boolean halfLineSideOne) {
            if (one) {
                PositionRatio positionRatio = new PositionRatio(resource.getAttributes().getG1(), resource.getAttributes().getMergedXnode().getG1dp()).setSideValue(g, halfLineSideOne);
                resource.getAttributes().setG1(positionRatio.sum);
                resource.getAttributes().getMergedXnode().setG1dp(positionRatio.ratio);
            } else {
                PositionRatio positionRatio = new PositionRatio(resource.getAttributes().getG2(), resource.getAttributes().getMergedXnode().getG2dp()).setSideValue(g, halfLineSideOne);
                resource.getAttributes().setG2(positionRatio.sum);
                resource.getAttributes().getMergedXnode().setG2dp(positionRatio.ratio);
            }
        }

        @Override
        public HalfLineImpl setG1(double g1) {
            ValidationUtil.checkG1(this, g1);
            double oldValue = getG1();
            setSideG(g1, true);
            index.notifyUpdate(TieLineImpl.this, getHalfLineAttribute() + ".g1", oldValue, g1);
            return this;
        }

        @Override
        public double getG2() {
            return one ?
                    TieLineImpl.this.getG1() * (1 - resource.getAttributes().getMergedXnode().getG1dp()) :
                    TieLineImpl.this.getG2() * (1 - resource.getAttributes().getMergedXnode().getG2dp());
        }

        @Override
        public HalfLineImpl setG2(double g2) {
            ValidationUtil.checkG2(this, g2);
            double oldValue = getG2();
            setSideG(g2, false);
            index.notifyUpdate(TieLineImpl.this, getHalfLineAttribute() + ".g2", oldValue, g2);
            return this;
        }

        @Override
        public double getB1() {
            return one ?
                    TieLineImpl.this.getB1() * resource.getAttributes().getMergedXnode().getB1dp() :
                    TieLineImpl.this.getB2() * resource.getAttributes().getMergedXnode().getB2dp();
        }

        private void setSideB(double b, boolean halfLineSideOne) {
            if (one) {
                PositionRatio positionRatio = new PositionRatio(resource.getAttributes().getB1(), resource.getAttributes().getMergedXnode().getB1dp()).setSideValue(b, halfLineSideOne);
                resource.getAttributes().setB1(positionRatio.sum);
                resource.getAttributes().getMergedXnode().setB1dp(positionRatio.ratio);
            } else {
                PositionRatio positionRatio = new PositionRatio(resource.getAttributes().getB2(), resource.getAttributes().getMergedXnode().getB2dp()).setSideValue(b, halfLineSideOne);
                resource.getAttributes().setB2(positionRatio.sum);
                resource.getAttributes().getMergedXnode().setB2dp(positionRatio.ratio);
            }
        }

        @Override
        public HalfLineImpl setB1(double b1) {
            ValidationUtil.checkB1(this, b1);
            double oldValue = getB1();
            setSideB(b1, true);
            index.notifyUpdate(TieLineImpl.this, getHalfLineAttribute() + ".b1", oldValue, b1);
            return this;
        }

        @Override
        public double getB2() {
            return one ?
                    TieLineImpl.this.getB1() * (1 - resource.getAttributes().getMergedXnode().getB1dp()) :
                    TieLineImpl.this.getB2() * (1 - resource.getAttributes().getMergedXnode().getB2dp());
        }

        @Override
        public HalfLineImpl setB2(double b2) {
            ValidationUtil.checkB2(this, b2);
            double oldValue = getB2();
            setSideB(b2, false);
            index.notifyUpdate(TieLineImpl.this, getHalfLineAttribute() + ".b2", oldValue, b2);
            return this;
        }

        private String getHalfLineAttribute() {
            return "half" + (one ? "1" : "2");
        }

        @Override
        public String getMessageHeader() {
            return String.format("Tie line side %s '%s':", one ? "1" : "2", TieLineImpl.this.getId());
        }

        private void checkValidationXnodeP(double xnodeP) {
            if (Double.isNaN(xnodeP)) {
                throw new ValidationException(TieLineImpl.this, "xnodeP is invalid");
            }
        }

        private void checkValidationXnodeQ(double xnodeQ) {
            if (Double.isNaN(xnodeQ)) {
                throw new ValidationException(TieLineImpl.this, "xnodeQ is invalid");
            }
        }
    }

    private ValidationException notSupportedForTieLinesException() {
        return new ValidationException(this, "direct modification of characteristics not supported for tie lines");
    }

    @Override
    public TieLineImpl setR(double r) {
        throw notSupportedForTieLinesException();
    }

    @Override
    public TieLineImpl setX(double x) {
        throw notSupportedForTieLinesException();
    }

    @Override
    public TieLineImpl setG1(double g1) {
        throw notSupportedForTieLinesException();
    }

    @Override
    public TieLineImpl setB1(double b1) {
        throw notSupportedForTieLinesException();
    }

    @Override
    public TieLineImpl setG2(double g2) {
        throw notSupportedForTieLinesException();
    }

    @Override
    public TieLineImpl setB2(double b2) {
        throw notSupportedForTieLinesException();
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

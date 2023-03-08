/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TapChangerStepAttributes;

import java.util.Objects;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class RatioTapChangerStepImpl implements RatioTapChangerStep {

    private final RatioTapChangerImpl ratioTapChanger;

    private final int tapPositionIndex;

    RatioTapChangerStepImpl(RatioTapChangerImpl ratioTapChanger, int tapPositionIndex) {
        this.ratioTapChanger = Objects.requireNonNull(ratioTapChanger);
        this.tapPositionIndex = tapPositionIndex;
    }

    private TapChangerStepAttributes getTapChangerStepAttributes(Resource<?> res) {
        return ratioTapChanger.getAttributes(res).getSteps().get(tapPositionIndex);
    }

    private TapChangerStepAttributes getTapChangerStepAttributes() {
        return ratioTapChanger.getAttributes().getSteps().get(tapPositionIndex);
    }

    @Override
    public double getRho() {
        return getTapChangerStepAttributes().getRho();
    }

    @Override
    public RatioTapChangerStepImpl setRho(double rho) {
        double oldValue = getTapChangerStepAttributes().getRho();
        if (rho != oldValue) {
            ratioTapChanger.getTransformer().updateResource(res -> getTapChangerStepAttributes(res).setRho(rho));
            ratioTapChanger.notifyUpdate("rho", oldValue, rho);
        }
        return this;
    }

    @Override
    public double getR() {
        return getTapChangerStepAttributes(ratioTapChanger.getResource()).getR();
    }

    @Override
    public RatioTapChangerStepImpl setR(double r) {
        double oldValue = getTapChangerStepAttributes().getR();
        if (r != oldValue) {
            ratioTapChanger.getTransformer().updateResource(res -> getTapChangerStepAttributes(res).setR(r));
            ratioTapChanger.notifyUpdate("r", oldValue, r);
        }
        return this;
    }

    @Override
    public double getX() {
        return getTapChangerStepAttributes().getX();
    }

    @Override
    public RatioTapChangerStepImpl setX(double x) {
        double oldValue = getTapChangerStepAttributes().getX();
        if (x != oldValue) {
            ratioTapChanger.getTransformer().updateResource(res -> getTapChangerStepAttributes(res).setX(x));
            ratioTapChanger.notifyUpdate("x", oldValue, x);
        }
        return this;
    }

    @Override
    public double getG() {
        return getTapChangerStepAttributes().getG();
    }

    @Override
    public RatioTapChangerStepImpl setG(double g) {
        double oldValue = getTapChangerStepAttributes().getG();
        if (g != oldValue) {
            ratioTapChanger.getTransformer().updateResource(res -> getTapChangerStepAttributes(res).setG(g));
            ratioTapChanger.notifyUpdate("g", oldValue, g);
        }
        return this;
    }

    @Override
    public double getB() {
        return getTapChangerStepAttributes().getB();
    }

    @Override
    public RatioTapChangerStepImpl setB(double b) {
        double oldValue = getTapChangerStepAttributes().getB();
        if (b != oldValue) {
            ratioTapChanger.getTransformer().updateResource(res -> getTapChangerStepAttributes(res).setB(b));
            ratioTapChanger.notifyUpdate("b", oldValue, b);
        }
        return this;
    }
}

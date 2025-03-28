/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.PhaseTapChangerStep;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TapChangerStepAttributes;

import java.util.Objects;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

public class PhaseTapChangerStepImpl implements PhaseTapChangerStep {

    private final PhaseTapChangerImpl phaseTapChanger;

    private final int tapPositionIndex;

    public PhaseTapChangerStepImpl(PhaseTapChangerImpl phaseTapChanger, int tapPositionIndex) {
        this.phaseTapChanger = Objects.requireNonNull(phaseTapChanger);
        this.tapPositionIndex = tapPositionIndex;
    }

    private AbstractIdentifiableImpl<?, ?> getTransformer() {
        return phaseTapChanger.getTransformer();
    }

    private TapChangerStepAttributes getTapChangerStepAttributes(Resource<?> res) {
        return phaseTapChanger.getAttributes(res).getSteps().get(tapPositionIndex);
    }

    private TapChangerStepAttributes getTapChangerStepAttributes() {
        return phaseTapChanger.getAttributes().getSteps().get(tapPositionIndex);
    }

    @Override
    public double getRho() {
        return getTapChangerStepAttributes().getRho();
    }

    @Override
    public PhaseTapChangerStepImpl setRho(double rho) {
        double oldValue = getTapChangerStepAttributes().getRho();
        if (rho != oldValue) {
            getTransformer().updateResource(res -> getTapChangerStepAttributes(res).setRho(rho),
                "rho", oldValue, rho);
        }
        return this;
    }

    @Override
    public double getR() {
        return getTapChangerStepAttributes().getR();
    }

    @Override
    public PhaseTapChangerStepImpl setR(double r) {
        double oldValue = getTapChangerStepAttributes().getR();
        if (r != oldValue) {
            getTransformer().updateResource(res -> getTapChangerStepAttributes(res).setR(r),
                "r", oldValue, r);
        }
        return this;
    }

    @Override
    public double getX() {
        return getTapChangerStepAttributes().getX();
    }

    @Override
    public PhaseTapChangerStepImpl setX(double x) {
        double oldValue = getTapChangerStepAttributes().getX();
        if (x != oldValue) {
            getTransformer().updateResource(res -> getTapChangerStepAttributes(res).setX(x),
                "x", oldValue, x);
        }
        return this;
    }

    @Override
    public double getB() {
        return getTapChangerStepAttributes().getB();
    }

    @Override
    public PhaseTapChangerStepImpl setB(double b) {
        double oldValue = getTapChangerStepAttributes().getB();
        if (b != oldValue) {
            getTransformer().updateResource(res -> getTapChangerStepAttributes(res).setB(b),
                "b", oldValue, b);
        }
        return this;
    }

    @Override
    public double getG() {
        return getTapChangerStepAttributes().getG();
    }

    @Override
    public PhaseTapChangerStepImpl setG(double g) {
        double oldValue = getTapChangerStepAttributes().getG();
        if (g != oldValue) {
            getTransformer().updateResource(res -> getTapChangerStepAttributes(res).setG(g),
                "g", oldValue, g);
        }
        return this;
    }

    @Override
    public double getAlpha() {
        return getTapChangerStepAttributes().getAlpha();
    }

    @Override
    public PhaseTapChangerStep setAlpha(double alpha) {
        double oldValue = getTapChangerStepAttributes().getAlpha();
        if (alpha != oldValue) {
            getTransformer().updateResource(res -> getTapChangerStepAttributes(res).setAlpha(alpha),
                "alpha", oldValue, alpha);
        }
        return this;
    }
}

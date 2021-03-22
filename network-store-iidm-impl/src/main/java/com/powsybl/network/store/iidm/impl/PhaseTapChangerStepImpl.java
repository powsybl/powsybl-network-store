/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.PhaseTapChangerStep;
import com.powsybl.network.store.model.PhaseTapChangerStepAttributes;

import java.util.Objects;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

public class PhaseTapChangerStepImpl implements PhaseTapChangerStep {

    private final PhaseTapChangerImpl phaseTapChanger;

    private final PhaseTapChangerStepAttributes attributes;

    public PhaseTapChangerStepImpl(PhaseTapChangerImpl phaseTapChanger, PhaseTapChangerStepAttributes attributes) {
        this.phaseTapChanger = Objects.requireNonNull(phaseTapChanger);
        this.attributes = attributes;
    }

    @Override
    public double getRho() {
        return attributes.getRho();
    }

    @Override
    public PhaseTapChangerStepImpl setRho(double rho) {
        double oldValue = attributes.getRho();
        attributes.setRho(rho);
        phaseTapChanger.updateResource();
        phaseTapChanger.notifyUpdate("rho", oldValue, rho);
        return this;
    }

    @Override
    public double getR() {
        return attributes.getR();
    }

    @Override
    public PhaseTapChangerStepImpl setR(double r) {
        double oldValue = attributes.getR();
        attributes.setR(r);
        phaseTapChanger.updateResource();
        phaseTapChanger.notifyUpdate("r", oldValue, r);
        return this;
    }

    @Override
    public double getX() {
        return attributes.getX();
    }

    @Override
    public PhaseTapChangerStepImpl setX(double x) {
        double oldValue = attributes.getX();
        attributes.setX(x);
        phaseTapChanger.updateResource();
        phaseTapChanger.notifyUpdate("x", oldValue, x);
        return this;
    }

    @Override
    public double getB() {
        return attributes.getB();
    }

    @Override
    public PhaseTapChangerStepImpl setB(double b) {
        double oldValue = attributes.getB();
        attributes.setB(b);
        phaseTapChanger.updateResource();
        phaseTapChanger.notifyUpdate("b", oldValue, b);
        return this;
    }

    @Override
    public double getG() {
        return attributes.getG();
    }

    @Override
    public PhaseTapChangerStepImpl setG(double g) {
        double oldValue = attributes.getG();
        attributes.setG(g);
        phaseTapChanger.updateResource();
        phaseTapChanger.notifyUpdate("g", oldValue, g);
        return this;
    }

    @Override
    public double getAlpha() {
        return attributes.getAlpha();
    }

    @Override
    public PhaseTapChangerStep setAlpha(double alpha) {
        double oldValue = attributes.getAlpha();
        attributes.setAlpha(alpha);
        phaseTapChanger.updateResource();
        phaseTapChanger.notifyUpdate("alpha", oldValue, alpha);
        return this;
    }
}

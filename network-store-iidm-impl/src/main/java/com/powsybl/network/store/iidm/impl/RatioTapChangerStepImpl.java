/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.network.store.model.RatioTapChangerStepAttributes;

import java.util.Objects;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class RatioTapChangerStepImpl implements RatioTapChangerStep {

    private final RatioTapChangerImpl ratioTapChanger;

    private final RatioTapChangerStepAttributes attributes;

    RatioTapChangerStepImpl(RatioTapChangerImpl ratioTapChanger, RatioTapChangerStepAttributes attributes) {
        this.ratioTapChanger = Objects.requireNonNull(ratioTapChanger);
        this.attributes = attributes;
    }

    @Override
    public double getRho() {
        return attributes.getRho();
    }

    @Override
    public RatioTapChangerStepImpl setRho(double rho) {
        double oldValue = attributes.getRho();
        attributes.setRho(rho);
        ratioTapChanger.updateResource();
        ratioTapChanger.notifyUpdate("rho", oldValue, rho);
        return this;
    }

    @Override
    public double getR() {
        return attributes.getR();
    }

    @Override
    public RatioTapChangerStepImpl setR(double r) {
        double oldValue = attributes.getR();
        attributes.setR(r);
        ratioTapChanger.updateResource();
        ratioTapChanger.notifyUpdate("r", oldValue, r);
        return this;
    }

    @Override
    public double getX() {
        return attributes.getX();
    }

    @Override
    public RatioTapChangerStepImpl setX(double x) {
        double oldValue = attributes.getX();
        attributes.setX(x);
        ratioTapChanger.updateResource();
        ratioTapChanger.notifyUpdate("x", oldValue, x);
        return this;
    }

    @Override
    public double getG() {
        return attributes.getG();
    }

    @Override
    public RatioTapChangerStepImpl setG(double g) {
        double oldValue = attributes.getG();
        attributes.setG(g);
        ratioTapChanger.updateResource();
        ratioTapChanger.notifyUpdate("g", oldValue, g);
        return this;
    }

    @Override
    public double getB() {
        return attributes.getB();
    }

    @Override
    public RatioTapChangerStepImpl setB(double b) {
        double oldValue = attributes.getB();
        attributes.setB(b);
        ratioTapChanger.updateResource();
        ratioTapChanger.notifyUpdate("b", oldValue, b);
        return this;
    }
}

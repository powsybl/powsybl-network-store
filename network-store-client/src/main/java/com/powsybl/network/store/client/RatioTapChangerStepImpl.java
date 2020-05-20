/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.network.store.model.RatioTapChangerStepAttributes;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class RatioTapChangerStepImpl implements RatioTapChangerStep {

    RatioTapChangerStepAttributes attributes;

    RatioTapChangerStepImpl(RatioTapChangerStepAttributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public double getRho() {
        return attributes.getRho();
    }

    @Override
    public RatioTapChangerStepImpl setRho(double rho) {
        attributes.setRho(rho);
        return this;
    }

    @Override
    public double getR() {
        return attributes.getR();
    }

    @Override
    public RatioTapChangerStepImpl setR(double r) {
        attributes.setR(r);
        return this;
    }

    @Override
    public double getX() {
        return attributes.getX();
    }

    @Override
    public RatioTapChangerStepImpl setX(double x) {
        attributes.setX(x);
        return this;
    }

    @Override
    public double getG() {
        return attributes.getG();
    }

    @Override
    public RatioTapChangerStepImpl setG(double g) {
        attributes.setG(g);
        return this;
    }

    @Override
    public double getB() {
        return attributes.getB();
    }

    @Override
    public RatioTapChangerStepImpl setB(double b) {
        attributes.setB(b);
        return this;
    }
}

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

    RatioTapChangerStepAttributes ratioTapChangerStepAttributes;

    RatioTapChangerStepImpl(RatioTapChangerStepAttributes ratioTapChangerStepAttributes) {
        this.ratioTapChangerStepAttributes = ratioTapChangerStepAttributes;
    }

    public int getPosition() {
        return ratioTapChangerStepAttributes.getPosition();
    }

    public double getRho() {
        return ratioTapChangerStepAttributes.getRho();
    }

    public RatioTapChangerStepImpl setRho(double rho) {
        ratioTapChangerStepAttributes.setRho(rho);
        return this;
    }

    public double getR() {
        return ratioTapChangerStepAttributes.getR();
    }

    public RatioTapChangerStepImpl setR(double r) {
        ratioTapChangerStepAttributes.setR(r);
        return this;
    }

    public double getX() {
        return ratioTapChangerStepAttributes.getX();
    }

    public RatioTapChangerStepImpl setX(double x) {
        ratioTapChangerStepAttributes.setX(x);
        return this;
    }

    public double getG() {
        return ratioTapChangerStepAttributes.getG();
    }

    public RatioTapChangerStepImpl setG(double g) {
        ratioTapChangerStepAttributes.setG(g);
        return this;
    }

    public double getB() {
        return ratioTapChangerStepAttributes.getB();
    }

    public RatioTapChangerStepImpl setB(double b) {
        ratioTapChangerStepAttributes.setB(b);
        return this;
    }
}

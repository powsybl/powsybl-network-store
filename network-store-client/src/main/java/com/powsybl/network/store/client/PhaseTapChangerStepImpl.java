/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.PhaseTapChangerStep;
import com.powsybl.network.store.model.PhaseTapChangerStepAttributes;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

public class PhaseTapChangerStepImpl implements PhaseTapChangerStep {

    PhaseTapChangerStepAttributes phaseTapChangerStepAttributes;

    public PhaseTapChangerStepImpl(PhaseTapChangerStepAttributes phaseTapChangerStepAttributes) {
        this.phaseTapChangerStepAttributes = phaseTapChangerStepAttributes;
    }

    public double getRho() {
        return phaseTapChangerStepAttributes.getRho();
    }

    public PhaseTapChangerStepImpl setRho(double rho) {
        phaseTapChangerStepAttributes.setRho(rho);
        return this;
    }

    public double getR() {
        return phaseTapChangerStepAttributes.getR();
    }

    public PhaseTapChangerStepImpl setR(double r) {
        phaseTapChangerStepAttributes.setR(r);
        return this;
    }

    public double getX() {
        return phaseTapChangerStepAttributes.getX();
    }

    public PhaseTapChangerStepImpl setX(double x) {
        phaseTapChangerStepAttributes.setX(x);
        return this;
    }

    public double getB() {
        return phaseTapChangerStepAttributes.getB();
    }

    public PhaseTapChangerStepImpl setB(double b) {
        phaseTapChangerStepAttributes.setB(b);
        return this;
    }

    public double getG() {
        return phaseTapChangerStepAttributes.getG();
    }

    public PhaseTapChangerStepImpl setG(double g) {
        phaseTapChangerStepAttributes.setG(g);
        return this;
    }

    @Override
    public double getAlpha() {
        return phaseTapChangerStepAttributes.getAlpha();
    }

    @Override
    public PhaseTapChangerStep setAlpha(double alpha) {
        phaseTapChangerStepAttributes.setAlpha(alpha);
        return this;
    }
}

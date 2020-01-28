package com.powsybl.network.store.client;

import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.network.store.model.RatioTapChangerStepAttributes;

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

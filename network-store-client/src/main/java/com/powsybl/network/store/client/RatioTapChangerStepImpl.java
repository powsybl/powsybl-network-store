package com.powsybl.network.store.client;

import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.network.store.model.RatioTapChangerStepAttributes;

public class RatioTapChangerStepImpl implements RatioTapChangerStep {

    RatioTapChangerStepAttributes attributes;

    RatioTapChangerStepImpl(RatioTapChangerStepAttributes attributes) {
        this.attributes = attributes;
    }

    public int getPosition() {
        return attributes.getPosition();
    }

    public double getRho() {
        return attributes.getRho();
    }

    public RatioTapChangerStepImpl setRho(double rho) {
        attributes.setRho(rho);
        return this;
    }

    public double getR() {
        return attributes.getR();
    }

    public RatioTapChangerStepImpl setR(double r) {
        attributes.setR(r);
        return this;
    }

    public double getX() {
        return attributes.getX();
    }

    public RatioTapChangerStepImpl setX(double x) {
        attributes.setX(x);
        return this;
    }

    public double getG() {
        return attributes.getG();
    }

    public RatioTapChangerStepImpl setG(double g) {
        attributes.setG(g);
        return this;
    }

    public double getB() {
        return attributes.getB();
    }

    public RatioTapChangerStepImpl setB(double b) {
        attributes.setB(b);
        return this;
    }
}

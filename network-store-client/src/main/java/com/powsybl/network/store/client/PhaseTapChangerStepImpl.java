package com.powsybl.network.store.client;

import com.powsybl.iidm.network.PhaseTapChangerStep;

public class PhaseTapChangerStepImpl implements PhaseTapChangerStep {

    private final int position;

    private double rho;

    private double r;

    private double x;

    private double g;

    private double b;

    private double alpha;

    public PhaseTapChangerStepImpl(int position, double alpha, double rho, double r, double x, double g, double b) {
        this.position = position;
        this.rho = rho;
        this.r = r;
        this.x = x;
        this.g = g;
        this.b = b;
        this.alpha = alpha;
    }

    public double getRho() {
        return rho;
    }

    public PhaseTapChangerStepImpl setRho(double rho) {
        this.rho = rho;
        return this;
    }

    public double getR() {
        return r;
    }

    public PhaseTapChangerStepImpl setR(double r) {
        this.r = r;
        return this;
    }

    public double getX() {
        return x;
    }

    public PhaseTapChangerStepImpl setX(double x) {
        this.x = x;
        return this;
    }

    public double getB() {
        return b;
    }

    public PhaseTapChangerStepImpl setB(double b) {
        this.b = b;
        return this;
    }

    public double getG() {
        return g;
    }

    public PhaseTapChangerStepImpl setG(double g) {
        this.g = g;
        return this;
    }

    @Override
    public double getAlpha() {
        return alpha;
    }

    @Override
    public PhaseTapChangerStep setAlpha(double alpha) {
        this.alpha = alpha;
        return this;
    }
}

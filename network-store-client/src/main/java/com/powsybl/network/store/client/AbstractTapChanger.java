package com.powsybl.network.store.client;

public abstract class AbstractTapChanger {

    protected int lowTapPosition = 0;

    protected Integer tapPosition;

    protected boolean regulating = false;

    protected double targetDeadband = Double.NaN;

    public AbstractTapChanger() {

    }

    public AbstractTapChanger(int lowTapPosition, Integer tapPosition, boolean regulating, double targetDeadband) {
        this.lowTapPosition = lowTapPosition;
        this.tapPosition = tapPosition;
        this.regulating = regulating;
        this.targetDeadband = targetDeadband;
    }
}

package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.DanglingLineData;
import com.powsybl.iidm.network.util.SV;

import java.util.Objects;

class DanglingLineBoundaryImpl implements Boundary {

    private final DanglingLine parent;

    DanglingLineBoundaryImpl(DanglingLine parent) {
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    public double getV() {
        if (!parent.isPaired() && valid(parent.getP0(), parent.getQ0())) {
            DanglingLineData danglingLineData = new DanglingLineData(parent, true);
            return danglingLineData.getBoundaryBusU();
        }
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), BaseBus.getV(b), BaseBus.getAngle(b), TwoSides.ONE).otherSideU(parent, true);
    }

    @Override
    public double getAngle() {
        if (!parent.isPaired() && valid(parent.getP0(), parent.getQ0())) {
            DanglingLineData danglingLineData = new DanglingLineData(parent, true);
            return Math.toDegrees(danglingLineData.getBoundaryBusTheta());
        }
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), BaseBus.getV(b), BaseBus.getAngle(b), TwoSides.ONE).otherSideA(parent, true);
    }

    @Override
    public double getP() {
        if (!parent.isPaired() && valid(parent.getP0(), parent.getQ0())) {
            return -parent.getP0();
        }
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), BaseBus.getV(b), BaseBus.getAngle(b), TwoSides.ONE).otherSideP(parent, true);
    }

    @Override
    public double getQ() {
        if (!parent.isPaired() && valid(parent.getP0(), parent.getQ0())) {
            return -parent.getQ0();
        }
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), BaseBus.getV(b), BaseBus.getAngle(b), TwoSides.ONE).otherSideQ(parent, true);
    }

    @Override
    public DanglingLine getDanglingLine() {
        return parent;
    }

    private static boolean valid(double p0, double q0) {
        return !Double.isNaN(p0) && !Double.isNaN(q0);
    }
}

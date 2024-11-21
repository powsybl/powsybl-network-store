/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.DanglingLineData;
import com.powsybl.iidm.network.util.SV;

import java.util.Objects;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class DanglingLineBoundaryImpl implements Boundary {

    private final DanglingLine parent;

    DanglingLineBoundaryImpl(DanglingLine parent) {
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    public double getV() {
        if (!parent.isPaired() && valid(parent.getP0(), parent.getQ0())) {
            DanglingLineData danglingLineData = new DanglingLineData(parent);
            return danglingLineData.getBoundaryBusU();
        }
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), BaseBus.getV(b), BaseBus.getAngle(b), TwoSides.ONE).otherSideU(parent, false);
    }

    @Override
    public double getAngle() {
        if (!parent.isPaired() && valid(parent.getP0(), parent.getQ0())) {
            DanglingLineData danglingLineData = new DanglingLineData(parent);
            return Math.toDegrees(danglingLineData.getBoundaryBusTheta());
        }
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), BaseBus.getV(b), BaseBus.getAngle(b), TwoSides.ONE).otherSideA(parent, false);
    }

    @Override
    public double getP() {
        if (!parent.isPaired() && valid(parent.getP0(), parent.getQ0())) {
            return -parent.getP0();
        }
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), BaseBus.getV(b), BaseBus.getAngle(b), TwoSides.ONE).otherSideP(parent, false);
    }

    @Override
    public double getQ() {
        if (!parent.isPaired() && valid(parent.getP0(), parent.getQ0())) {
            return -parent.getQ0();
        }
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), BaseBus.getV(b), BaseBus.getAngle(b), TwoSides.ONE).otherSideQ(parent, false);
    }

    @Override
    public DanglingLine getDanglingLine() {
        return parent;
    }

    private static boolean valid(double p0, double q0) {
        return !Double.isNaN(p0) && !Double.isNaN(q0);
    }
}

/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public interface BaseBus extends Bus {

    Stream<Terminal> getAllTerminalsStream();

    Iterable<Terminal> getAllTerminals();

    @Override
    default void visitConnectedEquipments(TopologyVisitor topologyVisitor) {
        visitEquipments(getConnectedTerminals(), topologyVisitor);
    }

    @Override
    default void visitConnectedOrConnectableEquipments(TopologyVisitor topologyVisitor) {
        visitEquipments(getAllTerminals(), topologyVisitor);
    }

    @Override
    default Iterable<DanglingLine> getDanglingLines(DanglingLineFilter danglingLineFilter) {
        return getDanglingLineStream(danglingLineFilter).collect(Collectors.toList());
    }

    @Override
    default Stream<DanglingLine> getDanglingLineStream(DanglingLineFilter danglingLineFilter) {
        return getDanglingLineStream().filter(danglingLineFilter.getPredicate());
    }

    @Override
    default double getP() {
        if (getConnectedTerminalCount() == 0) {
            return Double.NaN;
        }
        double p = 0;
        for (Terminal terminal : getConnectedTerminals()) {
            Connectable connectable = terminal.getConnectable();
            switch (connectable.getType()) {
                case BUSBAR_SECTION, SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR, LINE, TWO_WINDINGS_TRANSFORMER, THREE_WINDINGS_TRANSFORMER, DANGLING_LINE -> {
                    // Do nothing
                }
                case GENERATOR, BATTERY, LOAD, HVDC_CONVERTER_STATION -> {
                    if (!Double.isNaN(terminal.getP())) {
                        p += terminal.getP();
                    }
                }
                default -> throw new AssertionError();
            }
        }
        return p;
    }

    @Override
    default double getQ() {
        if (getConnectedTerminalCount() == 0) {
            return Double.NaN;
        }
        double q = 0;
        for (Terminal terminal : getConnectedTerminals()) {
            Connectable connectable = terminal.getConnectable();
            switch (connectable.getType()) {
                case BUSBAR_SECTION, LINE, TWO_WINDINGS_TRANSFORMER, THREE_WINDINGS_TRANSFORMER, DANGLING_LINE -> {
                    // Do nothing
                }
                case GENERATOR, BATTERY, LOAD, SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR, HVDC_CONVERTER_STATION -> {
                    if (!Double.isNaN(terminal.getQ())) {
                        q += terminal.getQ();
                    }
                }
                default -> throw new AssertionError();
            }
        }
        return q;
    }

    private void visitEquipments(Iterable<? extends Terminal> terminals, TopologyVisitor visitor) {
        Objects.requireNonNull(visitor);

        for (Terminal terminal : terminals) {
            Connectable connectable = terminal.getConnectable();
            switch (connectable.getType()) {
                case BUSBAR_SECTION -> visitor.visitBusbarSection((BusbarSection) connectable);
                case LINE -> {
                    Line line = (Line) connectable;
                    visitor.visitLine(line, line.getTerminal1() == terminal ? TwoSides.ONE
                        : TwoSides.TWO);
                }
                case GENERATOR -> visitor.visitGenerator((Generator) connectable);
                case BATTERY -> visitor.visitBattery((Battery) connectable);
                case SHUNT_COMPENSATOR -> visitor.visitShuntCompensator((ShuntCompensator) connectable);
                case TWO_WINDINGS_TRANSFORMER -> {
                    TwoWindingsTransformer twt = (TwoWindingsTransformer) connectable;
                    visitor.visitTwoWindingsTransformer(twt,
                        twt.getTerminal1() == terminal
                            ? TwoSides.ONE
                            : TwoSides.TWO);
                }
                case THREE_WINDINGS_TRANSFORMER -> {
                    ThreeWindingsTransformer thwt = (ThreeWindingsTransformer) connectable;
                    ThreeSides side;
                    if (thwt.getLeg1().getTerminal() == terminal) {
                        side = ThreeSides.ONE;
                    } else if (thwt.getLeg2().getTerminal() == terminal) {
                        side = ThreeSides.TWO;
                    } else {
                        side = ThreeSides.THREE;
                    }
                    visitor.visitThreeWindingsTransformer(thwt, side);
                }
                case LOAD -> visitor.visitLoad((Load) connectable);
                case DANGLING_LINE -> visitor.visitDanglingLine((DanglingLine) connectable);
                case STATIC_VAR_COMPENSATOR -> visitor.visitStaticVarCompensator((StaticVarCompensator) connectable);
                case HVDC_CONVERTER_STATION -> visitor.visitHvdcConverterStation((HvdcConverterStation<?>) connectable);
                case GROUND -> visitor.visitGround((Ground) connectable);
                default -> throw new AssertionError();
            }
        }
    }

    static double getV(Bus b) {
        return b == null ? Double.NaN : b.getV();
    }

    static double getAngle(Bus b) {
        return b == null ? Double.NaN : b.getAngle();
    }
}

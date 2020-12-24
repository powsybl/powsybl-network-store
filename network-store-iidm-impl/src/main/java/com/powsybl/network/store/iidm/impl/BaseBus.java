/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public interface BaseBus extends Bus {

    Stream<? extends Terminal> getAllTerminalsStream();

    public Iterable<? extends Terminal> getAllTerminals();

    @Override
    default void visitConnectedEquipments(TopologyVisitor topologyVisitor) {
        visitEquipments(getConnectedTerminals(), topologyVisitor);
    }

    @Override
    default void visitConnectedOrConnectableEquipments(TopologyVisitor topologyVisitor) {
        visitEquipments(getAllTerminals(), topologyVisitor);
    }

    private <T extends Terminal> void visitEquipments(Iterable<T> terminals, TopologyVisitor visitor) {
        Objects.requireNonNull(visitor);

        for (T terminal : terminals) {
            Connectable connectable = terminal.getConnectable();
            switch (connectable.getType()) {
                case BUSBAR_SECTION:
                    visitor.visitBusbarSection((BusbarSectionImpl) connectable);
                    break;

                case LINE:
                    LineImpl line = (LineImpl) connectable;
                    visitor.visitLine(line, line.getTerminal1() == terminal ? Branch.Side.ONE
                            : Branch.Side.TWO);
                    break;

                case GENERATOR:
                    visitor.visitGenerator((GeneratorImpl) connectable);
                    break;

                case BATTERY:
                    visitor.visitBattery((BatteryImpl) connectable);
                    break;

                case SHUNT_COMPENSATOR:
                    visitor.visitShuntCompensator((ShuntCompensatorImpl) connectable);
                    break;

                case TWO_WINDINGS_TRANSFORMER:
                    TwoWindingsTransformer twt = (TwoWindingsTransformer) connectable;
                    visitor.visitTwoWindingsTransformer(twt,
                            twt.getTerminal1() == terminal
                                    ? Branch.Side.ONE
                                    : Branch.Side.TWO);
                    break;

                case THREE_WINDINGS_TRANSFORMER:
                    ThreeWindingsTransformer thwt = (ThreeWindingsTransformer) connectable;
                    ThreeWindingsTransformer.Side side;
                    if (thwt.getLeg1().getTerminal() == terminal) {
                        side = ThreeWindingsTransformer.Side.ONE;
                    } else if (thwt.getLeg2().getTerminal() == terminal) {
                        side = ThreeWindingsTransformer.Side.TWO;
                    } else {
                        side = ThreeWindingsTransformer.Side.THREE;
                    }
                    visitor.visitThreeWindingsTransformer(thwt, side);
                    break;

                case LOAD:
                    visitor.visitLoad((LoadImpl) connectable);
                    break;

                case DANGLING_LINE:
                    visitor.visitDanglingLine((DanglingLineImpl) connectable);
                    break;

                case STATIC_VAR_COMPENSATOR:
                    visitor.visitStaticVarCompensator((StaticVarCompensator) connectable);
                    break;

                case HVDC_CONVERTER_STATION:
                    visitor.visitHvdcConverterStation((HvdcConverterStation<?>) connectable);
                    break;

                default:
                    throw new AssertionError();
            }
        }
    }
}

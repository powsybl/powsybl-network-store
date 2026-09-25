/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolderAdder;

import static com.powsybl.iidm.network.regulation.RegulationMode.REACTIVE_POWER;
import static com.powsybl.iidm.network.regulation.RegulationMode.VOLTAGE;

final class VoltageRegulationCompatibility {

    private VoltageRegulationCompatibility() {
    }

    static <T extends VoltageRegulationHolderAdder<T>> void createGeneratorRegulation(T adder,
                                                                                        double targetV,
                                                                                        double localTargetV,
                                                                                        double targetQ,
                                                                                        Boolean voltageRegulatorOn,
                                                                                        Terminal terminal) {
        if (Boolean.TRUE.equals(voltageRegulatorOn)) {
            VoltageRegulationAdder<T> voltageRegulation = adder.newVoltageRegulation().withMode(VOLTAGE);
            if (terminal != null) {
                voltageRegulation.withTargetValue(targetV).withTerminal(terminal);
                adder.setLocalTargetV(localTargetV);
            } else {
                setLocalTargetV(adder, targetV, localTargetV);
            }
            voltageRegulation.add();
            adder.setLocalTargetQ(targetQ);
        } else if (Boolean.FALSE.equals(voltageRegulatorOn) && !Double.isNaN(targetQ) && terminal != null) {
            adder.newVoltageRegulation()
                .withMode(VOLTAGE)
                .withTargetValue(targetV)
                .withTerminal(terminal)
                .withRegulating(false)
                .add();
            adder.setLocalTargetV(localTargetV);
            adder.setLocalTargetQ(targetQ);
        } else {
            setLocalTargetV(adder, targetV, localTargetV);
            adder.setLocalTargetQ(targetQ);
        }
    }

    static <T extends VoltageRegulationHolderAdder<T>> void createVscRegulation(T adder,
                                                                                  double targetV,
                                                                                  double localTargetV,
                                                                                  double targetQ,
                                                                                  Boolean voltageRegulatorOn,
                                                                                  Terminal terminal) {
        createGeneratorRegulation(adder, targetV, localTargetV, targetQ, voltageRegulatorOn, terminal);
    }

    static <T extends VoltageRegulationHolderAdder<T>> void createSvcRegulation(T adder,
                                                                                 RegulationMode regulationMode,
                                                                                 double targetV,
                                                                                 double targetQ,
                                                                                 Boolean regulating,
                                                                                 Terminal terminal) {
        if (regulationMode != null && regulating != null) {
            VoltageRegulationAdder<T> voltageRegulation = adder.newVoltageRegulation().withMode(regulationMode);
            double targetValue = Double.NaN;
            if (regulationMode == VOLTAGE) {
                if (terminal != null) {
                    targetValue = targetV;
                } else {
                    adder.setLocalTargetV(targetV);
                }
                adder.setLocalTargetQ(targetQ);
            } else if (regulationMode == REACTIVE_POWER) {
                if (terminal != null) {
                    targetValue = targetQ;
                } else {
                    adder.setLocalTargetQ(targetQ);
                }
                adder.setLocalTargetV(targetV);
            }
            voltageRegulation.withTerminal(terminal).withTargetValue(targetValue).withRegulating(regulating).add();
        } else {
            adder.newVoltageRegulation().withMode(VOLTAGE).withRegulating(false).add();
            adder.setLocalTargetV(targetV);
            adder.setLocalTargetQ(targetQ);
        }
    }

    static <T extends VoltageRegulationHolderAdder<T>> void createShuntRegulation(T adder,
                                                                                    boolean voltageRegulatorOn,
                                                                                    double targetV,
                                                                                    double targetDeadband,
                                                                                    Terminal terminal) {
        boolean withTerminal = terminal != null;
        adder.newVoltageRegulation()
            .withMode(VOLTAGE)
            .withTargetValue(withTerminal ? targetV : Double.NaN)
            .withTargetDeadband(targetDeadband)
            .withTerminal(terminal)
            .withRegulating(voltageRegulatorOn)
            .add();
        if (withTerminal) {
            adder.setLocalTargetV(Double.NaN);
        }
    }

    private static <T extends VoltageRegulationHolderAdder<T>> void setLocalTargetV(T adder,
                                                                                      double targetV,
                                                                                      double localTargetV) {
        adder.setLocalTargetV(Double.isNaN(localTargetV) && !Double.isNaN(targetV) ? targetV : localTargetV);
    }
}

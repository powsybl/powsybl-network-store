/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.powsybl.iidm.network.regulation.RegulationMode;

import java.util.Objects;

/**
 * Converts regulation data written by network-store versions predating native VoltageRegulation.
 */
public final class LegacyVoltageRegulationAttributesMapper {

    private static final String LEGACY_VOLTAGE_REGULATION = "voltageRegulation";

    private LegacyVoltageRegulationAttributesMapper() {
    }

    public static boolean migrate(IdentifiableAttributes attributes) {
        if (!(attributes instanceof AbstractRegulatingEquipmentAttributes regulatingAttributes)) {
            return false;
        }

        boolean migrated = false;
        if (regulatingAttributes.getVoltageRegulation() == null) {
            if (attributes instanceof GeneratorAttributes generator
                && generator.getRemoteReactivePowerControl() != null) {
                RemoteReactivePowerControlAttributes legacy = generator.getRemoteReactivePowerControl();
                regulatingAttributes.setVoltageRegulation(NetworkVoltageRegulationAttributes.builder()
                    .targetValue(legacy.getTargetQ())
                    .mode(RegulationMode.REACTIVE_POWER)
                    .regulating(legacy.isEnabled())
                    .terminal(legacy.getRegulatingTerminal())
                    .build());
                migrated = true;
            } else if (attributes instanceof StaticVarCompensatorAttributes svc
                && svc.getVoltagePerReactiveControl() != null) {
                VoltagePerReactivePowerControlAttributes legacy = svc.getVoltagePerReactiveControl();
                RegulatingPointAttributes regulatingPoint = svc.getRegulatingPoint();
                TerminalRefAttributes terminal = regulatingPoint == null ? null : regulatingPoint.getRegulatingTerminal();
                boolean regulating = regulatingPoint != null && Boolean.TRUE.equals(regulatingPoint.getRegulating());
                regulatingAttributes.setVoltageRegulation(NetworkVoltageRegulationAttributes.builder()
                    .targetValue(terminal == null ? Double.NaN : svc.getVoltageSetPoint())
                    .slope(legacy.getSlope())
                    .mode(RegulationMode.VOLTAGE_PER_REACTIVE_POWER)
                    .regulating(regulating)
                    .terminal(terminal)
                    .build());
                svc.setLocalTargetV(terminal == null ? svc.getVoltageSetPoint() : Double.NaN);
                svc.setLocalTargetQ(svc.getReactivePowerSetPoint());
                migrated = true;
            } else if (attributes instanceof BatteryAttributes battery) {
                ExtensionAttributes extension = battery.getExtensionAttributes() == null ? null
                    : battery.getExtensionAttributes().get(LEGACY_VOLTAGE_REGULATION);
                if (extension instanceof VoltageRegulationAttributes legacy) {
                    TerminalRefAttributes terminal = legacy.getRegulatingTerminal();
                    if (terminal != null && battery.getResource() != null
                        && Objects.equals(terminal.getConnectableId(), battery.getResource().getId())) {
                        terminal = null;
                    }
                    regulatingAttributes.setVoltageRegulation(NetworkVoltageRegulationAttributes.builder()
                        .targetValue(terminal == null ? Double.NaN : legacy.getTargetV())
                        .mode(RegulationMode.VOLTAGE)
                        .regulating(legacy.isVoltageRegulatorOn())
                        .terminal(terminal)
                        .build());
                    battery.setLocalTargetV(terminal == null ? legacy.getTargetV() : Double.NaN);
                    migrated = true;
                }
            }
        }

        if (attributes instanceof GeneratorAttributes generator) {
            migrated |= generator.getRemoteReactivePowerControl() != null;
            generator.setRemoteReactivePowerControl(null);
        } else if (attributes instanceof StaticVarCompensatorAttributes svc) {
            migrated |= svc.getVoltagePerReactiveControl() != null;
            svc.setVoltagePerReactiveControl(null);
        } else if (attributes instanceof BatteryAttributes battery) {
            if (battery.getExtensionAttributes() != null) {
                migrated |= battery.getExtensionAttributes().remove(LEGACY_VOLTAGE_REGULATION) != null;
            }
        }
        return migrated;
    }
}

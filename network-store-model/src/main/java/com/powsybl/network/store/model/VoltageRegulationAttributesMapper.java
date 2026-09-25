/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If applicable, see the accompanying LICENSE file.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.model;

import com.powsybl.iidm.network.regulation.RegulationMode;

/**
 * Converts regulation data written by pre-7.4 network-store versions to the
 * common voltage regulation representation.
 */
final class VoltageRegulationAttributesMapper {

    private VoltageRegulationAttributesMapper() {
    }

    static void normalize(Attributes attributes) {
        if (attributes instanceof RatioTapChangerAttributes ratioTapChanger) {
            if (ratioTapChanger.getVoltageRegulation() == null) {
                ratioTapChanger.setVoltageRegulation(fromRatioTapChanger(ratioTapChanger));
            }
            return;
        }
        if (!(attributes instanceof AbstractRegulatingEquipmentAttributes regulatingAttributes)
                || regulatingAttributes.getVoltageRegulation() != null) {
            return;
        }

        NetworkVoltageRegulationAttributes voltageRegulation = switch (attributes) {
            case GeneratorAttributes generator -> fromGenerator(generator);
            case BatteryAttributes battery -> fromBattery(battery);
            case StaticVarCompensatorAttributes svc -> fromStaticVarCompensator(svc);
            case VscConverterStationAttributes vsc -> fromVsc(vsc);
            case ShuntCompensatorAttributes shunt -> fromShunt(shunt);
            default -> null;
        };
        regulatingAttributes.setVoltageRegulation(voltageRegulation);
    }

    private static NetworkVoltageRegulationAttributes fromGenerator(GeneratorAttributes generator) {
        RemoteReactivePowerControlAttributes remote = generator.getRemoteReactivePowerControl();
        RegulatingPointAttributes point = generator.getRegulatingPoint();
        if (remote == null && point == null) {
            return null;
        }
        if (remote == null && !Boolean.TRUE.equals(point.getRegulating())
                && point.getRegulatingTerminal() == null) {
            return null;
        }
        boolean withTerminal = remote != null ? remote.getRegulatingTerminal() != null : point.getRegulatingTerminal() != null;
        boolean regulating = remote != null ? remote.isEnabled() : Boolean.TRUE.equals(point.getRegulating());
        RegulationMode mode = remote != null ? RegulationMode.REACTIVE_POWER : RegulationMode.VOLTAGE;
        double targetValue = remote != null ? remote.getTargetQ() : generator.getTargetV();
        if (!withTerminal) {
            targetValue = Double.NaN;
        }
        return NetworkVoltageRegulationAttributes.builder()
                .targetValue(targetValue)
                .mode(mode)
                .regulating(regulating)
                .terminal(remote != null ? remote.getRegulatingTerminal() : point.getRegulatingTerminal())
                .build();
    }

    private static NetworkVoltageRegulationAttributes fromBattery(BatteryAttributes battery) {
        VoltageRegulationAttributes legacy = getExtension(battery, "voltageRegulation", VoltageRegulationAttributes.class);
        if (legacy == null) {
            return null;
        }
        boolean withTerminal = legacy.getRegulatingTerminal() != null;
        return NetworkVoltageRegulationAttributes.builder()
                .targetValue(withTerminal ? legacy.getTargetV() : Double.NaN)
                .mode(RegulationMode.VOLTAGE)
                .regulating(legacy.isVoltageRegulatorOn())
                .terminal(legacy.getRegulatingTerminal())
                .build();
    }

    private static NetworkVoltageRegulationAttributes fromStaticVarCompensator(StaticVarCompensatorAttributes svc) {
        RegulatingPointAttributes point = svc.getRegulatingPoint();
        if (point == null && svc.getVoltagePerReactiveControl() == null) {
            return null;
        }
        if (point != null && !Boolean.TRUE.equals(point.getRegulating())
                && point.getRegulatingTerminal() == null
                && Double.isNaN(svc.getVoltageSetPoint())
                && Double.isNaN(svc.getReactivePowerSetPoint())
                && svc.getVoltagePerReactiveControl() == null) {
            return null;
        }
        RegulationMode mode = regulationMode(point, RegulationMode.VOLTAGE);
        boolean withTerminal = point != null && point.getRegulatingTerminal() != null;
        double targetValue = mode == RegulationMode.REACTIVE_POWER
                ? svc.getReactivePowerSetPoint() : svc.getVoltageSetPoint();
        return NetworkVoltageRegulationAttributes.builder()
                .targetValue(withTerminal ? targetValue : Double.NaN)
                .targetDeadband(Double.NaN)
                .slope(svc.getVoltagePerReactiveControl() != null ? svc.getVoltagePerReactiveControl().getSlope() : Double.NaN)
                .mode(mode)
                .regulating(point != null && Boolean.TRUE.equals(point.getRegulating()))
                .terminal(point != null ? point.getRegulatingTerminal() : null)
                .build();
    }

    private static NetworkVoltageRegulationAttributes fromVsc(VscConverterStationAttributes vsc) {
        RegulatingPointAttributes point = vsc.getRegulatingPoint();
        if (point == null) {
            return null;
        }
        if (!Boolean.TRUE.equals(point.getRegulating())
                && point.getRegulatingTerminal() == null
                && Double.isNaN(vsc.getVoltageSetPoint())
                && Double.isNaN(vsc.getReactivePowerSetPoint())) {
            return null;
        }
        boolean withTerminal = point.getRegulatingTerminal() != null;
        return NetworkVoltageRegulationAttributes.builder()
                .targetValue(withTerminal ? vsc.getVoltageSetPoint() : Double.NaN)
                .mode(RegulationMode.VOLTAGE)
                .regulating(Boolean.TRUE.equals(point.getRegulating()))
                .terminal(point.getRegulatingTerminal())
                .build();
    }

    private static NetworkVoltageRegulationAttributes fromShunt(ShuntCompensatorAttributes shunt) {
        RegulatingPointAttributes point = shunt.getRegulatingPoint();
        if (point == null) {
            return null;
        }
        if (!Boolean.TRUE.equals(point.getRegulating())
                && point.getRegulatingTerminal() == null
                && Double.isNaN(shunt.getTargetDeadband())) {
            return null;
        }
        boolean withTerminal = point.getRegulatingTerminal() != null;
        return NetworkVoltageRegulationAttributes.builder()
                .targetValue(withTerminal ? shunt.getTargetV() : Double.NaN)
                .targetDeadband(shunt.getTargetDeadband())
                .mode(RegulationMode.VOLTAGE)
                .regulating(Boolean.TRUE.equals(point.getRegulating()))
                .terminal(point.getRegulatingTerminal())
                .build();
    }

    private static NetworkVoltageRegulationAttributes fromRatioTapChanger(RatioTapChangerAttributes ratioTapChanger) {
        RegulatingPointAttributes point = ratioTapChanger.getRegulatingPoint();
        if (point == null) {
            return null;
        }
        if (!Boolean.TRUE.equals(point.getRegulating())
                && point.getRegulatingTerminal() == null
                && Double.isNaN(ratioTapChanger.getRegulationValue())
                && Double.isNaN(ratioTapChanger.getTargetDeadband())
                && point.getRegulationMode() == null) {
            return null;
        }
        return NetworkVoltageRegulationAttributes.builder()
                .targetValue(ratioTapChanger.getRegulationValue())
                .targetDeadband(ratioTapChanger.getTargetDeadband())
                .mode(parseMode(point.getRegulationMode()))
                .regulating(Boolean.TRUE.equals(point.getRegulating()))
                .terminal(point.getRegulatingTerminal())
                .build();
    }

    private static RegulationMode regulationMode(RegulatingPointAttributes point, RegulationMode defaultMode) {
        return point == null ? defaultMode : parseMode(point.getRegulationMode(), defaultMode);
    }

    private static RegulationMode parseMode(String mode) {
        return parseMode(mode, RegulationMode.VOLTAGE);
    }

    private static RegulationMode parseMode(String mode, RegulationMode defaultMode) {
        if (mode == null) {
            return defaultMode;
        }
        try {
            return RegulationMode.valueOf(mode);
        } catch (IllegalArgumentException ignored) {
            return defaultMode;
        }
    }

    private static <T extends ExtensionAttributes> T getExtension(AbstractIdentifiableAttributes attributes,
                                                                    String name, Class<T> type) {
        ExtensionAttributes extension = attributes.getExtensionAttributes() == null
                ? null : attributes.getExtensionAttributes().get(name);
        return type.isInstance(extension) ? type.cast(extension) : null;
    }
}

/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;
import com.powsybl.network.store.model.*;

import static com.powsybl.iidm.network.util.VoltageRegulationUtils.createVoltageRegulationBackwardCompatibility;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StaticVarCompensatorAdderImpl extends AbstractInjectionAdder<StaticVarCompensatorAdderImpl> implements StaticVarCompensatorAdder {

    private double bMin = Double.NaN;

    private double bMax = Double.NaN;

    private double voltageSetPoint = Double.NaN;

    private double reactivePowerSetPoint = Double.NaN;

    private Boolean regulating;

    RegulationMode regulationMode = RegulationMode.VOLTAGE;

    private Terminal regulatingTerminal;

    private double localTargetV = Double.NaN;

    private double localTargetQ = Double.NaN;

    private VoltageRegulation.VoltageRegulationAttributes voltageRegulationAttributes;

    private boolean voltageRegulationConfigured;

    StaticVarCompensatorAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(voltageLevelResource, index);
    }

    @Override
    public VoltageRegulationAdder<StaticVarCompensatorAdder> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(StaticVarCompensator.class, this, null, getIndex(), this,
                attributes -> {
                    voltageRegulationAttributes = attributes;
                    voltageRegulationConfigured = true;
                    return null;
                });
    }

    @Override
    public double getLocalTargetQ() {
        return localTargetQ;
    }

    @Override
    public StaticVarCompensatorAdder setLocalTargetQ(double localTargetQ) {
        this.localTargetQ = localTargetQ;
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setLocalTargetV(double localTargetV) {
        this.localTargetV = localTargetV;
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setBmin(double bMin) {
        this.bMin = bMin;
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setBmax(double bMax) {
        this.bMax = bMax;
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setVoltageSetpoint(double voltageSetPoint) {
        this.voltageSetPoint = voltageSetPoint;
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setReactivePowerSetpoint(double reactivePowerSetPoint) {
        this.reactivePowerSetPoint = reactivePowerSetPoint;
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setRegulationMode(RegulationMode regulationMode) {
        this.regulationMode = regulationMode;
        return this;
    }

    @Override
    public StaticVarCompensatorAdderImpl setRegulating(boolean regulating) {
        this.regulating = regulating;
        return this;
    }

    @Override
    public StaticVarCompensatorAdderImpl setRegulatingTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = regulatingTerminal;
        return this;
    }

    @Override
    public StaticVarCompensator add() {
        NetworkImpl network = getNetwork();
        if (network.getMinValidationLevel() == ValidationLevel.EQUIPMENT && regulating == null) {
            regulating = false;
        }
        String id = checkAndGetUniqueId();
        checkNodeBus();
        if (voltageRegulationAttributes == null && regulating != null) {
            createVoltageRegulationBackwardCompatibility(this, regulationMode, voltageSetPoint, reactivePowerSetPoint, regulating, regulatingTerminal);
        } else if (voltageRegulationConfigured) {
            if (!Double.isNaN(voltageSetPoint) && Double.isNaN(localTargetV)) {
                localTargetV = voltageSetPoint;
            }
            if (!Double.isNaN(reactivePowerSetPoint) && Double.isNaN(localTargetQ)) {
                localTargetQ = reactivePowerSetPoint;
            }
        }
        ValidationUtil.checkBmin(this, bMin);
        ValidationUtil.checkBmax(this, bMax);
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());

        TerminalRefAttributes terminalRefAttributes = TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal);
        TerminalRefAttributes voltageRegulationTerminalRef = voltageRegulationAttributes == null
                ? null
                : TerminalRefUtils.getTerminalRefAttributes(voltageRegulationAttributes.terminal());
        Boolean regulatingPointStatus = voltageRegulationAttributes == null
                ? regulating
                : Boolean.valueOf(voltageRegulationAttributes.isRegulating());
        RegulatingPointAttributes regulatingPointAttributes = new RegulatingPointAttributes(id, ResourceType.STATIC_VAR_COMPENSATOR, RegulatingTapChangerType.NONE,
            new TerminalRefAttributes(id, null), voltageRegulationTerminalRef != null ? voltageRegulationTerminalRef : terminalRefAttributes,
            String.valueOf(voltageRegulationAttributes == null ? regulationMode : voltageRegulationAttributes.mode()),
            ResourceType.STATIC_VAR_COMPENSATOR,
            regulatingPointStatus);
        Resource<StaticVarCompensatorAttributes> resource = Resource.staticVarCompensatorBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .attributes(StaticVarCompensatorAttributes.builder()
                        .voltageLevelId(getVoltageLevelResource().getId())
                        .name(getName())
                        .fictitious(isFictitious())
                        .node(getNode())
                        .bus(getBus())
                        .connectableBus(getConnectableBus() != null ? getConnectableBus() : getBus())
                        .bmin(bMin)
                        .bmax(bMax)
                        .voltageSetPoint(voltageSetPoint)
                        .reactivePowerSetPoint(reactivePowerSetPoint)
                        .localTargetV(localTargetV)
                        .localTargetQ(localTargetQ)
                        .regulatingPoint(regulatingPointAttributes)
                        .voltageRegulation(voltageRegulationAttributes == null ? null : NetworkVoltageRegulationAttributes.builder()
                                    .targetValue(voltageRegulationAttributes.targetValue())
                                    .targetDeadband(voltageRegulationAttributes.targetDeadband())
                                    .slope(voltageRegulationAttributes.slope())
                                    .mode(voltageRegulationAttributes.mode())
                                    .regulating(voltageRegulationAttributes.isRegulating())
                                    .terminal(TerminalRefUtils.getTerminalRefAttributes(voltageRegulationAttributes.terminal()))
                                    .build())
                        .build())
                .build();
        StaticVarCompensatorImpl svc = getIndex().createStaticVarCompensator(resource);

        svc.getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        if (!voltageRegulationConfigured && regulatingTerminal != null) {
            svc.setRegulatingTerminal(regulatingTerminal);
        }
        return svc;
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.STATIC_VAR_COMPENSATOR.getDescription();
    }
}

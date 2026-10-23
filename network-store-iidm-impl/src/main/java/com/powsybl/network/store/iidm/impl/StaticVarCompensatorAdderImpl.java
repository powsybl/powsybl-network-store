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

import static com.powsybl.iidm.network.util.VoltageRegulationUtils.createSvcVoltageRegulationBackwardCompatibility;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StaticVarCompensatorAdderImpl extends AbstractInjectionAdder<StaticVarCompensatorAdderImpl> implements StaticVarCompensatorAdder {

    private double bMin = Double.NaN;

    private double bMax = Double.NaN;

    private double voltageSetpoint = Double.NaN;

    private double reactivePowerSetpoint = Double.NaN;

    private RegulationMode regulationMode;

    private Terminal regulatingTerminal;

    private double localTargetQ = Double.NaN;

    private double localTargetV = Double.NaN;

    private Boolean regulating;

    private VoltageRegulation.VoltageRegulationAttributes voltageRegulationAttributes = null;

    StaticVarCompensatorAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(voltageLevelResource, index);
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
    public StaticVarCompensatorAdder setVoltageSetpoint(double voltageSetpoint) {
        this.voltageSetpoint = voltageSetpoint;
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setReactivePowerSetpoint(double reactivePowerSetpoint) {
        this.reactivePowerSetpoint = reactivePowerSetpoint;
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
    public StaticVarCompensatorAdder setLocalTargetQ(double localTargetQ) {
        this.localTargetQ = localTargetQ;
        return this;
    }

    @Override
    public double getLocalTargetQ() {
        return this.localTargetQ;
    }

    @Override
    public StaticVarCompensatorAdder setLocalTargetV(double localTargetV) {
        this.localTargetV = localTargetV;
        return this;
    }

    @Override
    public VoltageRegulationAdder<StaticVarCompensatorAdder> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(StaticVarCompensator.class, this, this, getNetwork(), this::setVoltageRegulationAttributes);
    }

    private void setVoltageRegulationAttributes(VoltageRegulation.VoltageRegulationAttributes voltageRegulationAttributes) {
        this.voltageRegulationAttributes = voltageRegulationAttributes;
    }

    @Override
    public StaticVarCompensator add() {
        NetworkImpl network = getNetwork();
        if (network.getMinValidationLevel() == ValidationLevel.EQUIPMENT && regulating == null) {
            regulating = false;
        }
        String id = checkAndGetUniqueId();
        checkNodeBus();

        // Backward compatibility
        if (voltageRegulationAttributes == null && regulating != null) {
            // If a static var compensator with old setters is added and voltageRegulation does not exist,
            // the new voltageRegulation will be created from the old attributes.
            createSvcVoltageRegulationBackwardCompatibility(this, regulationMode, voltageSetpoint, reactivePowerSetpoint, regulating, regulatingTerminal);
        } else {
            // In the case of a static var compensator with old setters and newVoltageRegulation method used
            // the old local attributes will be set without overriding the local attributes if already set
            if (!Double.isNaN(voltageSetpoint) && Double.isNaN(localTargetV)) {
                localTargetV = voltageSetpoint;
            }
            if (!Double.isNaN(reactivePowerSetpoint) && Double.isNaN(localTargetQ)) {
                localTargetQ = reactivePowerSetpoint;
            }
        }

        ValidationUtil.checkBmin(this, bMin);
        ValidationUtil.checkBmax(this, bMax);
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        ValidationUtil.checkLocalTargetQandV(this,
            StaticVarCompensator.class,
            localTargetV,
            localTargetQ,
            voltageRegulationAttributes,
            getNetwork().getMinValidationLevel(),
            getNetwork().getReportNodeContext().getReportNode());

        VoltageRegulationAttributes persistedVoltageRegulation = voltageRegulationAttributes != null
            ? VoltageRegulationAttributes.builder()
                .regulatingEquipmentId(id)
                .regulatingResourceType(ResourceType.STATIC_VAR_COMPENSATOR)
                .regulatingTapChangerType(RegulatingTapChangerType.NONE)
                .localTerminal(new TerminalRefAttributes(id, null))
                .regulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(voltageRegulationAttributes.terminal()))
                .regulatedResourceType(voltageRegulationAttributes.terminal() != null
                    ? ((AbstractIdentifiableImpl<?, ?>) voltageRegulationAttributes.terminal().getConnectable()).getResource().getType()
                    : ResourceType.STATIC_VAR_COMPENSATOR)
                .regulationMode(voltageRegulationAttributes.mode() != null ? voltageRegulationAttributes.mode().name() : null)
                .regulating(voltageRegulationAttributes.isRegulating())
                .targetValue(voltageRegulationAttributes.targetValue())
                .targetDeadband(voltageRegulationAttributes.targetDeadband())
                .slope(voltageRegulationAttributes.slope())
                .build()
            : null;

        Resource<StaticVarCompensatorAttributes> resource = Resource.staticVarCompensatorBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .attributes(StaticVarCompensatorAttributes.builder()
                        .voltageLevelId(getVoltageLevelResource().getId())
                        .name(getName())
                        .fictitious(isFictitious())
                        .equivalent(isEquivalent())
                        .node(getNode())
                        .bus(getBus())
                        .connectableBus(getConnectableBus() != null ? getConnectableBus() : getBus())
                        .bmin(bMin)
                        .bmax(bMax)
                        .localTargetV(localTargetV)
                        .localTargetQ(localTargetQ)
                        .voltageRegulation(persistedVoltageRegulation)
                        .build())
                .build();
        StaticVarCompensatorImpl svc = getIndex().createStaticVarCompensator(resource);

        svc.getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        if (voltageRegulationAttributes != null && voltageRegulationAttributes.terminal() != null) {
            ((TerminalImpl<?>) voltageRegulationAttributes.terminal()).setAsRegulatingPoint(svc.getRegulatingPoint());
            ((VoltageRegulationImpl) svc.getVoltageRegulation()).registerOnTerminal();
        }
        return svc;
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.STATIC_VAR_COMPENSATOR.getDescription();
    }
}

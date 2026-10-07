/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;
import com.powsybl.network.store.model.*;

import static com.powsybl.iidm.network.util.VoltageRegulationUtils.createGeneratorVoltageRegulationBackwardCompatibility;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class GeneratorAdderImpl extends AbstractInjectionAdder<GeneratorAdderImpl> implements GeneratorAdder {

    private EnergySource energySource = EnergySource.OTHER;

    private double minP = Double.NaN;

    private double maxP = Double.NaN;

    private Boolean voltageRegulatorOn;

    private double targetP = Double.NaN;

    private double localTargetQ = Double.NaN;

    private double targetV = Double.NaN;

    private double localTargetV = Double.NaN;

    private double ratedS = Double.NaN;

    private Terminal regulatingTerminal;

    private VoltageRegulation.VoltageRegulationAttributes voltageRegulationAttributes = null;

    private boolean condenser = false;

    GeneratorAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(voltageLevelResource, index);
    }

    @Override
    public GeneratorAdder setEnergySource(EnergySource energySource) {
        this.energySource = energySource;
        return this;

    }

    @Override
    public GeneratorAdder setMaxP(double maxP) {
        this.maxP = maxP;
        return this;

    }

    @Override
    public GeneratorAdder setMinP(double minP) {
        this.minP = minP;
        return this;

    }

    @Override
    public GeneratorAdder setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        this.voltageRegulatorOn = voltageRegulatorOn;
        return this;

    }

    @Override
    public GeneratorAdder setRegulatingTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = regulatingTerminal;
        return this;

    }

    @Override
    public GeneratorAdder setTargetP(double targetP) {
        this.targetP = targetP;
        return this;

    }

    @Override
    public GeneratorAdder setTargetQ(double targetQ) {
        return this.setLocalTargetQ(targetQ);
    }

    @Override
    public GeneratorAdder setLocalTargetQ(double localTargetQ) {
        this.localTargetQ = localTargetQ;
        return this;
    }

    @Override
    public double getLocalTargetQ() {
        return this.localTargetQ;
    }

    @Override
    public GeneratorAdder setTargetV(double targetV) {
        this.targetV = targetV;
        return this;
    }

    @Override
    public GeneratorAdder setTargetV(double targetV, double equivalentLocalTargetV) {
        this.targetV = targetV;
        return this.setLocalTargetV(equivalentLocalTargetV);
    }

    @Override
    public GeneratorAdder setLocalTargetV(double localTargetV) {
        this.localTargetV = localTargetV;
        return this;
    }

    @Override
    public GeneratorAdder setRatedS(double ratedS) {
        this.ratedS = ratedS;
        return this;
    }

    @Override
    public GeneratorAdder setCondenser(boolean condenser) {
        this.condenser = condenser;
        return this;
    }

    @Override
    public VoltageRegulationAdder<GeneratorAdder> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(Generator.class, this, this, getNetwork(), this::setVoltageRegulationAttributes);
    }

    private void setVoltageRegulationAttributes(VoltageRegulation.VoltageRegulationAttributes voltageRegulationAttributes) {
        this.voltageRegulationAttributes = voltageRegulationAttributes;
    }

    @Override
    public Generator add() {
        NetworkImpl network = getNetwork();
        if (network.getMinValidationLevel() == ValidationLevel.EQUIPMENT && voltageRegulatorOn == null) {
            voltageRegulatorOn = false;
        }
        String id = checkAndGetUniqueId();
        checkNodeBus();
        ValidationUtil.checkEnergySource(this, energySource);
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActivePowerSetpoint(this, targetP, getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
        ValidationUtil.checkActivePowerLimits(this, minP, maxP);
        ValidationUtil.checkRatedS(this, ratedS);
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        // Backward compatibility
        if (voltageRegulationAttributes == null && voltageRegulatorOn != null) {
            // If a generator with old setters is added and voltageRegulation does not exist,
            // the new voltageRegulation will be created from the old attributes.
            createGeneratorVoltageRegulationBackwardCompatibility(this, targetV, localTargetV, localTargetQ, voltageRegulatorOn, regulatingTerminal);
        } else {
            // In the case of a generator with old setters and newVoltageRegulation method used
            // the old local attributes will be set without overriding the local attributes if already set
            if (Double.isNaN(localTargetV) && !Double.isNaN(targetV)) {
                localTargetV = targetV;
            }
        }

        ValidationUtil.checkLocalTargetQandV(this,
            Generator.class,
            localTargetV,
            localTargetQ,
            voltageRegulationAttributes,
            getNetwork().getMinValidationLevel(),
            getNetwork().getReportNodeContext().getReportNode());

        MinMaxReactiveLimitsAttributes minMaxAttributes =
                MinMaxReactiveLimitsAttributes.builder()
                        .minQ(-Double.MAX_VALUE)
                        .maxQ(Double.MAX_VALUE)
                        .build();

        VoltageRegulationAttributes persistedVoltageRegulation = voltageRegulationAttributes != null
            ? VoltageRegulationAttributes.builder()
                .regulatingEquipmentId(id)
                .regulatingResourceType(ResourceType.GENERATOR)
                .regulatingTapChangerType(RegulatingTapChangerType.NONE)
                .localTerminal(new TerminalRefAttributes(id, null))
                .regulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(voltageRegulationAttributes.terminal()))
                .regulatedResourceType(voltageRegulationAttributes.terminal() != null
                    ? ((AbstractIdentifiableImpl<?, ?>) voltageRegulationAttributes.terminal().getConnectable()).getResource().getType()
                    : ResourceType.GENERATOR)
                .regulationMode(voltageRegulationAttributes.mode() != null ? voltageRegulationAttributes.mode().name() : null)
                .regulating(voltageRegulationAttributes.isRegulating())
                .targetValue(voltageRegulationAttributes.targetValue())
                .targetDeadband(voltageRegulationAttributes.targetDeadband())
                .slope(voltageRegulationAttributes.slope())
                .build()
            : null;

        Resource<GeneratorAttributes> resource = Resource.generatorBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .attributes(GeneratorAttributes.builder()
                        .voltageLevelId(getVoltageLevelResource().getId())
                        .name(getName())
                        .fictitious(isFictitious())
                        .equivalent(isEquivalent())
                        .node(getNode())
                        .bus(getBus())
                        .connectableBus(getConnectableBus() != null ? getConnectableBus() : getBus())
                        .energySource(energySource)
                        .maxP(maxP)
                        .minP(minP)
                        .targetP(targetP)
                        .localTargetQ(localTargetQ)
                        .localTargetV(localTargetV)
                        .voltageRegulation(persistedVoltageRegulation)
                        .ratedS(ratedS)
                        .reactiveLimits(minMaxAttributes)
                        .condenser(condenser)
                        .build())
                .build();
        GeneratorImpl generator = getIndex().createGenerator(resource);
        generator.getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        if (voltageRegulationAttributes != null && voltageRegulationAttributes.terminal() != null) {
            ((TerminalImpl<?>) voltageRegulationAttributes.terminal()).setAsRegulatingPoint(generator.getRegulatingPoint());
            ((VoltageRegulationImpl) generator.getVoltageRegulation()).registerOnTerminal();
        }
        return generator;
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.GENERATOR.getDescription();
    }
}

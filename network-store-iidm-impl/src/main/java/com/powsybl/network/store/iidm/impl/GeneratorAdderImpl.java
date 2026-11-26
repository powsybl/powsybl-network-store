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
    public GeneratorAdder setTargetP(double targetP) {
        this.targetP = targetP;
        return this;
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

    private void applyVoltageRegulationBackwardCompatibility() {
        if (voltageRegulationAttributes == null && voltageRegulatorOn != null) {
            createGeneratorVoltageRegulationBackwardCompatibility(this, targetV, localTargetV, localTargetQ, voltageRegulatorOn, regulatingTerminal);
        } else if (Double.isNaN(localTargetV) && !Double.isNaN(targetV)) {
            localTargetV = targetV;
        }
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
        applyVoltageRegulationBackwardCompatibility();

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

        VoltageRegulationAttributes persistedVoltageRegulation = null;
        if (voltageRegulationAttributes != null) {
            ResourceType regulatedResourceType = voltageRegulationAttributes.terminal() != null
                ? ((AbstractIdentifiableImpl<?, ?>) voltageRegulationAttributes.terminal().getConnectable()).getResource().getType()
                : ResourceType.GENERATOR;
            String regulationMode = voltageRegulationAttributes.mode() != null ? voltageRegulationAttributes.mode().name() : null;
            persistedVoltageRegulation = VoltageRegulationAttributes.builder()
                .regulatingEquipmentId(id)
                .regulatingResourceType(ResourceType.GENERATOR)
                .regulatingTapChangerType(RegulatingTapChangerType.NONE)
                .localTerminal(new TerminalRefAttributes(id, null))
                .regulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(voltageRegulationAttributes.terminal()))
                .regulatedResourceType(regulatedResourceType)
                .regulationMode(regulationMode)
                .regulating(voltageRegulationAttributes.isRegulating())
                .targetValue(voltageRegulationAttributes.targetValue())
                .targetDeadband(voltageRegulationAttributes.targetDeadband())
                .slope(voltageRegulationAttributes.slope())
                .build();
        }

        Resource<GeneratorAttributes> resource = createResource(id, persistedVoltageRegulation, minMaxAttributes);
        GeneratorImpl generator = getIndex().createGenerator(resource);
        generator.getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        if (voltageRegulationAttributes != null && voltageRegulationAttributes.terminal() != null) {
            ((TerminalImpl<?>) voltageRegulationAttributes.terminal()).setAsRegulatingPoint(generator.getRegulatingPoint());
            ((VoltageRegulationImpl) generator.getVoltageRegulation()).registerOnTerminal();
        }
        return generator;
    }

    private Resource<GeneratorAttributes> createResource(String id,
                                                         VoltageRegulationAttributes persistedVoltageRegulation,
                                                         MinMaxReactiveLimitsAttributes minMaxAttributes) {
        return Resource.generatorBuilder()
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
    }

    @Deprecated
    @Override
    protected String getTypeDescription() {
        return ResourceType.GENERATOR.getDescription();
    }

    @Deprecated(forRemoval = true, since = "7.4.0")
    @Override
    public GeneratorAdder setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        this.voltageRegulatorOn = voltageRegulatorOn;
        return this;
    }

    @Deprecated(forRemoval = true, since = "7.4.0")
    @Override
    public GeneratorAdder setRegulatingTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = regulatingTerminal;
        return this;
    }

    @Deprecated(forRemoval = true, since = "7.4.0")
    @Override
    public GeneratorAdder setTargetQ(double targetQ) {
        return this.setLocalTargetQ(targetQ);
    }

    @Deprecated(forRemoval = true, since = "7.4.0")
    @Override
    public GeneratorAdder setTargetV(double targetV) {
        this.targetV = targetV;
        return this;
    }

    @Deprecated(forRemoval = true, since = "7.4.0")
    @Override
    public GeneratorAdder setTargetV(double targetV, double equivalentLocalTargetV) {
        this.targetV = targetV;
        return this.setLocalTargetV(equivalentLocalTargetV);
    }
}

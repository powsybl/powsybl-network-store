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

import static com.powsybl.iidm.network.util.VoltageRegulationUtils.createVoltageRegulationBackwardCompatibility;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class GeneratorAdderImpl extends AbstractInjectionAdder<GeneratorAdderImpl> implements GeneratorAdder {

    private EnergySource energySource = EnergySource.OTHER;

    private double minP = Double.NaN;

    private double maxP = Double.NaN;

    private Boolean voltageRegulatorOn;

    private double targetP = Double.NaN;

    private double targetQ = Double.NaN;

    private double targetV = Double.NaN;

    private double equivalentLocalTargetV = Double.NaN;

    private double ratedS = Double.NaN;

    private Terminal regulatingTerminal;

    private boolean condenser = false;

    GeneratorAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(voltageLevelResource, index);
    }

    private VoltageRegulation.VoltageRegulationAttributes voltageRegulationAttributes;

    private boolean voltageRegulationConfigured;

    @Override
    public VoltageRegulationAdder<GeneratorAdder> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(Generator.class, this, null, getIndex(), this,
                attributes -> {
                    voltageRegulationAttributes = attributes;
                    voltageRegulationConfigured = true;
                    return null;
                });
    }

    @Override
    public double getLocalTargetQ() {
        return targetQ;
    }

    @Override
    public GeneratorAdder setLocalTargetQ(double localTargetQ) {
        this.targetQ = localTargetQ;
        return this;
    }

    @Override
    public GeneratorAdder setLocalTargetV(double localTargetV) {
        this.equivalentLocalTargetV = localTargetV;
        return this;
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
        this.targetQ = targetQ;
        return this;

    }

    @Override
    public GeneratorAdder setTargetV(double targetV) {
        this.targetV = targetV;
        return this;

    }

    @Override
    public GeneratorAdder setTargetV(double targetV, double equivalentLocalTargetV) {
        this.targetV = targetV;
        this.equivalentLocalTargetV = equivalentLocalTargetV;
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
        if (voltageRegulationAttributes == null && voltageRegulatorOn != null) {
            createVoltageRegulationBackwardCompatibility(this, targetV, equivalentLocalTargetV, targetQ, voltageRegulatorOn, regulatingTerminal);
        } else if (voltageRegulationConfigured && Double.isNaN(equivalentLocalTargetV) && !Double.isNaN(targetV)) {
            equivalentLocalTargetV = targetV;
        }
        ValidationUtil.checkLocalTargetQandV(this,
                Generator.class,
                equivalentLocalTargetV,
                targetQ,
                voltageRegulationAttributes,
                network.getMinValidationLevel(),
                network.getReportNodeContext().getReportNode());

        MinMaxReactiveLimitsAttributes minMaxAttributes =
                MinMaxReactiveLimitsAttributes.builder()
                        .minQ(-Double.MAX_VALUE)
                        .maxQ(Double.MAX_VALUE)
                        .build();

        TerminalRefAttributes terminalRefAttributes = TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal);
        TerminalRefAttributes voltageRegulationTerminalRef = voltageRegulationAttributes == null
                ? null
                : TerminalRefUtils.getTerminalRefAttributes(voltageRegulationAttributes.terminal());
        Boolean regulatingPointStatus = voltageRegulationAttributes == null
                ? voltageRegulatorOn
                : Boolean.valueOf(voltageRegulationAttributes.isRegulating());
        RegulatingPointAttributes regulatingPointAttributes = new RegulatingPointAttributes(id, ResourceType.GENERATOR, RegulatingTapChangerType.NONE,
            new TerminalRefAttributes(id, null), voltageRegulationTerminalRef != null ? voltageRegulationTerminalRef : terminalRefAttributes,
            null, ResourceType.GENERATOR, regulatingPointStatus);

        Resource<GeneratorAttributes> resource = Resource.generatorBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .attributes(GeneratorAttributes.builder()
                        .voltageLevelId(getVoltageLevelResource().getId())
                        .name(getName())
                        .fictitious(isFictitious())
                        .node(getNode())
                        .bus(getBus())
                        .connectableBus(getConnectableBus() != null ? getConnectableBus() : getBus())
                        .energySource(energySource)
                        .maxP(maxP)
                        .minP(minP)
                        .targetP(targetP)
                        .targetQ(targetQ)
                        .targetV(targetV)
                        .equivalentLocalTargetV(equivalentLocalTargetV)
                        .ratedS(ratedS)
                        .reactiveLimits(minMaxAttributes)
                        .regulatingPoint(regulatingPointAttributes)
                        .condenser(condenser)
                        .voltageRegulation(voltageRegulationAttributes != null
                                ? NetworkVoltageRegulationAttributes.builder()
                                    .targetValue(voltageRegulationAttributes.targetValue())
                                    .targetDeadband(voltageRegulationAttributes.targetDeadband())
                                    .slope(voltageRegulationAttributes.slope())
                                    .mode(voltageRegulationAttributes.mode())
                                    .regulating(voltageRegulationAttributes.isRegulating())
                                    .terminal(TerminalRefUtils.getTerminalRefAttributes(voltageRegulationAttributes.terminal()))
                                    .build()
                                : null)
                        .build())
                .build();
        GeneratorImpl generator = getIndex().createGenerator(resource);
        generator.getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        if (!voltageRegulationConfigured && regulatingTerminal != null) {
            generator.setRegulatingTerminal(regulatingTerminal);
        }
        return generator;
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.GENERATOR.getDescription();
    }
}

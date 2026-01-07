/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;

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
    public GeneratorAdder setTargetV(double targetV, double localBackupTargetV) {
        this.targetV = targetV;
        this.equivalentLocalTargetV = localBackupTargetV;
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
        String id = checkAndGetUniqueId();
        checkNodeBus();
        ValidationUtil.checkEnergySource(this, energySource);
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActivePowerSetpoint(this, targetP, getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, targetV, targetQ, getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
        ValidationUtil.checkActivePowerLimits(this, minP, maxP);
        ValidationUtil.checkRatedS(this, ratedS);
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        ValidationUtil.checkEquivalentLocalTargetV(this, equivalentLocalTargetV);

        MinMaxReactiveLimitsAttributes minMaxAttributes =
                MinMaxReactiveLimitsAttributes.builder()
                        .minQ(-Double.MAX_VALUE)
                        .maxQ(Double.MAX_VALUE)
                        .build();

        TerminalRefAttributes terminalRefAttributes = TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal);
        RegulatingPointAttributes regulatingPointAttributes = new RegulatingPointAttributes(id, ResourceType.GENERATOR, RegulatingTapChangerType.NONE,
            new TerminalRefAttributes(id, null), terminalRefAttributes, null, ResourceType.GENERATOR, voltageRegulatorOn);

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
                        .build())
                .build();
        GeneratorImpl generator = getIndex().createGenerator(resource);
        generator.getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        generator.setRegulatingTerminal(regulatingTerminal);
        return generator;
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.GENERATOR.getDescription();
    }
}

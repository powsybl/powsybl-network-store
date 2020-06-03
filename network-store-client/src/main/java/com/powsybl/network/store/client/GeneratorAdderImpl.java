/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.GeneratorAttributes;
import com.powsybl.network.store.model.MinMaxReactiveLimitsAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

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

    private double ratedS = Double.NaN;

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
        // TODO
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
    public GeneratorAdder setRatedS(double ratedS) {
        this.ratedS = ratedS;
        return this;
    }

    @Override
    public Generator add() {
        String id = checkAndGetUniqueId();
        checkNodeBus();
        ValidationUtil.checkEnergySource(this, energySource);
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActivePowerSetpoint(this, targetP);
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, targetV, targetQ);
        ValidationUtil.checkActivePowerLimits(this, minP, maxP);
        ValidationUtil.checkRatedS(this, ratedS);

        MinMaxReactiveLimitsAttributes minMaxAttributes =
                MinMaxReactiveLimitsAttributes.builder()
                        .minQ(-Double.MAX_VALUE)
                        .maxQ(Double.MAX_VALUE)
                        .build();

        Resource<GeneratorAttributes> resource = Resource.generatorBuilder(index.getNetwork().getUuid(), index.getResourceUpdater())
                .id(id)
                .attributes(GeneratorAttributes.builder()
                        .voltageLevelId(getVoltageLevelResource().getId())
                        .name(getName())
                        .node(getNode())
                        .bus(getBus())
                        .connectableBus(getConnectableBus())
                        .energySource(energySource)
                        .maxP(maxP)
                        .minP(minP)
                        .voltageRegulatorOn(voltageRegulatorOn)
                        .targetP(targetP)
                        .targetQ(targetQ)
                        .targetV(targetV)
                        .ratedS(ratedS)
                        .reactiveLimits(minMaxAttributes)
                        .build())
                .build();
        return getIndex().createGenerator(resource);
    }

    @Override
    protected String getTypeDescription() {
        return "Generator";
    }
}

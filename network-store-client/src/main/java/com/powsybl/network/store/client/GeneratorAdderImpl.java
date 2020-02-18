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
import com.powsybl.network.store.model.GeneratorAttributes;
import com.powsybl.network.store.model.MinMaxReactiveLimitsAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class GeneratorAdderImpl implements GeneratorAdder {

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    private final NetworkObjectIndex index;

    private String id;

    private String name;

    private Integer node;

    private String bus;

    private String connectableBus;

    private EnergySource energySource = EnergySource.OTHER;

    private double minP = Double.NaN;

    private double maxP = Double.NaN;

    private Boolean voltageRegulatorOn;

    private double targetP = Double.NaN;

    private double targetQ = Double.NaN;

    private double targetV = Double.NaN;

    private double ratedS = Double.NaN;

    GeneratorAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        this.voltageLevelResource = voltageLevelResource;
        this.index = index;
    }

    @Override
    public GeneratorAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public GeneratorAdder setEnsureIdUnicity(boolean ensureIdUnicity) {
        // TODO
        return this;
    }

    @Override
    public GeneratorAdder setName(String name) {
        this.name = name;
        return this;

    }

    @Override
    public GeneratorAdder setBus(String bus) {
        this.bus = bus;
        return this;
    }

    @Override
    public GeneratorAdder setConnectableBus(String connectableBus) {
        this.connectableBus = connectableBus;
        return this;
    }

    @Override
    public GeneratorAdder setNode(int node) {
        this.node = node;
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
        MinMaxReactiveLimitsAttributes minMaxAttributes =
                MinMaxReactiveLimitsAttributes.builder()
                        .minQ(-Double.MAX_VALUE)
                        .maxQ(Double.MAX_VALUE)
                        .build();

        Resource<GeneratorAttributes> resource = Resource.generatorBuilder().id(id)
                                                                            .attributes(GeneratorAttributes.builder()
                                                                                    .voltageLevelId(voltageLevelResource.getId())
                                                                                    .name(name)
                                                                                    .node(node)
                                                                                    .bus(bus)
                                                                                    .connectableBus(connectableBus)
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
        return index.createGenerator(resource);
    }
}

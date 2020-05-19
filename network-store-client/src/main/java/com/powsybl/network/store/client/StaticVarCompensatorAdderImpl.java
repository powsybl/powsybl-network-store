/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.StaticVarCompensatorAttributes;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StaticVarCompensatorAdderImpl implements StaticVarCompensatorAdder {

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    private final NetworkObjectIndex index;

    private String id;

    private String name;

    private Integer node;

    private String bus;

    private String connectableBus;

    private double bMin;

    private double bMax;

    private double voltageSetPoint;

    private double reactivePowerSetPoint;

    StaticVarCompensator.RegulationMode regulationMode;

    StaticVarCompensatorAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        this.voltageLevelResource = voltageLevelResource;
        this.index = index;
    }

    @Override
    public StaticVarCompensatorAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setEnsureIdUnicity(boolean ensureIdUnicity) {
        // TODO
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setName(String name) {
        this.name = name;
        return this;

    }

    @Override
    public StaticVarCompensatorAdder setNode(int node) {
        this.node = node;
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setBus(String bus) {
        this.bus = bus;
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setConnectableBus(String connectableBus) {
        this.connectableBus = connectableBus;
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
    public StaticVarCompensatorAdder setVoltageSetPoint(double voltageSetPoint) {
        this.voltageSetPoint = voltageSetPoint;
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setReactivePowerSetPoint(double reactivePowerSetPoint) {
        this.reactivePowerSetPoint = reactivePowerSetPoint;
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setRegulationMode(StaticVarCompensator.RegulationMode regulationMode) {
        this.regulationMode = regulationMode;
        return this;
    }

    @Override
    public StaticVarCompensator add() {
        Resource<StaticVarCompensatorAttributes> resource = Resource.staticVarCompensatorBuilder(index.getNetwork().getUuid(), index.getStoreClient())
                .id(id)
                .attributes(StaticVarCompensatorAttributes.builder()
                        .voltageLevelId(voltageLevelResource.getId())
                        .name(name)
                        .node(node)
                        .bus(bus)
                        .connectableBus(connectableBus)
                        .bmin(bMin)
                        .bmax(bMax)
                        .voltageSetPoint(voltageSetPoint)
                        .reactivePowerSetPoint(reactivePowerSetPoint)
                        .regulationMode(regulationMode)
                        .build())
                .build();
        return index.createStaticVarCompensator(resource);
    }
}

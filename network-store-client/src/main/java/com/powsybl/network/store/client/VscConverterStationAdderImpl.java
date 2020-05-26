/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.VscConverterStationAdder;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;
import com.powsybl.network.store.model.VscConverterStationAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VscConverterStationAdderImpl implements VscConverterStationAdder {

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    private final NetworkObjectIndex index;

    private String id;

    private String name;

    private Integer node;

    private String bus;

    private String connectableBus;

    private float lossFactor = Float.NaN;

    private Boolean voltageRegulatorOn;

    private double reactivePowerSetPoint = Double.NaN;

    private double voltageSetPoint = Double.NaN;

    VscConverterStationAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        this.voltageLevelResource = voltageLevelResource;
        this.index = index;
    }

    @Override
    public VscConverterStationAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public VscConverterStationAdder setEnsureIdUnicity(boolean ensureIdUnicity) {
        // TODO
        return this;
    }

    @Override
    public VscConverterStationAdder setName(String name) {
        this.name = name;
        return this;

    }

    @Override
    public VscConverterStationAdder setNode(int node) {
        this.node = node;
        return this;
    }

    @Override
    public VscConverterStationAdder setLossFactor(float lossFactor) {
        this.lossFactor = lossFactor;
        return this;
    }

    @Override
    public VscConverterStationAdder setBus(String bus) {
        this.bus = bus;
        return this;
    }

    @Override
    public VscConverterStationAdder setConnectableBus(String connectableBus) {
        this.connectableBus = connectableBus;
        return this;
    }

    @Override
    public VscConverterStationAdder setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        this.voltageRegulatorOn = voltageRegulatorOn;
        return this;
    }

    @Override
    public VscConverterStationAdder setVoltageSetpoint(double voltageSetPoint) {
        this.voltageSetPoint = voltageSetPoint;
        return this;
    }

    @Override
    public VscConverterStationAdder setReactivePowerSetpoint(double reactivePowerSetPoint) {
        this.reactivePowerSetPoint = reactivePowerSetPoint;
        return this;
    }

    @Override
    public VscConverterStation add() {
        Resource<VscConverterStationAttributes> resource = Resource.vscConverterStationBuilder(index.getNetwork().getUuid(), index.getResourceUpdater())
                .id(id)
                .attributes(VscConverterStationAttributes.builder()
                        .voltageLevelId(voltageLevelResource.getId())
                        .name(name)
                        .node(node)
                        .bus(bus)
                        .connectableBus(connectableBus)
                        .lossFactor(lossFactor)
                        .voltageRegulatorOn(voltageRegulatorOn)
                        .voltageSetPoint(voltageSetPoint)
                        .reactivePowerSetPoint(reactivePowerSetPoint)
                        .build())
                .build();
        return index.createVscConverterStation(resource);
    }
}

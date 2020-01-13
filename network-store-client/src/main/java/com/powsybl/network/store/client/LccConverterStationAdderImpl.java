/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.LccConverterStationAdder;
import com.powsybl.network.store.model.LccConverterStationAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class LccConverterStationAdderImpl implements LccConverterStationAdder {

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    private final NetworkObjectIndex index;

    private String id;

    private String name;

    private Integer node;

    private float lossFactor = Float.NaN;

    private float powerFactor = Float.NaN;

    LccConverterStationAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        this.voltageLevelResource = voltageLevelResource;
        this.index = index;
    }

    @Override
    public LccConverterStationAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public LccConverterStationAdder setEnsureIdUnicity(boolean ensureIdUnicity) {
        // TODO
        return this;
    }

    @Override
    public LccConverterStationAdder setName(String name) {
        this.name = name;
        return this;

    }

    @Override
    public LccConverterStationAdder setNode(int node) {
        this.node = node;
        return this;
    }

    @Override
    public LccConverterStationAdder setLossFactor(float lossFactor) {
        this.lossFactor = lossFactor;
        return this;
    }

    @Override
    public LccConverterStationAdder setPowerFactor(float powerFactor) {
        this.powerFactor = powerFactor;
        return this;
    }

    @Override
    public LccConverterStationAdder setBus(String bus) {
        // TODO
        return this;
    }

    @Override
    public LccConverterStationAdder setConnectableBus(String connectableBus) {
        // TODO
        return this;
    }

    @Override
    public LccConverterStation add() {
        Resource<LccConverterStationAttributes> resource = Resource.lccConverterStationBuilder()
                .id(id)
                .attributes(LccConverterStationAttributes.builder()
                        .voltageLevelId(voltageLevelResource.getId())
                        .name(name)
                        .node(node)
                        .lossFactor(lossFactor)
                        .powerFactor(powerFactor)
                        .build())
                .build();
        return index.createLccConverterStation(resource);
    }

}

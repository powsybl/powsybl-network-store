/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VoltageLevelAdder;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.SubstationAttributes;
import com.powsybl.network.store.model.VoltageLevelAttributes;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class VoltageLevelAdderImpl implements VoltageLevelAdder {

    private final NetworkObjectIndex index;

    private final Resource<SubstationAttributes> substationResource;

    private String id;

    private String name;

    private double nominalV;

    private double lowVoltageLimit;

    private double highVoltageLimit;

    private TopologyKind topologyKind;

    VoltageLevelAdderImpl(NetworkObjectIndex index, Resource<SubstationAttributes> substationResource) {
        this.index = index;
        this.substationResource = substationResource;
    }

    @Override
    public VoltageLevelAdder setId(String id) {
        this.id = Objects.requireNonNull(id);
        return this;
    }

    @Override
    public VoltageLevelAdder setEnsureIdUnicity(boolean ensureIdUnicity) {
        // TODO
        return this;
    }

    @Override
    public VoltageLevelAdder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public VoltageLevelAdder setNominalV(double nominalV) {
        this.nominalV = nominalV;
        return this;
    }

    @Override
    public VoltageLevelAdder setLowVoltageLimit(double lowVoltageLimit) {
        this.lowVoltageLimit = lowVoltageLimit;
        return this;
    }

    @Override
    public VoltageLevelAdder setHighVoltageLimit(double highVoltageLimit) {
        this.highVoltageLimit = highVoltageLimit;
        return this;
    }

    @Override
    public VoltageLevelAdder setTopologyKind(String topologyKind) {
        this.topologyKind = TopologyKind.valueOf(Objects.requireNonNull(topologyKind));
        return this;
    }

    @Override
    public VoltageLevelAdder setTopologyKind(TopologyKind topologyKind) {
        this.topologyKind = Objects.requireNonNull(topologyKind);
        return this;
    }

    @Override
    public VoltageLevel add() {
        // TODO validation
        Resource<VoltageLevelAttributes> voltageLevelResource = Resource.voltageLevelBuilder(index.getNetwork().getUuid(), index.getResourceUpdater())
                .id(id)
                .attributes(VoltageLevelAttributes.builder()
                                                  .substationId(substationResource.getId())
                                                  .name(name)
                                                  .nominalV(nominalV)
                                                  .lowVoltageLimit(lowVoltageLimit)
                                                  .highVoltageLimit(highVoltageLimit)
                                                  .topologyKind(topologyKind)
                                                  .build())
                .build();
        return index.createVoltageLevel(voltageLevelResource);
    }
}

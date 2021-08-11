/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VoltageLevelAdder;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.SubstationAttributes;
import com.powsybl.network.store.model.VoltageLevelAttributes;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class VoltageLevelAdderImpl extends AbstractIdentifiableAdder<VoltageLevelAdderImpl> implements VoltageLevelAdder {

    private final Resource<SubstationAttributes> substationResource;

    private double nominalV = Double.NaN;

    private double lowVoltageLimit = Double.NaN;

    private double highVoltageLimit = Double.NaN;

    private TopologyKind topologyKind;

    VoltageLevelAdderImpl(NetworkObjectIndex index, Resource<SubstationAttributes> substationResource) {
        super(index);
        this.substationResource = substationResource;
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
        String id = checkAndGetUniqueId();
        ValidationUtil.checkNominalV(this, nominalV);
        ValidationUtil.checkVoltageLimits(this, lowVoltageLimit, highVoltageLimit);
        ValidationUtil.checkTopologyKind(this, topologyKind);

        Resource<VoltageLevelAttributes> voltageLevelResource = Resource.voltageLevelBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .attributes(VoltageLevelAttributes.builder()
                                                  .substationId(substationResource.getId())
                                                  .name(getName())
                                                  .fictitious(isFictitious())
                                                  .nominalV(nominalV)
                                                  .lowVoltageLimit(lowVoltageLimit)
                                                  .highVoltageLimit(highVoltageLimit)
                                                  .topologyKind(topologyKind)
                                                  .build())
                .build();
        return getIndex().createVoltageLevel(voltageLevelResource);
    }

    @Override
    protected String getTypeDescription() {
        return "Voltage level";
    }
}

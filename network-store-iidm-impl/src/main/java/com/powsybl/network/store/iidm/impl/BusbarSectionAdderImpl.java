/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.BusbarSectionAdder;
import com.powsybl.network.store.model.BusbarSectionAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class BusbarSectionAdderImpl extends AbstractIdentifiableAdder<BusbarSectionAdderImpl> implements BusbarSectionAdder {

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    private Integer node;

    BusbarSectionAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(index);
        this.voltageLevelResource = voltageLevelResource;
    }

    @Override
    public BusbarSectionAdder setNode(int node) {
        this.node = node;
        return this;
    }

    @Override
    public BusbarSection add() {
        String id = checkAndGetUniqueId();

        Resource<BusbarSectionAttributes> resource = Resource.busbarSectionBuilder(index.getNetwork().getUuid(), index.getResourceUpdater())
                .id(id)
                .attributes(BusbarSectionAttributes.builder()
                                                   .voltageLevelId(voltageLevelResource.getId())
                                                   .name(getName())
                                                   .fictitious(isFictitious())
                                                   .node(node)
                                                   .build())
                .build();
        return getIndex().createBusbarSection(resource);
    }

    @Override
    protected String getTypeDescription() {
        return "Busbar section";
    }
}

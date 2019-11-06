/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.BusbarSectionAdder;
import com.powsybl.network.store.model.BusbarSectionAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class BusbarSectionAdderImpl implements BusbarSectionAdder {

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    private final NetworkObjectIndex index;

    private String id;

    private String name;

    private Integer node;

    BusbarSectionAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        this.voltageLevelResource = voltageLevelResource;
        this.index = index;
    }

    @Override
    public BusbarSectionAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public BusbarSectionAdder setEnsureIdUnicity(boolean ensureIdUnicity) {
        // TODO
        return this;
    }

    @Override
    public BusbarSectionAdder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public BusbarSectionAdder setNode(int node) {
        this.node = node;
        return this;
    }

    @Override
    public BusbarSection add() {
        Resource<BusbarSectionAttributes> resource = Resource.busbarSectionBuilder()
                .id(id)
                .attributes(BusbarSectionAttributes.builder()
                                                   .voltageLevelId(voltageLevelResource.getId())
                                                   .name(name)
                                                   .node(node)
                                                   .build())
                .build();
        return index.createBusbarSection(resource);
    }
}

/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.network.store.model.InternalConnectionAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class InternalConnectionAdderNodeBreakerImpl implements VoltageLevel.NodeBreakerView.InternalConnectionAdder {

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    private Integer node1;

    private Integer node2;

    InternalConnectionAdderNodeBreakerImpl(Resource<VoltageLevelAttributes> voltageLevelResource) {
        this.voltageLevelResource = voltageLevelResource;
    }

    @Override
    public VoltageLevel.NodeBreakerView.InternalConnectionAdder setNode1(int node1) {
        this.node1 = node1;
        return this;
    }

    @Override
    public VoltageLevel.NodeBreakerView.InternalConnectionAdder setNode2(int node2) {
        this.node2 = node2;
        return this;
    }

    @Override
    public void add() {
        InternalConnectionAttributes icAttributes = InternalConnectionAttributes.builder()
                        .node1(node1)
                        .node2(node2)
                        .build();
        voltageLevelResource.getAttributes().getInternalConnections().add(icAttributes);
    }

}

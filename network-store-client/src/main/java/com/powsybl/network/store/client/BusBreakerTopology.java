/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.model.*;

import java.util.List;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BusBreakerTopology extends AbstractTopology<String> {

    public <U extends InjectionAttributes> String getInjectionNode(Resource<U> resource) {
        return resource.getAttributes().getBus();
    }

    @Override
    protected <U extends BranchAttributes> String getBranchNode1(Resource<U> resource) {
        return resource.getAttributes().getBus1();
    }

    @Override
    protected <U extends BranchAttributes> String getBranchNode2(Resource<U> resource) {
        return resource.getAttributes().getBus2();
    }

    @Override
    protected <U extends ThreeWindingsTransformerAttributes> String get3wtNode1(Resource<U> resource) {
        return resource.getAttributes().getLeg1().getBus();
    }

    @Override
    protected <U extends ThreeWindingsTransformerAttributes> String get3wtNode2(Resource<U> resource) {
        return resource.getAttributes().getLeg2().getBus();
    }

    @Override
    protected <U extends ThreeWindingsTransformerAttributes> String get3wtNode3(Resource<U> resource) {
        return resource.getAttributes().getLeg3().getBus();
    }

    @Override
    protected <U extends SwitchAttributes> String getSwitchNode1(Resource<U> resource) {
        return resource.getAttributes().getBus1();
    }

    @Override
    protected <U extends SwitchAttributes> String getSwitchNode2(Resource<U> resource) {
        return resource.getAttributes().getBus2();
    }

    @Override
    protected CalculateBus<String> createBus(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource,
                                             List<Vertex<String>> vertices, Set<String> nodes) {
        String min = nodes.stream().min(String::compareTo).orElseThrow(IllegalStateException::new);
        String busId = min + "_merge";
        return new CalculateBus<>(index, voltageLevelResource.getId(), busId, busId, vertices);
    }
}

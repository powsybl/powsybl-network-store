/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.network.store.model.*;
import org.jgrapht.UndirectedGraph;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeBreakerTopology extends AbstractTopology<Integer> {

    @Override
    public <U extends InjectionAttributes> Integer getInjectionNodeOrBus(Resource<U> resource) {
        return resource.getAttributes().getNode();
    }

    @Override
    protected <U extends BranchAttributes> Integer getBranchNodeOrBus1(Resource<U> resource) {
        return resource.getAttributes().getNode1();
    }

    @Override
    protected <U extends BranchAttributes> Integer getBranchNodeOrBus2(Resource<U> resource) {
        return resource.getAttributes().getNode2();
    }

    @Override
    protected <U extends ThreeWindingsTransformerAttributes> Integer get3wtNodeOrBus1(Resource<U> resource) {
        return resource.getAttributes().getLeg1().getNode();
    }

    @Override
    protected <U extends ThreeWindingsTransformerAttributes> Integer get3wtNodeOrBus2(Resource<U> resource) {
        return resource.getAttributes().getLeg2().getNode();
    }

    @Override
    protected <U extends ThreeWindingsTransformerAttributes> Integer get3wtNodeOrBus3(Resource<U> resource) {
        return resource.getAttributes().getLeg3().getNode();
    }

    @Override
    protected <U extends SwitchAttributes> Integer getSwitchNodeOrBus1(Resource<U> resource) {
        return resource.getAttributes().getNode1();
    }

    @Override
    protected <U extends SwitchAttributes> Integer getSwitchNodeOrBus2(Resource<U> resource) {
        return resource.getAttributes().getNode2();
    }

    @Override
    protected CalculatedBus<Integer> createCalculatedBus(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource,
                                                         List<Vertex<Integer>> vertices) {
        // to have a unique and stable calculated bus id, we use voltage level id as a base id plus the minimum node
        int firstNode = vertices.stream().map(Vertex::getNodeOrBus).min(Integer::compare).orElseThrow(IllegalStateException::new);
        String busId = voltageLevelResource.getId() + "_" + firstNode;
        String busName = voltageLevelResource.getAttributes().getName() != null ? voltageLevelResource.getAttributes().getName() + "_" + firstNode : null;
        return new CalculatedBus<>(index, voltageLevelResource.getId(), busId, busName, vertices);
    }

    @Override
    protected void buildGraph(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource,
                              UndirectedGraph<Integer, Resource<SwitchAttributes>> graph, List<Vertex<Integer>> vertices) {
        UUID networkUuid = index.getNetwork().getUuid();

        // in addition to injections, branches and 3 windings transformers, in a node/breaker topology we also
        // have busbar sections and internal connections

        vertices.addAll(index.getStoreClient().getVoltageLevelBusbarSections(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(resource -> new Vertex<>(resource.getId(), ConnectableType.BUSBAR_SECTION, resource.getAttributes().getNode(), null))
                .collect(Collectors.toList()));

        super.buildGraph(index, voltageLevelResource, graph, vertices);

        for (InternalConnectionAttributes attributes : voltageLevelResource.getAttributes().getInternalConnections()) {
            ensureNodeOrBusExists(graph, attributes.getNode1());
            ensureNodeOrBusExists(graph, attributes.getNode2());
            graph.addEdge(attributes.getNode1(), attributes.getNode2(), null);
        }
    }
}

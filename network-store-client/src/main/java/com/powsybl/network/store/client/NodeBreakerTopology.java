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

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeBreakerTopology extends AbstractTopology<Integer> {

    public <U extends InjectionAttributes> Integer getInjectionNode(Resource<U> resource) {
        return resource.getAttributes().getNode();
    }

    private static Vertex<Integer> createVertexFromBusbarSection(Resource<BusbarSectionAttributes> resource) {
        return new Vertex<>(resource.getId(), ConnectableType.BUSBAR_SECTION, resource.getAttributes().getNode(), null);
    }

    @Override
    protected <U extends BranchAttributes> Integer getBranchNode1(Resource<U> resource) {
        return resource.getAttributes().getNode1();
    }

    @Override
    protected <U extends BranchAttributes> Integer getBranchNode2(Resource<U> resource) {
        return resource.getAttributes().getNode2();
    }

    @Override
    protected <U extends ThreeWindingsTransformerAttributes> Integer get3wtNode1(Resource<U> resource) {
        return resource.getAttributes().getLeg1().getNode();
    }

    @Override
    protected <U extends ThreeWindingsTransformerAttributes> Integer get3wtNode2(Resource<U> resource) {
        return resource.getAttributes().getLeg2().getNode();
    }

    @Override
    protected <U extends ThreeWindingsTransformerAttributes> Integer get3wtNode3(Resource<U> resource) {
        return resource.getAttributes().getLeg3().getNode();
    }

    @Override
    protected <U extends SwitchAttributes> Integer getSwitchNode1(Resource<U> resource) {
        return resource.getAttributes().getNode1();
    }

    @Override
    protected <U extends SwitchAttributes> Integer getSwitchNode2(Resource<U> resource) {
        return resource.getAttributes().getNode2();
    }

    @Override
    protected CalculateBus<Integer> createBus(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource,
                                     List<Vertex<Integer>> vertices, Set<Integer> nodes) {
        int min = nodes.stream().min(Integer::compare).orElseThrow(IllegalStateException::new);
        String busId = voltageLevelResource.getId() + "_" + min;
        String busName = voltageLevelResource.getAttributes().getName() != null ? voltageLevelResource.getAttributes().getName() + "_" + min : null;
        return new CalculateBus<>(index, voltageLevelResource.getId(), busId, busName, vertices);
    }

    @Override
    protected void buildGraph(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource,
                           UndirectedGraph<Integer, Resource<SwitchAttributes>> graph, List<Vertex<Integer>> vertices) {
        UUID networkUuid = index.getNetwork().getUuid();

        vertices.addAll(index.getStoreClient().getVoltageLevelBusbarSections(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(NodeBreakerTopology::createVertexFromBusbarSection)
                .collect(Collectors.toList()));

        super.buildGraph(index, voltageLevelResource, graph, vertices);

        for (InternalConnectionAttributes attributes : voltageLevelResource.getAttributes().getInternalConnections()) {
            ensureNodeExists(graph, attributes.getNode1());
            ensureNodeExists(graph, attributes.getNode2());
            graph.addEdge(attributes.getNode1(), attributes.getNode2(), null);
        }
    }
}

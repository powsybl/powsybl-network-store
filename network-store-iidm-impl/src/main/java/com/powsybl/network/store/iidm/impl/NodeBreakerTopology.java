/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.network.store.model.*;
import org.jgrapht.Graph;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeBreakerTopology extends AbstractTopology<Integer> {

    public static final NodeBreakerTopology INSTANCE = new NodeBreakerTopology();

    @Override
    protected Integer getNodeOrBus(Vertex vertex) {
        return vertex.getNode();
    }

    @Override
    protected Vertex createVertex(String id, IdentifiableType connectableType, Integer nodeOrBus, String side) {
        return new Vertex(id, connectableType, nodeOrBus, null, side);
    }

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
    protected void setNodeOrBusToCalculatedBusNum(Resource<VoltageLevelAttributes> voltageLevelResource, Map<Integer, Integer> nodeOrBusToCalculatedBusNum, boolean isBusView) {
        if (isBusView) {
            voltageLevelResource.getAttributes().setNodeToCalculatedBusForBusView(nodeOrBusToCalculatedBusNum);
        } else {
            voltageLevelResource.getAttributes().setNodeToCalculatedBusForBusBreakerView(nodeOrBusToCalculatedBusNum);
        }
    }

    @Override
    protected Map<Integer, Integer> getNodeOrBusToCalculatedBusNum(Resource<VoltageLevelAttributes> voltageLevelResource, boolean isBusView) {
        return isBusView ?
                voltageLevelResource.getAttributes().getNodeToCalculatedBusForBusView() :
                voltageLevelResource.getAttributes().getNodeToCalculatedBusForBusBreakerView();
    }

    @Override
    protected boolean isCalculatedBusValid(Set<Integer> nodesOrBusesConnected, Map<Integer, List<Vertex>> verticesByNodeOrBus, boolean isBusView) {
        EquipmentCount<Integer> equipmentCount = new EquipmentCount<>();
        equipmentCount.count(nodesOrBusesConnected, verticesByNodeOrBus);
        return !isBusView ? !nodesOrBusesConnected.isEmpty() :
                equipmentCount.busbarSectionCount >= 1 && equipmentCount.feederCount >= 1
                        || equipmentCount.branchCount >= 1 && equipmentCount.feederCount >= 2;
    }

    @Override
    protected void buildVertices(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource, List<Vertex> vertices) {
        super.buildVertices(index, voltageLevelResource, vertices);

        UUID networkUuid = index.getNetwork().getUuid();

        // in addition to injections, branches and 3 windings transformers, in a node/breaker topology we also
        // have busbar sections and internal connections

        vertices.addAll(index.getStoreClient().getVoltageLevelBusbarSections(networkUuid, index.getWorkingVariantNum(), voltageLevelResource.getId())
                .stream()
                .map(resource -> createVertex(resource.getId(), IdentifiableType.BUSBAR_SECTION, resource.getAttributes().getNode(), null))
                .collect(Collectors.toList()));
    }

    @Override
    protected void buildEdges(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource, boolean includeOpenSwitches, boolean isBusView,
                              Graph<Integer, Edge> graph) {
        super.buildEdges(index, voltageLevelResource, includeOpenSwitches, isBusView, graph);

        for (InternalConnectionAttributes attributes : voltageLevelResource.getAttributes().getInternalConnections()) {
            ensureNodeOrBusExists(graph, attributes.getNode1());
            ensureNodeOrBusExists(graph, attributes.getNode2());
            graph.addEdge(attributes.getNode1(), attributes.getNode2(), new Edge(attributes));
        }
    }

    @Override
    protected CalculatedBus createCalculatedBus(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource, int calculatedBusNum, boolean isBusView) {
        // to have a unique and stable calculated bus id, we use voltage level id as a base id plus the minimum node
        Map<Integer, Integer> nodeToCalculatedBus = isBusView ? voltageLevelResource.getAttributes().getNodeToCalculatedBusForBusView() : voltageLevelResource.getAttributes().getNodeToCalculatedBusForBusBreakerView();
        int firstNode = nodeToCalculatedBus.entrySet().stream().filter(e -> e.getValue() == calculatedBusNum).map(Map.Entry::getKey).min(Integer::compare).orElseThrow(IllegalStateException::new);
        String busId = voltageLevelResource.getId() + calculatedBusSeparator + firstNode;
        String busName = voltageLevelResource.getAttributes().getName() != null ? voltageLevelResource.getAttributes().getName() + calculatedBusSeparator + firstNode : null;
        return new CalculatedBus(index, voltageLevelResource.getId(), busId, busName, voltageLevelResource, calculatedBusNum, isBusView);
    }
}

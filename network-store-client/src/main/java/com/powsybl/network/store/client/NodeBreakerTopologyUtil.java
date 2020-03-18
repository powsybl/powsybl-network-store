/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.math.graph.TraverseResult;
import com.powsybl.math.graph.UndirectedGraph;
import com.powsybl.math.graph.UndirectedGraphImpl;
import com.powsybl.network.store.model.*;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class NodeBreakerTopologyUtil {

    public static final Predicate<Resource<SwitchAttributes>> BUS_VIEW_TERMINATE = resource -> resource.getAttributes().isOpen();

    public static final BiPredicate<UndirectedGraph<Vertex, Resource<SwitchAttributes>>, TIntArrayList> BUS_VIEW_BUS_VALIDATOR = (graph, nodes) -> {
        int feederCount = 0;
        int branchCount = 0;
        int busbarSectionCount = 0;
        for (int i = 0; i < nodes.size(); i++) {
            int node = nodes.get(i);
            Vertex vertex = graph.getVertexObject(node);
            if (vertex != null) {
                switch (vertex.getConnectableType()) {
                    case LINE:
                    case TWO_WINDINGS_TRANSFORMER:
                    case THREE_WINDINGS_TRANSFORMER:
                    case HVDC_CONVERTER_STATION:
                        branchCount++;
                        feederCount++;
                        break;

                    case LOAD:
                    case GENERATOR:
                    case BATTERY:
                    case SHUNT_COMPENSATOR:
                    case DANGLING_LINE:
                    case STATIC_VAR_COMPENSATOR:
                        feederCount++;
                        break;

                    case BUSBAR_SECTION:
                        busbarSectionCount++;
                        break;

                    default:
                        throw new IllegalStateException();
                }
            }
        }
        return (busbarSectionCount >= 1 && feederCount >= 1)
                || (branchCount >= 1 && feederCount >= 2);
    };

    private NodeBreakerTopologyUtil() {
    }

    private static Vertex applyBusbarSection(Resource<BusbarSectionAttributes> resource) {
        return new Vertex(resource.getId(), ConnectableType.BUSBAR_SECTION, resource.getAttributes().getNode(), null);
    }

    private static <U extends InjectionAttributes> Vertex applyInjection(Resource<U> resource) {
        ConnectableType connectableType;
        switch (resource.getType()) {
            case LOAD:
                connectableType = ConnectableType.LOAD;
                break;
            case GENERATOR:
                connectableType = ConnectableType.GENERATOR;
                break;
            case SHUNT_COMPENSATOR:
                connectableType = ConnectableType.SHUNT_COMPENSATOR;
                break;
            case VSC_CONVERTER_STATION:
            case LCC_CONVERTER_STATION:
                connectableType = ConnectableType.HVDC_CONVERTER_STATION;
                break;
            case STATIC_VAR_COMPENSATOR:
                connectableType = ConnectableType.STATIC_VAR_COMPENSATOR;
                break;
            case DANGLING_LINE:
                connectableType = ConnectableType.DANGLING_LINE;
                break;
            default:
                throw new IllegalStateException("Resource is not an injection: " + resource.getType());
        }
        return new Vertex(resource.getId(), connectableType, resource.getAttributes().getNode(), null);
    }

    private static <U extends BranchAttributes> Vertex applyBranch(Resource<U> resource, Resource<VoltageLevelAttributes> voltageLevelResource) {
        ConnectableType connectableType;
        switch (resource.getType()) {
            case LINE:
                connectableType = ConnectableType.LINE;
                break;
            case TWO_WINDINGS_TRANSFORMER:
                connectableType = ConnectableType.TWO_WINDINGS_TRANSFORMER;
                break;
            default:
                throw new IllegalStateException("Resource is not a branch: " + resource.getType());
        }
        int node;
        Branch.Side side;
        if (voltageLevelResource.getId().equals(resource.getAttributes().getVoltageLevelId1())) {
            node = resource.getAttributes().getNode1();
            side = Branch.Side.ONE;
        } else {
            node = resource.getAttributes().getNode2();
            side = Branch.Side.TWO;
        }
        return new Vertex(resource.getId(), connectableType, node, side.name());
    }

    private static Vertex apply3wt(Resource<ThreeWindingsTransformerAttributes> resource, Resource<VoltageLevelAttributes> voltageLevelResource) {
        int node;
        ThreeWindingsTransformer.Side side;
        if (voltageLevelResource.getId().equals(resource.getAttributes().getLeg1().getVoltageLevelId())) {
            node = resource.getAttributes().getLeg1().getNode();
            side = ThreeWindingsTransformer.Side.ONE;
        } else if (voltageLevelResource.getId().equals(resource.getAttributes().getLeg2().getVoltageLevelId())) {
            node = resource.getAttributes().getLeg2().getNode();
            side = ThreeWindingsTransformer.Side.TWO;
        } else {
            node = resource.getAttributes().getLeg3().getNode();
            side = ThreeWindingsTransformer.Side.THREE;
        }
        return new Vertex(resource.getId(), ConnectableType.THREE_WINDINGS_TRANSFORMER, node, side.name());
    }

    private static void ensureNodeExists(UndirectedGraph<Vertex, Resource<SwitchAttributes>> graph, int node) {
        for (int i = graph.getVertexCount(); i <= node; i++) {
            graph.addVertex();
        }
    }

    public static UndirectedGraph<Vertex, Resource<SwitchAttributes>> buildNodeBreakerGraph(NetworkObjectIndex index,
                                                                                            Resource<VoltageLevelAttributes> voltageLevelResource) {
        List<Vertex> vertices = new ArrayList<>();
        UUID networkUuid = index.getNetwork().getUuid();
        vertices.addAll(index.getStoreClient().getVoltageLevelBusbarSections(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(NodeBreakerTopologyUtil::applyBusbarSection)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelGenerators(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(NodeBreakerTopologyUtil::applyInjection)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelLoads(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(NodeBreakerTopologyUtil::applyInjection)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelShuntCompensators(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(NodeBreakerTopologyUtil::applyInjection)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelStaticVarCompensators(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(NodeBreakerTopologyUtil::applyInjection)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelVscConverterStation(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(NodeBreakerTopologyUtil::applyInjection)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelLccConverterStation(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(NodeBreakerTopologyUtil::applyInjection)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelDanglingLines(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(NodeBreakerTopologyUtil::applyInjection)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelLines(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(r -> applyBranch(r, voltageLevelResource))
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelTwoWindingsTransformers(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(r -> applyBranch(r, voltageLevelResource))
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelThreeWindingsTransformers(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(r -> apply3wt(r, voltageLevelResource))
                .collect(Collectors.toList()));

        UndirectedGraph<Vertex, Resource<SwitchAttributes>> graph = new UndirectedGraphImpl<>();
        for (Vertex v : vertices) {
            ensureNodeExists(graph, v.getNode());
            graph.setVertexObject(v.getNode(), v);
        }

        for (Resource<SwitchAttributes> resource : index.getStoreClient().getVoltageLevelSwitches(networkUuid, voltageLevelResource.getId())) {
            ensureNodeExists(graph, resource.getAttributes().getNode1());
            ensureNodeExists(graph, resource.getAttributes().getNode2());
            graph.addEdge(resource.getAttributes().getNode1(), resource.getAttributes().getNode2(), resource);
        }
        for (InternalConnectionAttributes attributes : voltageLevelResource.getAttributes().getInternalConnections()) {
            ensureNodeExists(graph, attributes.getNode1());
            ensureNodeExists(graph, attributes.getNode2());
            graph.addEdge(attributes.getNode1(), attributes.getNode2(), null);
        }

        return graph;
    }

    public static void traverse(NetworkObjectIndex index,
                                Resource<VoltageLevelAttributes> voltageLevelResource,
                                UndirectedGraph<Vertex, Resource<SwitchAttributes>> graph, int n,
                                Predicate<Resource<SwitchAttributes>> terminate,
                                BiPredicate<UndirectedGraph<Vertex, Resource<SwitchAttributes>>, TIntArrayList> busValidator,
                                boolean[] encountered, Map<String, Bus> calculateBuses) {
        if (!encountered[n]) {
            TIntArrayList nodes = new TIntArrayList(1);
            List<Vertex> vertices = new ArrayList<>();
            nodes.add(n);
            graph.traverse(n, (n1, e, n2) -> {
                Resource<SwitchAttributes> switchResource = graph.getEdgeObject(e);
                if (switchResource != null && terminate.test(switchResource)) {
                    return TraverseResult.TERMINATE;
                }
                nodes.add(n2);
                Vertex vertex = graph.getVertexObject(n2);
                if (vertex != null) {
                    vertices.add(vertex);
                }
                return TraverseResult.CONTINUE;
            }, encountered);

            // check that the component is a bus

            if (busValidator.test(graph, nodes)) {
                CalculateBus calculateBus = CalculateBus.create(index, voltageLevelResource, vertices);
                calculateBuses.put(calculateBus.getId(), calculateBus);
            }
        }
    }

    public static void traverseBusView(NetworkObjectIndex index,
                                       Resource<VoltageLevelAttributes> voltageLevelResource,
                                       UndirectedGraph<Vertex, Resource<SwitchAttributes>> graph, int n,
                                       boolean[] encountered, Map<String, Bus> calculateBuses) {
        traverse(index, voltageLevelResource, graph, n, BUS_VIEW_TERMINATE, BUS_VIEW_BUS_VALIDATOR, encountered, calculateBuses);
    }
}

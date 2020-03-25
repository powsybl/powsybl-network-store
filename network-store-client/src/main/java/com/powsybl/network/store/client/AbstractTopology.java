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
import com.powsybl.network.store.model.*;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractTopology<T> {

    private final BiPredicate<Map<T, Vertex<T>>, Set<T>> busViewBusValidator = (vertices, nodes) -> {
        int feederCount = 0;
        int branchCount = 0;
        int busbarSectionCount = 0;
        for (T node : nodes) {
            Vertex<T> vertex = vertices.get(node);
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

    protected abstract <U extends InjectionAttributes> T getInjectionNode(Resource<U> resource);

    private <U extends InjectionAttributes> Vertex<T> createVertexFromInjection(Resource<U> resource) {
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
        T node = getInjectionNode(resource);
        return node == null ? null : new Vertex<>(resource.getId(), connectableType, node, null);
    }

    protected abstract <U extends BranchAttributes> T getBranchNode1(Resource<U> resource);

    protected abstract <U extends BranchAttributes> T getBranchNode2(Resource<U> resource);

    private <U extends BranchAttributes> Vertex<T> createVertextFromBranch(Resource<U> resource, Resource<VoltageLevelAttributes> voltageLevelResource) {
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
        T node;
        Branch.Side side;
        if (voltageLevelResource.getId().equals(resource.getAttributes().getVoltageLevelId1())) {
            node = getBranchNode1(resource);
            side = Branch.Side.ONE;
        } else {
            node = getBranchNode2(resource);
            side = Branch.Side.TWO;
        }
        return node == null ? null : new Vertex<>(resource.getId(), connectableType, node, side.name());
    }

    protected abstract <U extends ThreeWindingsTransformerAttributes> T get3wtNode1(Resource<U> resource);

    protected abstract <U extends ThreeWindingsTransformerAttributes> T get3wtNode2(Resource<U> resource);

    protected abstract <U extends ThreeWindingsTransformerAttributes> T get3wtNode3(Resource<U> resource);

    private Vertex<T> apply3wt(Resource<ThreeWindingsTransformerAttributes> resource, Resource<VoltageLevelAttributes> voltageLevelResource) {
        T node;
        ThreeWindingsTransformer.Side side;
        if (voltageLevelResource.getId().equals(resource.getAttributes().getLeg1().getVoltageLevelId())) {
            node = get3wtNode1(resource);
            side = ThreeWindingsTransformer.Side.ONE;
        } else if (voltageLevelResource.getId().equals(resource.getAttributes().getLeg2().getVoltageLevelId())) {
            node = get3wtNode2(resource);
            side = ThreeWindingsTransformer.Side.TWO;
        } else {
            node = get3wtNode3(resource);
            side = ThreeWindingsTransformer.Side.THREE;
        }
        return node == null ? null : new Vertex<>(resource.getId(), ConnectableType.THREE_WINDINGS_TRANSFORMER, node, side.name());
    }

    protected void ensureNodeExists(UndirectedGraph<T, Resource<SwitchAttributes>> graph, T node) {
        if (!graph.containsVertex(node)) {
            graph.addVertex(node);
        }
    }

    public UndirectedGraph<T, Resource<SwitchAttributes>>  buildGraph(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource,
                                                                      Map<T, Vertex<T>> vertexMap) {
        UndirectedGraph<T, Resource<SwitchAttributes>> graph = new Pseudograph<>((i, v1) -> { throw new IllegalStateException(); });
        List<Vertex<T>> vertices = new ArrayList<>();
        buildGraph(index, voltageLevelResource, graph, vertices);
        vertexMap.putAll(vertices.stream().collect(Collectors.toMap(Vertex::getNode, v -> v)));
        return graph;
    }

    protected abstract <U extends SwitchAttributes> T getSwitchNode1(Resource<U> resource);

    protected abstract <U extends SwitchAttributes> T getSwitchNode2(Resource<U> resource);

    protected void buildGraph(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource,
                           UndirectedGraph<T, Resource<SwitchAttributes>> graph, List<Vertex<T>> vertices) {
        UUID networkUuid = index.getNetwork().getUuid();
        vertices.addAll(index.getStoreClient().getVoltageLevelGenerators(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(this::createVertexFromInjection)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelLoads(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(this::createVertexFromInjection)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelShuntCompensators(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(this::createVertexFromInjection)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelStaticVarCompensators(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(this::createVertexFromInjection)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelVscConverterStation(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(this::createVertexFromInjection)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelLccConverterStation(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(this::createVertexFromInjection)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelDanglingLines(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(this::createVertexFromInjection)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelLines(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(resource -> createVertextFromBranch(resource, voltageLevelResource))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelTwoWindingsTransformers(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(resource -> createVertextFromBranch(resource, voltageLevelResource))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelThreeWindingsTransformers(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(resource -> apply3wt(resource, voltageLevelResource))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        for (Vertex<T> v : vertices) {
            graph.addVertex(v.getNode());
        }

        for (Resource<SwitchAttributes> resource : index.getStoreClient().getVoltageLevelSwitches(networkUuid, voltageLevelResource.getId())) {
            if (!resource.getAttributes().isOpen()) {
                T node1 = getSwitchNode1(resource);
                T node2 = getSwitchNode2(resource);
                ensureNodeExists(graph, node1);
                ensureNodeExists(graph, node2);
                graph.addEdge(node1, node2, resource);
            }
        }
    }

    protected abstract CalculateBus<T> createBus(NetworkObjectIndex index,
                                              Resource<VoltageLevelAttributes> voltageLevelResource,
                                              List<Vertex<T>> vertices,
                                              Set<T> nodes);

    private Map<String, Bus> calculateBus(NetworkObjectIndex index,
                                          Resource<VoltageLevelAttributes> voltageLevelResource,
                                          UndirectedGraph<T, Resource<SwitchAttributes>> graph,
                                          Map<T, Vertex<T>> vertices,
                                          BiPredicate<Map<T, Vertex<T>>, Set<T>> busValidator) {
        Map<String, Bus> calculateBuses = new HashMap<>();
        for (Set<T> nodes : new ConnectivityInspector<>(graph).connectedSets()) {
            // check that the component is a bus
            if (busValidator.test(vertices, nodes)) {
                CalculateBus<T> calculateBus = createBus(index, voltageLevelResource, new ArrayList<>(vertices.values()), nodes);
                calculateBuses.put(calculateBus.getId(), calculateBus);
            }
        }
        return calculateBuses;
    }

    private CalculateBus<T> calculateBus(NetworkObjectIndex index,
                                         Resource<VoltageLevelAttributes> voltageLevelResource,
                                         UndirectedGraph<T, Resource<SwitchAttributes>> graph,
                                         Map<T, Vertex<T>> vertices,
                                         BiPredicate<Map<T, Vertex<T>>, Set<T>> busValidator,
                                         T vertex) {
        Set<T> nodes = new ConnectivityInspector<>(graph).connectedSetOf(vertex);

        // check that the component is a bus
        if (busValidator.test(vertices, nodes)) {
            return createBus(index, voltageLevelResource, new ArrayList<>(vertices.values()), nodes);
        }
        return null;
    }

    public Map<String, Bus> calculateBuses(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource) {
        Map<T, Vertex<T>> vertices = new HashMap<>();
        UndirectedGraph<T, Resource<SwitchAttributes>> graph = buildGraph(index, voltageLevelResource, vertices);
        return calculateBus(index, voltageLevelResource, graph, vertices, busViewBusValidator);
    }

    public CalculateBus<T> calculateBus(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource, T vertex) {
        Map<T, Vertex<T>> vertices = new HashMap<>();
        UndirectedGraph<T, Resource<SwitchAttributes>> graph = buildGraph(index, voltageLevelResource, vertices);
        return calculateBus(index, voltageLevelResource, graph, vertices, busViewBusValidator, vertex);
    }
}

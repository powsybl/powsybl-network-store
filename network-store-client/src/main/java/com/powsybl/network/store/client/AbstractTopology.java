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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractTopology<T> {

    protected abstract T getNodeOrBus(Vertex vertex);

    protected abstract Vertex createVertex(String id, ConnectableType connectableType, T nodeOrBus, String side);

    private static class EquipmentCount {
        int feederCount = 0;
        int branchCount = 0;
        int busbarSectionCount = 0;
    }

    private static void countEquipments(EquipmentCount equipmentCount, Vertex vertex) {
        switch (vertex.getConnectableType()) {
            case LINE:
            case TWO_WINDINGS_TRANSFORMER:
            case THREE_WINDINGS_TRANSFORMER:
            case HVDC_CONVERTER_STATION:
                equipmentCount.branchCount++;
                equipmentCount.feederCount++;
                break;

            case LOAD:
            case GENERATOR:
            case BATTERY:
            case SHUNT_COMPENSATOR:
            case DANGLING_LINE:
            case STATIC_VAR_COMPENSATOR:
                equipmentCount.feederCount++;
                break;

            case BUSBAR_SECTION:
                equipmentCount.busbarSectionCount++;
                break;

            default:
                throw new IllegalStateException();
        }
    }

    private static <E> boolean busViewBusValidator(Map<E, List<Vertex>>verticesByNodeOrBus, Set<E> nodesOrBuses) {
        EquipmentCount equipmentCount = new EquipmentCount();
        for (E nodeOrBus : nodesOrBuses) {
            List<Vertex> vertices = verticesByNodeOrBus.get(nodeOrBus);
            if (vertices != null) {
                for (Vertex vertex : vertices) {
                    if (vertex != null) {
                        countEquipments(equipmentCount, vertex);
                    }
                }
            }
        }
        return (equipmentCount.busbarSectionCount >= 1 && equipmentCount.feederCount >= 1)
                || (equipmentCount.branchCount >= 1 && equipmentCount.feederCount >= 2);
    }

    protected abstract <U extends InjectionAttributes> T getInjectionNodeOrBus(Resource<U> resource);

    private <U extends InjectionAttributes> Vertex createVertexFromInjection(Resource<U> resource) {
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
        T nodeOrBus = getInjectionNodeOrBus(resource);
        return nodeOrBus == null ? null : createVertex(resource.getId(), connectableType, nodeOrBus, null);
    }

    protected abstract <U extends BranchAttributes> T getBranchNodeOrBus1(Resource<U> resource);

    protected abstract <U extends BranchAttributes> T getBranchNodeOrBus2(Resource<U> resource);

    private <U extends BranchAttributes> Vertex createVertextFromBranch(Resource<U> resource, Resource<VoltageLevelAttributes> voltageLevelResource) {
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
        T nodeOrBus;
        Branch.Side side;
        if (voltageLevelResource.getId().equals(resource.getAttributes().getVoltageLevelId1())) {
            nodeOrBus = getBranchNodeOrBus1(resource);
            side = Branch.Side.ONE;
        } else {
            nodeOrBus = getBranchNodeOrBus2(resource);
            side = Branch.Side.TWO;
        }
        return nodeOrBus == null ? null : createVertex(resource.getId(), connectableType, nodeOrBus, side.name());
    }

    protected abstract <U extends ThreeWindingsTransformerAttributes> T get3wtNodeOrBus1(Resource<U> resource);

    protected abstract <U extends ThreeWindingsTransformerAttributes> T get3wtNodeOrBus2(Resource<U> resource);

    protected abstract <U extends ThreeWindingsTransformerAttributes> T get3wtNodeOrBus3(Resource<U> resource);

    private Vertex createVertexFrom3wt(Resource<ThreeWindingsTransformerAttributes> resource, Resource<VoltageLevelAttributes> voltageLevelResource) {
        T nodeOrBus;
        ThreeWindingsTransformer.Side side;
        if (voltageLevelResource.getId().equals(resource.getAttributes().getLeg1().getVoltageLevelId())) {
            nodeOrBus = get3wtNodeOrBus1(resource);
            side = ThreeWindingsTransformer.Side.ONE;
        } else if (voltageLevelResource.getId().equals(resource.getAttributes().getLeg2().getVoltageLevelId())) {
            nodeOrBus = get3wtNodeOrBus2(resource);
            side = ThreeWindingsTransformer.Side.TWO;
        } else {
            nodeOrBus = get3wtNodeOrBus3(resource);
            side = ThreeWindingsTransformer.Side.THREE;
        }
        return nodeOrBus == null ? null : createVertex(resource.getId(), ConnectableType.THREE_WINDINGS_TRANSFORMER, nodeOrBus, side.name());
    }

    protected void ensureNodeOrBusExists(UndirectedGraph<T, Resource<SwitchAttributes>> graph, T nodeOrBus) {
        if (!graph.containsVertex(nodeOrBus)) {
            graph.addVertex(nodeOrBus);
        }
    }

    public UndirectedGraph<T, Resource<SwitchAttributes>>  buildGraph(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource,
                                                                      Map<T, List<Vertex>> verticesByNodeOrBus) {
        UndirectedGraph<T, Resource<SwitchAttributes>> graph = new Pseudograph<>((i, v1) -> {
            throw new IllegalStateException();
        });
        List<Vertex> vertices = new ArrayList<>();
        buildGraph(index, voltageLevelResource, graph, vertices);
        verticesByNodeOrBus.putAll(vertices.stream().collect(Collectors.groupingBy(this::getNodeOrBus)));
        return graph;
    }

    protected abstract <U extends SwitchAttributes> T getSwitchNodeOrBus1(Resource<U> resource);

    protected abstract <U extends SwitchAttributes> T getSwitchNodeOrBus2(Resource<U> resource);

    protected void buildGraph(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource,
                           UndirectedGraph<T, Resource<SwitchAttributes>> graph, List<Vertex> vertices) {
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
                .map(resource -> createVertexFrom3wt(resource, voltageLevelResource))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        for (Vertex vertex : vertices) {
            graph.addVertex(getNodeOrBus(vertex));
        }

        for (Resource<SwitchAttributes> resource : index.getStoreClient().getVoltageLevelSwitches(networkUuid, voltageLevelResource.getId())) {
            if (!resource.getAttributes().isOpen()) {
                T nodeOrBus1 = getSwitchNodeOrBus1(resource);
                T nodeOrBus2 = getSwitchNodeOrBus2(resource);
                ensureNodeOrBusExists(graph, nodeOrBus1);
                ensureNodeOrBusExists(graph, nodeOrBus2);
                graph.addEdge(nodeOrBus1, nodeOrBus2, resource);
            }
        }
    }

    public List<Set<Vertex>> findConnectedVerticesList(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource,
                                                       BiPredicate<Map<T, List<Vertex>>, Set<T>> busValidator) {
        List<Set<Vertex>> connectedVerticesList = new ArrayList<>();

        // build graph
        Map<T, List<Vertex>> verticesByNodeOrBus = new HashMap<>();
        UndirectedGraph<T, Resource<SwitchAttributes>> graph = buildGraph(index, voltageLevelResource, verticesByNodeOrBus);

        // find node/bus connected sets
        for (Set<T> nodesOrBuses : new ConnectivityInspector<>(graph).connectedSets()) {
            // filter connected vertices that cannot be a calculated bus
            if (busValidator.test(verticesByNodeOrBus, nodesOrBuses)) {
                Set<Vertex> connectedVertices = nodesOrBuses.stream()
                        .flatMap(nodeOrBus -> verticesByNodeOrBus.getOrDefault(nodeOrBus, Collections.emptyList()).stream())
                        .collect(Collectors.toSet());
                connectedVerticesList.add(connectedVertices);
            }
        }

        return connectedVerticesList;
    }

    protected abstract CalculatedBus createCalculatedBus(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource,
                                                         CalculatedBusAttributes calculatedBusAttributes);

    private List<CalculatedBusAttributes> getCalculatedBusAttributesList(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource) {
        List<CalculatedBusAttributes> calculatedBusAttributesList = voltageLevelResource.getAttributes().getCalculatedBuses();
        if (calculatedBusAttributesList == null) {
            calculatedBusAttributesList = findConnectedVerticesList(index, voltageLevelResource, AbstractTopology::busViewBusValidator)
                    .stream()
                    .map(connectedVertices -> new CalculatedBusAttributes(connectedVertices, null, null))
                    .collect(Collectors.toList());
            voltageLevelResource.getAttributes().setCalculatedBuses(calculatedBusAttributesList);
        }
        System.out.println(voltageLevelResource.getId() + " " + calculatedBusAttributesList);
        return calculatedBusAttributesList;
    }

    public Map<String, Bus> calculateBuses(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource) {
        List<CalculatedBusAttributes> calculatedBusAttributesList = getCalculatedBusAttributesList(index, voltageLevelResource);

        return calculatedBusAttributesList.stream()
                .map(CalculatedBusAttributes -> createCalculatedBus(index, voltageLevelResource, CalculatedBusAttributes))
                .collect(Collectors.toMap(CalculatedBus::getId, Function.identity()));
    }

    public CalculatedBus calculateBus(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource, T nodeOrBus) {
        List<CalculatedBusAttributes> calculatedBusAttributesList = getCalculatedBusAttributesList(index, voltageLevelResource);

        for (CalculatedBusAttributes calculatedBusAttributes : calculatedBusAttributesList) {
            for (Vertex vertex : calculatedBusAttributes.getVertices()) {
                if (getNodeOrBus(vertex).equals(nodeOrBus)) {
                    return createCalculatedBus(index, voltageLevelResource, calculatedBusAttributes);
                }
            }
        }
        return null;
    }
}

/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractTopology<T> {

    protected abstract T getNodeOrBus(Vertex vertex);

    protected abstract Vertex createVertex(String id, ConnectableType connectableType, T nodeOrBus, String side);

    protected static class EquipmentCount {
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

    protected EquipmentCount countEquipments(Set<T> nodesOrBusesConnected, Map<T, List<Vertex>> verticesByNodeOrBus) {
        EquipmentCount equipmentCount = new EquipmentCount();
        for (T nodeOrBus : nodesOrBusesConnected) {
            List<Vertex> connectedVertices = verticesByNodeOrBus.get(nodeOrBus);
            if (connectedVertices == null) {
                continue;
            }
            for (Vertex vertex : connectedVertices) {
                if (vertex != null) {
                    countEquipments(equipmentCount, vertex);
                }
            }
        }
        return equipmentCount;
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
            case BATTERY:
                connectableType = ConnectableType.BATTERY;
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

    private <U extends BranchAttributes> List<Vertex> createVertextFromBranch(Resource<U> resource, Resource<VoltageLevelAttributes> voltageLevelResource) {
        List<Vertex> vertices = new ArrayList<>(2);
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
        if (voltageLevelResource.getId().equals(resource.getAttributes().getVoltageLevelId1())) {
            T nodeOrBus = getBranchNodeOrBus1(resource);
            if (nodeOrBus != null) {
                vertices.add(createVertex(resource.getId(), connectableType, nodeOrBus, Branch.Side.ONE.name()));
            }
        }
        if (voltageLevelResource.getId().equals(resource.getAttributes().getVoltageLevelId2())) {
            T nodeOrBus = getBranchNodeOrBus2(resource);
            if (nodeOrBus != null) {
                vertices.add(createVertex(resource.getId(), connectableType, nodeOrBus, Branch.Side.TWO.name()));
            }
        }
        return vertices;
    }

    protected abstract <U extends ThreeWindingsTransformerAttributes> T get3wtNodeOrBus1(Resource<U> resource);

    protected abstract <U extends ThreeWindingsTransformerAttributes> T get3wtNodeOrBus2(Resource<U> resource);

    protected abstract <U extends ThreeWindingsTransformerAttributes> T get3wtNodeOrBus3(Resource<U> resource);

    private List<Vertex> createVertexFrom3wt(Resource<ThreeWindingsTransformerAttributes> resource, Resource<VoltageLevelAttributes> voltageLevelResource) {
        List<Vertex> vertices = new ArrayList<>(3);
        if (voltageLevelResource.getId().equals(resource.getAttributes().getLeg1().getVoltageLevelId())) {
            T nodeOrBus = get3wtNodeOrBus1(resource);
            if (nodeOrBus != null) {
                vertices.add(createVertex(resource.getId(), ConnectableType.THREE_WINDINGS_TRANSFORMER, nodeOrBus, ThreeWindingsTransformer.Side.ONE.name()));
            }
        }
        if (voltageLevelResource.getId().equals(resource.getAttributes().getLeg2().getVoltageLevelId())) {
            T nodeOrBus = get3wtNodeOrBus2(resource);
            if (nodeOrBus != null) {
                vertices.add(createVertex(resource.getId(), ConnectableType.THREE_WINDINGS_TRANSFORMER, nodeOrBus, ThreeWindingsTransformer.Side.TWO.name()));
            }
        }
        if (voltageLevelResource.getId().equals(resource.getAttributes().getLeg3().getVoltageLevelId())) {
            T nodeOrBus = get3wtNodeOrBus3(resource);
            if (nodeOrBus != null) {
                vertices.add(createVertex(resource.getId(), ConnectableType.THREE_WINDINGS_TRANSFORMER, nodeOrBus, ThreeWindingsTransformer.Side.THREE.name()));
            }
        }
        return vertices;
    }

    protected void ensureNodeOrBusExists(Graph<T, Edge> graph, T nodeOrBus) {
        if (!graph.containsVertex(nodeOrBus)) {
            graph.addVertex(nodeOrBus);
        }
    }

    public Graph<T, Edge> buildGraph(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource, boolean includeOpenSwitches) {
        return buildGraph(index, voltageLevelResource, includeOpenSwitches, false);
    }

    public Graph<T, Edge> buildGraph(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource) {
        return buildGraph(index, voltageLevelResource, false, false);
    }

    public Graph<T, Edge> buildGraph(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource,
                                     boolean includeOpenSwitches, boolean isBusView) {
        Map<T, List<Vertex>> verticesByNodeOrBus = new HashMap<>();
        return buildGraph(index, voltageLevelResource, includeOpenSwitches, isBusView, verticesByNodeOrBus);
    }

    public Graph<T, Edge> buildGraph(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource,
                                     boolean includeOpenSwitches, boolean isBusView, Map<T, List<Vertex>> verticesByNodeOrBus) {
        Graph<T, Edge> graph = new Pseudograph<>(Edge.class);
        List<Vertex> vertices = new ArrayList<>();
        buildGraph(index, voltageLevelResource, includeOpenSwitches, isBusView, graph, vertices);
        verticesByNodeOrBus.putAll(vertices.stream().collect(Collectors.groupingBy(this::getNodeOrBus)));
        return graph;
    }

    protected abstract <U extends SwitchAttributes> T getSwitchNodeOrBus1(Resource<U> resource);

    protected abstract <U extends SwitchAttributes> T getSwitchNodeOrBus2(Resource<U> resource);

    protected List<Vertex> buildVertices(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource) {
        List<Vertex> vertices = new ArrayList<>();
        buildVertices(index, voltageLevelResource, vertices);
        return vertices;
    }

    protected void buildVertices(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource,
                                 List<Vertex> vertices) {
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
        vertices.addAll(index.getStoreClient().getVoltageLevelBatteries(networkUuid, voltageLevelResource.getId())
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
        vertices.addAll(index.getStoreClient().getVoltageLevelVscConverterStations(networkUuid, voltageLevelResource.getId())
                .stream()
                .map(this::createVertexFromInjection)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelLccConverterStations(networkUuid, voltageLevelResource.getId())
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
                .flatMap(resource -> createVertextFromBranch(resource, voltageLevelResource).stream())
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelTwoWindingsTransformers(networkUuid, voltageLevelResource.getId())
                .stream()
                .flatMap(resource -> createVertextFromBranch(resource, voltageLevelResource).stream())
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelThreeWindingsTransformers(networkUuid, voltageLevelResource.getId())
                .stream()
                .flatMap(resource -> createVertexFrom3wt(resource, voltageLevelResource).stream())
                .collect(Collectors.toList()));
    }

    protected void buildEdges(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource,
                              boolean includeOpenSwitches, boolean isBusView, Graph<T, Edge> graph) {
        UUID networkUuid = index.getNetwork().getUuid();

        for (Resource<SwitchAttributes> resource : index.getStoreClient().getVoltageLevelSwitches(networkUuid, voltageLevelResource.getId())) {
            T nodeOrBus1 = getSwitchNodeOrBus1(resource);
            T nodeOrBus2 = getSwitchNodeOrBus2(resource);
            ensureNodeOrBusExists(graph, nodeOrBus1);
            ensureNodeOrBusExists(graph, nodeOrBus2);
            if ((includeOpenSwitches || !resource.getAttributes().isOpen()) && (isBusView || !resource.getAttributes().isRetained())) {
                graph.addEdge(nodeOrBus1, nodeOrBus2, new Edge(resource.getAttributes()));
            }
        }
    }

    protected void buildGraph(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource,
                              boolean includeOpenSwitches, boolean isBusView, Graph<T, Edge> graph, List<Vertex> vertices) {
        buildVertices(index, voltageLevelResource, vertices);

        for (Vertex vertex : vertices) {
            graph.addVertex(getNodeOrBus(vertex));
        }

        buildEdges(index, voltageLevelResource, includeOpenSwitches, isBusView, graph);
    }

    protected abstract boolean isCalculatedBusValid(Set<T> nodesOrBusesConnectedSet, Map<T, List<Vertex>> verticesByNodeOrBus, boolean isBusView);

    public List<Set<Vertex>> findConnectedVerticesList(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource, boolean includeOpenSwitches, boolean isBusView) {
        List<Set<Vertex>> connectedVerticesList = new ArrayList<>();

        // build graph
        Map<T, List<Vertex>> verticesByNodeOrBus = new HashMap<>();
        Graph<T, Edge> graph = buildGraph(index, voltageLevelResource, includeOpenSwitches, isBusView, verticesByNodeOrBus);

        // find node/bus connected sets
        for (Set<T> nodesOrBusesConnected : new ConnectivityInspector<>(graph).connectedSets()) {
            // filter connected vertices that cannot be a calculated bus
            if (isCalculatedBusValid(nodesOrBusesConnected, verticesByNodeOrBus, isBusView)) {
                Set<Vertex> connectedVertices = nodesOrBusesConnected.stream()
                        .flatMap(nodeOrBus -> verticesByNodeOrBus.getOrDefault(nodeOrBus, Collections.emptyList()).stream())
                        .collect(Collectors.toSet());
                connectedVerticesList.add(connectedVertices);
            }
        }

        return connectedVerticesList;
    }

    protected abstract CalculatedBus createCalculatedBus(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource, int calculatedBusNum);

    protected abstract void setNodeOrBusToCalculatedBusNum(Resource<VoltageLevelAttributes> voltageLevelResource, Map<T, Integer> nodeOrBusToCalculatedBusNum);

    protected abstract Map<T, Integer> getNodeOrBusToCalculatedBusNum(Resource<VoltageLevelAttributes> voltageLevelResource);

    static class CalculationResult<T> {

        private final List<CalculatedBusAttributes> calculatedBuses;

        private final Map<T, Integer> nodeOrBusToCalculatedBusNum;

        CalculationResult(List<CalculatedBusAttributes> calculatedBuses, Map<T, Integer> nodeOrBusToCalculatedBusNum) {
            this.calculatedBuses = Objects.requireNonNull(calculatedBuses);
            this.nodeOrBusToCalculatedBusNum = Objects.requireNonNull(nodeOrBusToCalculatedBusNum);
        }

        List<CalculatedBusAttributes> getCalculatedBuses() {
            return calculatedBuses;
        }

        Map<T, Integer> getNodeOrBusToCalculatedBusNum() {
            return nodeOrBusToCalculatedBusNum;
        }
    }

    private CalculationResult<T> getCalculatedBusAttributesList(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource, boolean includeOpenSwitches, boolean isBusView) {
        List<CalculatedBusAttributes> calculatedBusAttributesList;
        Map<T, Integer> nodeOrBusToCalculatedBusNum;
        if (voltageLevelResource.getAttributes().isCalculatedBusesValid() && (voltageLevelResource.getAttributes().isCalculatedBusesForBusView() == isBusView)) {
            calculatedBusAttributesList = voltageLevelResource.getAttributes().getCalculatedBuses();
            nodeOrBusToCalculatedBusNum = getNodeOrBusToCalculatedBusNum(voltageLevelResource);
        } else {
            calculatedBusAttributesList = findConnectedVerticesList(index, voltageLevelResource, includeOpenSwitches, isBusView)
                    .stream()
                    .map(connectedVertices -> new CalculatedBusAttributes(connectedVertices, null, null, Double.NaN, Double.NaN))
                    .collect(Collectors.toList());
            voltageLevelResource.getAttributes().setCalculatedBuses(calculatedBusAttributesList);

            // index calculated buses per node or bus
            nodeOrBusToCalculatedBusNum = new HashMap<>();
            for (int calculatedBusNum = 0; calculatedBusNum < calculatedBusAttributesList.size(); calculatedBusNum++) {
                CalculatedBusAttributes calculatedBusAttributes = calculatedBusAttributesList.get(calculatedBusNum);
                for (Vertex vertex : calculatedBusAttributes.getVertices()) {
                    T nodeOrBus = getNodeOrBus(vertex);
                    nodeOrBusToCalculatedBusNum.put(nodeOrBus, calculatedBusNum);
                }
            }
            setNodeOrBusToCalculatedBusNum(voltageLevelResource, nodeOrBusToCalculatedBusNum);

            voltageLevelResource.getAttributes().setCalculatedBusesValid(true);
            voltageLevelResource.getAttributes().setCalculatedBusesForBusView(isBusView);
        }

        return new CalculationResult<>(calculatedBusAttributesList, nodeOrBusToCalculatedBusNum);
    }

    public Map<String, Bus> calculateBuses(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource) {
        return calculateBuses(index, voltageLevelResource, false, false);
    }

    public Map<String, Bus> calculateBuses(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource, boolean includeOpenSwitches, boolean isBusView) {
        List<CalculatedBusAttributes> calculatedBusAttributesList = getCalculatedBusAttributesList(index, voltageLevelResource, includeOpenSwitches, isBusView).getCalculatedBuses();
        Map<String, Bus> calculatedBuses = new LinkedHashMap<>(calculatedBusAttributesList.size());
        for (int calculatedBusNum = 0; calculatedBusNum < calculatedBusAttributesList.size(); calculatedBusNum++) {
            CalculatedBus calculatedBus = createCalculatedBus(index, voltageLevelResource, calculatedBusNum);
            calculatedBuses.put(calculatedBus.getId(), calculatedBus);
        }
        return calculatedBuses;
    }

    public CalculatedBus calculateBus(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource, T nodeOrBus) {
        return calculateBus(index, voltageLevelResource, nodeOrBus, false, false);
    }

    public CalculatedBus calculateBus(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource, T nodeOrBus, boolean includeOpenSwitches, boolean isBusView) {
        Map<T, Integer> nodeOrBusToCalculatedBusNum = getCalculatedBusAttributesList(index, voltageLevelResource, includeOpenSwitches, isBusView).getNodeOrBusToCalculatedBusNum();
        Integer calculatedBusNum = nodeOrBusToCalculatedBusNum.get(nodeOrBus);
        return calculatedBusNum != null ? createCalculatedBus(index, voltageLevelResource, calculatedBusNum) : null;
    }

    public static Terminal getTerminal(NetworkObjectIndex index, Vertex vertex) {
        Objects.requireNonNull(index);
        Objects.requireNonNull(vertex);
        switch (vertex.getConnectableType()) {
            case BUSBAR_SECTION:
                return index.getBusbarSection(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal();
            case LINE:
                return index.getLine(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal(Branch.Side.valueOf(vertex.getSide()));
            case TWO_WINDINGS_TRANSFORMER:
                return index.getTwoWindingsTransformer(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal(Branch.Side.valueOf(vertex.getSide()));
            case THREE_WINDINGS_TRANSFORMER:
                return index.getThreeWindingsTransformer(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal(ThreeWindingsTransformer.Side.valueOf(vertex.getSide()));
            case GENERATOR:
                return index.getGenerator(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal();
            case BATTERY:
                return index.getBattery(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal();
            case LOAD:
                return index.getLoad(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal();
            case SHUNT_COMPENSATOR:
                return index.getShuntCompensator(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal();
            case DANGLING_LINE:
                return index.getDanglingLine(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal();
            case STATIC_VAR_COMPENSATOR:
                return index.getStaticVarCompensator(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal();
            case HVDC_CONVERTER_STATION:
                return index.getHvdcConverterStation(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal();
            default:
                throw new IllegalStateException("Connectable type not supported: " + vertex.getConnectableType());
        }
    }
}

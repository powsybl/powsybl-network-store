/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractTopology<T> {

    protected final String calculatedBusSeparator;

    protected AbstractTopology() {
        calculatedBusSeparator = PlatformConfig.defaultConfig().getOptionalModuleConfig("network-store")
                .flatMap(moduleConfig -> moduleConfig.getOptionalStringProperty("calculated-bus-separator"))
                .orElse("_");
    }

    protected abstract T getNodeOrBus(Vertex vertex);

    protected abstract Vertex createVertex(String id, IdentifiableType connectableType, T nodeOrBus, String side);

    protected static class EquipmentCount<T> {
        int feederCount = 0;
        int branchCount = 0;
        int busbarSectionCount = 0;

        protected void count(Set<T> nodesOrBusesConnected, Map<T, List<Vertex>> verticesByNodeOrBus) {
            for (T nodeOrBus : nodesOrBusesConnected) {
                List<Vertex> connectedVertices = verticesByNodeOrBus.get(nodeOrBus);
                if (connectedVertices == null) {
                    continue;
                }
                for (Vertex vertex : connectedVertices) {
                    if (vertex != null) {
                        count(vertex);
                    }
                }
            }
        }

        private void count(Vertex vertex) {
            switch (vertex.getConnectableType()) {
                case LINE, TWO_WINDINGS_TRANSFORMER, THREE_WINDINGS_TRANSFORMER, HVDC_CONVERTER_STATION, DANGLING_LINE -> {
                    branchCount++;
                    feederCount++;
                }
                case LOAD, GENERATOR, BATTERY, SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR -> feederCount++;
                case BUSBAR_SECTION -> busbarSectionCount++;
                case GROUND -> {
                    // Do nothing
                }
                default -> throw new IllegalStateException();
            }
        }
    }

    protected abstract <U extends InjectionAttributes> T getInjectionNodeOrBus(Resource<U> resource);

    private <U extends InjectionAttributes> Vertex createVertexFromInjection(Resource<U> resource) {
        IdentifiableType connectableType = switch (resource.getType()) {
            case LOAD -> IdentifiableType.LOAD;
            case GENERATOR -> IdentifiableType.GENERATOR;
            case BATTERY -> IdentifiableType.BATTERY;
            case SHUNT_COMPENSATOR -> IdentifiableType.SHUNT_COMPENSATOR;
            case VSC_CONVERTER_STATION, LCC_CONVERTER_STATION -> IdentifiableType.HVDC_CONVERTER_STATION;
            case STATIC_VAR_COMPENSATOR -> IdentifiableType.STATIC_VAR_COMPENSATOR;
            case DANGLING_LINE -> IdentifiableType.DANGLING_LINE;
            case GROUND -> IdentifiableType.GROUND;
            default -> throw new IllegalStateException("Resource is not an injection: " + resource.getType());
        };
        T nodeOrBus = getInjectionNodeOrBus(resource);
        return nodeOrBus == null ? null : createVertex(resource.getId(), connectableType, nodeOrBus, null);
    }

    protected abstract <U extends BranchAttributes> T getBranchNodeOrBus1(Resource<U> resource);

    protected abstract <U extends BranchAttributes> T getBranchNodeOrBus2(Resource<U> resource);

    private <U extends BranchAttributes> List<Vertex> createVertextFromBranch(Resource<U> resource, Resource<VoltageLevelAttributes> voltageLevelResource) {
        List<Vertex> vertices = new ArrayList<>(2);
        IdentifiableType connectableType = switch (resource.getType()) {
            case LINE -> IdentifiableType.LINE;
            case TWO_WINDINGS_TRANSFORMER -> IdentifiableType.TWO_WINDINGS_TRANSFORMER;
            default -> throw new IllegalStateException("Resource is not a branch: " + resource.getType());
        };
        if (voltageLevelResource.getId().equals(resource.getAttributes().getVoltageLevelId1())) {
            T nodeOrBus = getBranchNodeOrBus1(resource);
            if (nodeOrBus != null) {
                vertices.add(createVertex(resource.getId(), connectableType, nodeOrBus, TwoSides.ONE.name()));
            }
        }
        if (voltageLevelResource.getId().equals(resource.getAttributes().getVoltageLevelId2())) {
            T nodeOrBus = getBranchNodeOrBus2(resource);
            if (nodeOrBus != null) {
                vertices.add(createVertex(resource.getId(), connectableType, nodeOrBus, TwoSides.TWO.name()));
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
                vertices.add(createVertex(resource.getId(), IdentifiableType.THREE_WINDINGS_TRANSFORMER, nodeOrBus, ThreeSides.ONE.name()));
            }
        }
        if (voltageLevelResource.getId().equals(resource.getAttributes().getLeg2().getVoltageLevelId())) {
            T nodeOrBus = get3wtNodeOrBus2(resource);
            if (nodeOrBus != null) {
                vertices.add(createVertex(resource.getId(), IdentifiableType.THREE_WINDINGS_TRANSFORMER, nodeOrBus, ThreeSides.TWO.name()));
            }
        }
        if (voltageLevelResource.getId().equals(resource.getAttributes().getLeg3().getVoltageLevelId())) {
            T nodeOrBus = get3wtNodeOrBus3(resource);
            if (nodeOrBus != null) {
                vertices.add(createVertex(resource.getId(), IdentifiableType.THREE_WINDINGS_TRANSFORMER, nodeOrBus, ThreeSides.THREE.name()));
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
                                     boolean includeOpenSwitches, boolean includeRetainSwitches) {
        Map<T, List<Vertex>> verticesByNodeOrBus = new HashMap<>();
        return buildGraph(index, voltageLevelResource, includeOpenSwitches, includeRetainSwitches, verticesByNodeOrBus);
    }

    public Graph<T, Edge> buildGraph(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource,
                                     boolean includeOpenSwitches, boolean includeRetainSwitches, Map<T, List<Vertex>> verticesByNodeOrBus) {
        Graph<T, Edge> graph = new Pseudograph<>(Edge.class);
        List<Vertex> vertices = new ArrayList<>();
        buildGraph(index, voltageLevelResource, includeOpenSwitches, includeRetainSwitches, graph, vertices);
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

        vertices.addAll(index.getStoreClient().getVoltageLevelGenerators(networkUuid, index.getWorkingVariantNum(), voltageLevelResource.getId())
                .stream()
                .map(this::createVertexFromInjection)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelLoads(networkUuid, index.getWorkingVariantNum(), voltageLevelResource.getId())
                .stream()
                .map(this::createVertexFromInjection)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelBatteries(networkUuid, index.getWorkingVariantNum(), voltageLevelResource.getId())
                .stream()
                .map(this::createVertexFromInjection)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelShuntCompensators(networkUuid, index.getWorkingVariantNum(), voltageLevelResource.getId())
                .stream()
                .map(this::createVertexFromInjection)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelStaticVarCompensators(networkUuid, index.getWorkingVariantNum(), voltageLevelResource.getId())
                .stream()
                .map(this::createVertexFromInjection)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelVscConverterStations(networkUuid, index.getWorkingVariantNum(), voltageLevelResource.getId())
                .stream()
                .map(this::createVertexFromInjection)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelLccConverterStations(networkUuid, index.getWorkingVariantNum(), voltageLevelResource.getId())
                .stream()
                .map(this::createVertexFromInjection)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelDanglingLines(networkUuid, index.getWorkingVariantNum(), voltageLevelResource.getId())
                .stream()
                .map(this::createVertexFromInjection)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelGrounds(networkUuid, index.getWorkingVariantNum(), voltageLevelResource.getId())
                .stream()
                .map(this::createVertexFromInjection)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelLines(networkUuid, index.getWorkingVariantNum(), voltageLevelResource.getId())
                .stream()
                .flatMap(resource -> createVertextFromBranch(resource, voltageLevelResource).stream())
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelTwoWindingsTransformers(networkUuid, index.getWorkingVariantNum(), voltageLevelResource.getId())
                .stream()
                .flatMap(resource -> createVertextFromBranch(resource, voltageLevelResource).stream())
                .collect(Collectors.toList()));
        vertices.addAll(index.getStoreClient().getVoltageLevelThreeWindingsTransformers(networkUuid, index.getWorkingVariantNum(), voltageLevelResource.getId())
                .stream()
                .flatMap(resource -> createVertexFrom3wt(resource, voltageLevelResource).stream())
                .collect(Collectors.toList()));
    }

    protected void buildEdges(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource,
                              boolean includeOpenSwitches, boolean includeRetainSwitches, Graph<T, Edge> graph) {
        UUID networkUuid = index.getNetwork().getUuid();

        for (Resource<SwitchAttributes> resource : index.getStoreClient().getVoltageLevelSwitches(networkUuid, index.getWorkingVariantNum(), voltageLevelResource.getId())) {
            T nodeOrBus1 = getSwitchNodeOrBus1(resource);
            T nodeOrBus2 = getSwitchNodeOrBus2(resource);
            ensureNodeOrBusExists(graph, nodeOrBus1);
            ensureNodeOrBusExists(graph, nodeOrBus2);
            if ((includeOpenSwitches || !resource.getAttributes().isOpen()) && (includeRetainSwitches || !resource.getAttributes().isRetained())) {
                graph.addEdge(nodeOrBus1, nodeOrBus2, new Edge(resource.getAttributes()));
            }
        }
    }

    protected void buildGraph(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource,
                              boolean includeOpenSwitches, boolean includeRetainSwitches, Graph<T, Edge> graph, List<Vertex> vertices) {
        buildVertices(index, voltageLevelResource, vertices);

        for (Vertex vertex : vertices) {
            graph.addVertex(getNodeOrBus(vertex));
        }

        buildEdges(index, voltageLevelResource, includeOpenSwitches, includeRetainSwitches, graph);
    }

    protected abstract boolean isCalculatedBusValid(Set<T> nodesOrBusesConnectedSet, Map<T, List<Vertex>> verticesByNodeOrBus, boolean isBusView);

    public List<ConnectedSetResult<T>> findConnectedSetList(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource, boolean isBusView) {
        List<ConnectedSetResult<T>> connectedSetsList = new ArrayList<>();

        // build graph
        Map<T, List<Vertex>> verticesByNodeOrBus = new HashMap<>();
        Graph<T, Edge> graph = buildGraph(index, voltageLevelResource, false, isBusView, verticesByNodeOrBus);

        // find node/bus connected sets
        for (Set<T> connectedNodesOrBuses : new ConnectivityInspector<>(graph).connectedSets()) {
            // filter connected vertices that cannot be a calculated bus
            if (isCalculatedBusValid(connectedNodesOrBuses, verticesByNodeOrBus, isBusView)) {
                Set<Vertex> connectedVertices = connectedNodesOrBuses.stream()
                        .flatMap(nodeOrBus -> verticesByNodeOrBus.getOrDefault(nodeOrBus, Collections.emptyList()).stream())
                        .collect(Collectors.toSet());
                connectedSetsList.add(new ConnectedSetResult<>(connectedVertices, connectedNodesOrBuses));
            }
        }

        return connectedSetsList;
    }

    protected abstract CalculatedBus createCalculatedBus(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource, int calculatedBusNum, boolean isBusView);

    protected abstract void setNodeOrBusToCalculatedBusNum(Resource<VoltageLevelAttributes> voltageLevelResource, Map<T, Integer> nodeOrBusToCalculatedBusNum, boolean isBusView);

    protected abstract Map<T, Integer> getNodeOrBusToCalculatedBusNum(Resource<VoltageLevelAttributes> voltageLevelResource, boolean isBusView);

    private static class CalculationResult<T> {

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

    private static class ConnectedSetResult<T> {

        private final Set<Vertex> connectedVertices;

        private final Set<T> connectedNodesOrBuses;

        ConnectedSetResult(Set<Vertex> connectedVertices, Set<T> connectedNodesOrBuses) {
            this.connectedVertices = Objects.requireNonNull(connectedVertices);
            this.connectedNodesOrBuses = Objects.requireNonNull(connectedNodesOrBuses);
        }

        Set<Vertex> getConnectedVertices() {
            return connectedVertices;
        }

        Set<T> getConnectedNodesOrBuses() {
            return connectedNodesOrBuses;
        }
    }

    private boolean isCalculatedBusesValid(Resource<VoltageLevelAttributes> voltageLevelResource, boolean isBusView) {
        return isBusView ?
                voltageLevelResource.getAttributes().isCalculatedBusesValid() && voltageLevelResource.getAttributes().getCalculatedBusesForBusView() != null :
                voltageLevelResource.getAttributes().isCalculatedBusesValid() && voltageLevelResource.getAttributes().getCalculatedBusesForBusBreakerView() != null;
    }

    private void setCalculatedBuses(Resource<VoltageLevelAttributes> voltageLevelResource, boolean isBusView, List<CalculatedBusAttributes> calculatedBusAttributesList) {
        if (!voltageLevelResource.getAttributes().isCalculatedBusesValid()) { // reset all
            voltageLevelResource.getAttributes().setCalculatedBusesForBusView(null);
            voltageLevelResource.getAttributes().setCalculatedBusesForBusBreakerView(null);
        }

        if (isBusView) { // set calculated buses
            voltageLevelResource.getAttributes().setCalculatedBusesForBusView(calculatedBusAttributesList);
        } else {
            voltageLevelResource.getAttributes().setCalculatedBusesForBusBreakerView(calculatedBusAttributesList);
        }
    }

    private CalculatedBusAttributes findFirstMatchingNodeBreakerCalculatedBusAttributes(Resource<VoltageLevelAttributes> voltageLevelResource,
            ConnectedSetResult<T> connectedSet, boolean isBusView) {
        // TODO Some day we may decide to start preserving phase/angle values
        // in nodebreaker topology even after invalidating the views, so we
        // could remove the check for isCalculatedBusesValid. Here it controls
        // whether we preserve or not the phase/angle values accross the other
        // view. For now we do not preserve to be consistent with the behavior
        // of not preserving values from the same view after invalidation.
        List<CalculatedBusAttributes> calculatedBusAttributesInOtherView = isBusView ? voltageLevelResource.getAttributes().getCalculatedBusesForBusBreakerView() : voltageLevelResource.getAttributes().getCalculatedBusesForBusView();
        Map<Integer, Integer> nodesToCalculatedBusesInOtherView = isBusView ? voltageLevelResource.getAttributes().getNodeToCalculatedBusForBusBreakerView() : voltageLevelResource.getAttributes().getNodeToCalculatedBusForBusView();
        Set<Integer> nodes = (Set<Integer>) connectedSet.getConnectedNodesOrBuses();
        if (voltageLevelResource.getAttributes().isCalculatedBusesValid()
            && !CollectionUtils.isEmpty(calculatedBusAttributesInOtherView)
            && !MapUtils.isEmpty(nodesToCalculatedBusesInOtherView)
            && !nodes.isEmpty()) {
            // busNumInOtherView is deterministic for the busbreakerview because all busbreakerviewbuses correspond
            // to the same busviewbus. For the busview, busNumInOtherView will be non deterministic, it will
            // be one of the busbreakerbuses of this busviewbus.
            Integer node = nodes.iterator().next();
            Integer busNumInOtherView = nodesToCalculatedBusesInOtherView.get(node);
            if (busNumInOtherView != null) {
                return calculatedBusAttributesInOtherView.get(busNumInOtherView);
            }
        }
        return null;
    }

    private CalculatedBusAttributes createCalculatedBusAttributesWithVAndAngle(NetworkObjectIndex index,
                                           Resource<VoltageLevelAttributes> voltageLevelResource,
                                           ConnectedSetResult<T> connectedSet,
                                           boolean isBusView) {
        double v = Double.NaN;
        double angle = Double.NaN;
        if (voltageLevelResource.getAttributes().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            CalculatedBusAttributes busAttributes = findFirstMatchingNodeBreakerCalculatedBusAttributes(voltageLevelResource, connectedSet, isBusView);
            if (busAttributes != null) {
                v = busAttributes.getV();
                angle = busAttributes.getAngle();
            }
        } else { // BUS_BREAKER
            // currently for busbreakertopology the phase/angle values are preserved
            // when set in the busbreakerview which is in a sense always valid.
            // So mimic the behavior and always preserve them also in the busview
            // by *not* testing for isCalculatedBusesValid.
            Set<String> configuredBusesIds = (Set<String>) connectedSet.getConnectedNodesOrBuses();
            if (!configuredBusesIds.isEmpty()) {
                // nondeterministic, chooses a random configuredbus in this busviewbus
                String configuredBusId = configuredBusesIds.iterator().next();
                Bus b = index.getConfiguredBus(configuredBusId).orElseThrow(IllegalStateException::new);
                v = b.getV();
                angle = b.getAngle();
            }
        }
        return new CalculatedBusAttributes(connectedSet.getConnectedVertices(), null, null, v, angle);
    }

    private CalculationResult<T> getCalculatedBusAttributesList(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource, boolean isBusView) {
        List<CalculatedBusAttributes> calculatedBusAttributesList;
        Map<T, Integer> nodeOrBusToCalculatedBusNum;
        if (isCalculatedBusesValid(voltageLevelResource, isBusView)) {
            calculatedBusAttributesList = isBusView ? voltageLevelResource.getAttributes().getCalculatedBusesForBusView() : voltageLevelResource.getAttributes().getCalculatedBusesForBusBreakerView();
            nodeOrBusToCalculatedBusNum = getNodeOrBusToCalculatedBusNum(voltageLevelResource, isBusView);
        } else {
            // calculate buses
            List<ConnectedSetResult<T>> connectedSetList = findConnectedSetList(index, voltageLevelResource, isBusView);
            calculatedBusAttributesList = connectedSetList
                    .stream()
                    //TODO in this case in nodebreaker topology we currently don't preserve any values from
                    //the same view if it was already computed but is invalidated.
                    //we could do it some day (we need to define good heuristics to
                    //match previous values to new buses).
                    //NOTE: We chose to have the same behavior when getting the values from the other view
                    // get V and Angle values from other view if available
                    .map(connectedSet -> createCalculatedBusAttributesWithVAndAngle(index, voltageLevelResource, connectedSet, isBusView))
                    .collect(Collectors.toList());
            setCalculatedBuses(voltageLevelResource, isBusView, calculatedBusAttributesList);

            // set index calculated buses per node or bus
            nodeOrBusToCalculatedBusNum = new HashMap<>();
            for (int calculatedBusNum = 0; calculatedBusNum < calculatedBusAttributesList.size(); calculatedBusNum++) {
                for (T nodeOrBus : connectedSetList.get(calculatedBusNum).getConnectedNodesOrBuses()) {
                    nodeOrBusToCalculatedBusNum.put(nodeOrBus, calculatedBusNum);
                }
            }
            setNodeOrBusToCalculatedBusNum(voltageLevelResource, nodeOrBusToCalculatedBusNum, isBusView);

            // validate calculation
            voltageLevelResource.getAttributes().setCalculatedBusesValid(true);
            index.updateVoltageLevelResource(voltageLevelResource);
            index.getNetwork().invalidateComponents();
        }

        return new CalculationResult<>(calculatedBusAttributesList, nodeOrBusToCalculatedBusNum);
    }

    public Map<String, Bus> calculateBuses(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource) {
        return calculateBuses(index, voltageLevelResource, false);
    }

    public Map<String, Bus> calculateBuses(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource, boolean isBusView) {
        List<CalculatedBusAttributes> calculatedBusAttributesList = getCalculatedBusAttributesList(index, voltageLevelResource, isBusView).getCalculatedBuses();
        Map<String, Bus> calculatedBuses = new LinkedHashMap<>(calculatedBusAttributesList.size());
        for (int calculatedBusNum = 0; calculatedBusNum < calculatedBusAttributesList.size(); calculatedBusNum++) {
            CalculatedBus calculatedBus = createCalculatedBus(index, voltageLevelResource, calculatedBusNum, isBusView);
            calculatedBuses.put(calculatedBus.getId(), calculatedBus);
        }
        return calculatedBuses;
    }

    public CalculatedBus calculateBus(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource, T nodeOrBus) {
        return calculateBus(index, voltageLevelResource, nodeOrBus, false);
    }

    public CalculatedBus calculateBus(NetworkObjectIndex index, Resource<VoltageLevelAttributes> voltageLevelResource, T nodeOrBus, boolean isBusView) {
        Map<T, Integer> nodeOrBusToCalculatedBusNum = getCalculatedBusAttributesList(index, voltageLevelResource, isBusView).getNodeOrBusToCalculatedBusNum();
        Integer calculatedBusNum = nodeOrBusToCalculatedBusNum.get(nodeOrBus);
        return calculatedBusNum != null ? createCalculatedBus(index, voltageLevelResource, calculatedBusNum, isBusView) : null;
    }

    public static Terminal getTerminal(NetworkObjectIndex index, Vertex vertex) {
        Objects.requireNonNull(index);
        Objects.requireNonNull(vertex);
        return switch (vertex.getConnectableType()) {
            case BUSBAR_SECTION ->
                index.getBusbarSection(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal();
            case LINE ->
                index.getLine(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal(TwoSides.valueOf(vertex.getSide()));
            case TWO_WINDINGS_TRANSFORMER ->
                index.getTwoWindingsTransformer(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal(TwoSides.valueOf(vertex.getSide()));
            case THREE_WINDINGS_TRANSFORMER ->
                index.getThreeWindingsTransformer(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal(ThreeSides.valueOf(vertex.getSide()));
            case GENERATOR -> index.getGenerator(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal();
            case BATTERY -> index.getBattery(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal();
            case LOAD -> index.getLoad(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal();
            case SHUNT_COMPENSATOR ->
                index.getShuntCompensator(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal();
            case DANGLING_LINE ->
                index.getDanglingLine(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal();
            case STATIC_VAR_COMPENSATOR ->
                index.getStaticVarCompensator(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal();
            case HVDC_CONVERTER_STATION ->
                index.getHvdcConverterStation(vertex.getId()).orElseThrow(IllegalStateException::new).getTerminal();
            default ->
                throw new IllegalStateException("Connectable type not supported: " + vertex.getConnectableType());
        };
    }
}

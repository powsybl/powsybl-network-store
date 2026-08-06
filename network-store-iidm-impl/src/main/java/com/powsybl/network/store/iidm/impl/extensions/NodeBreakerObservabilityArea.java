/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.ObservabilityArea;
import com.powsybl.iidm.network.util.Networks;
import com.powsybl.network.store.iidm.impl.VoltageLevelImpl;
import com.powsybl.network.store.model.CharacteristicsAttributes;
import com.powsybl.network.store.model.ObservabilityAreaAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
@AutoService(Extension.class)
public class NodeBreakerObservabilityArea extends AbstractExtension<VoltageLevel> implements ObservabilityArea {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeBreakerObservabilityArea.class);

    private final NodeBreakerViewImpl nodeBreakerView;
    private final BusBreakerViewImpl busBreakerView;
    private final BusViewImpl busView;

    public NodeBreakerObservabilityArea(VoltageLevel voltageLevel) {
        super(voltageLevel);
        var attributes = getAttributes();
        this.nodeBreakerView = new NodeBreakerViewImpl(convert(
                attributes.getObservabilityAreaByNodes(),
                attributes.getObservabilityAreaByBusViewBus(),
                attributes.getNodesByBus(),
                voltageLevel));
        this.busBreakerView = new BusBreakerViewImpl();
        this.busView = new BusViewImpl();
    }

    private static Map<Integer, NodeBreakerAreaCharacteristics> convert(Map<Integer, CharacteristicsAttributes> observabilityAreasByNode,
                                                                        Map<String, CharacteristicsAttributes> observabilityAreaByBus,
                                                                        List<Set<Integer>> nodesByBus,
                                                                        VoltageLevel voltageLevel) {
        Map<Integer, NodeBreakerAreaCharacteristics> observabilityAreas = new HashMap<>();

        if (observabilityAreasByNode.isEmpty()) {
            for (Map.Entry<String, CharacteristicsAttributes> e : observabilityAreaByBus.entrySet()) {
                String busId = e.getKey();
                Bus bus = voltageLevel.getBusView().getBus(busId);
                if (bus == null) {
                    throw new PowsyblException("Bus-view bus " + busId + " does not exist");
                }
                Map<String, Set<Integer>> nodesByBusMap = Networks.getNodesByBus(voltageLevel);
                NodeBreakerAreaCharacteristics c = new NodeBreakerAreaCharacteristics(
                        nodesByBusMap.get(busId),
                        toCharacteristics(e.getValue()),
                        voltageLevel);
                nodesByBusMap.get(busId).forEach(node -> observabilityAreas.put(node, c));
            }
        } else {
            for (Set<Integer> nodes : nodesByBus) {
                NodeBreakerAreaCharacteristics c = new NodeBreakerAreaCharacteristics(
                        nodes,
                        toCharacteristics(observabilityAreasByNode.get(nodes.iterator().next())),
                        voltageLevel);
                nodes.forEach(node -> observabilityAreas.put(node, c));
            }
        }
        return observabilityAreas;
    }

    private static AbstractAreaCharacteristics.Characteristics toCharacteristics(CharacteristicsAttributes characteristicsAttributes) {
        return new AbstractAreaCharacteristics.Characteristics(characteristicsAttributes.getAreaNumber(), characteristicsAttributes.getStatus());
    }

    private VoltageLevelImpl getVoltageLevel() {
        return (VoltageLevelImpl) getExtendable();
    }

    private ObservabilityAreaAttributes getAttributes() {
        return (ObservabilityAreaAttributes) getVoltageLevel().getResource()
                .getAttributes()
                .getExtensionAttributes()
                .get(ObservabilityArea.NAME);
    }

    @Override
    public NodeBreakerView getNodeBreakerView() {
        return nodeBreakerView;
    }

    @Override
    public BusBreakerView getBusBreakerView() {
        return busBreakerView;
    }

    @Override
    public BusView getBusView() {
        return busView;
    }

    @Override
    public AreaCharacteristics getObservabilityArea(Terminal terminal) {
        return nodeBreakerView.getObservabilityArea(terminal.getNodeBreakerView().getNode());
    }

    @Override
    public Collection<AreaCharacteristics> getObservabilityAreas() {
        return new HashSet<>(nodeBreakerView.observabilityAreas.values());
    }

    @Override
    public boolean isConsistentWithTopology() {
        Collection<Set<Integer>> trueNodes = Networks.getNodesByBus(getVoltageLevel()).values();
        for (Set<Integer> nodes : nodeBreakerView.observabilityAreas.values().stream().map(c -> c.getNodeBreakerData().getNodes()).collect(Collectors.toSet())) {
            if (trueNodes.stream().noneMatch(n -> n.containsAll(nodes))) {
                return false;
            }
        }
        for (Set<Integer> nodes : trueNodes) {
            if (nodeBreakerView.observabilityAreas.values().stream().map(c -> c.getNodeBreakerData().getNodes()).noneMatch(n -> n.containsAll(nodes))) {
                return false;
            }
        }
        return true;
    }

    private AreaCharacteristics getObservabilityArea(String busId, Set<Integer> nodes, boolean throwException) {
        AreaCharacteristics value = null;
        for (int node : nodes) {
            AreaCharacteristics tmp = nodeBreakerView.observabilityAreas.get(node);
            if (value != null) {
                if (tmp == null) {
                    handleIncompleteObservabilityAreaError(busId, throwException);
                    continue;
                }
                if (value != tmp) {
                    handleOverridingObservabilityArea(busId, throwException);
                }
            } else {
                value = tmp;
            }
        }
        return value;
    }

    private void handleIncompleteObservabilityAreaError(String busId, boolean throwException) {
        LOGGER.error("Inconsistent observability areas: only part of nodes of bus-view bus {} are defined", busId);
        if (throwException) {
            throw new PowsyblException("Inconsistent observability areas: only part of nodes of bus-view bus " + busId
                    + " are defined");
        }
    }

    private void handleOverridingObservabilityArea(String busId, boolean throwException) {
        LOGGER.error("Inconsistent observability areas: bus-view bus {} has different area numbers and/or status. Some will be lost.",
                busId);
        if (throwException) {
            throw new PowsyblException("Inconsistent observability areas: bus-view bus " + busId
                    + " has different area numbers and/or status");
        }
    }

    static class NodeBreakerViewImpl implements NodeBreakerView {

        private final Map<Integer, NodeBreakerAreaCharacteristics> observabilityAreas = new HashMap<>();

        NodeBreakerViewImpl(Map<Integer, NodeBreakerAreaCharacteristics> observabilityAreas) {
            this.observabilityAreas.putAll(observabilityAreas);
        }

        @Override
        public Map<Integer, AreaCharacteristics> getObservabilityAreaByNode() {
            return Collections.unmodifiableMap(observabilityAreas);
        }

        @Override
        public AreaCharacteristics getObservabilityArea(int node) {
            return observabilityAreas.get(node);
        }
    }

    class BusBreakerViewImpl implements BusBreakerView {

        @Override
        public Map<String, AreaCharacteristics> getObservabilityAreaByBus() {
            return getObservabilityAreaByBus(true);
        }

        @Override
        public Map<String, AreaCharacteristics> getObservabilityAreaByBus(boolean throwException) {
            Map<String, AreaCharacteristics> observabilityAreaByBus = new HashMap<>();
            Networks.getNodesByBus(getVoltageLevel()).forEach((busId, nodes) -> {
                AreaCharacteristics characteristics = NodeBreakerObservabilityArea.this.getObservabilityArea(busId, nodes, throwException);
                for (Bus bus : getVoltageLevel().getBusBreakerView().getBusesFromBusViewBusId(busId)) {
                    observabilityAreaByBus.put(bus.getId(), characteristics);
                }
            });
            return observabilityAreaByBus;
        }

        @Override
        public AreaCharacteristics getObservabilityArea(String busId) {
            return getObservabilityArea(busId, true);
        }

        @Override
        public AreaCharacteristics getObservabilityArea(String busId, boolean throwException) {
            Bus bus = getVoltageLevel().getBusBreakerView().getBus(busId);
            if (bus == null) {
                return null;
            }
            Bus busViewBus = bus.getConnectedTerminalStream().map(t -> t.getBusView().getBus()).filter(Objects::nonNull).findFirst().orElse(null);
            if (busViewBus == null) {
                return null;
            }
            String busViewBusId = busViewBus.getId();
            return NodeBreakerObservabilityArea.this.getObservabilityArea(busViewBusId, Networks.getNodesByBus(getVoltageLevel()).get(busViewBusId), throwException);
        }
    }

    class BusViewImpl implements BusView {

        @Override
        public Map<String, AreaCharacteristics> getObservabilityAreaByBus() {
            return getObservabilityAreaByBus(true);
        }

        @Override
        public Map<String, AreaCharacteristics> getObservabilityAreaByBus(boolean throwException) {
            Map<String, AreaCharacteristics> observabilityAreaByBus = new HashMap<>();
            Networks.getNodesByBus(getVoltageLevel())
                    .forEach((busId, nodes) -> observabilityAreaByBus.put(busId, NodeBreakerObservabilityArea.this.getObservabilityArea(busId, nodes, throwException)));
            return observabilityAreaByBus;
        }

        @Override
        public AreaCharacteristics getObservabilityArea(String busId) {
            return getObservabilityArea(busId, true);
        }

        @Override
        public AreaCharacteristics getObservabilityArea(String busId, boolean throwException) {
            return NodeBreakerObservabilityArea.this.getObservabilityArea(busId, Networks.getNodesByBus(getVoltageLevel()).get(busId), throwException);
        }
    }
}

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
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.ObservabilityArea;
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
public class BusBreakerObservabilityArea extends AbstractExtension<VoltageLevel> implements ObservabilityArea {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusBreakerObservabilityArea.class);
    private static final String NOT_SUPPORTED_IN_BB_TOPOLOGY = "Not supported in a bus breaker topology";

    private final NodeBreakerViewImpl nodeBreakerView;
    private final BusBreakerViewImpl busBreakerView;
    private final BusViewImpl busView;

    BusBreakerObservabilityArea(VoltageLevel voltageLevel) {
        super(voltageLevel);
        var attributes = getAttributes();
        nodeBreakerView = new NodeBreakerViewImpl();
        busBreakerView = new BusBreakerViewImpl(convert(
                attributes.getObservabilityAreaByBusBreakerViewBus(),
                attributes.getObservabilityAreaByBusViewBus(),
                attributes.getBusBreakerViewBusesByBusViewBus(),
                voltageLevel));
        busView = new BusViewImpl();
    }

    private static Map<String, BusBreakerAreaCharacteristics> convert(Map<String, CharacteristicsAttributes> observabilityAreaByBusBreakerViewBus,
                                                                      Map<String, CharacteristicsAttributes> observabilityAreaByBusViewBus,
                                                                      List<Set<String>> busBreakerViewBusesByBus, VoltageLevel voltageLevel) {
        Map<String, BusBreakerAreaCharacteristics> observabilityAreas = new HashMap<>();
        if (observabilityAreaByBusBreakerViewBus.isEmpty()) {
            for (Map.Entry<String, CharacteristicsAttributes> e : observabilityAreaByBusViewBus.entrySet()) {
                String busId = e.getKey();
                Bus bus = voltageLevel.getBusView().getBus(busId);
                if (bus == null) {
                    throw new PowsyblException("Bus-view bus " + busId + " does not exist");
                }
                BusBreakerAreaCharacteristics c = new BusBreakerAreaCharacteristics(
                        voltageLevel.getBusBreakerView()
                                .getBusStreamFromBusViewBusId(busId)
                                .map(Identifiable::getId)
                                .collect(Collectors.toSet()),
                        toCharacteristics(e.getValue()),
                        voltageLevel);
                voltageLevel.getBusBreakerView().getBusStreamFromBusViewBusId(busId)
                        .forEach(b -> observabilityAreas.put(b.getId(), c));
            }
        } else {
            for (Set<String> buses : busBreakerViewBusesByBus) {
                BusBreakerAreaCharacteristics area = new BusBreakerAreaCharacteristics(
                        buses,
                        toCharacteristics(observabilityAreaByBusBreakerViewBus.get(buses.iterator().next())),
                        voltageLevel);
                buses.forEach(busId -> observabilityAreas.put(busId, area));
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
        return (ObservabilityAreaAttributes) getVoltageLevel().getResource().getAttributes().getExtensionAttributes().get(ObservabilityArea.NAME);
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
        return busBreakerView.getObservabilityArea(terminal.getBusBreakerView().getBus().getId());
    }

    @Override
    public Collection<AreaCharacteristics> getObservabilityAreas() {
        return new HashSet<>(busBreakerView.observabilityAreas.values());
    }

    @Override
    public boolean isConsistentWithTopology() {
        Collection<Set<String>> trueBuses = getVoltageLevel().getBusView().getBusStream().map(b -> getVoltageLevel().getBusBreakerView().getBusesFromBusViewBusId(b.getId()))
                .filter(buses -> !buses.isEmpty())
                .map(buses -> {
                    Set<String> ids = new HashSet<>();
                    buses.forEach(b -> ids.add(b.getId()));
                    return ids;
                })
                .collect(Collectors.toSet());
        for (Set<String> buses : busBreakerView.observabilityAreas.values().stream().map(c -> c.getBusBreakerData().getBusIds()).collect(Collectors.toSet())) {
            if (trueBuses.stream().noneMatch(b -> b.containsAll(buses))) {
                return false;
            }
        }
        for (Set<String> buses : trueBuses) {
            if (busBreakerView.observabilityAreas.values().stream().map(c -> c.getBusBreakerData().getBusIds()).noneMatch(b -> b.containsAll(buses))) {
                return false;
            }
        }
        return true;
    }

    static class NodeBreakerViewImpl implements NodeBreakerView {

        @Override
        public Map<Integer, AreaCharacteristics> getObservabilityAreaByNode() {
            throw new UnsupportedOperationException(NOT_SUPPORTED_IN_BB_TOPOLOGY);
        }

        @Override
        public AreaCharacteristics getObservabilityArea(int node) {
            throw new UnsupportedOperationException(NOT_SUPPORTED_IN_BB_TOPOLOGY);
        }
    }

    static class BusBreakerViewImpl implements BusBreakerView {

        private final Map<String, BusBreakerAreaCharacteristics> observabilityAreas = new HashMap<>();

        BusBreakerViewImpl(Map<String, BusBreakerAreaCharacteristics> observabilityAreas) {
            this.observabilityAreas.putAll(observabilityAreas);
        }

        @Override
        public Map<String, AreaCharacteristics> getObservabilityAreaByBus() {
            return getObservabilityAreaByBus(true);
        }

        @Override
        public Map<String, AreaCharacteristics> getObservabilityAreaByBus(boolean throwException) {
            return Collections.unmodifiableMap(observabilityAreas);
        }

        @Override
        public AreaCharacteristics getObservabilityArea(String busId) {
            return getObservabilityArea(busId, true);
        }

        @Override
        public AreaCharacteristics getObservabilityArea(String busId, boolean throwException) {
            return observabilityAreas.get(busId);
        }
    }

    class BusViewImpl implements BusView {

        @Override
        public Map<String, AreaCharacteristics> getObservabilityAreaByBus() {
            return getObservabilityAreaByBus(true);
        }

        @Override
        public Map<String, AreaCharacteristics> getObservabilityAreaByBus(boolean throwException) {
            Map<String, AreaCharacteristics> observabilityAreaByBusViewBus = new HashMap<>();
            busBreakerView.observabilityAreas.forEach((key, value) -> {
                Bus bus = getVoltageLevel().getBusView().getMergedBus(key);
                if (bus == null) {
                    handleNotExistingBusError(key, throwException);
                    return;
                }
                String busViewBusId = bus.getId();
                if (observabilityAreaByBusViewBus.containsKey(busViewBusId)) {
                    AreaCharacteristics previous = observabilityAreaByBusViewBus.get(busViewBusId);
                    if (previous != value) {
                        handleOverridingObservabilityAreaError(busViewBusId, throwException);
                    }
                } else {
                    observabilityAreaByBusViewBus.put(busViewBusId, value);
                }
            });
            return observabilityAreaByBusViewBus;
        }

        @Override
        public AreaCharacteristics getObservabilityArea(String busId) {
            return getObservabilityArea(busId, true);
        }

        @Override
        public AreaCharacteristics getObservabilityArea(String busId, boolean throwException) {
            AreaCharacteristics characteristics = null;
            for (Map.Entry<String, BusBreakerAreaCharacteristics> e : busBreakerView.observabilityAreas.entrySet()) {
                Bus bus = getVoltageLevel().getBusBreakerView().getBus(e.getKey());
                Bus mergedBus = getVoltageLevel().getBusView().getMergedBus(e.getKey());

                if (bus == null || mergedBus == null) {
                    handleNotExistingBusError(e.getKey(), throwException);
                    continue;
                }

                if (mergedBus.getId().equals(busId)) {
                    if (characteristics != null && characteristics != e.getValue()) {
                        handleOverridingObservabilityAreaError(busId, throwException);
                    } else {
                        characteristics = e.getValue();
                    }
                }
            }
            return characteristics;
        }

        private void handleNotExistingBusError(String busId, boolean throwException) {
            LOGGER.error("Inconsistent observability areas: bus {} does not exist anymore in bus-breaker view", busId);
            if (throwException) {
                throw new PowsyblException("Inconsistent observability areas: bus " + busId + " does not exist anymore in bus-breaker view");
            }
        }

        private void handleOverridingObservabilityAreaError(String busId, boolean throwException) {
            LOGGER.error("Inconsistent observability areas: bus {} is associated " +
                    "to different area numbers and/or status. Some will be lost.", busId);
            if (throwException) {
                throw new PowsyblException("Inconsistent observability areas: bus " + busId + " is associated " +
                        "to different area numbers and/or status");
            }
        }
    }
}

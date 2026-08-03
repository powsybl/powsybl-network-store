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
import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.ObservabilityArea;
import com.powsybl.iidm.network.extensions.ObservabilityAreaAdder;
import com.powsybl.network.store.iidm.impl.VoltageLevelImpl;
import com.powsybl.network.store.model.CharacteristicsAttributes;
import com.powsybl.network.store.model.ObservabilityAreaAttributes;

import java.util.*;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
@AutoService(ExtensionAdder.class)
public class ObservabilityAreaAdderImpl extends AbstractIidmExtensionAdder<VoltageLevel, ObservabilityArea> implements ObservabilityAreaAdder {

    private final Map<String, AbstractAreaCharacteristics.Characteristics> observabilityAreaByBusViewBus = new HashMap<>();
    private final Map<String, AbstractAreaCharacteristics.Characteristics> observabilityAreaByBusBreakerViewBus = new HashMap<>();
    private final Map<Integer, AbstractAreaCharacteristics.Characteristics> observabilityAreaByNodes = new HashMap<>();

    private final List<Set<Integer>> nodesByBus = new ArrayList<>();
    private final List<Set<String>> busBreakerViewBusesByBusViewBus = new ArrayList<>();

    protected ObservabilityAreaAdderImpl(VoltageLevel extendable) {
        super(extendable);
    }

    @Override
    protected ObservabilityArea createExtension(VoltageLevel voltageLevel) {
        int nonEmptyMap = (observabilityAreaByNodes.isEmpty() ? 0 : 1) + (observabilityAreaByBusBreakerViewBus.isEmpty() ? 0 : 1) + (observabilityAreaByBusViewBus.isEmpty() ? 0 : 1);
        if (nonEmptyMap > 1) {
            throw new PowsyblException("Observability areas must be exclusively filled by bus-view buses OR nodes, not both");
        }
        ObservabilityAreaAttributes observabilityAreaAttributes = new ObservabilityAreaAttributes(
                convertStringMap(observabilityAreaByBusViewBus),
                convertStringMap(observabilityAreaByBusBreakerViewBus),
                convertIntMap(observabilityAreaByNodes),
                nodesByBus,
                busBreakerViewBusesByBusViewBus);

        if (TopologyKind.NODE_BREAKER.equals(voltageLevel.getTopologyKind())) {
            if (!observabilityAreaByBusBreakerViewBus.isEmpty()) {
                throw new PowsyblException("Observability areas must be exclusively filled by bus-view buses or nodes in node-breaker voltage levels");
            }
            ((VoltageLevelImpl) voltageLevel).updateResourceWithoutNotification(res -> res.getAttributes().getExtensionAttributes().put(ObservabilityArea.NAME, observabilityAreaAttributes));
            return new NodeBreakerObservabilityArea(voltageLevel);
        } else if (TopologyKind.BUS_BREAKER.equals(voltageLevel.getTopologyKind())) {
            if (!observabilityAreaByNodes.isEmpty()) {
                throw new PowsyblException("Observability areas must be exclusively filled by bus-view buses or bus-breaker-view buses in bus-breaker voltage levels");
            }
            ((VoltageLevelImpl) voltageLevel).updateResourceWithoutNotification(res -> res.getAttributes().getExtensionAttributes().put(ObservabilityArea.NAME, observabilityAreaAttributes));
            return new BusBreakerObservabilityArea(voltageLevel);
        }
        throw new AssertionError("Unexpected voltage level " + voltageLevel.getId() + " topology: " + voltageLevel.getTopologyKind());
    }

    @Override
    public ObservabilityAreaAdder withObservabilityAreaByBusViewBus(String busViewBusId, int observabilityAreaNumber, ObservabilityArea.ObservabilityStatus observabilityStatus) {
        observabilityAreaByBusViewBus.put(busViewBusId, new AbstractAreaCharacteristics.Characteristics(observabilityAreaNumber, observabilityStatus));
        return this;
    }

    @Override
    public ObservabilityAreaAdder withObservabilityAreaByBusBreakerViewBuses(Set<String> busBreakerViewBusIds, int observabilityAreaNumber, ObservabilityArea.ObservabilityStatus observabilityStatus) {
        AbstractAreaCharacteristics.Characteristics characteristics = new AbstractAreaCharacteristics.Characteristics(observabilityAreaNumber, observabilityStatus);
        busBreakerViewBusIds.forEach(busId -> observabilityAreaByBusBreakerViewBus.put(busId, characteristics));
        busBreakerViewBusesByBusViewBus.add(busBreakerViewBusIds);
        return this;
    }

    @Override
    public ObservabilityAreaAdder withObservabilityAreaByNodes(Set<Integer> nodes, int observabilityAreaNumber, ObservabilityArea.ObservabilityStatus observabilityStatus) {
        AbstractAreaCharacteristics.Characteristics characteristics = new AbstractAreaCharacteristics.Characteristics(observabilityAreaNumber, observabilityStatus);
        nodes.forEach(n -> observabilityAreaByNodes.put(n, characteristics));
        nodesByBus.add(nodes);
        return this;
    }

    private CharacteristicsAttributes toCharacteristicsAttributes(AbstractAreaCharacteristics.Characteristics characteristics) {
        return new CharacteristicsAttributes(characteristics.areaNumber(), characteristics.status());
    }

    private Map<String, CharacteristicsAttributes> convertStringMap(Map<String, AbstractAreaCharacteristics.Characteristics> map) {
        Map<String, CharacteristicsAttributes> mapToReturn = new HashMap<>();
        map.forEach((o, characteristics) -> mapToReturn.put(o, toCharacteristicsAttributes(characteristics)));
        return mapToReturn;
    }

    private Map<Integer, CharacteristicsAttributes> convertIntMap(Map<Integer, AbstractAreaCharacteristics.Characteristics> map) {
        Map<Integer, CharacteristicsAttributes> mapToReturn = new HashMap<>();
        map.forEach((o, characteristics) -> mapToReturn.put(o, toCharacteristicsAttributes(characteristics)));
        return mapToReturn;
    }
}

/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.CgmesIidmMapping;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.model.CgmesIidmMappingAttributes;
import com.powsybl.network.store.model.TerminalRefAttributes;

import java.util.*;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class CgmesIidmMappingImpl extends AbstractExtension<Network> implements CgmesIidmMapping {

    private final NetworkImpl network;

    public CgmesIidmMappingImpl(NetworkImpl network) {
        this.network = network;
    }

    CgmesIidmMappingImpl(NetworkImpl network, Set<String> topologicalNodes) {
//        this.network = network;
//        getCgmesIidmMappingAttributes().setBusTopologicalNodeMap(new HashMap<>());
//        getCgmesIidmMappingAttributes().setEquipmentSideTopologicalNodeMap(new HashMap<>());
//        getCgmesIidmMappingAttributes().setUnmapped(new HashSet<>());
//        getUnmapped().addAll(Objects.requireNonNull(topologicalNodes));
        this(network.initCgmesIidmMappingAttributes(new HashMap<TerminalRefAttributes, String>(), new HashMap<String, Set<String>>(), topologicalNodes));
    }

    private CgmesIidmMappingAttributes getCgmesIidmMappingAttributes() {
        return network.getResource().getAttributes().getCgmesIidmMapping();
    }

    private Map<String, Set<String>> getBusTopologicalNodeMap() {
        return getCgmesIidmMappingAttributes().getBusTopologicalNodeMap();
    }

    private Set<String> getUnmapped() {
        return getCgmesIidmMappingAttributes().getUnmapped();
    }

    private Map<TerminalRefAttributes, String> getEquipmentSideTopologicalNodeMap() {
        return getCgmesIidmMappingAttributes().getEquipmentSideTopologicalNodeMap();
    }

    @Override
    public Set<String> getTopologicalNodes(String busId) {
        Map<String, Set<String>> busTopologicalNodeMap = getBusTopologicalNodeMap();
        if (busTopologicalNodeMap.isEmpty()) {
            calculate();
        }
        return busTopologicalNodeMap.get(busId);
    }

    @Override
    public String getTopologicalNode(String equipmentId, int side) {
        return getEquipmentSideTopologicalNodeMap().get(new TerminalRefAttributes(equipmentId, String.valueOf(side)));
    }

    @Override
    public boolean isMapped(String busId) {
        Map<String, Set<String>> busTopologicalNodeMap = getBusTopologicalNodeMap();
        if (busTopologicalNodeMap.isEmpty()) {
            calculate();
        }
        return busTopologicalNodeMap.containsKey(busId);
    }

    @Override
    public boolean isEmpty() {
        Map<String, Set<String>> busTopologicalNodeMap = getBusTopologicalNodeMap();
        if (busTopologicalNodeMap.isEmpty()) {
            calculate();
        }
        return busTopologicalNodeMap.isEmpty();
    }

    private void calculate() {
        getEquipmentSideTopologicalNodeMap().forEach((terminalRef, tn) -> {
            Identifiable i = getExtendable().getIdentifiable(terminalRef.getConnectableId());
            if (i instanceof Connectable) {
                Connectable c = (Connectable) i;
                Terminal t = (Terminal) c.getTerminals().get(Integer.parseInt(terminalRef.getSide()) - 1);
                // BusView Buses should be considered
                // And it is ok that a Eq,Side does not have a mapping to a BusView bus (it is "disconnected")
                // but had always a mapping to a BusBreakerView bus (at bus/breaker level even disconnected terminals receive a configured bus)
                Bus bus = t.getBusView().getBus();
                if (bus == null) {
                    return;
                }
                String busId = t.getBusView().getBus().getId();
                checkAlreadyMapped(busId, tn);
                getBusTopologicalNodeMap().computeIfAbsent(busId, bid -> new HashSet<>()).add(tn);
                getUnmapped().remove(tn);
            }
        });
    }

    @Override
    public CgmesIidmMapping put(String equipmentId, int side, String topologicalNodeId) {
        getEquipmentSideTopologicalNodeMap().put(new TerminalRefAttributes(equipmentId, String.valueOf(side)), topologicalNodeId);
        return this;
    }

    @Override
    public CgmesIidmMapping put(String busId, String topologicalNodeId) {
        // This method is called when the unmapped list has already been completed
        // There are no "pending" TNs to be removed from unmapped
        // The check to see if this TN has also been mapped to a different bus
        // can not be the same that we apply when removing elements from "unmapped"
        if (getUnmapped().contains(topologicalNodeId)) {
            throw new PowsyblException("Inconsistency: TN " + topologicalNodeId + " has been considered unmapped, but now a mapping to bus " + busId + " is being added");
        }
        getBusTopologicalNodeMap().computeIfAbsent(busId, b -> new HashSet<>()).add(topologicalNodeId);
        return this;
    }

    private void checkAlreadyMapped(String busId, String topologicalNodeId) {
        // TN has been removed from unmapped collection (that starts with all TNs)
        // and this bus has not received it
        // because no mappings exist for this bus: get(busId) == null
        // or because the TN can not be found in the mappings for this bus: !get(busId).contains(TN)
        Map<String, Set<String>> busTopologicalNodeMap = getBusTopologicalNodeMap();
        if (!getUnmapped().contains(topologicalNodeId) && (busTopologicalNodeMap.get(busId) == null || !busTopologicalNodeMap.get(busId).contains(topologicalNodeId))) {
            throw new PowsyblException("TopologicalNode " + topologicalNodeId + " is already mapped to another bus");
        }
    }

    @Override
    public Map<String, Set<String>> topologicalNodesByBusViewBusMap() {
        Map<String, Set<String>> busTopologicalNodeMap = getBusTopologicalNodeMap();
        if (busTopologicalNodeMap.isEmpty()) {
            calculate();
        }
        return new HashMap<>(busTopologicalNodeMap);
    }

    @Override
    public Set<String> getUnmappedTopologicalNodes() {
        if (getBusTopologicalNodeMap().isEmpty()) {
            calculate();
        }
        return getUnmapped();
    }
}

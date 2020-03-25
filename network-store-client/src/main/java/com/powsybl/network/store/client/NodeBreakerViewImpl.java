/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeBreakerViewImpl implements VoltageLevel.NodeBreakerView {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeBreakerViewImpl.class);

    private final TopologyKind topologyKind;

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    private final NetworkObjectIndex index;

    public NodeBreakerViewImpl(TopologyKind topologyKind, Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        this.topologyKind = topologyKind;
        this.voltageLevelResource = voltageLevelResource;
        this.index = index;
    }

    static NodeBreakerViewImpl create(TopologyKind topologyKind, Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        return new NodeBreakerViewImpl(topologyKind, voltageLevelResource, index);
    }

    private void checkTopologyKind() {
        if (topologyKind == TopologyKind.BUS_BREAKER) {
            throw new PowsyblException("Not supported in a bus breaker topology");
        }
    }

    @Override
    public int getNodeCount() {
        checkTopologyKind();
        return voltageLevelResource.getAttributes().getNodeCount();
    }

    @Override
    public int[] getNodes() {
        checkTopologyKind();
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public VoltageLevel.NodeBreakerView setNodeCount(int count) {
        checkTopologyKind();
        voltageLevelResource.getAttributes().setNodeCount(count);
        return this;
    }

    @Override
    public BusbarSectionAdder newBusbarSection() {
        checkTopologyKind();
        return new BusbarSectionAdderImpl(voltageLevelResource, index);
    }

    @Override
    public List<BusbarSection> getBusbarSections() {
        checkTopologyKind();
        return index.getBusbarSections(voltageLevelResource.getId());
    }

    @Override
    public Stream<BusbarSection> getBusbarSectionStream() {
        checkTopologyKind();
        return getBusbarSections().stream();
    }

    @Override
    public int getBusbarSectionCount() {
        checkTopologyKind();
        return getBusbarSections().size();
    }

    @Override
    public BusbarSection getBusbarSection(String id) {
        checkTopologyKind();
        return index.getBusbarSection(id)
                .orElse(null);
    }

    @Override
    public void traverse(int node, Traverser traverser) {
        checkTopologyKind();
        // TODO
        //throw new UnsupportedOperationException("TODO");
    }

    @Override
    public SwitchAdder newSwitch() {
        checkTopologyKind();
        return new SwitchAdderNodeBreakerImpl(voltageLevelResource, index, null);
    }

    @Override
    public InternalConnectionAdder newInternalConnection() {
        checkTopologyKind();
        return new InternalConnectionAdderNodeBreakerImpl(voltageLevelResource);
    }

    @Override
    public SwitchAdder newBreaker() {
        checkTopologyKind();
        return new SwitchAdderNodeBreakerImpl(voltageLevelResource, index, SwitchKind.BREAKER);
    }

    @Override
    public SwitchAdder newDisconnector() {
        checkTopologyKind();
        return new SwitchAdderNodeBreakerImpl(voltageLevelResource, index, SwitchKind.DISCONNECTOR);
    }

    @Override
    public List<Switch> getSwitches() {
        checkTopologyKind();
        return index.getSwitches(voltageLevelResource.getId());
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        checkTopologyKind();
        return getSwitches().stream();
    }

    @Override
    public int getSwitchCount() {
        checkTopologyKind();
        return getSwitches().size();
    }

    @Override
    public void removeSwitch(String switchId) {
        checkTopologyKind();
        throw new UnsupportedOperationException("TODO");
    }

    public Switch getSwitch(String id) {
        checkTopologyKind();
        return index.getSwitch(id)
                .orElse(null);
    }

    @Override
    public int getNode1(String switchId) {
        checkTopologyKind();
        return ((SwitchImpl) getSwitch(switchId)).getNode1();
    }

    @Override
    public int getNode2(String switchId) {
        checkTopologyKind();
        return ((SwitchImpl) getSwitch(switchId)).getNode2();
    }

    @Override
    public Terminal getTerminal(int node) {
        checkTopologyKind();
        // TODO
        return null;
    }

    @Override
    public Terminal getTerminal1(String switchId) {
        checkTopologyKind();
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Terminal getTerminal2(String switchId) {
        checkTopologyKind();
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<InternalConnection> getInternalConnections() {
        checkTopologyKind();
        return voltageLevelResource.getAttributes().getInternalConnections().stream().map(InternalConnectionImpl::create).collect(Collectors.toList());
    }

    @Override
    public Stream<InternalConnection> getInternalConnectionStream() {
        checkTopologyKind();
        return getInternalConnections().stream();
    }

    @Override
    public int getInternalConnectionCount() {
        checkTopologyKind();
        return getInternalConnections().size();
    }
}

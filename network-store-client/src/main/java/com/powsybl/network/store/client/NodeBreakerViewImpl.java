/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeBreakerViewImpl implements VoltageLevel.NodeBreakerView {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeBreakerViewImpl.class);

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    private final NetworkObjectIndex index;

    public NodeBreakerViewImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        this.voltageLevelResource = voltageLevelResource;
        this.index = index;
    }

    static NodeBreakerViewImpl create(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        return new NodeBreakerViewImpl(voltageLevelResource, index);
    }

    @Override
    public int getNodeCount() {
        return voltageLevelResource.getAttributes().getNodeCount();
    }

    @Override
    public int[] getNodes() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public VoltageLevel.NodeBreakerView setNodeCount(int count) {
        voltageLevelResource.getAttributes().setNodeCount(count);
        return this;
    }

    @Override
    public BusbarSectionAdder newBusbarSection() {
        return new BusbarSectionAdderImpl(voltageLevelResource, index);
    }

    @Override
    public List<BusbarSection> getBusbarSections() {
        return index.getBusbarSections(voltageLevelResource.getId());
    }

    @Override
    public Stream<BusbarSection> getBusbarSectionStream() {
        return getBusbarSections().stream();
    }

    @Override
    public int getBusbarSectionCount() {
        return getBusbarSections().size();
    }

    @Override
    public BusbarSection getBusbarSection(String id) {
        return getBusbarSectionStream()
                .filter(busbarSection -> busbarSection.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void traverse(int node, Traverser traverser) {
        // TODO
        //throw new UnsupportedOperationException("TODO");
    }

    @Override
    public SwitchAdder newSwitch() {
        return new SwitchAdderNodeBreakerImpl(voltageLevelResource, index, null);
    }

    @Override
    public InternalConnectionAdder newInternalConnection() {
        return new InternalConnectionAdderNodeBreakerImpl(voltageLevelResource, index);
    }

    @Override
    public SwitchAdder newBreaker() {
        return new SwitchAdderNodeBreakerImpl(voltageLevelResource, index, SwitchKind.BREAKER);
    }

    @Override
    public SwitchAdder newDisconnector() {
        return new SwitchAdderNodeBreakerImpl(voltageLevelResource, index, SwitchKind.DISCONNECTOR);
    }

    @Override
    public List<Switch> getSwitches() {
        return index.getSwitches(voltageLevelResource.getId());
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        return getSwitches().stream();
    }

    @Override
    public int getSwitchCount() {
        return getSwitches().size();
    }

    @Override
    public void removeSwitch(String switchId) {
        throw new UnsupportedOperationException("TODO");
    }

    public Switch getSwitch(String id) {
        return getSwitchStream()
                .filter(sw -> sw.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public int getNode1(String switchId) {
        return ((SwitchImpl) getSwitch(switchId)).getNode1();
    }

    @Override
    public int getNode2(String switchId) {
        return ((SwitchImpl) getSwitch(switchId)).getNode2();
    }

    @Override
    public Terminal getTerminal(int node) {
        // TODO
        return null;
    }

    @Override
    public Terminal getTerminal1(String switchId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Terminal getTerminal2(String switchId) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<InternalConnection> getInternalConnections() {
        return index.getInternalConnections(voltageLevelResource.getId());
    }

    @Override
    public Stream<InternalConnection> getInternalConnectionStream() {
        return getInternalConnections().stream();
    }

    @Override
    public int getInternalConnectionCount() {
        return getInternalConnections().size();
    }
}

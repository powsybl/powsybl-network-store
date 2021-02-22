/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
abstract class AbstractInjectionAdder<T extends AbstractInjectionAdder<T>> extends AbstractIdentifiableAdder<T> {

    private Resource<VoltageLevelAttributes> voltageLevelResource;

    private Integer node;

    private String bus;

    private String connectableBus;

    public AbstractInjectionAdder(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(index);
        this.voltageLevelResource = voltageLevelResource;
    }

    protected Integer getNode() {
        return node;
    }

    public T setNode(int node) {
        this.node = node;
        return (T) this;
    }

    protected String getBus() {
        return bus;
    }

    public T setBus(String bus) {
        this.bus = bus;
        return (T) this;
    }

    protected String getConnectableBus() {
        return connectableBus;
    }

    public T setConnectableBus(String connectableBus) {
        this.connectableBus = connectableBus;
        return (T) this;
    }

    protected Resource<VoltageLevelAttributes> getVoltageLevelResource() {
        return voltageLevelResource;
    }

    protected void checkNodeBus() {
        String connectionBus = getConnectionBus();
        if (node == null && connectionBus == null) {
            throw new ValidationException(this, "connectable bus is not set");
        }

        if (node != null && connectionBus != null) {
            throw new ValidationException(this, "connection node and connection bus are exclusives");
        }

        if (connectionBus != null) {
            checkBus(connectionBus);
        } else {
            checkNode();
        }
    }

    private void checkBus(String connectionBus) {
        if (getVoltageLevelResource().getAttributes().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            throw new ValidationException(this, "bus only used in a bus breaker topology");
        }
        if (index.getBus(connectionBus).isEmpty()) {
            throw new ValidationException(this, "connectable bus '" + connectionBus + "' not found");
        }
    }

    private void checkNode() {
        if (getVoltageLevelResource().getAttributes().getTopologyKind() == TopologyKind.BUS_BREAKER) {
            throw new ValidationException(this, "node only used in a node breaker topology");
        }

        Terminal terminal = getNetwork().getVoltageLevel(getVoltageLevelResource().getId()).getNodeBreakerView().getTerminal(node);
        if (terminal != null) {
            throw new ValidationException(this, terminal.getConnectable().getId() + " is already connected to the node " + node);
        }
    }

    private String getConnectionBus() {
        if (bus != null) {
            if ((connectableBus != null) && (!bus.equals(connectableBus))) {
                throw new ValidationException(this, "connection bus is different to connectable bus");
            }
            return bus;
        } else {
            return connectableBus;
        }
    }
}

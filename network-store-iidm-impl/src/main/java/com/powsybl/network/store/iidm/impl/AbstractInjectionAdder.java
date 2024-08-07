/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.VoltageLevel;
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

        VoltageLevel voltageLevel = getNetwork().getVoltageLevel(getVoltageLevelResource().getId());
        if (connectionBus != null) {
            checkBus(connectionBus, voltageLevel);
        } else {
            checkNode(node, voltageLevel);
        }
    }

    private String getConnectionBus() {
        if (bus != null) {
            if (connectableBus != null && !bus.equals(connectableBus)) {
                throw new ValidationException(this, "connection bus is different to connectable bus");
            }
            return bus;
        } else {
            return connectableBus;
        }
    }
}

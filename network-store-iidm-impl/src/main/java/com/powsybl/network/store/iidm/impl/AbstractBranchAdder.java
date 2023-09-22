/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.VoltageLevel;

/**
 *
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
abstract class AbstractBranchAdder<T extends AbstractBranchAdder<T>> extends AbstractIdentifiableAdder<T> {

    private Integer node1;

    private String bus1;

    private String connectableBus1;

    private String voltageLevelId1;

    private Integer node2;

    private String bus2;

    private String connectableBus2;

    private String voltageLevelId2;

    public AbstractBranchAdder(NetworkObjectIndex index) {
        super(index);
    }

    public Integer getNode1() {
        return node1;
    }

    public T setNode1(int node1) {
        this.node1 = node1;
        return (T) this;
    }

    public String getBus1() {
        return bus1;
    }

    public T setBus1(String bus1) {
        this.bus1 = bus1;
        if (connectableBus1 == null && bus1 != null) {
            connectableBus1 = bus1;
        }
        return (T) this;
    }

    public String getConnectableBus1() {
        return connectableBus1;
    }

    public T setConnectableBus1(String connectableBus1) {
        this.connectableBus1 = connectableBus1;
        return (T) this;
    }

    public String getVoltageLevelId1() {
        return voltageLevelId1;
    }

    public T setVoltageLevel1(String voltageLevelId1) {
        this.voltageLevelId1 = voltageLevelId1;
        return (T) this;
    }

    protected void checkNodeBus1() {
        String connectionBus = getConnectionBus1();
        if (node1 != null && connectionBus != null) {
            throw new ValidationException(this, "connection node 1 and connection bus 1 are exclusives");
        }

        if (node1 == null && connectionBus == null) {
            throw new ValidationException(this, "connectable bus 1 is not set");
        }

        if (connectionBus != null && index.getConfiguredBus(connectionBus).isEmpty()) {
            throw new ValidationException(this, "connectable bus 1 '" + connectionBus + " not found");
        }
    }

    private String getConnectionBus1() {
        if (bus1 != null) {
            if (!bus1.equals(connectableBus1)) {
                throw new ValidationException(this, "connection bus 1 is different to connectable bus 1");
            }
            return bus1;
        } else {
            return connectableBus1;
        }
    }

    protected VoltageLevel checkVoltageLevel1() {
        if (voltageLevelId1 == null) {
            String defaultVoltageLevelId1 = checkAndGetDefaultVoltageLevelId(connectableBus1);
            if (defaultVoltageLevelId1 == null) {
                throw new ValidationException(this, "first voltage level is not set and has no default value");
            } else {
                voltageLevelId1 = defaultVoltageLevelId1;
            }
        }
        VoltageLevel voltageLevel1 = getNetwork().getVoltageLevel(voltageLevelId1);
        if (voltageLevel1 == null) {
            throw new ValidationException(this, "first voltage level '" + voltageLevelId1 + "' not found");
        }
        return voltageLevel1;
    }

    public Integer getNode2() {
        return node2;
    }

    public T setNode2(int node2) {
        this.node2 = node2;
        return (T) this;
    }

    public String getBus2() {
        return bus2;
    }

    public T setBus2(String bus2) {
        this.bus2 = bus2;
        if (connectableBus2 == null && bus2 != null) {
            connectableBus2 = bus2;
        }
        return (T) this;
    }

    public String getConnectableBus2() {
        return connectableBus2;
    }

    public T setConnectableBus2(String connectableBus2) {
        this.connectableBus2 = connectableBus2;
        return (T) this;
    }

    public String getVoltageLevelId2() {
        return voltageLevelId2;
    }

    public T setVoltageLevel2(String voltageLevelId2) {
        this.voltageLevelId2 = voltageLevelId2;
        return (T) this;
    }

    protected void checkNodeBus2() {
        String connectionBus = getConnectionBus2();
        if (node2 != null && connectionBus != null) {
            throw new ValidationException(this, "connection node 2 and connection bus 2 are exclusives");
        }

        if (node2 == null && connectionBus == null) {
            throw new ValidationException(this, "connectable bus 2 is not set");
        }

        if (connectionBus != null && index.getConfiguredBus(connectionBus).isEmpty()) {
            throw new ValidationException(this, "connectable bus 2 '" + connectionBus + " not found");
        }
    }

    private String getConnectionBus2() {
        if (bus2 != null) {
            if (connectableBus2 != null && !bus2.equals(connectableBus2)) {
                throw new ValidationException(this, "connection bus 2 is different to connectable bus 2");
            }
            return bus2;
        } else {
            return connectableBus2;
        }
    }

    protected VoltageLevel checkVoltageLevel2() {
        if (voltageLevelId2 == null) {
            String defaultVoltageLevelId2 = checkAndGetDefaultVoltageLevelId(connectableBus2);
            if (defaultVoltageLevelId2 == null) {
                throw new ValidationException(this, "second voltage level is not set and has no default value");
            } else {
                voltageLevelId2 = defaultVoltageLevelId2;
            }
        }
        VoltageLevel voltageLevel2 = getNetwork().getVoltageLevel(voltageLevelId2);
        if (voltageLevel2 == null) {
            throw new ValidationException(this, "second voltage level '" + voltageLevelId2 + "' not found");
        }
        return voltageLevel2;
    }
}

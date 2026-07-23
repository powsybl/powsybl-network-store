/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.BusTopologyPoint;
import com.powsybl.iidm.network.TopologyKind;

import java.util.Objects;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class BusTopologyPointImpl implements BusTopologyPoint {

    private final String voltageLevelId;

    private final String connectableBusId;

    private final boolean connected;

    public BusTopologyPointImpl(String voltageLevelId, String connectableBusId, boolean connected) {
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId);
        this.connectableBusId = Objects.requireNonNull(connectableBusId);
        this.connected = connected;
    }

    @Override
    public TopologyKind getTopologyKind() {
        return TopologyKind.BUS_BREAKER;
    }

    @Override
    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    @Override
    public String getConnectableBusId() {
        return connectableBusId;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public String toString() {
        return "BusTopologyPoint(" +
                "voltageLevelId='" + voltageLevelId + '\'' +
                ", connectableBusId='" + connectableBusId + '\'' +
                ", connected=" + connected +
                ')';
    }
}

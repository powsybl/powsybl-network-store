/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.ConnectableType;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Vertex<T> {

    private final String id;

    private final ConnectableType connectableType;

    private final T nodeOrBus;

    private final String side;

    Vertex(String id, ConnectableType connectableType, T nodeOrBus, String side) {
        this.id = Objects.requireNonNull(id);
        this.connectableType = Objects.requireNonNull(connectableType);
        this.nodeOrBus = nodeOrBus;
        this.side = side;
    }

    public String getId() {
        return id;
    }

    public ConnectableType getConnectableType() {
        return connectableType;
    }

    public T getNodeOrBus() {
        return nodeOrBus;
    }

    public String getSide() {
        return side;
    }

    @Override
    public String toString() {
        return "Vertex(" +
                "id='" + id + '\'' +
                ", connectableType=" + connectableType +
                ", nodeOrBus=" + nodeOrBus +
                ", side=" + side +
                ')';
    }
}

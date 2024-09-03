/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.extensions.ConnectablePosition;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class FeederImpl implements ConnectablePosition.Feeder {

    private String name;

    private Integer order;

    private ConnectablePosition.Direction direction;

    public FeederImpl(String name) {
        this(name, null, null);
    }

    public FeederImpl(String name, int order) {
        this(name, order, null);
    }

    public FeederImpl(String name, ConnectablePosition.Direction direction) {
        this(name, null, direction);
    }

    public FeederImpl(String name, Integer order, ConnectablePosition.Direction direction) {
        this.name = Objects.requireNonNull(name);
        this.order = order;
        this.direction = Objects.requireNonNullElse(direction, ConnectablePosition.Direction.UNDEFINED);
    }

    @Override
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    @Override
    public ConnectablePosition.Feeder setName(String name) {
        this.name = Objects.requireNonNull(name);
        return this;
    }

    @Override
    public Optional<Integer> getOrder() {
        return Optional.ofNullable(order);
    }

    @Override
    public ConnectablePosition.Feeder setOrder(int order) {
        this.order = order;
        return this;
    }

    @Override
    public ConnectablePosition.Feeder removeOrder() {
        this.order = null;
        return this;
    }

    @Override
    public ConnectablePosition.Direction getDirection() {
        return direction;
    }

    @Override
    public ConnectablePosition.Feeder setDirection(ConnectablePosition.Direction direction) {
        this.direction = Objects.requireNonNull(direction);
        return this;
    }
}

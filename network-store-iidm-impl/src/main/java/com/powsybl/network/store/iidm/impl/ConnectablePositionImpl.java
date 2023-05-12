/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.network.store.model.ConnectablePositionAttributes;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public class ConnectablePositionImpl<C extends Connectable<C>> extends AbstractExtension<C>
        implements ConnectablePosition<C> {

    public class FeederImpl implements Feeder {

        private final Function<Connectable<C>, ConnectablePositionAttributes> getter;

        public FeederImpl(Function<Connectable<C>, ConnectablePositionAttributes> getter) {
            this.getter = Objects.requireNonNull(getter);
        }

        private ConnectablePositionAttributes getAttributes() {
            return getter.apply(getExtendable());
        }

        @Override
        public Optional<String> getName() {
            return Optional.ofNullable(getAttributes().getLabel());
        }

        @Override
        public Feeder setName(String name) {
            getAttributes().setLabel(Objects.requireNonNull(name));
            return this;
        }

        @Override
        public Optional<Integer> getOrder() {
            return Optional.ofNullable(getAttributes().getOrder());
        }

        @Override
        public Feeder removeOrder() {
            getAttributes().setOrder(null);
            return this;
        }

        @Override
        public Feeder setOrder(int order) {
            getAttributes().setOrder(order);
            return this;
        }

        @Override
        public Direction getDirection() {
            return Direction.valueOf(getAttributes().getDirection().name());
        }

        @Override
        public Feeder setDirection(Direction direction) {
            getAttributes().setDirection(Direction.valueOf(Objects.requireNonNull(direction).name()));
            return this;
        }
    }

    private final Function<Connectable<C>, ConnectablePositionAttributes> positionAttributesGetter;
    private final Function<Connectable<C>, ConnectablePositionAttributes> positionAttributesGetter1;
    private final Function<Connectable<C>, ConnectablePositionAttributes> positionAttributesGetter2;
    private final Function<Connectable<C>, ConnectablePositionAttributes> positionAttributesGetter3;

    public ConnectablePositionImpl(C connectable,
                                   Function<Connectable<C>, ConnectablePositionAttributes> positionAttributesGetter,
                                   Function<Connectable<C>, ConnectablePositionAttributes> positionAttributesGetter1,
                                   Function<Connectable<C>, ConnectablePositionAttributes> positionAttributesGetter2,
                                   Function<Connectable<C>, ConnectablePositionAttributes> positionAttributesGetter3) {
        super(connectable);
        this.positionAttributesGetter = positionAttributesGetter;
        this.positionAttributesGetter1 = positionAttributesGetter1;
        this.positionAttributesGetter2 = positionAttributesGetter2;
        this.positionAttributesGetter3 = positionAttributesGetter3;
    }

    private FeederImpl getFeeder(Function<Connectable<C>, ConnectablePositionAttributes> positionAttributesGetter) {
        return (positionAttributesGetter != null && positionAttributesGetter.apply(getExtendable()) != null) ?
                new FeederImpl(positionAttributesGetter) : null;
    }

    @Override
    public FeederImpl getFeeder() {
        return getFeeder(positionAttributesGetter);
    }

    @Override
    public FeederImpl getFeeder1() {
        return getFeeder(positionAttributesGetter1);
    }

    @Override
    public FeederImpl getFeeder2() {
        return getFeeder(positionAttributesGetter2);
    }

    @Override
    public FeederImpl getFeeder3() {
        return getFeeder(positionAttributesGetter3);
    }
}

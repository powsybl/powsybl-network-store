/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.network.store.model.ConnectableDirection;
import com.powsybl.network.store.model.ConnectablePositionAttributes;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public class ConnectablePositionImpl<C extends Connectable<C>>
        implements ConnectablePosition<C> {

    C connectable;

    @Override
    public C getExtendable() {
        return connectable;
    }

    @Override
    public void setExtendable(C connectable) {
        this.connectable = connectable;
    }

    public static class FeederImpl implements Feeder {

        private ConnectablePositionAttributes cpa;

        public FeederImpl(ConnectablePositionAttributes cpa) {
            this.cpa = cpa;
        }

        public ConnectablePositionAttributes getConnectablePositionAttributes() {
            return cpa;
        }

        @Override
        public Optional<String> getName() {
            return Optional.ofNullable(cpa.getLabel());
        }

        @Override
        public Feeder setName(String name) {
            cpa.setLabel(Objects.requireNonNull(name));
            return this;
        }

        @Override
        public Optional<Integer> getOrder() {
            return Optional.ofNullable(cpa.getOrder());
        }

        @Override
        public Feeder removeOrder() {
            cpa.setOrder(null);
            return this;
        }

        @Override
        public Feeder setOrder(int order) {
            cpa.setOrder(order);
            return this;
        }

        @Override
        public Direction getDirection() {
            return Direction.valueOf(cpa.getDirection().name());
        }

        @Override
        public Feeder setDirection(Direction direction) {
            cpa.setDirection(ConnectableDirection.valueOf(Objects.requireNonNull(direction).name()));
            return this;
        }
    }

    private FeederImpl feeder;
    private FeederImpl feeder1;
    private FeederImpl feeder2;
    private FeederImpl feeder3;

    public ConnectablePositionImpl(C connectable,
            FeederImpl feeder,
            FeederImpl feeder1,
            FeederImpl feeder2,
            FeederImpl feeder3) {
        ConnectablePosition.check(feeder, feeder1, feeder2, feeder3);
        this.connectable = connectable;
        this.feeder = feeder;
        this.feeder1 = feeder1;
        this.feeder2 = feeder2;
        this.feeder3 = feeder3;
    }

    @Override
    public FeederImpl getFeeder() {
        return feeder;
    }

    @Override
    public FeederImpl getFeeder1() {
        return feeder1;
    }

    @Override
    public FeederImpl getFeeder2() {
        return feeder2;
    }

    @Override
    public FeederImpl getFeeder3() {
        return feeder3;
    }

}

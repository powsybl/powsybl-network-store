/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.iidm.extensions.ConnectablePosition.Feeder;
import com.powsybl.sld.iidm.extensions.ConnectablePositionAdder;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public class ConnectablePositionAdderImpl<C extends Connectable<C>>
        extends AbstractExtensionAdder<C, ConnectablePosition<C>> implements ConnectablePositionAdder<C> {

    /**
     * Create a networkstore ConnectablePositionImpl with the appropriate feeders.
     * For example AbstractInjection uses feeder; AbstractBranch uses feeder1 and
     * feeder2; ThreeWindingTransformer uses feeder1 and feeder2 and feeder3.
     * @author Jon Harper <jon.harper at rte-france.com>
     */
    public static interface ConnectablePositionCreator<T extends Connectable<T>> {
        ConnectablePositionImpl<T> createConnectablePositionExtension(
                Feeder feeder, Feeder feeder1, Feeder feeder2, Feeder feeder3);
    }

    // Use the default in memory implementation to hold values.
    private com.powsybl.sld.iidm.extensions.ConnectablePositionImpl.FeederImpl feeder;
    private com.powsybl.sld.iidm.extensions.ConnectablePositionImpl.FeederImpl feeder1;
    private com.powsybl.sld.iidm.extensions.ConnectablePositionImpl.FeederImpl feeder2;
    private com.powsybl.sld.iidm.extensions.ConnectablePositionImpl.FeederImpl feeder3;

    ConnectablePositionAdderImpl(C connectable) {
        super(connectable);
    }

    private abstract static class AbstractFeederImplAdder<C extends Connectable<C>> implements FeederAdder<C> {
        protected String name;

        protected int order;

        protected ConnectablePosition.Direction direction;

        public FeederAdder<C> withName(String name) {
            this.name = name;
            return this;
        }

        public FeederAdder<C> withOrder(int order) {
            this.order = order;
            return this;
        }

        public FeederAdder<C> withDirection(ConnectablePosition.Direction direction) {
            this.direction = direction;
            return this;
        }

    }

    @Override
    public ConnectablePositionImpl<C> createExtension(C extendable) {
        return ((ConnectablePositionCreator) extendable).createConnectablePositionExtension(feeder, feeder1, feeder2,
                feeder3);
    }

    @Override
    public FeederAdder<C> newFeeder() {
        return new AbstractFeederImplAdder<C>() {
            @Override
            public ConnectablePositionAdder<C> add() {
                ConnectablePositionAdderImpl.this.feeder = new com.powsybl.sld.iidm.extensions.ConnectablePositionImpl.FeederImpl(
                        name, order,
                        direction);
                return ConnectablePositionAdderImpl.this;
            }
        };
    }

    @Override
    public FeederAdder<C> newFeeder1() {
        return new AbstractFeederImplAdder<C>() {
            @Override
            public ConnectablePositionAdder<C> add() {
                ConnectablePositionAdderImpl.this.feeder1 = new com.powsybl.sld.iidm.extensions.ConnectablePositionImpl.FeederImpl(
                        name, order,
                        direction);
                return ConnectablePositionAdderImpl.this;
            }
        };
    }

    @Override
    public FeederAdder<C> newFeeder2() {
        return new AbstractFeederImplAdder<C>() {
            @Override
            public ConnectablePositionAdder<C> add() {
                ConnectablePositionAdderImpl.this.feeder2 = new com.powsybl.sld.iidm.extensions.ConnectablePositionImpl.FeederImpl(
                        name, order,
                        direction);
                return ConnectablePositionAdderImpl.this;
            }
        };
    }

    @Override
    public FeederAdder<C> newFeeder3() {
        return new AbstractFeederImplAdder<C>() {
            @Override
            public ConnectablePositionAdder<C> add() {
                ConnectablePositionAdderImpl.this.feeder3 = new com.powsybl.sld.iidm.extensions.ConnectablePositionImpl.FeederImpl(
                        name, order,
                        direction);
                return ConnectablePositionAdderImpl.this;
            }
        };
    }
}

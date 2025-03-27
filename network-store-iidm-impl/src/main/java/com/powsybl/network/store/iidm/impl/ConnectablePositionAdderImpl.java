/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.network.store.iidm.impl.extensions.AbstractIidmExtensionAdder;
import com.powsybl.network.store.model.ConnectablePositionAttributes;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public class ConnectablePositionAdderImpl<C extends Connectable<C>>
        extends AbstractIidmExtensionAdder<C, ConnectablePosition<C>> implements ConnectablePositionAdder<C> {

    private FeederAdderImpl feederAdder;
    private FeederAdderImpl feederAdder1;
    private FeederAdderImpl feederAdder3;
    private FeederAdderImpl feederAdder2;

    ConnectablePositionAdderImpl(C connectable) {
        super(connectable);
    }

    private class FeederAdderImpl implements FeederAdder<C> {

        private String name;

        private Integer order;

        private ConnectablePosition.Direction direction;

        public FeederAdderImpl withName(String name) {
            this.name = name;
            return this;
        }

        public FeederAdderImpl withOrder(int order) {
            this.order = order;
            return this;
        }

        public FeederAdderImpl withDirection(ConnectablePosition.Direction direction) {
            this.direction = direction;
            return this;
        }

        @Override
        public ConnectablePositionAdderImpl<C> add() {
            return ConnectablePositionAdderImpl.this;
        }
    }

    private ConnectablePositionImpl<C> createInjectionExtension(C extendable) {
        if (feederAdder != null) {
            ((AbstractInjectionImpl<?, ?>) extendable).updateResource(res -> {
                res.getAttributes().setPosition(ConnectablePositionAttributes.builder()
                        .label(feederAdder.name)
                        .order(feederAdder.order)
                        .direction(feederAdder.direction)
                        .build());
            });
        }
        return new ConnectablePositionImpl<>(extendable,
            connectable -> ((AbstractInjectionImpl<?, ?>) connectable).getResource().getAttributes().getPosition(),
            null,
            null,
            null);
    }

    private ConnectablePositionImpl<C> createBranchExtension(C extendable) {
        ((AbstractBranchImpl<?, ?>) extendable).updateResource(res -> {
            if (feederAdder1 != null) {
                res.getAttributes().setPosition1(ConnectablePositionAttributes.builder()
                        .label(feederAdder1.name)
                        .order(feederAdder1.order)
                        .direction(feederAdder1.direction)
                        .build());
            }
            if (feederAdder2 != null) {
                res.getAttributes().setPosition2(ConnectablePositionAttributes.builder()
                        .label(feederAdder2.name)
                        .order(feederAdder2.order)
                        .direction(feederAdder2.direction)
                        .build());
            }
        });
        return new ConnectablePositionImpl<>(extendable,
            null,
            connectable -> ((AbstractBranchImpl<?, ?>) connectable).getResource().getAttributes().getPosition1(),
            connectable -> ((AbstractBranchImpl<?, ?>) connectable).getResource().getAttributes().getPosition2(),
            null);
    }

    private ConnectablePositionImpl<C> createThreeWindingsTransformerExtension(C extendable) {
        ((ThreeWindingsTransformerImpl) extendable).updateResource(res -> {
            if (feederAdder1 != null) {
                res.getAttributes().setPosition1(ConnectablePositionAttributes.builder()
                        .label(feederAdder1.name)
                        .order(feederAdder1.order)
                        .direction(feederAdder1.direction)
                        .build());
            }
            if (feederAdder2 != null) {
                res.getAttributes().setPosition2(ConnectablePositionAttributes.builder()
                        .label(feederAdder2.name)
                        .order(feederAdder2.order)
                        .direction(feederAdder2.direction)
                        .build());
            }
            if (feederAdder3 != null) {
                res.getAttributes().setPosition3(ConnectablePositionAttributes.builder()
                        .label(feederAdder3.name)
                        .order(feederAdder3.order)
                        .direction(feederAdder3.direction)
                        .build());
            }
        });
        return new ConnectablePositionImpl<>(extendable,
            null,
            connectable -> ((ThreeWindingsTransformerImpl) connectable).getResource().getAttributes().getPosition1(),
            connectable -> ((ThreeWindingsTransformerImpl) connectable).getResource().getAttributes().getPosition2(),
            connectable -> ((ThreeWindingsTransformerImpl) connectable).getResource().getAttributes().getPosition3());
    }

    @Override
    public ConnectablePositionImpl<C> createExtension(C extendable) {
        if (extendable instanceof AbstractInjectionImpl) {
            return createInjectionExtension(extendable);
        } else if (extendable instanceof AbstractBranchImpl) {
            return createBranchExtension(extendable);
        } else if (extendable instanceof ThreeWindingsTransformerImpl) {
            return createThreeWindingsTransformerExtension(extendable);
        } else {
            throw new IllegalStateException("Connectable cannot support position extension");
        }
    }

    @Override
    public FeederAdder<C> newFeeder() {
        feederAdder = new FeederAdderImpl();
        return feederAdder;
    }

    @Override
    public FeederAdder<C> newFeeder1() {
        feederAdder1 = new FeederAdderImpl();
        return feederAdder1;
    }

    @Override
    public FeederAdder<C> newFeeder2() {
        feederAdder2 = new FeederAdderImpl();
        return feederAdder2;
    }

    @Override
    public FeederAdder<C> newFeeder3() {
        feederAdder3 = new FeederAdderImpl();
        return feederAdder3;
    }
}

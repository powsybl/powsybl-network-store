/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.ThreeSides;
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

        private static final String LABEL = "label";
        private static final String ORDER = "order";
        private static final String DIRECTION = "direction";
        private static final String NOT_SUPPORTED = "Not supported side";
        private final ThreeSides side;
        private final Function<Connectable<C>, ConnectablePositionAttributes> getter;

        public FeederImpl(Function<Connectable<C>, ConnectablePositionAttributes> getter, ThreeSides side) {
            this.getter = Objects.requireNonNull(getter);
            this.side = side;
        }

        private ThreeSides getSide() {
            return side;
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
            String oldValue = getAttributes().getLabel();
            if (!Objects.equals(oldValue, name)) {
                if (getExtendable() instanceof AbstractInjectionImpl<?, ?> injection) {
                    updateInjectionLabelResource(injection, name);
                    notifyUpdate(injection, LABEL, oldValue, name);
                } else if (getExtendable() instanceof AbstractBranchImpl<?, ?> branch) {
                    updateBranchLabelResource(branch, name);
                    notifyUpdate(branch, LABEL, oldValue, name);
                } else if (getExtendable() instanceof ThreeWindingsTransformerImpl windingsTransformer) {
                    updateTransformerLabelResource(windingsTransformer, name);
                    notifyUpdate(windingsTransformer, LABEL, oldValue, name);
                } else {
                    throw new AssertionError("Unexpected connectable instance: " + getExtendable().getClass());
                }
            }
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
            Integer oldValue = getAttributes().getOrder();
            if (!Objects.equals(oldValue, order)) {
                if (getExtendable() instanceof AbstractInjectionImpl<?, ?> injection) {
                    updateInjectionOrderResource(injection, order);
                    notifyUpdate(injection, ORDER, oldValue, order);

                } else if (getExtendable() instanceof AbstractBranchImpl<?, ?> branch) {
                    updateBranchOrderResource(branch, order);
                    notifyUpdate(branch, ORDER, oldValue, order);
                } else if (getExtendable() instanceof ThreeWindingsTransformerImpl windingsTransformer) {
                    updateTransformerOrderResource(windingsTransformer, order);
                    notifyUpdate(windingsTransformer, ORDER, oldValue, order);
                } else {
                    throw new AssertionError("Unexpected connectable instance: " + getExtendable().getClass());
                }
            }
            return this;
        }

        @Override
        public Direction getDirection() {
            return Direction.valueOf(getAttributes().getDirection().name());
        }

        @Override
        public Feeder setDirection(Direction direction) {
            Direction oldValue = getAttributes().getDirection();
            if (!Objects.equals(oldValue, direction)) {
                if (getExtendable() instanceof AbstractInjectionImpl<?, ?> injection) {
                    updateInjectionDirectionResource(injection, direction);
                    notifyUpdate(injection, DIRECTION, oldValue, direction);
                } else if (getExtendable() instanceof AbstractBranchImpl<?, ?> branch) {
                    updateBranchDirectionResource(branch, direction);
                    notifyUpdate(branch, DIRECTION, oldValue, direction);
                } else if (getExtendable() instanceof ThreeWindingsTransformerImpl windingsTransformer) {
                    updateTransformerDirectionResource(windingsTransformer, direction);
                    notifyUpdate(windingsTransformer, DIRECTION, oldValue, direction);
                } else {
                    throw new AssertionError("Unexpected connectable instance: " + getExtendable().getClass());
                }
            }
            return this;
        }

        private void updateInjectionLabelResource(AbstractInjectionImpl<?, ?> injection, String name) {
            injection.updateResource(res -> res.getAttributes().getPosition().setLabel(Objects.requireNonNull(name)));
        }

        private void updateBranchLabelResource(AbstractBranchImpl<?, ?> branch, String name) {
            Objects.requireNonNull(name);
            ThreeSides sides = Objects.requireNonNull(getSide());
            branch.updateResource(res -> {
                if (sides == ThreeSides.ONE) {
                    res.getAttributes().getPosition1().setLabel(name);
                } else if (sides == ThreeSides.TWO) {
                    res.getAttributes().getPosition2().setLabel(name);
                } else {
                    throw new PowsyblException(NOT_SUPPORTED);
                }
            });
        }

        private void updateTransformerLabelResource(ThreeWindingsTransformerImpl windingsTransformer, String name) {
            Objects.requireNonNull(name);
            ThreeSides sides = Objects.requireNonNull(getSide());
            windingsTransformer.updateResource(res -> {
                if (sides == ThreeSides.ONE) {
                    res.getAttributes().getPosition1().setLabel(name);
                } else if (sides == ThreeSides.TWO) {
                    res.getAttributes().getPosition2().setLabel(name);
                } else if (sides == ThreeSides.THREE) {
                    res.getAttributes().getPosition3().setLabel(name);
                } else {
                    throw new PowsyblException(NOT_SUPPORTED);
                }
            });
        }

        private void updateInjectionOrderResource(AbstractInjectionImpl<?, ?> injection, int order) {
            injection.updateResource(res -> res.getAttributes().getPosition().setOrder(order));
        }

        private void updateBranchOrderResource(AbstractBranchImpl<?, ?> branch, int order) {
            ThreeSides sides = Objects.requireNonNull(getSide());
            branch.updateResource(res -> {
                if (sides == ThreeSides.ONE) {
                    res.getAttributes().getPosition1().setOrder(order);
                } else if (sides == ThreeSides.TWO) {
                    res.getAttributes().getPosition2().setOrder(order);
                } else {
                    throw new PowsyblException(NOT_SUPPORTED);
                }
            });
        }

        private void updateTransformerOrderResource(ThreeWindingsTransformerImpl windingsTransformer, int order) {
            ThreeSides sides = Objects.requireNonNull(getSide());
            windingsTransformer.updateResource(res -> {
                if (sides == ThreeSides.ONE) {
                    res.getAttributes().getPosition1().setOrder(order);
                } else if (sides == ThreeSides.TWO) {
                    res.getAttributes().getPosition2().setOrder(order);
                } else if (sides == ThreeSides.THREE) {
                    res.getAttributes().getPosition3().setOrder(order);
                } else {
                    throw new PowsyblException(NOT_SUPPORTED);
                }
            });
        }

        private void updateInjectionDirectionResource(AbstractInjectionImpl<?, ?> injection, Direction direction) {
            injection.updateResource(res -> res.getAttributes().getPosition()
                    .setDirection(Direction.valueOf(Objects.requireNonNull(direction).name())));
        }

        private void updateBranchDirectionResource(AbstractBranchImpl<?, ?> branch, Direction direction) {
            Objects.requireNonNull(direction);
            ThreeSides sides = Objects.requireNonNull(getSide());
            branch.updateResource(res -> {
                if (sides == ThreeSides.ONE) {
                    res.getAttributes().getPosition1().setDirection(direction);
                } else if (sides == ThreeSides.TWO) {
                    res.getAttributes().getPosition2().setDirection(direction);
                } else {
                    throw new PowsyblException(NOT_SUPPORTED);
                }
            });
        }

        private void updateTransformerDirectionResource(ThreeWindingsTransformerImpl windingsTransformer, Direction direction) {
            Objects.requireNonNull(direction);
            ThreeSides sides = Objects.requireNonNull(getSide());
            windingsTransformer.updateResource(res -> {
                if (sides == ThreeSides.ONE) {
                    res.getAttributes().getPosition1().setDirection(direction);
                } else if (sides == ThreeSides.TWO) {
                    res.getAttributes().getPosition2().setDirection(direction);
                } else if (sides == ThreeSides.THREE) {
                    res.getAttributes().getPosition3().setDirection(direction);
                } else {
                    throw new PowsyblException(NOT_SUPPORTED);
                }
            });
        }

        private void notifyUpdate(AbstractConnectableImpl<?, ?> connectable, String attribute, Object oldValue, Object newValue) {
            String variantId = connectable.getNetwork().getVariantManager().getWorkingVariantId();
            connectable.getNetwork().getIndex().notifyUpdate(getExtendable(), attribute, variantId, oldValue, newValue);
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

    private FeederImpl getFeeder(Function<Connectable<C>, ConnectablePositionAttributes> positionAttributesGetter, ThreeSides side) {
        return (positionAttributesGetter != null && positionAttributesGetter.apply(getExtendable()) != null) ?
                new FeederImpl(positionAttributesGetter, side) : null;
    }

    @Override
    public FeederImpl getFeeder() {
        return getFeeder(positionAttributesGetter, null);
    }

    @Override
    public FeederImpl getFeeder1() {
        return getFeeder(positionAttributesGetter1, ThreeSides.ONE);
    }

    @Override
    public FeederImpl getFeeder2() {
        return getFeeder(positionAttributesGetter2, ThreeSides.TWO);
    }

    @Override
    public FeederImpl getFeeder3() {
        return getFeeder(positionAttributesGetter3, ThreeSides.THREE);
    }
}

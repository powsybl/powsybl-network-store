/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.Extension;
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
        private static final String UNEXPECTED_EXTENDABLE = "Unexpected extendable instance: ";
        private final ThreeSides side;
        private final Function<Connectable<C>, ConnectablePositionAttributes> getter;
        private final ConnectablePosition<C> connectablePosition;

        public FeederImpl(ConnectablePosition<C> connectablePosition, Function<Connectable<C>, ConnectablePositionAttributes> getter, ThreeSides side) {
            this.connectablePosition = connectablePosition;
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
                    updateInjectionLabelResource(connectablePosition, injection, name, oldValue, name);
                } else if (getExtendable() instanceof AbstractBranchImpl<?, ?> branch) {
                    updateBranchLabelResource(connectablePosition, branch, name, oldValue, name);
                } else if (getExtendable() instanceof ThreeWindingsTransformerImpl windingsTransformer) {
                    updateTransformerLabelResource(connectablePosition, windingsTransformer, name, oldValue, name);
                } else {
                    throw new AssertionError(UNEXPECTED_EXTENDABLE + getExtendable().getClass());
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
                    updateInjectionOrderResource(connectablePosition, injection, order, oldValue, order);
                } else if (getExtendable() instanceof AbstractBranchImpl<?, ?> branch) {
                    updateBranchOrderResource(connectablePosition, branch, order, oldValue, order);
                } else if (getExtendable() instanceof ThreeWindingsTransformerImpl windingsTransformer) {
                    updateTransformerOrderResource(connectablePosition, windingsTransformer, order, oldValue, order);
                } else {
                    throw new AssertionError(UNEXPECTED_EXTENDABLE + getExtendable().getClass());
                }
            }
            return this;
        }

        @Override
        public Direction getDirection() {
            return getAttributes().getDirection();
        }

        @Override
        public Feeder setDirection(Direction direction) {
            Direction oldValue = getAttributes().getDirection();
            if (!Objects.equals(oldValue, direction)) {
                if (getExtendable() instanceof AbstractInjectionImpl<?, ?> injection) {
                    updateInjectionDirectionResource(connectablePosition, injection, direction, oldValue, direction);
                } else if (getExtendable() instanceof AbstractBranchImpl<?, ?> branch) {
                    updateBranchDirectionResource(connectablePosition, branch, direction, oldValue, direction);
                } else if (getExtendable() instanceof ThreeWindingsTransformerImpl windingsTransformer) {
                    updateTransformerDirectionResource(connectablePosition, windingsTransformer, direction, oldValue, direction);
                } else {
                    throw new AssertionError(UNEXPECTED_EXTENDABLE + getExtendable().getClass());
                }
            }
            return this;
        }

        private void updateInjectionLabelResource(Extension<?> extension, AbstractInjectionImpl<?, ?> injection, String name, Object oldValue, Object newValue) {
            injection.updateResourceExtension(extension,
                res -> res.getAttributes().getPosition().setLabel(Objects.requireNonNull(name)),
                LABEL, oldValue, newValue);
        }

        private void updateBranchLabelResource(Extension<?> extension, AbstractBranchImpl<?, ?> branch, String name, Object oldValue, Object newValue) {
            Objects.requireNonNull(name);
            ThreeSides sides = Objects.requireNonNull(getSide());
            branch.updateResourceExtension(extension, res -> {
                if (sides == ThreeSides.ONE) {
                    res.getAttributes().getPosition1().setLabel(name);
                } else if (sides == ThreeSides.TWO) {
                    res.getAttributes().getPosition2().setLabel(name);
                } else {
                    throw new PowsyblException(NOT_SUPPORTED);
                }
            }, LABEL, oldValue, newValue);
        }

        private void updateTransformerLabelResource(Extension<?> extension, ThreeWindingsTransformerImpl windingsTransformer, String name, Object oldValue, Object newValue) {
            Objects.requireNonNull(name);
            ThreeSides sides = Objects.requireNonNull(getSide());
            windingsTransformer.updateResourceExtension(extension, res -> {
                if (sides == ThreeSides.ONE) {
                    res.getAttributes().getPosition1().setLabel(name);
                } else if (sides == ThreeSides.TWO) {
                    res.getAttributes().getPosition2().setLabel(name);
                } else if (sides == ThreeSides.THREE) {
                    res.getAttributes().getPosition3().setLabel(name);
                } else {
                    throw new PowsyblException(NOT_SUPPORTED);
                }
            }, LABEL, oldValue, newValue);
        }

        private void updateInjectionOrderResource(Extension<?> extension, AbstractInjectionImpl<?, ?> injection, int order, Object oldValue, Object newValue) {
            injection.updateResourceExtension(extension,
                res -> res.getAttributes().getPosition().setOrder(order),
                ORDER, oldValue, newValue);
        }

        private void updateBranchOrderResource(Extension<?> extension, AbstractBranchImpl<?, ?> branch, int order, Object oldValue, Object newValue) {
            ThreeSides sides = Objects.requireNonNull(getSide());
            branch.updateResourceExtension(extension,
                res -> {
                    if (sides == ThreeSides.ONE) {
                        res.getAttributes().getPosition1().setOrder(order);
                    } else if (sides == ThreeSides.TWO) {
                        res.getAttributes().getPosition2().setOrder(order);
                    } else {
                        throw new PowsyblException(NOT_SUPPORTED);
                    }
                }, ORDER, oldValue, newValue);
        }

        private void updateTransformerOrderResource(Extension<?> extension, ThreeWindingsTransformerImpl windingsTransformer, int order, Object oldValue, Object newValue) {
            ThreeSides sides = Objects.requireNonNull(getSide());
            windingsTransformer.updateResourceExtension(extension,
                res -> {
                    if (sides == ThreeSides.ONE) {
                        res.getAttributes().getPosition1().setOrder(order);
                    } else if (sides == ThreeSides.TWO) {
                        res.getAttributes().getPosition2().setOrder(order);
                    } else if (sides == ThreeSides.THREE) {
                        res.getAttributes().getPosition3().setOrder(order);
                    } else {
                        throw new PowsyblException(NOT_SUPPORTED);
                    }
                }, ORDER, oldValue, newValue);
        }

        private void updateInjectionDirectionResource(Extension<?> extension, AbstractInjectionImpl<?, ?> injection, Direction direction, Object oldValue, Object newValue) {
            injection.updateResourceExtension(extension, res -> res.getAttributes().getPosition()
                .setDirection(Direction.valueOf(Objects.requireNonNull(direction).name())),
                    DIRECTION, oldValue, newValue);
        }

        private void updateBranchDirectionResource(Extension<?> extension, AbstractBranchImpl<?, ?> branch, Direction direction, Object oldValue, Object newValue) {
            Objects.requireNonNull(direction);
            ThreeSides sides = Objects.requireNonNull(getSide());
            branch.updateResourceExtension(extension, res -> {
                if (sides == ThreeSides.ONE) {
                    res.getAttributes().getPosition1().setDirection(direction);
                } else if (sides == ThreeSides.TWO) {
                    res.getAttributes().getPosition2().setDirection(direction);
                } else {
                    throw new PowsyblException(NOT_SUPPORTED);
                }
            }, DIRECTION, oldValue, newValue);
        }

        private void updateTransformerDirectionResource(Extension<?> extension, ThreeWindingsTransformerImpl windingsTransformer, Direction direction, Object oldValue, Object newValue) {
            Objects.requireNonNull(direction);
            ThreeSides sides = Objects.requireNonNull(getSide());
            windingsTransformer.updateResourceExtension(extension, res -> {
                if (sides == ThreeSides.ONE) {
                    res.getAttributes().getPosition1().setDirection(direction);
                } else if (sides == ThreeSides.TWO) {
                    res.getAttributes().getPosition2().setDirection(direction);
                } else if (sides == ThreeSides.THREE) {
                    res.getAttributes().getPosition3().setDirection(direction);
                } else {
                    throw new PowsyblException(NOT_SUPPORTED);
                }
            }, DIRECTION, oldValue, newValue);
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
                new FeederImpl(this, positionAttributesGetter, side) : null;
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

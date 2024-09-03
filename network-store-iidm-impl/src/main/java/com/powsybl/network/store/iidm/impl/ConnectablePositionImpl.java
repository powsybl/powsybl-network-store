/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

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
            String oldValue = getAttributes().getLabel();
            if (!Objects.equals(oldValue, name)) {
                if (getExtendable() instanceof AbstractInjectionImpl<?, ?> injection) {
                    injection.updateResource(res -> res.getAttributes().getPosition().setLabel(Objects.requireNonNull(name)));
                    String variantId = injection.getNetwork().getVariantManager().getWorkingVariantId();
                    injection.getNetwork().getIndex().notifyUpdate(getExtendable(), "label", variantId, oldValue, name);
                } else if (getExtendable() instanceof AbstractBranchImpl<?, ?> branch) {
                    branch.updateResource(res -> {
                        if (getSide() == ThreeSides.ONE) {
                            res.getAttributes().getPosition1().setLabel(Objects.requireNonNull(name));
                        } else if (getSide() == ThreeSides.TWO) {
                            res.getAttributes().getPosition2().setLabel(Objects.requireNonNull(name));
                        }
                    });
                    String variantId = branch.getNetwork().getVariantManager().getWorkingVariantId();
                    branch.getNetwork().getIndex().notifyUpdate(getExtendable(), "label", variantId, oldValue, name);
                } else if (getExtendable() instanceof ThreeWindingsTransformerImpl windingsTransformer) {
                    windingsTransformer.updateResource(res -> {
                        if (getSide() == ThreeSides.ONE) {
                            res.getAttributes().getPosition1().setLabel(Objects.requireNonNull(name));
                        } else if (getSide() == ThreeSides.TWO) {
                            res.getAttributes().getPosition2().setLabel(Objects.requireNonNull(name));
                        } else if (getSide() == ThreeSides.THREE) {
                            res.getAttributes().getPosition3().setLabel(Objects.requireNonNull(name));
                        }
                    });
                    String variantId = windingsTransformer.getNetwork().getVariantManager().getWorkingVariantId();
                    windingsTransformer.getNetwork().getIndex().notifyUpdate(getExtendable(), "label", variantId, oldValue, name);
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
                    injection.updateResource(res -> res.getAttributes().getPosition().setOrder(order));
                    String variantId = injection.getNetwork().getVariantManager().getWorkingVariantId();
                    injection.getNetwork().getIndex().notifyUpdate(getExtendable(), "order", variantId, oldValue, order);

                } else if (getExtendable() instanceof AbstractBranchImpl<?, ?> branch) {
                    branch.updateResource(res -> {
                        if (getSide() == ThreeSides.ONE) {
                            res.getAttributes().getPosition1().setOrder(Objects.requireNonNull(order));
                        } else if (getSide() == ThreeSides.TWO) {
                            res.getAttributes().getPosition2().setOrder(Objects.requireNonNull(order));
                        }
                    });
                    String variantId = branch.getNetwork().getVariantManager().getWorkingVariantId();
                    branch.getNetwork().getIndex().notifyUpdate(getExtendable(), "order", variantId, oldValue, order);
                } else if (getExtendable() instanceof ThreeWindingsTransformerImpl windingsTransformer) {
                    windingsTransformer.updateResource(res -> {
                        if (getSide() == ThreeSides.ONE) {
                            res.getAttributes().getPosition1().setOrder(Objects.requireNonNull(order));
                        } else if (getSide() == ThreeSides.TWO) {
                            res.getAttributes().getPosition2().setOrder(Objects.requireNonNull(order));
                        } else if (getSide() == ThreeSides.THREE) {
                            res.getAttributes().getPosition3().setOrder(Objects.requireNonNull(order));
                        }
                    });
                    String variantId = windingsTransformer.getNetwork().getVariantManager().getWorkingVariantId();
                    windingsTransformer.getNetwork().getIndex().notifyUpdate(getExtendable(), "order", variantId, oldValue, order);
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
                    injection.updateResource(res -> res.getAttributes().getPosition().setDirection(Direction.valueOf(Objects.requireNonNull(direction).name())));
                    String variantId = injection.getNetwork().getVariantManager().getWorkingVariantId();
                    injection.getNetwork().getIndex().notifyUpdate(getExtendable(), "direction", variantId, oldValue, direction);

                } else if (getExtendable() instanceof AbstractBranchImpl<?, ?> branch) {
                    branch.updateResource(res -> {
                        if (getSide() == ThreeSides.ONE) {
                            res.getAttributes().getPosition1().setDirection(Objects.requireNonNull(direction));
                        } else if (getSide() == ThreeSides.TWO) {
                            res.getAttributes().getPosition2().setDirection(Objects.requireNonNull(direction));
                        }
                    });
                    String variantId = branch.getNetwork().getVariantManager().getWorkingVariantId();
                    branch.getNetwork().getIndex().notifyUpdate(getExtendable(), "direction", variantId, oldValue, direction);
                } else if (getExtendable() instanceof ThreeWindingsTransformerImpl windingsTransformer) {
                    windingsTransformer.updateResource(res -> {
                        if (getSide() == ThreeSides.ONE) {
                            res.getAttributes().getPosition1().setDirection(Objects.requireNonNull(direction));
                        } else if (getSide() == ThreeSides.TWO) {
                            res.getAttributes().getPosition2().setDirection(Objects.requireNonNull(direction));
                        } else if (getSide() == ThreeSides.THREE) {
                            res.getAttributes().getPosition3().setDirection(Objects.requireNonNull(direction));
                        }
                    });
                    String variantId = windingsTransformer.getNetwork().getVariantManager().getWorkingVariantId();
                    windingsTransformer.getNetwork().getIndex().notifyUpdate(getExtendable(), "direction", variantId, oldValue, direction);
                }
            }
            return this;
        }
    }

    public ThreeSides side;
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
                new FeederImpl(connectable -> {
                    ConnectablePositionAttributes attributes = positionAttributesGetter.apply(connectable);
                    this.side = side;
                    return attributes;
                }) : null;
    }

    public ThreeSides getSide() {
        return side;
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

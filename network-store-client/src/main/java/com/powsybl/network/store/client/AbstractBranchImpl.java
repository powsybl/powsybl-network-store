/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.*;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractBranchImpl<T extends Branch<T>, U extends BranchAttributes> extends AbstractIdentifiableImpl<T, U> implements CurrentLimitsOwner<Branch.Side> {

    private final Terminal terminal1;

    private final Terminal terminal2;

    private CurrentLimits currentLimits1;

    private CurrentLimits currentLimits2;

    protected AbstractBranchImpl(NetworkObjectIndex index, Resource<U> resource) {
        super(index, resource);

        terminal1 = TerminalNodeBreakerImpl.create(index, resource, attributes -> new InjectionAttributes() {

            @Override
            public String getName() {
                return attributes.getName();
            }

            @Override
            public Map<String, String> getProperties() {
                return attributes.getProperties();
            }

            @Override
            public void setProperties(Map<String, String> properties) {
                attributes.setProperties(properties);
            }

            @Override
            public String getVoltageLevelId() {
                return attributes.getVoltageLevelId1();
            }

            @Override
            public Integer getNode() {
                return attributes.getNode1();
            }

            @Override
            public double getP() {
                return attributes.getP1();
            }

            @Override
            public void setP(double p) {
                attributes.setP1(p);
            }

            @Override
            public double getQ() {
                return attributes.getQ1();
            }

            @Override
            public void setQ(double q) {
                attributes.setQ1(q);
            }

            @Override
            public ConnectablePositionAttributes getPosition() {
                return attributes.getPosition1();
            }

            @Override
            public void setPosition(ConnectablePositionAttributes position) {
                attributes.setPosition1(position);
            }
        }, getBranch());

        terminal2 = TerminalNodeBreakerImpl.create(index, resource, attributes -> new InjectionAttributes() {

            @Override
            public String getName() {
                return attributes.getName();
            }

            @Override
            public Map<String, String> getProperties() {
                return attributes.getProperties();
            }

            @Override
            public void setProperties(Map<String, String> properties) {
                attributes.setProperties(properties);
            }

            @Override
            public String getVoltageLevelId() {
                return attributes.getVoltageLevelId2();
            }

            @Override
            public Integer getNode() {
                return attributes.getNode2();
            }

            @Override
            public double getP() {
                return attributes.getP2();
            }

            @Override
            public void setP(double p) {
                attributes.setP2(p);
            }

            @Override
            public double getQ() {
                return attributes.getQ2();
            }

            @Override
            public void setQ(double q) {
                attributes.setQ2(q);
            }

            @Override
            public ConnectablePositionAttributes getPosition() {
                return attributes.getPosition2();
            }

            @Override
            public void setPosition(ConnectablePositionAttributes position) {
                attributes.setPosition2(position);
            }
        }, getBranch());
    }

    protected abstract T getBranch();

    public List<? extends Terminal> getTerminals() {
        return Arrays.asList(terminal1, terminal2);
    }

    public Terminal getTerminal1() {
        return terminal1;
    }

    public Terminal getTerminal2() {
        return terminal2;
    }

    public Terminal getTerminal(Branch.Side side) {
        switch (side) {
            case ONE:
                return terminal1;
            case TWO:
                return terminal2;
            default:
                throw new UnsupportedOperationException();
        }
    }

    public Terminal getTerminal(String voltageLevelId) {
        Objects.requireNonNull(voltageLevelId);
        if (terminal1.getVoltageLevel().getId().equals(voltageLevelId)) {
            return terminal1;
        } else if (terminal2.getVoltageLevel().getId().equals(voltageLevelId)) {
            return terminal2;
        } else {
            throw new AssertionError();
        }
    }

    public Branch.Side getSide(Terminal terminal) {
        if (terminal == terminal1) {
            return Branch.Side.ONE;
        } else if (terminal == terminal2) {
            return Branch.Side.TWO;
        } else {
            throw new AssertionError();
        }
    }

    @Override
    public void setCurrentLimits(Branch.Side side, CurrentLimitsAttributes currentLimits) {
        if (side == Branch.Side.ONE) {
            currentLimits1 = new CurrentLimitsImpl(currentLimits);
        } else if (side == Branch.Side.TWO) {
            currentLimits2 = new CurrentLimitsImpl(currentLimits);
        }
    }

    public CurrentLimitsAdder newCurrentLimits1() {
        return new CurrentLimitsAdderImpl(Branch.Side.ONE, this);
    }

    public CurrentLimitsAdder newCurrentLimits2() {
        return new CurrentLimitsAdderImpl(Branch.Side.TWO, this);
    }

    public void remove() {
        throw new UnsupportedOperationException("TODO");
    }

    public CurrentLimits getCurrentLimits(Branch.Side side) {
        throw new UnsupportedOperationException("TODO");
    }

    public CurrentLimits getCurrentLimits1() {
        throw new UnsupportedOperationException("TODO");
    }

    public CurrentLimits getCurrentLimits2() {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isOverloaded() {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isOverloaded(float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    public int getOverloadDuration() {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean checkPermanentLimit(Branch.Side side, float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean checkPermanentLimit(Branch.Side side) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean checkPermanentLimit1(float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean checkPermanentLimit1() {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean checkPermanentLimit2(float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean checkPermanentLimit2() {
        throw new UnsupportedOperationException("TODO");
    }

    public Branch.Overload checkTemporaryLimits(Branch.Side side, float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    public Branch.Overload checkTemporaryLimits(Branch.Side side) {
        throw new UnsupportedOperationException("TODO");
    }

    public Branch.Overload checkTemporaryLimits1(float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    public Branch.Overload checkTemporaryLimits1() {
        throw new UnsupportedOperationException("TODO");
    }

    public Branch.Overload checkTemporaryLimits2(float limitReduction) {
        throw new UnsupportedOperationException("TODO");
    }

    public Branch.Overload checkTemporaryLimits2() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public <E extends Extension<T>> void addExtension(Class<? super E> type, E extension) {
        super.addExtension(type, extension);
        if (type == ConnectablePosition.class) {
            ConnectablePosition position = (ConnectablePosition) extension;
            resource.getAttributes().setPosition1(ConnectablePositionAttributes.builder()
                    .label(position.getFeeder1().getName())
                    .order(position.getFeeder1().getOrder())
                    .direction(ConnectableDirection.valueOf(position.getFeeder1().getDirection().name()))
                    .build());
            resource.getAttributes().setPosition2(ConnectablePositionAttributes.builder()
                    .label(position.getFeeder2().getName())
                    .order(position.getFeeder2().getOrder())
                    .direction(ConnectableDirection.valueOf(position.getFeeder2().getDirection().name()))
                    .build());
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends Extension<T>> E createConnectablePositionExtension() {
        E extension = null;
        ConnectablePositionAttributes positionAttributes1 = resource.getAttributes().getPosition1();
        ConnectablePositionAttributes positionAttributes2 = resource.getAttributes().getPosition2();
        if (positionAttributes1 != null && positionAttributes2 != null) {
            extension = (E) new ConnectablePosition<>(getBranch(),
                                                      null,
                                                      new ConnectablePosition.Feeder(positionAttributes1.getLabel(),
                                                                                     positionAttributes1.getOrder(),
                                                                                     ConnectablePosition.Direction.valueOf(positionAttributes1.getDirection().name())),
                                                      new ConnectablePosition.Feeder(positionAttributes2.getLabel(),
                                                                                     positionAttributes2.getOrder(),
                                                                                     ConnectablePosition.Direction.valueOf(positionAttributes2.getDirection().name())),
                                                      null);
        }
        return extension;
    }

    @Override
    public <E extends Extension<T>> E getExtension(Class<? super E> type) {
        E extension = super.getExtension(type);
        if (type == ConnectablePosition.class) {
            extension = createConnectablePositionExtension();
        }
        return extension;
    }

    @Override
    public <E extends Extension<T>> E getExtensionByName(String name) {
        E extension = super.getExtensionByName(name);
        if (name.equals("position")) {
            extension = createConnectablePositionExtension();
        }
        return extension;
    }
}

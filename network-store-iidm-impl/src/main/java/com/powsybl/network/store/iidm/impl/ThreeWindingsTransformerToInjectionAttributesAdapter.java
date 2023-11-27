/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.network.store.model.*;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ThreeWindingsTransformerToInjectionAttributesAdapter implements InjectionAttributes {

    private final ThreeWindingsTransformerImpl.LegImpl leg;

    private final ThreeWindingsTransformerAttributes attributes;

    private final ThreeSides side;

    public ThreeWindingsTransformerToInjectionAttributesAdapter(ThreeWindingsTransformerImpl.LegImpl leg, ThreeWindingsTransformerAttributes attributes, ThreeSides side) {
        this.leg = Objects.requireNonNull(leg);
        this.attributes = attributes;
        this.side = side;
    }

    private IllegalStateException createUnknownSideException() {
        return new IllegalStateException("Unknown side: " + side);
    }

    private LegAttributes getLegAttributes() {
        switch (side) {
            case ONE:
                return attributes.getLeg1();
            case TWO:
                return attributes.getLeg2();
            case THREE:
                return attributes.getLeg3();
            default:
                throw createUnknownSideException();
        }
    }

    @Override
    public Resource getResource() {
        return attributes.getResource();
    }

    @Override
    public void setResource(Resource resource) {
        attributes.setResource(resource);
    }

    @Override
    public String getName() {
        return attributes.getName();
    }

    @Override
    public void setName(String name) {
        attributes.setName(name);
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
        return getLegAttributes().getVoltageLevelId();
    }

    @Override
    public void setVoltageLevelId(String voltageLevelId) {
        getLegAttributes().setVoltageLevelId(voltageLevelId);
    }

    @Override
    public Integer getNode() {
        return getLegAttributes().getNode();
    }

    @Override
    public void setNode(Integer node) {
        getLegAttributes().setNode(node);
    }

    @Override
    public String getBus() {
        return getLegAttributes().getBus();
    }

    @Override
    public void setBus(String bus) {
        String oldValue = getLegAttributes().getBus();
        getLegAttributes().setBus(bus);
        leg.notifyUpdate("bus", oldValue, bus);
    }

    @Override
    public void setConnectableBus(String bus) {
        getLegAttributes().setConnectableBus(bus);
    }

    @Override
    public String getConnectableBus() {
        return getLegAttributes().getConnectableBus();
    }

    @Override
    public double getP() {
        switch (side) {
            case ONE:
                return attributes.getP1();
            case TWO:
                return attributes.getP2();
            case THREE:
                return attributes.getP3();
            default:
                throw createUnknownSideException();
        }
    }

    @Override
    public void setP(double p) {
        switch (side) {
            case ONE:
                attributes.setP1(p);
                break;
            case TWO:
                attributes.setP2(p);
                break;
            case THREE:
                attributes.setP3(p);
                break;
            default:
                throw createUnknownSideException();
        }
    }

    @Override
    public double getQ() {
        switch (side) {
            case ONE:
                return attributes.getQ1();
            case TWO:
                return attributes.getQ2();
            case THREE:
                return attributes.getQ3();
            default:
                throw createUnknownSideException();
        }
    }

    @Override
    public void setQ(double q) {
        switch (side) {
            case ONE:
                attributes.setQ1(q);
                break;
            case TWO:
                attributes.setQ2(q);
                break;
            case THREE:
                attributes.setQ3(q);
                break;
            default:
                throw createUnknownSideException();
        }
    }

    @Override
    public ConnectablePositionAttributes getPosition() {
        switch (side) {
            case ONE:
                return attributes.getPosition1();
            case TWO:
                return attributes.getPosition2();
            case THREE:
                return attributes.getPosition3();
            default:
                throw createUnknownSideException();
        }
    }

    @Override
    public void setPosition(ConnectablePositionAttributes position) {
        switch (side) {
            case ONE:
                attributes.setPosition1(position);
                break;
            case TWO:
                attributes.setPosition2(position);
                break;
            case THREE:
                attributes.setPosition3(position);
                break;
            default:
                throw createUnknownSideException();
        }
    }

    @Override
    public boolean isFictitious() {
        return attributes.isFictitious();
    }

    @Override
    public void setFictitious(boolean fictitious) {
        attributes.setFictitious(fictitious);
    }

    @Override
    public Set<String> getAliasesWithoutType() {
        return attributes.getAliasesWithoutType();
    }

    @Override
    public void setAliasesWithoutType(Set<String> aliasesWothoutType) {
        attributes.setAliasesWithoutType(aliasesWothoutType);
    }

    @Override
    public Map<String, String> getAliasByType() {
        return attributes.getAliasByType();
    }

    @Override
    public void setAliasByType(Map<String, String> aliasByType) {
        attributes.setAliasByType(aliasByType);
    }
}

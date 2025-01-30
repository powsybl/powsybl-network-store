/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.network.store.model.*;

import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ThreeWindingsTransformerToInjectionAttributesAdapter extends AbstractIdentifiableToInjectionAttributesAdapter<ThreeWindingsTransformerAttributes> {

    private final ThreeSides side;

    public ThreeWindingsTransformerToInjectionAttributesAdapter(ThreeWindingsTransformerAttributes attributes, ThreeSides side) {
        super(attributes);
        this.side = side;
    }

    private IllegalStateException createUnknownSideException() {
        return new IllegalStateException("Unknown side: " + side);
    }

    private LegAttributes getLegAttributes() {
        return switch (side) {
            case ONE -> attributes.getLeg1();
            case TWO -> attributes.getLeg2();
            case THREE -> attributes.getLeg3();
        };
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
        getLegAttributes().setBus(bus);
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
        return switch (side) {
            case ONE -> attributes.getP1();
            case TWO -> attributes.getP2();
            case THREE -> attributes.getP3();
        };
    }

    @Override
    public void setP(double p) {
        switch (side) {
            case ONE -> attributes.setP1(p);
            case TWO -> attributes.setP2(p);
            case THREE -> attributes.setP3(p);
            default -> throw createUnknownSideException();
        }
    }

    @Override
    public double getQ() {
        return switch (side) {
            case ONE -> attributes.getQ1();
            case TWO -> attributes.getQ2();
            case THREE -> attributes.getQ3();
        };
    }

    @Override
    public void setQ(double q) {
        switch (side) {
            case ONE -> attributes.setQ1(q);
            case TWO -> attributes.setQ2(q);
            case THREE -> attributes.setQ3(q);
            default -> throw createUnknownSideException();
        }
    }

    @Override
    public ConnectablePositionAttributes getPosition() {
        return switch (side) {
            case ONE -> attributes.getPosition1();
            case TWO -> attributes.getPosition2();
            case THREE -> attributes.getPosition3();
        };
    }

    @Override
    public void setPosition(ConnectablePositionAttributes position) {
        switch (side) {
            case ONE -> attributes.setPosition1(position);
            case TWO -> attributes.setPosition2(position);
            case THREE -> attributes.setPosition3(position);
            default -> throw createUnknownSideException();
        }
    }

    @Override
    public Set<RegulatingEquipmentIdentifier> getRegulatingEquipments() {
        return attributes.getRegulatingEquipments();
    }

    @Override
    public void setRegulatingEquipments(Set<RegulatingEquipmentIdentifier> regulatingEquipments) {
        attributes.setRegulatingEquipments(regulatingEquipments);
    }
}

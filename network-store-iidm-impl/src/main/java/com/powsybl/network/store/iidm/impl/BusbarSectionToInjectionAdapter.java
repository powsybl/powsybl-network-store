/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.network.store.model.BusbarSectionAttributes;
import com.powsybl.network.store.model.ConnectablePositionAttributes;
import com.powsybl.network.store.model.RegulatingEquipmentIdentifier;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BusbarSectionToInjectionAdapter extends AbstractIdentifiableToInjectionAttributesAdapter<BusbarSectionAttributes> {

    public BusbarSectionToInjectionAdapter(BusbarSectionAttributes attributes) {
        super(attributes);
    }

    @Override
    public String getVoltageLevelId() {
        return attributes.getVoltageLevelId();
    }

    @Override
    public void setVoltageLevelId(String voltageLevelId) {
        attributes.setVoltageLevelId(voltageLevelId);
    }

    @Override
    public Integer getNode() {
        return attributes.getNode();
    }

    @Override
    public void setNode(Integer node) {
        attributes.setNode(node);
    }

    @Override
    public String getBus() {
        throw new AssertionError();
    }

    @Override
    public void setBus(String bus) {
        throw new AssertionError();
    }

    @Override
    public String getConnectableBus() {
        throw new AssertionError();
    }

    @Override
    public void setConnectableBus(String bus) {
        throw new AssertionError();
    }

    @Override
    public double getP() {
        return 0;
    }

    @Override
    public void setP(double p) {
        throw new AssertionError();
    }

    @Override
    public double getQ() {
        return 0;
    }

    @Override
    public void setQ(double q) {
        throw new AssertionError();
    }

    @Override
    public ConnectablePositionAttributes getPosition() {
        throw new AssertionError();
    }

    @Override
    public void setPosition(ConnectablePositionAttributes position) {
        throw new AssertionError();
    }

    @Override
    public List<RegulatingEquipmentIdentifier> getRegulatingEquipments() {
        return attributes.getRegulatingEquipments();
    }

    @Override
    public void setRegulatingEquipments(List<RegulatingEquipmentIdentifier> regulatingEquipments) {
        attributes.setRegulatingEquipments(regulatingEquipments);
    }
}

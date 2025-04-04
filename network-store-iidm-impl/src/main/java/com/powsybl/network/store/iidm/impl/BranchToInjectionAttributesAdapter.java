/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Branch;
import com.powsybl.network.store.model.BranchAttributes;
import com.powsybl.network.store.model.ConnectablePositionAttributes;
import com.powsybl.network.store.model.RegulatingEquipmentIdentifier;

import java.util.Objects;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BranchToInjectionAttributesAdapter extends AbstractIdentifiableToInjectionAttributesAdapter<BranchAttributes> {

    private final AbstractBranchImpl<? extends Branch<?>, ? extends BranchAttributes> branch;

    private final boolean side1;

    public BranchToInjectionAttributesAdapter(AbstractBranchImpl<? extends Branch<?>, ? extends BranchAttributes> branch, BranchAttributes attributes, boolean side1) {
        super(attributes);
        this.branch = Objects.requireNonNull(branch);
        this.side1 = side1;
    }

    @Override
    public String getVoltageLevelId() {
        return side1 ? attributes.getVoltageLevelId1() : attributes.getVoltageLevelId2();
    }

    @Override
    public void setVoltageLevelId(String voltageLevelId) {
        if (side1) {
            attributes.setVoltageLevelId1(voltageLevelId);
        } else {
            attributes.setVoltageLevelId2(voltageLevelId);
        }
    }

    @Override
    public Integer getNode() {
        return side1 ? attributes.getNode1() : attributes.getNode2();
    }

    @Override
    public void setNode(Integer node) {
        if (side1) {
            attributes.setNode1(node);
        } else {
            attributes.setNode2(node);
        }
    }

    @Override
    public String getBus() {
        return side1 ? attributes.getBus1() : attributes.getBus2();
    }

    @Override
    public void setBus(String bus) {
        if (side1) {
            attributes.setBus1(bus);
        } else {
            attributes.setBus2(bus);
        }
    }

    @Override
    public void setConnectableBus(String bus) {
        if (side1) {
            attributes.setConnectableBus1(bus);
        } else {
            attributes.setConnectableBus2(bus);
        }
    }

    @Override
    public String getConnectableBus() {
        return side1 ? attributes.getConnectableBus1() : attributes.getConnectableBus2();
    }

    @Override
    public double getP() {
        return side1 ? attributes.getP1() : attributes.getP2();
    }

    @Override
    public void setP(double p) {
        if (side1) {
            attributes.setP1(p);
        } else {
            attributes.setP2(p);
        }
    }

    @Override
    public double getQ() {
        return side1 ? attributes.getQ1() : attributes.getQ2();
    }

    @Override
    public void setQ(double q) {
        if (side1) {
            attributes.setQ1(q);
        } else {
            attributes.setQ2(q);
        }
    }

    @Override
    public ConnectablePositionAttributes getPosition() {
        return side1 ? attributes.getPosition1() : attributes.getPosition2();
    }

    @Override
    public void setPosition(ConnectablePositionAttributes position) {
        if (side1) {
            attributes.setPosition1(position);
        } else {
            attributes.setPosition2(position);
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

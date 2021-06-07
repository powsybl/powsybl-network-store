/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Branch;
import com.powsybl.network.store.model.*;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BranchToInjectionAttributesAdapter implements InjectionAttributes {

    private final AbstractBranchImpl<? extends Branch<?>, ? extends BranchAttributes> branch;

    private final BranchAttributes attributes;

    private final boolean side1;

    public BranchToInjectionAttributesAdapter(AbstractBranchImpl<? extends Branch<?>, ? extends BranchAttributes> branch, BranchAttributes attributes, boolean side1) {
        this.branch = Objects.requireNonNull(branch);
        this.attributes = attributes;
        this.side1 = side1;
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
    public Map<String, String> getProperties() {
        return attributes.getProperties();
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        attributes.setProperties(properties);
    }

    @Override
    public String getVoltageLevelId() {
        return side1 ? attributes.getVoltageLevelId1() : attributes.getVoltageLevelId2();
    }

    @Override
    public Integer getNode() {
        return side1 ? attributes.getNode1() : attributes.getNode2();
    }

    @Override
    public String getBus() {
        return side1 ? attributes.getBus1() : attributes.getBus2();
    }

    @Override
    public void setBus(String bus) {
        if (side1) {
            String oldValue = attributes.getBus1();
            attributes.setBus1(bus);
            branch.notifyUpdate("bus1", oldValue, bus, true);
        } else {
            String oldValue = attributes.getBus2();
            attributes.setBus2(bus);
            branch.notifyUpdate("bus2", oldValue, bus, true);
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
            double oldValue = attributes.getP1();
            attributes.setP1(p);
            branch.notifyUpdate("p1", oldValue, p, true);
        } else {
            double oldValue = attributes.getP2();
            attributes.setP2(p);
            branch.notifyUpdate("p2", oldValue, p, true);
        }
    }

    @Override
    public double getQ() {
        return side1 ? attributes.getQ1() : attributes.getQ2();
    }

    @Override
    public void setQ(double q) {
        if (side1) {
            double oldValue = attributes.getQ1();
            attributes.setQ1(q);
            branch.notifyUpdate("q1", oldValue, q, true);
        } else {
            double oldValue = attributes.getQ2();
            attributes.setQ2(q);
            branch.notifyUpdate("q2", oldValue, q, true);
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
    public ActivePowerControlAttributes getActivePowerControl() {
        return null;
    }

    @Override
    public void setActivePowerControl(ActivePowerControlAttributes activePowerControl) {
        //empty on purpose, it cannot have an activePowerControl
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
    public void setAliasesWithoutType(Set<String> aliasesWithoutType) {
        attributes.setAliasesWithoutType(aliasesWithoutType);
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

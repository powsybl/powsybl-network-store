/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.network.store.model.*;

import java.util.Map;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BusbarSectionToInjectionAdapter implements InjectionAttributes {

    private final BusbarSectionAttributes attributes;

    public BusbarSectionToInjectionAdapter(BusbarSectionAttributes attributes) {
        this.attributes = attributes;
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
    public boolean isFictitious() {
        return attributes.isFictitious();
    }

    @Override
    public void setFictitious(boolean fictitious) {
        attributes.setFictitious(fictitious);
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
    public ActivePowerControlAttributes getActivePowerControl() {
        return null;
    }

    @Override
    public void setActivePowerControl(ActivePowerControlAttributes activePowerControl) {
        //empty on purpose, it cannot have an activePowerControl
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

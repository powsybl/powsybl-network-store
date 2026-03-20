/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Area;
import com.powsybl.iidm.network.AreaBoundary;
import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.util.BoundaryLineBoundaryImpl;
import com.powsybl.network.store.model.AreaBoundaryAttributes;
import com.powsybl.network.store.model.TerminalRefAttributes;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class AreaBoundaryImpl implements AreaBoundary {

    private final NetworkObjectIndex index;

    private final AreaBoundaryAttributes areaBoundaryAttributes;

    protected AreaBoundaryImpl(AreaBoundaryAttributes areaBoundaryAttributes, NetworkObjectIndex index) {
        this.index = index;
        this.areaBoundaryAttributes = areaBoundaryAttributes;
    }

    @Override
    public Area getArea() {
        return this.index.getArea(areaBoundaryAttributes.getAreaId()).orElse(null);
    }

    @Override
    public Optional<Terminal> getTerminal() {
        return Optional.ofNullable(getRefTerminal());
    }

    private Terminal getRefTerminal() {
        TerminalRefAttributes terminalRefAttributes = areaBoundaryAttributes.getTerminal();
        return TerminalRefUtils.getTerminal(index, terminalRefAttributes);
    }

    @Override
    public Optional<Boundary> getBoundary() {
        if (areaBoundaryAttributes.getBoundaryBoundaryLineId() == null) {
            return Optional.empty();
        }
        Optional<BoundaryLineImpl> boundaryLine = index.getBoundaryLine(areaBoundaryAttributes.getBoundaryBoundaryLineId());
        return boundaryLine.map(BoundaryLineBoundaryImpl::new);
    }

    @Override
    public boolean isAc() {
        return areaBoundaryAttributes.getAc();
    }

    @Override
    public double getP() {
        return getBoundary().map(Boundary::getP).orElseGet(() -> getRefTerminal().getP());
    }

    @Override
    public double getQ() {
        return getBoundary().map(Boundary::getQ).orElseGet(() -> getRefTerminal().getQ());
    }

    @Override
    public boolean hasProperty() {
        Map<String, String> properties = areaBoundaryAttributes.getProperties();
        return properties != null && !properties.isEmpty();
    }

    @Override
    public boolean hasProperty(String key) {
        Map<String, String> properties = areaBoundaryAttributes.getProperties();
        return properties != null && properties.containsKey(key);
    }

    @Override
    public String getProperty(String key) {
        Map<String, String> properties = areaBoundaryAttributes.getProperties();
        return properties != null ? properties.get(key) : null;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        Map<String, String> properties = areaBoundaryAttributes.getProperties();
        return properties != null ? properties.getOrDefault(key, defaultValue) : defaultValue;
    }

    @Override
    public String setProperty(String key, String value) {
        MutableObject<String> oldValue = new MutableObject<>();
        Map<String, String> properties = areaBoundaryAttributes.getProperties();
        if (properties == null) {
            properties = new HashMap<>();
        }
        oldValue.setValue(properties.put(key, value));

        Map<String, String> finalProperties = properties;
        ((AreaImpl) getArea()).updateResourceWithoutNotification(r -> areaBoundaryAttributes.setProperties(finalProperties));
        return oldValue.getValue();
    }

    @Override
    public boolean removeProperty(String key) {
        Map<String, String> properties = areaBoundaryAttributes.getProperties();
        if (properties != null && properties.containsKey(key)) {
            ((AreaImpl) getArea()).updateResourceWithoutNotification(r -> areaBoundaryAttributes.getProperties().remove(key));
            return true;
        }
        return false;
    }

    @Override
    public Set<String> getPropertyNames() {
        Map<String, String> properties = areaBoundaryAttributes.getProperties();
        return properties != null ? properties.keySet() : Collections.emptySet();
    }
}

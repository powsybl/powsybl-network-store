/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.OperationalLimitsGroupAttributes;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.mutable.MutableObject;

/**
 * @author Ayoub LABIDI <ayoub.labidi at rte-france.com>
 */
public class OperationalLimitsGroupImpl<S> implements OperationalLimitsGroup, Validable {

    private final LimitsOwner<S> owner;

    protected final S side;

    private final OperationalLimitsGroupAttributes attributes;

    private static final String PROPERTIES = "properties";

    public OperationalLimitsGroupImpl(LimitsOwner<S> owner, S side, OperationalLimitsGroupAttributes attributes) {
        this.owner = Objects.requireNonNull(owner);
        this.side = side;
        this.attributes = Objects.requireNonNull(attributes);
    }

    @Override
    public String getId() {
        return attributes.getId();
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits() {
        return Optional.ofNullable(attributes.getCurrentLimits() != null ? new CurrentLimitsImpl<>(owner, side, getId(), attributes.getCurrentLimits()) : null);
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits() {
        return Optional.ofNullable(attributes.getActivePowerLimits() != null ? new ActivePowerLimitsImpl<>(owner, side, getId(), attributes.getActivePowerLimits()) : null);
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits() {
        return Optional.ofNullable(attributes.getApparentPowerLimits() != null ? new ApparentPowerLimitsImpl<>(owner, side, getId(), attributes.getApparentPowerLimits()) : null);
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        return new CurrentLimitsAdderImpl<>(side, owner, getId());
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits() {
        return new ActivePowerLimitsAdderImpl<>(side, owner, getId());
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits() {
        return new ApparentPowerLimitsAdderImpl<>(side, owner, getId());
    }

    @Override
    public void removeCurrentLimits() {
        var currentLimits = attributes.getCurrentLimits();
        if (currentLimits != null) {
            owner.setCurrentLimits(side, null, attributes.getId());
            attributes.setCurrentLimits(null);
        }
    }

    @Override
    public void removeActivePowerLimits() {
        var activePowerLimits = attributes.getActivePowerLimits();
        if (activePowerLimits != null) {
            owner.setActivePowerLimits(side, null, attributes.getId());
            attributes.setActivePowerLimits(null);
        }
    }

    @Override
    public void removeApparentPowerLimits() {
        var apparentPowerLimits = attributes.getApparentPowerLimits();
        if (apparentPowerLimits != null) {
            owner.setApparentPowerLimits(side, null, attributes.getId());
            attributes.setApparentPowerLimits(null);
        }
    }

    @Override
    public boolean isEmpty() {
        return attributes.getActivePowerLimits() == null && attributes.getApparentPowerLimits() == null && attributes.getCurrentLimits() == null;
    }

    @Override
    public MessageHeader getMessageHeader() {
        return new DefaultMessageHeader("Operational limits group ", getId());
    }

    @Override
    public boolean hasProperty() {
        return !MapUtils.isEmpty(attributes.getProperties());
    }

    @Override
    public boolean hasProperty(String key) {
        return MapUtils.getObject(attributes.getProperties(), key) != null;
    }

    @Override
    public String getProperty(String key) {
        return MapUtils.getObject(attributes.getProperties(), key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return MapUtils.getObject(attributes.getProperties(), key, defaultValue);
    }

    @Override
    public String setProperty(String key, String value) {
        MutableObject<String> oldValue = new MutableObject<>();
        Map<String, String> properties = attributes.getProperties();
        if (properties == null) {
            properties = new HashMap<>();
        }
        oldValue.setValue(properties.put(key, value));

        Map<String, String> finalProperties = properties;
        if (Objects.isNull(oldValue.getValue())) {
            owner.getIdentifiable().updateResourcePropertyAdded(r -> attributes.setProperties(finalProperties),
                PROPERTIES + "[" + key + "]", value);
        } else {
            owner.getIdentifiable().updateResourcePropertyReplaced(r -> attributes.setProperties(finalProperties),
                PROPERTIES + "[" + key + "]", oldValue.getValue(), value);
        }
        return oldValue.getValue();
    }

    @Override
    public boolean removeProperty(String key) {
        Map<String, String> properties = attributes.getProperties();
        if (properties != null && properties.containsKey(key)) {
            String oldValue = properties.get(key);
            owner.getIdentifiable().updateResourcePropertyRemoved(r -> attributes.getProperties().remove(key),
                PROPERTIES + "[" + key + "]", oldValue);
            return true;
        }
        return false;
    }

    @Override
    public Set<String> getPropertyNames() {
        return hasProperty() ? attributes.getProperties().keySet() : Set.of();
    }

    @Override
    public Network getNetwork() {
        return owner.getIdentifiable().getNetwork();
    }
}

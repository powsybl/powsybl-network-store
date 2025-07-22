/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.OperationalLimitsGroupAttributes;

/**
 * @author Ayoub LABIDI <ayoub.labidi at rte-france.com>
 */
public class OperationalLimitsGroupImpl<S> implements OperationalLimitsGroup, Validable {

    private final LimitsOwner<S> owner;

    protected final S side;

    private final OperationalLimitsGroupAttributes attributes;

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
        // FIXME: implement this method
        return false;
    }

    @Override
    public boolean hasProperty(String s) {
        // FIXME: implement this method
        return false;
    }

    @Override
    public String getProperty(String s) {
        // FIXME: implement this method
        return null;
    }

    @Override
    public String getProperty(String s, String s1) {
        // FIXME: implement this method
        return null;
    }

    @Override
    public String setProperty(String s, String s1) {
        // FIXME: implement this method
        return null;
    }

    @Override
    public boolean removeProperty(String s) {
        // FIXME: implement this method
        return true;
    }

    @Override
    public Set<String> getPropertyNames() {
        // FIXME: implement this method
        return Set.of();
    }

    @Override
    public Network getNetwork() {
        return owner.getIdentifiable().getNetwork();
    }
}

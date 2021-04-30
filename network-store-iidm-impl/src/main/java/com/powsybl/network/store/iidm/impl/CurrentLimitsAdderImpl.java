/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.LimitsAttributes;
import com.powsybl.network.store.model.TemporaryCurrentLimitAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class CurrentLimitsAdderImpl<S, OWNER extends LimitsOwner<S>> implements CurrentLimitsAdder {

    private static final Comparator<Integer> ACCEPTABLE_DURATION_COMPARATOR = (acceptableDuraction1, acceptableDuraction2) -> acceptableDuraction2 - acceptableDuraction1;

    private static final Logger LOGGER = LoggerFactory.getLogger(CurrentLimitsAdderImpl.class);

    private final OWNER owner;

    private final S side;

    private double permanentLimit = Double.NaN;

    private TreeMap<Integer, TemporaryCurrentLimitAttributes> temporaryLimits = new TreeMap<>(ACCEPTABLE_DURATION_COMPARATOR);

    CurrentLimitsAdderImpl(S side, OWNER owner) {
        this.owner = owner;
        this.side = side;
    }

    @Override
    public CurrentLimitsAdder setPermanentLimit(double permanentLimit) {
        this.permanentLimit = permanentLimit;
        return this;
    }

    public OWNER getOwner() {
        return owner;
    }

    @Override
    public TemporaryLimitAdder beginTemporaryLimit() {
        return new TemporaryLimitCurrentLimitAdderImpl(this);
    }

    @Override
    public double getPermanentLimit() {
        return this.permanentLimit;
    }

    @Override
    public double getTemporaryLimitValue(int acceptableDuration) {
        TemporaryCurrentLimitAttributes tl = getTemporaryLimits().get(acceptableDuration);
        return tl != null ? tl.getValue() : Double.NaN;
    }

    @Override
    public boolean hasTemporaryLimits() {
        return !temporaryLimits.isEmpty();
    }

    public void addTemporaryLimit(TemporaryCurrentLimitAttributes temporaryLimitAttribute) {
        temporaryLimits.put(temporaryLimitAttribute.getAcceptableDuration(), temporaryLimitAttribute);
    }

    public SortedMap<Integer, TemporaryCurrentLimitAttributes> getTemporaryLimits() {
        return temporaryLimits;
    }

    private void checkTemporaryLimits() {
        // check temporary limits are ok and are consistents with permanent
        double previousLimit = Double.NaN;
        for (TemporaryCurrentLimitAttributes tl : temporaryLimits.values()) { // iterate in ascending order
            if (tl.getName() == null) {
                throw new ValidationException(owner, "name is not set");
            }
            if (Double.isNaN(tl.getValue())) {
                throw new ValidationException(owner, "temporary limit value is not set");
            }
            if (tl.getValue() <= 0) {
                throw new ValidationException(owner, "temporary limit value must be > 0");
            }
            if (tl.getAcceptableDuration() < 0) {
                throw new ValidationException(owner, "acceptable duration must be >= 0");
            }
            if (tl.getValue() <= permanentLimit) {
                LOGGER.debug("{}, temporary limit should be greather than permanent limit", owner.getMessageHeader());
            }
            if (Double.isNaN(previousLimit)) {
                previousLimit = tl.getValue();
            } else {
                if (tl.getValue() <= previousLimit) {
                    LOGGER.debug("{} : temporary limits should be in ascending value order", owner.getMessageHeader());
                }
            }
        }
        // check name unicity
        temporaryLimits.values().stream()
                .collect(Collectors.groupingBy(TemporaryCurrentLimitAttributes::getName))
                .forEach((name, temporaryLimits1) -> {
                    if (temporaryLimits1.size() > 1) {
                        throw new ValidationException(owner, temporaryLimits1.size() + "temporary limits have the same name " + name);
                    }
                });
    }

    @Override
    public CurrentLimits add() {
        ValidationUtil.checkPermanentLimit(owner, permanentLimit);
        checkTemporaryLimits();

        LimitsAttributes attributes = LimitsAttributes.builder()
                .permanentLimit(permanentLimit)
                .temporaryLimits(temporaryLimits)
                .build();
        owner.setCurrentLimits(side, attributes);
        return new CurrentLimitsImpl(owner, attributes);
    }
}

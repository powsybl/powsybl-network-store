/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.LoadingLimitsAdder;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.LimitsAttributes;
import com.powsybl.network.store.model.TemporaryLimitAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public abstract class AbstractLoadingLimitsAdderImpl<S, OWNER extends LimitsOwner<S>, L extends LoadingLimits, A extends LoadingLimitsAdder<L, A>>
        implements LoadingLimitsAdderExt<S, OWNER, L, A> {

    private static final Comparator<Integer> ACCEPTABLE_DURATION_COMPARATOR = (acceptableDuraction1, acceptableDuraction2) -> acceptableDuraction2 - acceptableDuraction1;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLoadingLimitsAdderImpl.class);

    protected final OWNER owner;

    protected final S side;

    protected double permanentLimit = Double.NaN;

    protected TreeMap<Integer, TemporaryLimitAttributes> temporaryLimits;

    protected AbstractLoadingLimitsAdderImpl(S side, OWNER owner) {
        this.owner = owner;
        this.side = side;
    }

    @Override
    public A setPermanentLimit(double permanentLimit) {
        this.permanentLimit = permanentLimit;
        return (A) this;
    }

    public OWNER getOwner() {
        return owner;
    }

    @Override
    public double getPermanentLimit() {
        return this.permanentLimit;
    }

    @Override
    public double getTemporaryLimitValue(int acceptableDuration) {
        TemporaryLimitAttributes tl = getTemporaryLimits().get(acceptableDuration);
        return tl != null ? tl.getValue() : Double.NaN;

    }

    @Override
    public boolean hasTemporaryLimits() {
        return temporaryLimits != null && !temporaryLimits.isEmpty();
    }

    public void addTemporaryLimit(TemporaryLimitAttributes temporaryLimitAttribute) {
        if (temporaryLimits == null) {
            temporaryLimits = new TreeMap<>(ACCEPTABLE_DURATION_COMPARATOR);
        }
        temporaryLimits.put(temporaryLimitAttribute.getAcceptableDuration(), temporaryLimitAttribute);
    }

    public Map<Integer, TemporaryLimitAttributes> getTemporaryLimits() {
        return temporaryLimits == null ? Collections.emptyMap() : temporaryLimits;
    }

    private void checkTemporaryLimits() {
        if (temporaryLimits == null) {
            return;
        }

        // check temporary limits are ok and are consistents with permanent
        double previousLimit = Double.NaN;
        for (TemporaryLimitAttributes tl : temporaryLimits.values()) { // iterate in ascending order
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
                .collect(Collectors.groupingBy(TemporaryLimitAttributes::getName))
                .forEach((name, temporaryLimits1) -> {
                    if (temporaryLimits1.size() > 1) {
                        throw new ValidationException(owner, temporaryLimits1.size() + "temporary limits have the same name " + name);
                    }
                });
    }

    protected abstract L createAndSetLimit(LimitsAttributes attributes);

    @Override
    public L add() {
        ValidationUtil.checkPermanentLimit(owner, permanentLimit);
        checkTemporaryLimits();

        LimitsAttributes attributes = LimitsAttributes.builder()
                .permanentLimit(permanentLimit)
                .temporaryLimits(temporaryLimits)
                .build();

        return createAndSetLimit(attributes);
    }
}

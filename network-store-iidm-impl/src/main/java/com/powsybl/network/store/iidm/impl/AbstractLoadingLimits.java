/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.LimitsAttributes;
import com.powsybl.network.store.model.TemporaryLimitAttributes;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractLoadingLimits<S, O extends LimitsOwner<S>, T extends LoadingLimits> implements LoadingLimits {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLoadingLimits.class);

    public static final class TemporaryLimitImpl implements LoadingLimits.TemporaryLimit {

        private final TemporaryLimitAttributes attributes;

        public TemporaryLimitImpl(TemporaryLimitAttributes attributes) {
            this.attributes = attributes;
        }

        @Override
        public String getName() {
            return attributes.getName();
        }

        @Override
        public double getValue() {
            return attributes.getValue();
        }

        @Override
        public int getAcceptableDuration() {
            return attributes.getAcceptableDuration();
        }

        @Override
        public boolean isFictitious() {
            return attributes.isFictitious();
        }
    }

    private final LimitsAttributes attributes;

    protected final O owner;

    protected final S side;

    protected final String operationalGroupId;

    protected AbstractLoadingLimits(O owner, S side, String operationalGroupId, LimitsAttributes attributes) {
        this.owner = Objects.requireNonNull(owner);
        this.side = side;
        this.operationalGroupId = operationalGroupId;
        this.attributes = Objects.requireNonNull(attributes);
    }

    @Override
    public double getPermanentLimit() {
        return attributes.getPermanentLimit();
    }

    @Override
    public T setPermanentLimit(double permanentLimit) {
        ValidationUtil.checkPermanentLimit(owner, permanentLimit, getTemporaryLimits(), ValidationLevel.STEADY_STATE_HYPOTHESIS, owner.getIdentifiable().getNetwork().getReportNodeContext().getReportNode());
        attributes.setPermanentLimit(permanentLimit);
        return (T) this;
    }

    @Override
    public Collection<TemporaryLimit> getTemporaryLimits() {
        return attributes.getTemporaryLimits() == null ? Collections.emptyList()
            : attributes.getTemporaryLimits().values().stream().sorted().map(TemporaryLimitImpl::new).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public LoadingLimits.TemporaryLimit getTemporaryLimit(int acceptableDuration) {
        if (attributes.getTemporaryLimits() == null) {
            return null;
        }
        TemporaryLimitAttributes temporaryLimitAttributes = attributes.getTemporaryLimits().get(acceptableDuration);
        return temporaryLimitAttributes == null ? null : new TemporaryLimitImpl(temporaryLimitAttributes);
    }

    @Override
    public double getTemporaryLimitValue(int acceptableDuration) {
        LoadingLimits.TemporaryLimit tl = getTemporaryLimit(acceptableDuration);
        return tl != null ? tl.getValue() : Double.NaN;
    }

    @Override
    public T setTemporaryLimitValue(int acceptableDuration, double temporaryLimitValue) {
        if (temporaryLimitValue < 0 || Double.isNaN(temporaryLimitValue)) {
            throw new ValidationException(owner, "Temporary limit value must be a positive double");
        }
        TemporaryLimitAttributes identifiedLimit = attributes.getTemporaryLimits() == null ? null : attributes.getTemporaryLimits().get(acceptableDuration);
        if (identifiedLimit == null) {
            throw new ValidationException(owner, "No temporary limit found for the given acceptable duration");
        }
        TreeMap<Integer, TemporaryLimitAttributes> temporaryLimits = new TreeMap<>(attributes.getTemporaryLimits());
        // Creation of index markers
        Map.Entry<Integer, TemporaryLimitAttributes> biggerDurationEntry = temporaryLimits.lowerEntry(acceptableDuration);
        Map.Entry<Integer, TemporaryLimitAttributes> smallerDurationEntry = temporaryLimits.higherEntry(acceptableDuration);

        double previousValue = identifiedLimit.getValue();

        if (isTemporaryLimitValueValid(biggerDurationEntry, smallerDurationEntry, acceptableDuration, temporaryLimitValue)) {
            LOGGER.info("Temporary limit value changed from {} to {}", previousValue, temporaryLimitValue);
        } else {
            LOGGER.warn("Temporary limit value changed from {} to {}, but it is not valid", previousValue, temporaryLimitValue);
        }
        TemporaryLimitAttributes newTemporaryLimit = TemporaryLimitAttributes.builder()
            .name(identifiedLimit.getName())
            .value(temporaryLimitValue)
            .acceptableDuration(acceptableDuration)
            .build();
        attributes.getTemporaryLimits().put(acceptableDuration, newTemporaryLimit);
        return (T) this;
    }

    protected boolean isTemporaryLimitValueValid(Map.Entry<Integer, TemporaryLimitAttributes> biggerDurationEntry,
                                                 Map.Entry<Integer, TemporaryLimitAttributes> smallerDurationEntry,
                                                 int acceptableDuration,
                                                 double temporaryLimitValue) {

        boolean checkAgainstSmaller = smallerDurationEntry == null
            || smallerDurationEntry.getValue() != null
            && smallerDurationEntry.getValue().getAcceptableDuration() < acceptableDuration
            && smallerDurationEntry.getValue().getValue() > temporaryLimitValue;
        boolean checkAgainstBigger = biggerDurationEntry == null
            || biggerDurationEntry.getValue() != null
            && biggerDurationEntry.getValue().getAcceptableDuration() > acceptableDuration
            && biggerDurationEntry.getValue().getValue() < temporaryLimitValue;
        return temporaryLimitValue > attributes.getPermanentLimit() && checkAgainstBigger && checkAgainstSmaller;
    }
}

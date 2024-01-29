/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.powsybl.iidm.network.LimitType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Charly Boutier <charly.boutier at rte-france.com>
 */
public interface LimitHolder {

    String EXCEPTION_UNKNOWN_SIDE = "Unknown side";
    String EXCEPTION_UNKNOWN_TEMPORARY_LIMIT_TYPE = "Unknown temporary limit type";

    default LimitsAttributes getLimits(LimitType type, int side) {
        return switch (type) {
            case CURRENT -> getCurrentLimits(side);
            case APPARENT_POWER -> getApparentPowerLimits(side);
            case ACTIVE_POWER -> getActivePowerLimits(side);
            default -> throw new IllegalArgumentException(EXCEPTION_UNKNOWN_TEMPORARY_LIMIT_TYPE);
        };
    }

    LimitsAttributes getCurrentLimits(int side);

    LimitsAttributes getApparentPowerLimits(int side);

    LimitsAttributes getActivePowerLimits(int side);

    default void setLimits(LimitType type, int side, LimitsAttributes limits) {
        switch (type) {
            case CURRENT -> setCurrentLimits(side, limits);
            case APPARENT_POWER -> setApparentPowerLimits(side, limits);
            case ACTIVE_POWER -> setActivePowerLimits(side, limits);
            default -> throw new IllegalArgumentException(EXCEPTION_UNKNOWN_TEMPORARY_LIMIT_TYPE);
        }
    }

    void setCurrentLimits(int side, LimitsAttributes limits);

    void setApparentPowerLimits(int side, LimitsAttributes limits);

    void setActivePowerLimits(int side, LimitsAttributes limits);

    @JsonIgnore
    List<Integer> getSideList();

    default List<TemporaryLimitAttributes> getTemporaryLimitsByTypeAndSide(LimitType type, int side) {
        LimitsAttributes limits = getLimits(type, side);
        if (limits != null && limits.getTemporaryLimits() != null) {
            List<TemporaryLimitAttributes> temporaryLimits = new ArrayList<>(limits.getTemporaryLimits().values());
            temporaryLimits.forEach(e -> {
                e.setSide(side);
                e.setLimitType(type);
            });
            return temporaryLimits;
        }
        return Collections.emptyList();
    }

    @JsonIgnore
    default List<TemporaryLimitAttributes> getAllTemporaryLimits() {
        List<TemporaryLimitAttributes> result = new ArrayList<>();
        for (Integer side : getSideList()) {
            result.addAll(getTemporaryLimitsByTypeAndSide(LimitType.CURRENT, side));
            result.addAll(getTemporaryLimitsByTypeAndSide(LimitType.ACTIVE_POWER, side));
            result.addAll(getTemporaryLimitsByTypeAndSide(LimitType.APPARENT_POWER, side));
        }
        return result;
    }
}

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
import java.util.List;
import java.util.Map;

/**
 * @author Charly Boutier <charly.boutier at rte-france.com>
 */
public interface LimitHolder {

    String EXCEPTION_UNKNOWN_SIDE = "Unknown side";
    String EXCEPTION_UNKNOWN_TEMPORARY_LIMIT_TYPE = "Unknown temporary limit type";

    default LimitsAttributes getLimits(LimitType type, int side, String operationalLimitsGroupId) {
        return switch (type) {
            case CURRENT -> getCurrentLimits(side, operationalLimitsGroupId);
            case APPARENT_POWER -> getApparentPowerLimits(side, operationalLimitsGroupId);
            case ACTIVE_POWER -> getActivePowerLimits(side, operationalLimitsGroupId);
            default -> throw new IllegalArgumentException(EXCEPTION_UNKNOWN_TEMPORARY_LIMIT_TYPE);
        };
    }

    LimitsAttributes getCurrentLimits(int side, String operationalLimitsGroupId);

    LimitsAttributes getApparentPowerLimits(int side, String operationalLimitsGroupId);

    LimitsAttributes getActivePowerLimits(int side, String operationalLimitsGroupId);

    default void setLimits(LimitType type, int side, LimitsAttributes limits, String operationalLimitsGroupId) {
        switch (type) {
            case CURRENT -> setCurrentLimits(side, limits, operationalLimitsGroupId);
            case APPARENT_POWER -> setApparentPowerLimits(side, limits, operationalLimitsGroupId);
            case ACTIVE_POWER -> setActivePowerLimits(side, limits, operationalLimitsGroupId);
            default -> throw new IllegalArgumentException(EXCEPTION_UNKNOWN_TEMPORARY_LIMIT_TYPE);
        }
    }

    Map<String, OperationalLimitsGroupAttributes> getOperationalLimitsGroups(int side);

    void setCurrentLimits(int side, LimitsAttributes limits, String operationalLimitsGroupId);

    void setApparentPowerLimits(int side, LimitsAttributes limits, String operationalLimitsGroupId);

    void setActivePowerLimits(int side, LimitsAttributes limits, String operationalLimitsGroupId);

    @JsonIgnore
    List<Integer> getSideList();

    default void fillLimitsInfosByTypeAndSide(LimitsInfos result, LimitType type, int side) {
        Map<String, OperationalLimitsGroupAttributes> operationalLimitsGroups = getOperationalLimitsGroups(side);
        if (operationalLimitsGroups != null) {
            for (Map.Entry<String, OperationalLimitsGroupAttributes> entry : operationalLimitsGroups.entrySet()) {
                LimitsAttributes limits = getLimits(type, side, entry.getKey());
                if (limits != null) {
                    if (limits.getTemporaryLimits() != null) {
                        List<TemporaryLimitAttributes> temporaryLimits = new ArrayList<>(
                                limits.getTemporaryLimits().values());
                        temporaryLimits.forEach(e -> {
                            e.setSide(side);
                            e.setLimitType(type);
                            e.setOperationalLimitsGroupId(entry.getKey());
                        });
                        result.getTemporaryLimits().addAll(temporaryLimits);
                    }
                    if (limits.getPermanentLimit() != Double.NaN) {
                        result.getPermanentLimits().add(PermanentLimitAttributes.builder()
                                .side(side)
                                .limitType(type)
                                .value(limits.getPermanentLimit())
                                .operationalLimitsGroupId(entry.getKey())
                                .build());
                    }
                }
            }
        }
    }

    @JsonIgnore
    default LimitsInfos getAllLimitsInfos() {
        LimitsInfos result = new LimitsInfos();
        for (Integer side : getSideList()) {
            fillLimitsInfosByTypeAndSide(result, LimitType.CURRENT, side);
            fillLimitsInfosByTypeAndSide(result, LimitType.ACTIVE_POWER, side);
            fillLimitsInfosByTypeAndSide(result, LimitType.APPARENT_POWER, side);
        }
        return result;
    }
}

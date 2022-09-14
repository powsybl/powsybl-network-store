/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Charly Boutier <charly.boutier at rte-france.com>
 */
public interface LimitSelector {

    LimitsAttributes getLimits(TemporaryLimitType type, int side);

    void setLimits(TemporaryLimitType type, int side, LimitsAttributes limits);

    String getEquipmentType();

    List<Integer> getSideList();

    default List<TemporaryLimitAttributes> getTemporaryLimitsByTypeAndSide(TemporaryLimitType type, int side) {
        LimitsAttributes limits = getLimits(type, side);
        if (limits != null && limits.getTemporaryLimits() != null) {
            List<TemporaryLimitAttributes> temporaryLimits = new ArrayList<>(limits.getTemporaryLimits().values());
            temporaryLimits.forEach(e -> {
                e.setSide(side);
                e.setLimitType(type);
                e.setEquipmentType(getEquipmentType());
            });
            return temporaryLimits;
        }
        return new ArrayList<>();
    }

    @JsonIgnore
    default List<TemporaryLimitAttributes> getAllTemporaryLimits() {
        List<TemporaryLimitAttributes> result = new ArrayList<>();
        for (Integer side : getSideList()) {
            result.addAll(getTemporaryLimitsByTypeAndSide(TemporaryLimitType.CURRENT_LIMIT, side));
            result.addAll(getTemporaryLimitsByTypeAndSide(TemporaryLimitType.ACTIVE_POWER_LIMIT, side));
            result.addAll(getTemporaryLimitsByTypeAndSide(TemporaryLimitType.APPARENT_POWER_LIMIT, side));
        }
        return result;
    }
}

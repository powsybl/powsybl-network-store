/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;
import java.util.Objects;

/**
 * @author Sylvain Bouzols <sylvain.bouzols at rte-france.com>
 * @author Charly Boutier <charly.boutier at rte-france.com>
 */
public enum TemporaryLimitType {
    CURRENT_LIMIT("CURRENT_LIMIT", "Current Limit"),
    ACTIVE_POWER_LIMIT("ACTIVE_POWER_LIMIT", "Active Power Limit"),
    APPARENT_POWER_LIMIT("APPARENT_POWER_LIMIT", "Apparent Power Limit");

    private final String value;
    private final String description;

    TemporaryLimitType(String value, String description) {
        this.value = Objects.requireNonNull(value);
        this.description = Objects.requireNonNull(description);
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static TemporaryLimitType getByValue(String value) {
        for (TemporaryLimitType e : TemporaryLimitType.values()) {
            if (e.getValue() != null && e.getValue().equals(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Enum unknown entry");
    }
}

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
 */
public enum TemporaryLimitType {
    CURRENT_LIMIT("Current Limit"),
    ACTIVE_POWER_LIMIT("Active Power Limit"),
    APPARENT_POWER_LIMIT("Apparent Power Limit");

    private final String description;

    TemporaryLimitType(String description) {
        this.description = Objects.requireNonNull(description);
    }

    public String getDescription() {
        return description;
    }
}

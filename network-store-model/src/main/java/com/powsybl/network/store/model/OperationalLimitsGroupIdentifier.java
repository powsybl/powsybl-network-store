/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public final class OperationalLimitsGroupIdentifier {

    private static final String DELIMITER = "\u001F";
    private static final Pattern DELIMITER_PATTERN = Pattern.compile(Pattern.quote(DELIMITER));

    private final String branchId;
    private final String operationalLimitsGroupId;
    private final int side;

    public static OperationalLimitsGroupIdentifier of(String branchId, String operationalLimitsGroupId, int side) {
        return new OperationalLimitsGroupIdentifier(branchId, operationalLimitsGroupId, side);
    }

    @JsonValue
    public String toKeyString() {
        return branchId + DELIMITER + operationalLimitsGroupId + DELIMITER + side;
    }

    @JsonCreator
    public static OperationalLimitsGroupIdentifier fromKeyString(String keyString) {
        Objects.requireNonNull(keyString);

        String[] parts = DELIMITER_PATTERN.split(keyString, 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid key string format: " + keyString);
        }

        return new OperationalLimitsGroupIdentifier(parts[0], parts[1], Integer.parseInt(parts[2]));
    }
}

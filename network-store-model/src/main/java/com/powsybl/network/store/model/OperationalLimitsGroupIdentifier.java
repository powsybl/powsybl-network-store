/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.io.UncheckedIOException;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class OperationalLimitsGroupIdentifier {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private String branchId;
    private String operationalLimitsGroupId;
    private int side;

    public static OperationalLimitsGroupIdentifier of(String branchId, String operationalLimitsGroupId, int side) {
        return new OperationalLimitsGroupIdentifier(branchId, operationalLimitsGroupId, side);
    }

    @Override
    public String toString() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}

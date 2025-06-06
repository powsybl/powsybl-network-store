/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public record OperationalLimitsGroupIdentifier(String branchId, String operationalLimitsGroupId, int side) {
    @JsonCreator
    public OperationalLimitsGroupIdentifier(@JsonProperty("branchId") String branchId,
                                            @JsonProperty("operationalLimitsGroupId") String operationalLimitsGroupId,
                                            @JsonProperty("side") int side) {
        this.branchId = branchId;
        this.operationalLimitsGroupId = operationalLimitsGroupId;
        this.side = side;
    }

    public static OperationalLimitsGroupIdentifier of(String branchId, String operationalLimitsGroupId, int side) {
        return new OperationalLimitsGroupIdentifier(branchId, operationalLimitsGroupId, side);
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

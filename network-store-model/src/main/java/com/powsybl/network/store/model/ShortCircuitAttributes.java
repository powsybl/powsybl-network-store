/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Seddik Yengui <seddik.yengui at rte-france.com>
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Short Circuit attributes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShortCircuitAttributes {
    private double directSubtransX;

    private double directTransX;

    private double stepUpTransformerX;
}

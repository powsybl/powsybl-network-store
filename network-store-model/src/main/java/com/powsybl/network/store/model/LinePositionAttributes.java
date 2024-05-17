/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
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

import java.util.List;

/**
 * @author Seddik Yengui <seddik.yengui_externe at rte-france.com>
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Line position attributes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinePositionAttributes implements ExtensionAttributes {
    private List<Coordinate> coordinates;
}

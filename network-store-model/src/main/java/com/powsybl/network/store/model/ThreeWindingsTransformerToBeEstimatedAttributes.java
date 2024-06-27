/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Three windings transformer to be estimated attributes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThreeWindingsTransformerToBeEstimatedAttributes implements ExtensionAttributes {
    @Builder.Default
    private boolean rtc1Status = false;
    @Builder.Default
    private boolean rtc2Status = false;
    @Builder.Default
    private boolean rtc3Status = false;
    @Builder.Default
    private boolean ptc1Status = false;
    @Builder.Default
    private boolean ptc2Status = false;
    @Builder.Default
    private boolean ptc3Status = false;
}

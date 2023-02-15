/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Base voltage mapping attributes")
public class BaseVoltageMappingAttributes {
    @Schema(description = "Base voltage mapping")
    @Builder.Default
    Map<Double, BaseVoltageSourceAttribute> baseVoltageMap = new HashMap<>();

    public Map<Double, BaseVoltageSourceAttribute> getBaseVoltages() {
        return baseVoltageMap;
    }

}

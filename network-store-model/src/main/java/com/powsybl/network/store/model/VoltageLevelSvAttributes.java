/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@EqualsAndHashCode(callSuper = false)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Voltage level SV attributes")
public class VoltageLevelSvAttributes extends AbstractAttributes implements Attributes {

    @Schema(description = "Calculated buses for bus view")
    private List<CalculatedBusAttributes> calculatedBusesForBusView;

    @Schema(description = "Calculated buses for bus breaker view")
    private List<CalculatedBusAttributes> calculatedBusesForBusBreakerView;
}

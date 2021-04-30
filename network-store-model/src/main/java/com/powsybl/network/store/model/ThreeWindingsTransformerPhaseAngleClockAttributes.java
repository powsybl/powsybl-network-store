/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("ThreeWindingsTransformer Phase Angle Clock Attributes")
public class ThreeWindingsTransformerPhaseAngleClockAttributes {

    @ApiModelProperty("leg 2 phase angle clock")
    private Integer phaseAngleClockLeg2;

    @ApiModelProperty("leg 3 phase angle clock")
    private Integer phaseAngleClockLeg3;
}

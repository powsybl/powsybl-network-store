/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.powsybl.iidm.network.ReactiveLimitsKind;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("Min max reactive limits attributes")
public class MinMaxReactiveLimitsAttributes implements ReactiveLimitsAttributes {

    @ApiModelProperty("Kind of reactive limit")
    private final ReactiveLimitsKind kind = ReactiveLimitsKind.MIN_MAX;

    @ApiModelProperty("Reactive power minimum value")
    private double minQ;

    @ApiModelProperty("Reactive power maximum value")
    private double maxQ;

}

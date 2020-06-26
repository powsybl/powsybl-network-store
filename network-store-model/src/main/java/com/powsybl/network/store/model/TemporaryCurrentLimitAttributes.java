/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
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
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("Temporary current limit attributes")
public class TemporaryCurrentLimitAttributes {

    @ApiModelProperty("Temporary limit name")
    private String name;

    @ApiModelProperty("Temporary limit value")
    private double value;

    @ApiModelProperty("Temporary limit acceptable duration")
    private int acceptableDuration;

    @ApiModelProperty("Temporary limit is fictitious")
    private Boolean fictitious;
}

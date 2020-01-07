/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.ReactiveLimitsKind;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.TreeMap;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("Reactive capability curve attributes")
public class ReactiveCapabilityCurveAttributes implements ReactiveLimitsAttributes {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Kind of reactive limit")
    private ReactiveLimitsKind kind = ReactiveLimitsKind.CURVE;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("curve points")
    private TreeMap<Double, ReactiveCapabilityCurvePointAttributes> points;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("curve point count")
    private int pointCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("active power minimum value")
    private double minP;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("active power maximum value")
    private double maxP;
}

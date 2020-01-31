/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("Three windings transformer attributes")
public class ThreeWindingsTransformerAttributes implements IdentifiableAttributes {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("3 windings transformer name")
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @ApiModelProperty("Side 1 active power in MW")
    private double p1;

    @ApiModelProperty("Side 1 reactive power in MVar")
    private double q1;

    @ApiModelProperty("Side 2 active power in MW")
    private double p2;

    @ApiModelProperty("Side 2 reactive power in MVar")
    private double q2;

    @ApiModelProperty("Side 3 active power in MW")
    private double p3;

    @ApiModelProperty("Side 3 reactive power in MVar")
    private double q3;

    @ApiModelProperty("Side 1 leg")
    private LegAttributes leg1;

    @ApiModelProperty("Side 2 leg")
    private LegAttributes leg2;

    @ApiModelProperty("Side 3 leg")
    private LegAttributes leg3;

    @ApiModelProperty("Side 1 connectable position (for substation diagram)")
    private ConnectablePositionAttributes position1;

    @ApiModelProperty("Side 2 connectable position (for substation diagram)")
    private ConnectablePositionAttributes position2;

    @ApiModelProperty("Side 3 connectable position (for substation diagram)")
    private ConnectablePositionAttributes position3;

    @ApiModelProperty("RatedU at the fictitious bus in kV")
    private double ratedU0;

}

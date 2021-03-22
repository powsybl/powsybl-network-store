/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.powsybl.iidm.network.HvdcLine;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Map;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("HVDC line attributes")
public class HvdcLineAttributes extends AbstractAttributes implements IdentifiableAttributes {

    @ApiModelProperty("HVDC line name")
    private String name;

    @ApiModelProperty("fictitious")
    private boolean fictitious;

    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @ApiModelProperty("Aliases without type")
    private Set<String> aliasesWithoutType;

    @ApiModelProperty("Alias by type")
    private Map<String, String> aliasByType;

    @ApiModelProperty("Resistance")
    private double r = Double.NaN;

    @ApiModelProperty("Converter mode")
    private HvdcLine.ConvertersMode convertersMode;

    @ApiModelProperty("Nominal voltage")
    private double nominalV;

    @ApiModelProperty("Active power setpoint in MW")
    private double activePowerSetpoint;

    @ApiModelProperty("Maximum active power in MW")
    private double maxP = Double.NaN;

    @ApiModelProperty("Side 1 converter station ID")
    private String converterStationId1;

    @ApiModelProperty("Side 2 converter station ID")
    private String converterStationId2;
}

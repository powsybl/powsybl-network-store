/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("CgmesControlArea")
public class CgmesControlAreaAttributes {

    @ApiModelProperty("ID")
    private String id;

    @ApiModelProperty("Name")
    private String name;

    @ApiModelProperty("Code EIC")
    private String energyIdentificationCodeEic;

    @ApiModelProperty("Terminals")
    private Set<TerminalRefAttributes> terminals;

    @ApiModelProperty("Boundaries")
    private Set<TerminalRefAttributes> boundaries;

    @ApiModelProperty("Net interchange")
    private double netInterchange;
}

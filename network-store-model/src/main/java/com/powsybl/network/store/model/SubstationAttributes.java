/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.Country;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("Substation attributes")
public class SubstationAttributes implements IdentifiableAttributes {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Substation name")
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Country where the susbstation is")
    private Country country;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("TSO the substation belongs to")
    private String tso;
}

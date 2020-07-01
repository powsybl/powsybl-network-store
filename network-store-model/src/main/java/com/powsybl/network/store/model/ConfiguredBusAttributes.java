/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("ConfiguredBus attributes")
public class ConfiguredBusAttributes extends AbstractAttributes implements IdentifiableAttributes<ConfiguredBusAttributes>, Contained {

    @ApiModelProperty("Bus id")
    private String id;

    @ApiModelProperty("Bus name")
    private String name;

    @ApiModelProperty("Bus fictitious")
    private Boolean fictitious;

    @ApiModelProperty("voltage level id")
    private String voltageLevelId;

    @ApiModelProperty("bus voltage magnitude in Kv")
    private double v;

    @ApiModelProperty("voltage angle of the bus in degree")
    private double angle;

    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @Override
    @JsonIgnore
    public Set<String> getContainerIds() {
        return Collections.singleton(voltageLevelId);
    }
}

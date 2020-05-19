/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableSet;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
public class ConfiguredBusAttributes extends AbstractAttributes implements IdentifiableAttributes, RelatedVoltageLevelsAttributes {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Bus id")
    private String id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Bus name")
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Bus fictitious")
    private boolean fictitious;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("voltage level id")
    private String voltageLevelId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("bus voltage magnitude in Kv")
    private double v;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("voltage angle of the bus in degree")
    private double angle;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    public ConfiguredBusAttributes(ConfiguredBusAttributes other) {
        super(other);
        this.id = other.id;
        this.name = other.name;
        this.voltageLevelId = other.voltageLevelId;
        this.v = other.v;
        this.angle = other.angle;
        this.properties = other.properties;
    }

    @Override
    @JsonIgnore
    public Set<String> getVoltageLevels() {
        return ImmutableSet.<String>builder().add(voltageLevelId).build();
    }
}

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

import java.util.*;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("ConfiguredBus attributes")
public class ConfiguredBusAttributes extends AbstractAttributes implements IdentifiableAttributes, Contained {

    @ApiModelProperty("Bus id")
    private String id;

    @ApiModelProperty("Bus name")
    private String name;

    @ApiModelProperty("Bus fictitious")
    private boolean fictitious;

    @ApiModelProperty("Aliases without type")
    private Set<String> aliasesWithoutType = new HashSet<>();

    @ApiModelProperty("Alias by type")
    private Map<String, String> aliasByType = new HashMap<>();

    @ApiModelProperty("voltage level id")
    private String voltageLevelId;

    @ApiModelProperty("bus voltage magnitude in Kv")
    @Builder.Default
    private double v = Double.NaN;

    @ApiModelProperty("voltage angle of the bus in degree")
    @Builder.Default
    private double angle = Double.NaN;

    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    public ConfiguredBusAttributes(ConfiguredBusAttributes other) {
        super(other);
        this.id = other.id;
        this.name = other.name;
        this.voltageLevelId = other.voltageLevelId;
        this.v = other.v;
        this.angle = other.angle;
        this.fictitious = other.fictitious;
        this.properties = other.properties;
    }

    @Override
    @JsonIgnore
    public Set<String> getContainerIds() {
        return Collections.singleton(voltageLevelId);
    }
}

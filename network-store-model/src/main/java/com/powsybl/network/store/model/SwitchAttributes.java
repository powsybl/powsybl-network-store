/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.powsybl.iidm.network.SwitchKind;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("Switch attributes")
public class SwitchAttributes extends AbstractAttributes implements ConnectableAttributes, Contained, NodeBreakerBiConnectable {

    @ApiModelProperty("Voltage level ID")
    private String voltageLevelId;

    @ApiModelProperty("Switch name")
    private String name;

    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @ApiModelProperty("Aliases without type")
    private Set<String> aliasesWithoutType;

    @ApiModelProperty("Alias by type")
    private Map<String, String> aliasByType;

    @ApiModelProperty("Switch kind")
    private SwitchKind kind;

    @ApiModelProperty("Connection node side 1 in node/breaker topology")
    private Integer node1;

    @ApiModelProperty("Connection node side 2 in node/breaker topology")
    private Integer node2;

    @ApiModelProperty("Connection bus side 1 in bus/breaker topology")
    private String bus1;

    @ApiModelProperty("Connection bus side 2 in bus/breaker topology")
    private String bus2;

    @ApiModelProperty("Switch open status")
    private boolean open;

    @ApiModelProperty("Switch retained status")
    private boolean retained;

    @ApiModelProperty("Switch fictitious status")
    private boolean fictitious;

    @Override
    @JsonIgnore
    public Set<String> getContainerIds() {
        return Collections.singleton(voltageLevelId);
    }

    public SwitchAttributes(SwitchAttributes other) {
        super(other);
        this.voltageLevelId = other.voltageLevelId;
        this.name = other.name;
        this.fictitious = other.fictitious;
        this.properties = other.properties;
        this.aliasesWithoutType = other.aliasesWithoutType;
        this.aliasByType = other.aliasByType;
        this.kind = other.kind;
        this.node1 = other.node1;
        this.node2 = other.node2;
        this.bus1 = other.bus1;
        this.bus2 = other.bus2;
        this.open = other.open;
        this.retained = other.retained;
        this.fictitious = other.fictitious;
    }
}

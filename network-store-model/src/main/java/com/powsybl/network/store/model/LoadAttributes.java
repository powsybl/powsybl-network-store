/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.powsybl.iidm.network.LoadType;
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
@ApiModel("Load attributes")
public class LoadAttributes extends AbstractAttributes implements InjectionAttributes {

    @ApiModelProperty("Voltage level ID")
    private String voltageLevelId;

    @ApiModelProperty("Load name")
    private String name;

    @ApiModelProperty("fictitious")
    private boolean fictitious;

    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @ApiModelProperty("Aliases without type")
    private Set<String> aliasesWithoutType;

    @ApiModelProperty("Alias by type")
    private Map<String, String> aliasByType;

    @ApiModelProperty("Connection node in node/breaker topology")
    private Integer node;

    @ApiModelProperty("Connection bus in bus/breaker topology")
    private String bus;

    @ApiModelProperty("Possible connection bus in bus/breaker topology")
    private String connectableBus;

    @ApiModelProperty("Load type")
    private LoadType loadType;

    @ApiModelProperty("Load constant active power in MW")
    private double p0;

    @ApiModelProperty("Load constant reactive power in MW")
    private double q0;

    @ApiModelProperty("Active power in MW")
    @Builder.Default
    private double p = Double.NaN;

    @ApiModelProperty("Reactive power in MW")
    @Builder.Default
    private double q = Double.NaN;

    @ApiModelProperty("Load detail")
    private LoadDetailAttributes loadDetail;

    @ApiModelProperty("Connectable position (for substation diagram)")
    private ConnectablePositionAttributes position;

    public LoadAttributes(LoadAttributes other) {
        super(other);
        this.voltageLevelId = other.voltageLevelId;
        this.name = other.name;
        this.fictitious = other.fictitious;
        this.properties = other.properties;
        this.aliasesWithoutType = other.aliasesWithoutType;
        this.aliasByType = other.aliasByType;
        this.node = other.node;
        this.bus = other.bus;
        this.connectableBus = other.connectableBus;
        this.loadType = other.loadType;
        this.p0 = other.p0;
        this.q0 = other.q0;
        this.p = other.p;
        this.q = other.q;
        this.position = other.position;
        this.loadDetail = other.loadDetail;
    }
}

/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.powsybl.iidm.network.LoadType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Load attributes")
public class LoadAttributes extends AbstractIdentifiableAttributes implements InjectionAttributes {

    @Schema(description = "Voltage level ID")
    private String voltageLevelId;

    @Schema(description = "Connection node in node/breaker topology")
    private Integer node;

    @Schema(description = "Connection bus in bus/breaker topology")
    private String bus;

    @Schema(description = "Possible connection bus in bus/breaker topology")
    private String connectableBus;

    @Schema(description = "Load type")
    private LoadType loadType;

    @Schema(description = "Load constant active power in MW")
    private double p0;

    @Schema(description = "Load constant reactive power in MW")
    private double q0;

    @Schema(description = "Active power in MW")
    @Builder.Default
    private double p = Double.NaN;

    @Schema(description = "Reactive power in MW")
    @Builder.Default
    private double q = Double.NaN;

    @Schema(description = "Load detail")
    private LoadDetailAttributes loadDetail;

    @Schema(description = "Connectable position (for substation diagram)")
    private ConnectablePositionAttributes position;

    @Builder.Default
    @Schema(description = "regulatingEquipments")
    private Map<String, ResourceType> regulatingEquipments = new HashMap<>();
}

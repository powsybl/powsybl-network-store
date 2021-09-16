/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.powsybl.iidm.network.HvdcLine;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.HashMap;
import java.util.HashSet;
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
@Schema(description = "HVDC line attributes")
public class HvdcLineAttributes extends AbstractAttributes implements IdentifiableAttributes {

    @Schema(description = "HVDC line name")
    private String name;

    @Schema(description = "fictitious")
    private boolean fictitious;

    @Schema(description = "Properties")
    private Map<String, String> properties;

    @Schema(description = "Aliases without type")
    @Builder.Default
    private Set<String> aliasesWithoutType = new HashSet<>();

    @Schema(description = "Alias by type")
    @Builder.Default
    private Map<String, String> aliasByType = new HashMap<>();

    @Schema(description = "Resistance")
    private double r = Double.NaN;

    @Schema(description = "Converter mode")
    private HvdcLine.ConvertersMode convertersMode;

    @Schema(description = "Nominal voltage")
    private double nominalV;

    @Schema(description = "Active power setpoint in MW")
    private double activePowerSetpoint;

    @Schema(description = "Maximum active power in MW")
    private double maxP = Double.NaN;

    @Schema(description = "Side 1 converter station ID")
    private String converterStationId1;

    @Schema(description = "Side 2 converter station ID")
    private String converterStationId2;

    @Schema(description = "Hvdc angle droop active power control")
    private HvdcAngleDroopActivePowerControlAttributes hvdcAngleDroopActivePowerControl;

    @Schema(description = "Hvdc operator active power range")
    private HvdcOperatorActivePowerRangeAttributes hvdcOperatorActivePowerRange;
}

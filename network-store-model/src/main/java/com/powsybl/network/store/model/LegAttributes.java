/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Three windings transformer leg attributes")
public class LegAttributes implements TapChangerParentAttributes, FlowsLimitsAttributes {

    @Schema(description = "Voltage level ID")
    private String voltageLevelId;

    @Schema(description = "Connection node in node/breaker topology")
    private Integer node;

    @Schema(description = "Connection bus in bus/breaker topology")
    private String bus;

    @Schema(description = "Possible connection bus in bus/breaker topology")
    private String connectableBus;

    @Schema(description = "Nominal series resistance specified in ohm at the voltage of the leg")
    private double r;

    @Schema(description = "Nominal series reactance specified in ohm at the voltage of the leg")
    private double x;

    @Schema(description = "Nominal magnetizing conductance specified in S at the voltage of the leg")
    private double g;

    @Schema(description = "Nominal nominal magnetizing susceptance specified in S  at the voltage of the leg")
    private double b;

    @Schema(description = "Rated voltage in kV")
    private double ratedU;

    @Schema(description = "Rated apparent power in MVA")
    private double ratedS;

    @Schema(description = "Leg number")
    private int legNumber;

    @Schema(description = "PhaseTapChangerAttributes")
    private PhaseTapChangerAttributes phaseTapChangerAttributes;

    @Schema(description = "RatioTapChangerAttributes")
    private RatioTapChangerAttributes ratioTapChangerAttributes;

    @Schema(description = "OperationalLimitGroup")
    private List<OperationalLimitGroupAttributes> operationalLimitsGroups;

    @Schema(description = "selected OperationalLimitGroupId")
    private String selectedOperationalLimitsGroupId;
}

/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
public class LegAttributes implements TapChangerParentAttributes {

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

    @Schema(description = "currentLimitsAttributes")
    private LimitsAttributes currentLimitsAttributes;

    @Schema(description = "apparent power limits")
    private LimitsAttributes apparentPowerLimitsAttributes;

    @Schema(description = "active power limits")
    private LimitsAttributes activePowerLimitsAttributes;

    @JsonIgnore
    @Override
    public List<PhaseTapChangerStepAttributes> getPhaseTapChangerSteps() {
        if (phaseTapChangerAttributes != null && phaseTapChangerAttributes.getSteps() != null) {
            List<PhaseTapChangerStepAttributes> steps = phaseTapChangerAttributes.getSteps();
            for (int i = 0; i < steps.size(); i++) {
                steps.get(i).setIndex(i);
            }
            return steps;
        }
        return Collections.emptyList();
    }

    @JsonIgnore
    @Override
    public List<RatioTapChangerStepAttributes> getRatioTapChangerSteps() {
        if (ratioTapChangerAttributes != null && ratioTapChangerAttributes.getSteps() != null) {
            List<RatioTapChangerStepAttributes> steps = ratioTapChangerAttributes.getSteps();
            for (int i = 0; i < steps.size(); i++) {
                steps.get(i).setIndex(i);
            }
            return steps;
        }
        return Collections.emptyList();
    }
}

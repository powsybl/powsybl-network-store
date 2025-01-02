/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Load attributes")
public class BranchSvAttributes extends AbstractAttributes implements Attributes {

    @Schema(description = "Side 1 active power in MW")
    @Builder.Default
    private double p1 = Double.NaN;

    @Schema(description = "Side 1 reactive power in MVar")
    @Builder.Default
    private double q1 = Double.NaN;

    @Schema(description = "Side 2 active power in MW")
    @Builder.Default
    private double p2 = Double.NaN;

    @Schema(description = "Side 2 reactive power in MVar")
    @Builder.Default
    private double q2 = Double.NaN;

    public static void bindAttributes(BranchSvAttributes attributes, List<Object> values) {
        values.add(attributes.getP1());
        values.add(attributes.getQ1());
        values.add(attributes.getP2());
        values.add(attributes.getQ2());
    }

    public static void updateAttributes(BranchAttributes existingAttributes, BranchSvAttributes newAttributes) {
        existingAttributes.setP1(newAttributes.getP1());
        existingAttributes.setQ1(newAttributes.getQ1());
        existingAttributes.setP2(newAttributes.getP2());
        existingAttributes.setQ2(newAttributes.getQ2());
    }
}

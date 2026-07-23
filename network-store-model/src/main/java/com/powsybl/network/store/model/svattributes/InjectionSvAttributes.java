/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model.svattributes;

import com.powsybl.network.store.model.AbstractAttributes;
import com.powsybl.network.store.model.Attributes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Injection attributes for sv model")
public class InjectionSvAttributes extends AbstractAttributes implements Attributes {

    @Schema(description = "Active power in MW")
    @Builder.Default
    private double p = Double.NaN;

    @Schema(description = "Reactive power in MW")
    @Builder.Default
    private double q = Double.NaN;
}

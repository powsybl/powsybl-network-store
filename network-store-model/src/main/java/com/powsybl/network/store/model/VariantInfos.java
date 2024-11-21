/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Variant infos")
public class VariantInfos {

    @Schema(description = "Variant ID")
    private String id;

    @Schema(description = "Variant number")
    private int num;

    @Schema(description = "Variant mode")
    private VariantMode variantMode;

    @Schema(description = "Source variant number")
    private int srcVariantNum;
}

/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "CGMES tap changer attributes")
public class CgmesTapChangerAttributes {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "Combined tap change ID")
    private String combinedTapChangerId;

    @Schema(description = "Type")
    private String type;

    @Schema(description = "Hidden")
    private boolean hidden;

    @Schema(description = "Step")
    private Integer step;

    @Schema(description = "Control ID")
    private String controlId;
}

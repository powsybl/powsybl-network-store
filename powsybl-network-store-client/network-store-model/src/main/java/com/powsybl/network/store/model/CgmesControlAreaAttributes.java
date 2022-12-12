/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "CgmesControlArea")
public class CgmesControlAreaAttributes {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "Name")
    private String name;

    @Schema(description = "Code EIC")
    private String energyIdentificationCodeEic;

    @Schema(description = "Terminals")
    @Builder.Default
    private List<TerminalRefAttributes> terminals = new ArrayList<>();

    @Schema(description = "Boundaries")
    @Builder.Default
    private List<TerminalRefAttributes> boundaries = new ArrayList<>();

    @Schema(description = "Net interchange")
    private double netInterchange;
}

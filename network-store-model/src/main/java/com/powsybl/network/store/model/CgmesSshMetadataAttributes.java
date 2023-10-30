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

import java.util.List;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "CGMES SSH metadata attributes")
public class CgmesSshMetadataAttributes {

    @Schema(description = "Id")
    private String id;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "SSH version")
    private int sshVersion;

    @Schema(description = "Dependencies")
    private List<String> dependencies;

    @Schema(description = "Modeling authority set")
    private String modelingAuthoritySet;
}

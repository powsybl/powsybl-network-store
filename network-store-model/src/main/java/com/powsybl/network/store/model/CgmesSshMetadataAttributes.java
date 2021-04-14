/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel("CGMES SSH metadata attributes")
public class CgmesSshMetadataAttributes {

    @ApiModelProperty("Description")
    private String description;

    @ApiModelProperty("SV version")
    private int sshVersion;

    @ApiModelProperty("Dependencies")
    private List<String> dependencies;

    @ApiModelProperty("Modeling authority set")
    private String modelingAuthoritySet;
}

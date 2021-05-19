/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("CGMES IIDM Mapping attributes")
public class CgmesIidmMappingAttributes {

    @ApiModelProperty("Equipment side topological node map")
    private Map<TerminalRefAttributes, String> equipmentSideTopologicalNodeMap;

    @ApiModelProperty("Bus topological node map")
    private Map<String, Set<String>> busTopologicalNodeMap;

    @ApiModelProperty("Unmapped")
    private Set<String> unmapped;
}

/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "CGMES IIDM Mapping attributes")
@JsonSerialize(using = CgmesIidmMappingAttributesSerializer.class)
@JsonDeserialize(using = CgmesIidmMappingAttributesDeserializer.class)
public class CgmesIidmMappingAttributes {

    @Schema(description = "Equipment side topological node map")
    private Map<TerminalRefAttributes, String> equipmentSideTopologicalNodeMap;

    @Schema(description = "Bus topological node map")
    private Map<String, Set<String>> busTopologicalNodeMap;

    @Schema(description = "Unmapped")
    private Set<String> unmapped;
}

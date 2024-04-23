/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesSubset;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * @author Etienne HOMER {@literal <etienne.homer at rte-france.com>}
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "CGMES metadata model attributes")
public class CgmesMetadataModelAttributes {

    @Schema(description = "CGMES metadata models")
    private List<CgmesMetadataModel> models = new ArrayList<>();

    @Schema(description = "Subset Model")
    private EnumMap<CgmesSubset, CgmesMetadataModel> subsetModel = new EnumMap<>(CgmesSubset.class);

    private CgmesSubset subset;
    private String id;
    private String description;
    private int version;
    private String modelingAuthoritySet;
    private final Set<String> profiles = new HashSet<>();
    private final Set<String> dependentOn = new HashSet<>();
    private final Set<String> supersedes = new HashSet<>();

}

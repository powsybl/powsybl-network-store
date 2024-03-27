/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.powsybl.iidm.network.Country;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Substation attributes")
public class SubstationAttributes extends AbstractIdentifiableAttributes {

    @Schema(description = "Country where the susbstation is")
    private Country country;

    @Schema(description = "TSO the substation belongs to")
    private String tso;

    @Schema(description = "Geographic tags the substation is associated to")
    private Set<String> geographicalTags;

    @Schema(description = "Entsoe area the substation belongs to")
    private EntsoeAreaAttributes entsoeArea;
}

/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.joda.time.DateTime;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("Network attributes")
public class NetworkAttributes extends AbstractAttributes implements IdentifiableAttributes {

    @ApiModelProperty(value = "Network UUID", required = true)
    private UUID uuid;

    @ApiModelProperty("Network name")
    private String name;

    @ApiModelProperty("fictitious")
    private boolean fictitious;

    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @ApiModelProperty("Aliases without type")
    private Set<String> aliasesWithoutType;

    @ApiModelProperty("Alias by type")
    private Map<String, String> aliasByType;

    @ApiModelProperty("Id by alias")
    private Map<String, String> idByAlias;

    @ApiModelProperty(value = "Network date", required = true)
    private DateTime caseDate;

    @ApiModelProperty("Forecast distance")
    private int forecastDistance = 0;

    @ApiModelProperty("Source format")
    private String sourceFormat;

    @Builder.Default
    @ApiModelProperty("Connected components validity")
    private boolean connectedComponentsValid = false;

    @Builder.Default
    @ApiModelProperty("Synchronous components validity")
    private boolean synchronousComponentsValid = false;

    @ApiModelProperty("CGMES SV metadata")
    private CgmesSvMetadataAttributes cgmesSvMetadata;

    @ApiModelProperty("CGMES SSH metadata")
    private CgmesSshMetadataAttributes cgmesSshMetadata;

    @ApiModelProperty("CIM characteristics")
    private CimCharacteristicsAttributes cimCharacteristics;

    @ApiModelProperty("CGMES control areas")
    private CgmesControlAreasAttributes cgmesControlAreas;

    @ApiModelProperty("CGMES IIDM mapping")
    private CgmesIidmMappingAttributes cgmesIidmMapping;
}

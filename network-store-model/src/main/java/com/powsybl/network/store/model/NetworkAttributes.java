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

import java.util.Map;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("Network attributes")
public class NetworkAttributes extends AbstractAttributes implements IdentifiableAttributes<NetworkAttributes> {

    @ApiModelProperty(value = "Network UUID", required = true)
    private UUID uuid;

    @ApiModelProperty("Network name")
    private String name;

    @ApiModelProperty("fictitious")
    private Boolean fictitious;

    @ApiModelProperty("Properties")
    private Map<String, String> properties;

    @ApiModelProperty(value = "Network date", required = true)
    private DateTime caseDate;

    @ApiModelProperty("Forecast distance")
    private Integer forecastDistance;

    @ApiModelProperty("Source format")
    private String sourceFormat;

    @Builder.Default
    @ApiModelProperty("Connected components validity")
    private Boolean connectedComponentsValid = Boolean.FALSE;

    @Builder.Default
    @ApiModelProperty("Synchronous components validity")
    private Boolean synchronousComponentsValid = Boolean.FALSE;

    @Override
    public void initUpdatedAttributes(NetworkAttributes updatedAttributes) {
        updatedAttributes.setUuid(uuid);
    }
}

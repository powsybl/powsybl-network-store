/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.powsybl.iidm.network.Bus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Network attributes")
public class NetworkAttributes extends AbstractIdentifiableAttributes {

    public static final int FULL_VARIANT_INDICATOR = -1;

    @Schema(description = "Network UUID", required = true)
    private UUID uuid;

    @Schema(description = "Variant ID")
    private String variantId;

    @Schema(description = "Clone strategy")
    @Builder.Default
    private CloneStrategy cloneStrategy = CloneStrategy.PARTIAL;

    @Schema(description = "Full variant number")
    @Builder.Default
    private int fullVariantNum = FULL_VARIANT_INDICATOR;

    @Schema(description = "Id by alias")
    private Map<String, String> idByAlias;

    @Schema(description = "Network date", required = true)
    @Builder.Default
    private ZonedDateTime caseDate = ZonedDateTime.now();

    @Schema(description = "Forecast distance")
    @Builder.Default
    private int forecastDistance = 0;

    @Schema(description = "Source format")
    private String sourceFormat;

    @Builder.Default
    @Schema(description = "Connected components validity")
    private boolean connectedComponentsValid = false;

    @Builder.Default
    @Schema(description = "Synchronous components validity")
    private boolean synchronousComponentsValid = false;

    @Schema(description = "CIM characteristics")
    private CimCharacteristicsAttributes cimCharacteristics;

    @Schema(description = "CGMES control areas")
    private CgmesControlAreasAttributes cgmesControlAreas;

    @Schema(description = "Base voltage mapping")
    private BaseVoltageMappingAttributes baseVoltageMapping;

    @JsonIgnore
    private Map<String, Bus> busCache;

    @JsonIgnore
    public boolean isFullVariant() {
        return fullVariantNum == FULL_VARIANT_INDICATOR;
    }

    public static boolean isFullVariant(int fullVariantNum) {
        return fullVariantNum == FULL_VARIANT_INDICATOR;
    }
}

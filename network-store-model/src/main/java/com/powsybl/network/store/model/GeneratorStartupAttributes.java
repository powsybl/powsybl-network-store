/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.commons.json.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Generator Startup attributes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneratorStartupAttributes implements ExtensionAttributes {

    private double plannedActivePowerSetpoint;

    private double startupCost;

    private double marginalCost;

    private double plannedOutageRate;

    private double forcedOutageRate;

    @Override
    public String toJson() {
        return JsonUtil.toJson(this::writeJson);
    }

    @Override
    public void writeJson(JsonGenerator generator) {
        try {
            generator.writeStartObject();
            generator.writeStringField("@class", this.getClass().getName());
            generator.writeNumberField("plannedActivePowerSetpoint", plannedActivePowerSetpoint);
            generator.writeNumberField("startupCost", startupCost);
            generator.writeNumberField("marginalCost", marginalCost);
            generator.writeNumberField("plannedOutageRate", plannedOutageRate);
            generator.writeNumberField("forcedOutageRate", forcedOutageRate);
            generator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

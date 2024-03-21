/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Active power control attributes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivePowerControlAttributes implements ExtensionAttributes {

    private boolean participate;

    private double droop;

    private double participationFactor;

    // TODO Why use a custom toJson while we could just use mapper.writeValueAsString()??
    @Override
    public String toJson() {
        return JsonUtil.toJson(this::writeJson);
    }

    @Override
    public void writeJson(JsonGenerator generator) {
        try {
            generator.writeStartObject();
            generator.writeStringField("@class", this.getClass().getName());
            generator.writeBooleanField("participate", participate);
            generator.writeNumberField("droop", droop);
            generator.writeNumberField("participationFactor", participationFactor);
            generator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

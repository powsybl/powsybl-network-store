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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Active power control attributes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperatingStatusAttributes implements ExtensionAttributes {

    private String operatingStatus;

    @Override
    public String toJson() {
        return JsonUtil.toJson(this::writeJson);
    }

    @Override
    public void writeJson(JsonGenerator generator) {
        try {
            generator.writeStartObject();
            generator.writeStringField("@class", this.getClass().getName());
            generator.writeStringField("operatingStatus", operatingStatus);
            generator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

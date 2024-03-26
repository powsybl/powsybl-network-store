package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Active power control attributes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperatingStatusAttributes implements ExtensionAttributes {

    private String operatingStatus;
}

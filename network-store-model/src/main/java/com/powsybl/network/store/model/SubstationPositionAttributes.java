package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.extensions.Coordinate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Substation position attributes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubstationPositionAttributes implements ExtensionAttributes {
    private Coordinate coordinate;
}

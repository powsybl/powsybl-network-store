package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Seddik Yengui <seddik.yengui at rte-france.com>
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Generator Short Circuit attributes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneratorShortCircuitAttributes {
    private double directSubtransX;

    private double directTransX;

    private double stepUpTransformerX;
}

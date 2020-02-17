package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("ConfiguredBus attributes")
public class ConfiguredBusAttributes implements IdentifiableAttributes {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Bus id")
    private String id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Bus name")
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("ensure id unicity")
    private boolean ensureIdUnicity;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("voltage level id")
    private String voltageLevelId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("v")
    private double v;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("angle")
    private double angle;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("p")
    private double p;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("q")
    private double q;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("Properties")
    private Map<String, String> properties;

}

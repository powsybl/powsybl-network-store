package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Abdelsalem HEDHILI <abdelsalem.hedhili at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("PhaseTapChangerStep attributes")
public class PhaseTapChangerStepAttributes {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("position")
    private int position;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("rho")
    private double rho;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("r")
    private double r;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("x")
    private double x;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("g")
    private double g;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("b")
    private double b;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("alpha")
    private double alpha;
}

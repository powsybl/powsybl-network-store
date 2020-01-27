package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.iidm.network.PhaseTapChanger;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Abdelsalem HEDHILI <abdelsalem.hedhili at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel("PhaseTapChanger attributes")
public class PhaseTapChangerAttributes {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("lowTapPosition")
    private int lowTapPosition;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("tapPosition")
    private Integer tapPosition;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("regulationMode")
    private PhaseTapChanger.RegulationMode regulationMode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("steps")
    private List<PhaseTapChangerStepAttributes> steps;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("regulationValue")
    private double regulationValue;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("regulating")
    private boolean regulating;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty("targetDeadband")
    private double targetDeadband;

}

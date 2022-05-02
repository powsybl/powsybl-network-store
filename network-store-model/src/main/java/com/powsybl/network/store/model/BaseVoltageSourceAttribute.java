package com.powsybl.network.store.model;

import com.powsybl.cgmes.extensions.BaseVoltageMapping;
import com.powsybl.cgmes.extensions.Source;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Base voltage source attributes")
public class BaseVoltageSourceAttribute implements BaseVoltageMapping.BaseVoltageSource {
    @Schema(description = "Base voltage id")
    private String id;
    @Schema(description = "Nominal voltage")
    private double nominalV;
    @Schema(description = "Source")
    private Source source;

    public String getId() {
        return id;
    }

    public double getNominalV() {
        return nominalV;
    }

    public Source getSource() {
        return source;
    }
}

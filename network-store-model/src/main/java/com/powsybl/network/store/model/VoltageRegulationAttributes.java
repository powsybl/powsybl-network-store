package com.powsybl.network.store.model;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Voltage regulation attributes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoltageRegulationAttributes implements ExtensionAttributes {
    private boolean voltageRegulatorOn;

    private double targetV;

    private TerminalRefAttributes regulatingTerminal;
}

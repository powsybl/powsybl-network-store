/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.powsybl.iidm.network.SwitchKind;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Switch attributes")
public class SwitchAttributes extends AbstractIdentifiableAttributes implements ConnectableAttributes, Contained, NodeBreakerBiConnectable {

    @Schema(description = "Voltage level ID")
    private String voltageLevelId;

    @Schema(description = "Switch kind")
    private SwitchKind kind;

    @Schema(description = "Connection node side 1 in node/breaker topology")
    private Integer node1;

    @Schema(description = "Connection node side 2 in node/breaker topology")
    private Integer node2;

    @Schema(description = "Connection bus side 1 in bus/breaker topology")
    private String bus1;

    @Schema(description = "Connection bus side 2 in bus/breaker topology")
    private String bus2;

    @Schema(description = "Switch open status")
    private boolean open;

    @Schema(description = "Switch retained status")
    private boolean retained;

    @Override
    @JsonIgnore
    public Set<String> getContainerIds() {
        return Collections.singleton(voltageLevelId);
    }
}

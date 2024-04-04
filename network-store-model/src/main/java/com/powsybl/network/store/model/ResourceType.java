/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum ResourceType {
    NETWORK("Network"),
    SUBSTATION("Substation"),
    VOLTAGE_LEVEL("Voltage level"),
    LOAD("Load"),
    GENERATOR("Generator"),
    BATTERY("Battery"),
    SHUNT_COMPENSATOR("Shunt compensator"),
    VSC_CONVERTER_STATION("VSC converter station"),
    LCC_CONVERTER_STATION("LCC converter station"),
    STATIC_VAR_COMPENSATOR("Static var compensator"),
    BUSBAR_SECTION("Busbar section"),
    SWITCH("Switch"),
    TWO_WINDINGS_TRANSFORMER("2 windings transformer"),
    THREE_WINDINGS_TRANSFORMER("3 windings transformer"),
    LINE("AC Line"),
    HVDC_LINE("HVDC line"),
    DANGLING_LINE("Dangling line"),
    GROUND("Ground"),
    CONFIGURED_BUS("Configured bus"),
    TIE_LINE("Tie line");

    private final String description;

    ResourceType(String description) {
        this.description = Objects.requireNonNull(description);
    }

    public String getDescription() {
        return description;
    }
}

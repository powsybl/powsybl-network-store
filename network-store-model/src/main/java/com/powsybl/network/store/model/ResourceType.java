/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum ResourceType {
    NETWORK(null),
    SUBSTATION(NETWORK),
    VOLTAGE_LEVEL(SUBSTATION),
    LOAD(VOLTAGE_LEVEL),
    GENERATOR(VOLTAGE_LEVEL),
    SHUNT_COMPENSATOR(VOLTAGE_LEVEL),
    VSC_CONVERTER_STATION(VOLTAGE_LEVEL),
    LCC_CONVERTER_STATION(VOLTAGE_LEVEL),
    STATIC_VAR_COMPENSATOR(VOLTAGE_LEVEL),
    BUSBAR_SECTION(VOLTAGE_LEVEL),
    SWITCH(VOLTAGE_LEVEL),
    TWO_WINDINGS_TRANSFORMER(VOLTAGE_LEVEL),
    THREE_WINDINGS_TRANSFORMER(VOLTAGE_LEVEL),
    LINE(VOLTAGE_LEVEL),
    HVDC_LINE(VOLTAGE_LEVEL),
    DANGLING_LINE(VOLTAGE_LEVEL),
    CONFIGURED_BUS(VOLTAGE_LEVEL);

    private final ResourceType container;

    ResourceType(ResourceType container) {
        this.container = container;
    }

    public ResourceType getContainer() {
        return container;
    }
}

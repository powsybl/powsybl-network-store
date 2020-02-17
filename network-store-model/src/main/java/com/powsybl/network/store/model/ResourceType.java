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
    NETWORK,
    SUBSTATION,
    VOLTAGE_LEVEL,
    LOAD,
    GENERATOR,
    SHUNT_COMPENSATOR,
    VSC_CONVERTER_STATION,
    LCC_CONVERTER_STATION,
    STATIC_VAR_COMPENSATOR,
    BUSBAR_SECTION,
    SWITCH,
    TWO_WINDINGS_TRANSFORMER,
    THREE_WINDINGS_TRANSFORMER,
    LINE,
    HVDC_LINE,
    DANGLING_LINE,
    CONFIGURED_BUS
}

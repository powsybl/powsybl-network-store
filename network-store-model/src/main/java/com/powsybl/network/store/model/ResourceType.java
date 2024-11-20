/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.IdentifiableType;

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
    TIE_LINE("Tie line"),
    RATIO_TAP_CHANGER("Ratio tap changer"),
    PHASE_TAP_CHANGER("Phase tap changer");

    private final String description;

    ResourceType(String description) {
        this.description = Objects.requireNonNull(description);
    }

    public String getDescription() {
        return description;
    }

    public static ResourceType convert(IdentifiableType identifiableType) {
        return switch (identifiableType) {
            case NETWORK -> NETWORK;
            case SUBSTATION -> SUBSTATION;
            case VOLTAGE_LEVEL -> VOLTAGE_LEVEL;
            case HVDC_LINE -> HVDC_LINE;
            case SWITCH -> SWITCH;
            case BUSBAR_SECTION -> BUSBAR_SECTION;
            case LINE -> LINE;
            case TIE_LINE -> TIE_LINE;
            case TWO_WINDINGS_TRANSFORMER -> TWO_WINDINGS_TRANSFORMER;
            case THREE_WINDINGS_TRANSFORMER -> THREE_WINDINGS_TRANSFORMER;
            case GENERATOR -> GENERATOR;
            case BATTERY -> BATTERY;
            case LOAD -> LOAD;
            case SHUNT_COMPENSATOR -> SHUNT_COMPENSATOR;
            case DANGLING_LINE -> DANGLING_LINE;
            case STATIC_VAR_COMPENSATOR -> STATIC_VAR_COMPENSATOR;
            // for now LCC are not implemented yet but it has to be fixed with the implementation
            // it will need something to difference both
            case HVDC_CONVERTER_STATION -> VSC_CONVERTER_STATION;
            case GROUND -> GROUND;
            default -> throw new PowsyblException("can not be converted to resourceType");
        };
    }
}

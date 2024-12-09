/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.ThreeSides;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public enum RegulatingTapChangerType {
    RATIO_TAP_CHANGER,
    RATIO_TAP_CHANGER_SIDE_ONE,
    RATIO_TAP_CHANGER_SIDE_TWO,
    RATIO_TAP_CHANGER_SIDE_THREE,
    PHASE_TAP_CHANGER,
    PHASE_TAP_CHANGER_SIDE_ONE,
    PHASE_TAP_CHANGER_SIDE_TWO,
    PHASE_TAP_CHANGER_SIDE_THREE;

    public static RegulatingTapChangerType getThreeWindingType(ThreeSides side, RegulatingTapChangerType type) {
        return switch (type) {
            case PHASE_TAP_CHANGER ->
                getSideType(side, PHASE_TAP_CHANGER_SIDE_ONE, PHASE_TAP_CHANGER_SIDE_TWO, PHASE_TAP_CHANGER_SIDE_THREE);
            case RATIO_TAP_CHANGER ->
                getSideType(side, RATIO_TAP_CHANGER_SIDE_ONE, RATIO_TAP_CHANGER_SIDE_TWO, RATIO_TAP_CHANGER_SIDE_THREE);
            default -> throw new PowsyblException("type to select a three winding tap changer type " +
                "must be RATIO_TAP_CHANGER or PHASE_TAP_CHANGER");
        };
    }

    private static RegulatingTapChangerType getSideType(ThreeSides side, RegulatingTapChangerType enum1, RegulatingTapChangerType enum2, RegulatingTapChangerType enum3) {
        return switch (side) {
            case ONE -> enum1;
            case TWO -> enum2;
            case THREE -> enum3;
        };
    }
}

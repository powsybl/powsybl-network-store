/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import java.util.Collections;
import java.util.List;

/**
 * @author Sylvain Bouzols <sylvain.bouzols at rte-france.com>
 */
public interface TapChangerHolder {

    String EXCEPTION_UNKNOWN_SIDE = "Unknown side";
    String EXCEPTION_UNKNOWN_TAP_CHANGER_TYPE = "Unknown Tap Changer type";

    RatioTapChangerAttributes getRatioTapChangerAttributes(int side);

    PhaseTapChangerAttributes getPhaseTapChangerAttributes(int side);

    void setRatioTapChangerAttributes(int side, RatioTapChangerAttributes tapChanger);

    void setPhaseTapChangerAttributes(int side, PhaseTapChangerAttributes tapChanger);

    default List<PhaseTapChangerStepAttributes> getPhaseTapChangerStepsBySide(int side) {
        PhaseTapChangerAttributes tapChanger = getPhaseTapChangerAttributes(side);
        if (tapChanger != null && tapChanger.getSteps() != null) {
            List<PhaseTapChangerStepAttributes> steps = tapChanger.getSteps();
            for (int i = 0; i < steps.size(); i++) {
                steps.get(i).setIndex(i);
                steps.get(i).setSide(side);
            }
            return steps;
        }
        return Collections.emptyList();
    }

    default List<RatioTapChangerStepAttributes> getRatioTapChangerStepsBySide(int side) {
        RatioTapChangerAttributes tapChanger = getRatioTapChangerAttributes(side);
        if (tapChanger != null && tapChanger.getSteps() != null) {
            List<RatioTapChangerStepAttributes> steps = tapChanger.getSteps();
            for (int i = 0; i < steps.size(); i++) {
                steps.get(i).setIndex(i);
                steps.get(i).setSide(side);
            }
            return steps;
        }
        return Collections.emptyList();
    }
}

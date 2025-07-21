/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.network.store.model.RegulatingPointAttributes;
import com.powsybl.network.store.model.RegulatingTapChangerType;
import com.powsybl.network.store.model.ResourceType;
import com.powsybl.network.store.model.TerminalRefAttributes;

import java.util.Objects;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public abstract class AbstractTapChangerAdder {

    protected final NetworkObjectIndex index;

    protected int lowTapPosition = 0;

    protected Integer tapPosition;

    protected boolean regulating = false;

    protected double targetDeadband = Double.NaN;

    protected Terminal regulatingTerminal;

    protected boolean loadTapChangingCapabilities;

    protected Integer solvedTapPosition;

    protected AbstractTapChangerAdder(NetworkObjectIndex index) {
        this.index = Objects.requireNonNull(index);
    }

    protected RegulatingPointAttributes createRegulationPointAttributes(TapChangerParent tapChangerParent, RegulatingTapChangerType regulatingTapChangerType,
                                                                     String regulationMode, Boolean regulating) {
        RegulatingTapChangerType finalRegulatingTapChangerType = regulatingTapChangerType;
        ResourceType resourceType = ResourceType.TWO_WINDINGS_TRANSFORMER;
        if (tapChangerParent instanceof ThreeWindingsTransformerImpl.LegImpl leg) {
            ThreeSides side = leg.getSide();
            finalRegulatingTapChangerType = RegulatingTapChangerType.getThreeWindingsTransformerTapChangerType(side, regulatingTapChangerType);
            resourceType = ResourceType.THREE_WINDINGS_TRANSFORMER;
        }
        TerminalRefAttributes terminalRefAttributes = TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal);
        // local terminal is null for tapChanger because it has more than one
        return new RegulatingPointAttributes(tapChangerParent.getTransformer().getId(), resourceType, finalRegulatingTapChangerType,
            null, terminalRefAttributes, regulationMode, resourceType, regulating);
    }

    public static void checkPositionCreation(Integer position, int lowTapPosition, int highTapPosition, TapChangerParent tapChangerParent, String message) {
        if (position != null && (position < lowTapPosition || position > highTapPosition)) {
            throw new ValidationException(tapChangerParent, "incorrect " + message + " "
                + position + " [" + lowTapPosition + ", "
                + highTapPosition + "]");
        }
    }
}

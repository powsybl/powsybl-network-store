/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.network.store.model.TapChangerAttributes;

import java.util.Objects;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public abstract class AbstractTapChanger {

    protected final NetworkObjectIndex index;

    protected int lowTapPosition = 0;

    protected Integer tapPosition;

    protected boolean regulating = false;

    protected double targetDeadband = Double.NaN;

    protected Terminal regulatingTerminal;

    public AbstractTapChanger(NetworkObjectIndex index) {
        this.index = Objects.requireNonNull(index);
    }

    public AbstractTapChanger(NetworkObjectIndex index, int lowTapPosition, Integer tapPosition, boolean regulating, double targetDeadband) {
        this.index = Objects.requireNonNull(index);
        this.lowTapPosition = lowTapPosition;
        this.tapPosition = tapPosition;
        this.regulating = regulating;
        this.targetDeadband = targetDeadband;
    }

    protected static void checkOnlyOneTapChangerRegulatingEnabled(Validable validable,
                                                                  TapChangerAttributes tapChangerNotIncludingTheModified,
                                                                  boolean regulating) {
        if (regulating && tapChangerNotIncludingTheModified != null && tapChangerNotIncludingTheModified.isRegulating()) {
            throw new ValidationException(validable, "Only one regulating control enabled is allowed");
        }
    }
}

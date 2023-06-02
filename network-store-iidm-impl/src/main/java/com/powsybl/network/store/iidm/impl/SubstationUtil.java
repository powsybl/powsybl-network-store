/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public final class SubstationUtil {

    private SubstationUtil() {
    }

    static void checkRemovability(Substation substation) {
        Objects.requireNonNull(substation);
        for (VoltageLevel vl : substation.getVoltageLevels()) {
            for (Connectable connectable : vl.getConnectables()) {
                if (connectable instanceof Branch) {
                    checkRemovability(substation, (Branch) connectable);
                } else if (connectable instanceof ThreeWindingsTransformer) {
                    checkRemovability(substation, (ThreeWindingsTransformer) connectable);
                } else if (connectable instanceof HvdcConverterStation) {
                    checkRemovability(substation, (HvdcConverterStation) connectable);
                }
            }
        }
    }

    private static void checkRemovability(Substation substation, Branch branch) {
        Objects.requireNonNull(substation);
        Objects.requireNonNull(branch);
        Substation s1 = branch.getTerminal1().getVoltageLevel().getSubstation().orElse(null);
        Substation s2 = branch.getTerminal2().getVoltageLevel().getSubstation().orElse(null);
        if (s1 != substation || s2 != substation) {
            throw createIsolationException(substation);
        }
    }

    private static void checkRemovability(Substation substation, ThreeWindingsTransformer twt) {
        Objects.requireNonNull(substation);
        Objects.requireNonNull(twt);
        Substation s1 = twt.getLeg1().getTerminal().getVoltageLevel().getSubstation().orElse(null);
        Substation s2 = twt.getLeg2().getTerminal().getVoltageLevel().getSubstation().orElse(null);
        Substation s3 = twt.getLeg3().getTerminal().getVoltageLevel().getSubstation().orElse(null);
        if (s1 != substation || s2 != substation || s3 != substation) {
            throw createIsolationException(substation);
        }
    }

    private static void checkRemovability(Substation substation, HvdcConverterStation station) {
        Objects.requireNonNull(substation);
        Objects.requireNonNull(station);
        HvdcLine hvdcLine = substation.getNetwork().getHvdcLine(station);
        if (hvdcLine != null) {
            Substation s1 = hvdcLine.getConverterStation1().getTerminal().getVoltageLevel().getSubstation().orElse(null);
            Substation s2 = hvdcLine.getConverterStation2().getTerminal().getVoltageLevel().getSubstation().orElse(null);
            if (s1 != substation || s2 != substation) {
                throw createIsolationException(substation);
            }
        }
    }

    private static PowsyblException createIsolationException(Substation substation) {
        return new PowsyblException("The substation " + substation.getId() + " is still connected to another substation");

    }
}

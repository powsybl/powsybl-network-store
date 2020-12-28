package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

public final class SubstationUtil {

    private SubstationUtil() {
    }

    static void checkRemovability(Substation substation) {
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
        Substation s1 = branch.getTerminal1().getVoltageLevel().getSubstation();
        Substation s2 = branch.getTerminal2().getVoltageLevel().getSubstation();
        if ((s1 != substation) || (s2 != substation)) {
            throw createIsolationException(substation);
        }
    }

    private static void checkRemovability(Substation substation, ThreeWindingsTransformer twt) {
        Substation s1 = twt.getLeg1().getTerminal().getVoltageLevel().getSubstation();
        Substation s2 = twt.getLeg2().getTerminal().getVoltageLevel().getSubstation();
        Substation s3 = twt.getLeg3().getTerminal().getVoltageLevel().getSubstation();
        if ((s1 != substation) || (s2 != substation) || (s3 != substation)) {
            throw createIsolationException(substation);
        }
    }

    private static void checkRemovability(Substation substation, HvdcConverterStation station) {
        HvdcLine hvdcLine = substation.getNetwork().getHvdcLine(station);
        if (hvdcLine != null) {
            Substation s1 = hvdcLine.getConverterStation1().getTerminal().getVoltageLevel().getSubstation();
            Substation s2 = hvdcLine.getConverterStation2().getTerminal().getVoltageLevel().getSubstation();
            if ((s1 != substation) || (s2 != substation)) {
                throw createIsolationException(substation);
            }
        }
    }

    private static PowsyblException createIsolationException(Substation substation) {
        return new PowsyblException("The substation " + substation.getId() + " is still connected to another substation");

    }
}

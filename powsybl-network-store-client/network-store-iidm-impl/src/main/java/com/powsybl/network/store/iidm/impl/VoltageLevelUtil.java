package com.powsybl.network.store.iidm.impl;

import com.google.common.collect.Sets;
import com.powsybl.iidm.network.*;

import java.util.Set;

public final class VoltageLevelUtil {

    static final Set<IdentifiableType> MULTIPLE_TERMINALS_CONNECTABLE_TYPES = Sets.immutableEnumSet(
            IdentifiableType.LINE,
            IdentifiableType.TWO_WINDINGS_TRANSFORMER,
            IdentifiableType.THREE_WINDINGS_TRANSFORMER);

    private VoltageLevelUtil() {
    }

    static void checkRemovability(VoltageLevel voltageLevel) {
        Network network = voltageLevel.getNetwork();

        for (Connectable<?> connectable : voltageLevel.getConnectables()) {
            IdentifiableType type = connectable.getType();
            if (MULTIPLE_TERMINALS_CONNECTABLE_TYPES.contains(type)) {
                // Reject lines, 2WT and 3WT
                throw new AssertionError("The voltage level '" + voltageLevel.getId() + "' cannot be removed because of a remaining " + type);
            } else if ((type == IdentifiableType.HVDC_CONVERTER_STATION) && (network.getHvdcLine((HvdcConverterStation<?>) connectable) != null)) {
                // Reject all converter stations connected to a HVDC line
                throw new AssertionError("The voltage level '" + voltageLevel.getId() + "' cannot be removed because of a remaining HVDC line");
            }
        }
    }
}

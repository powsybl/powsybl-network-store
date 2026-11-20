/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public final class VoltageRegulationTestUtils {

    private VoltageRegulationTestUtils() {
    }

    public static void assertNewVoltageRegulationInMultiVariants(Network network, VoltageRegulationHolder<?> holder) {
        VariantManager variantManager = network.getVariantManager();
        holder.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withTargetValue(123)
                .withRegulating(false)
                .build();
        String variant1 = "variant1";
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, List.of(variant1));
        variantManager.setWorkingVariant(variant1);

        assertNotNull(holder.newVoltageRegulation().withSlope(1).withRegulating(false).build());
        assertVariant1Values(holder.getVoltageRegulation());

        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertInitialVariantValues(holder.getVoltageRegulation());
    }

    private static void assertVariant1Values(VoltageRegulation voltageRegulation) {
        assertNotNull(voltageRegulation);
        assertNull(voltageRegulation.getMode());
        assertNull(voltageRegulation.getTerminal());
        assertTrue(Double.isNaN(voltageRegulation.getTargetValue()));
        assertTrue(Double.isNaN(voltageRegulation.getTargetDeadband()));
        assertEquals(1, voltageRegulation.getSlope());
        assertFalse(voltageRegulation.isRegulating());
    }

    private static void assertInitialVariantValues(VoltageRegulation voltageRegulation) {
        assertNotNull(voltageRegulation);
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());
        assertNull(voltageRegulation.getTerminal());
        assertEquals(123, voltageRegulation.getTargetValue());
        assertTrue(Double.isNaN(voltageRegulation.getTargetDeadband()));
        assertTrue(Double.isNaN(voltageRegulation.getSlope()));
        assertFalse(voltageRegulation.isRegulating());
    }
}

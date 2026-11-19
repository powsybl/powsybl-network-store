/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkEventRecorder;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.events.UpdateNetworkEvent;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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

    public static void assertVoltageRegulationRemovalEventByValue(Network network, VoltageRegulationHolder<?> holder, String id) {
        VoltageRegulation expectedOldValue = holder.getVoltageRegulation();
        assertNotNull(expectedOldValue);

        NetworkEventRecorder eventRecorder = new NetworkEventRecorder();
        network.addListener(eventRecorder);
        holder.removeVoltageRegulation();

        assertEquals(1, eventRecorder.getEvents().size());
        UpdateNetworkEvent event = assertInstanceOf(UpdateNetworkEvent.class, eventRecorder.getEvents().getFirst());
        assertEquals(VoltageRegulation.NotifyUpdateKey.REMOVE_REGULATION.getKey(), event.attribute());
        assertEquals(id, event.id());
        assertNull(event.newValue());
        assertVoltageRegulationValuesEqual(expectedOldValue, assertInstanceOf(VoltageRegulation.class, event.oldValue()));
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

    private static void assertVoltageRegulationValuesEqual(VoltageRegulation expected, VoltageRegulation actual) {
        assertEquals(expected.getMode(), actual.getMode());
        assertEquals(terminalId(expected), terminalId(actual));
        assertEquals(expected.isRegulating(), actual.isRegulating());
        assertDoubleEquals(expected.getTargetValue(), actual.getTargetValue());
        assertDoubleEquals(expected.getTargetDeadband(), actual.getTargetDeadband());
        assertDoubleEquals(expected.getSlope(), actual.getSlope());
    }

    private static String terminalId(VoltageRegulation voltageRegulation) {
        return voltageRegulation.getTerminal() == null ? null : voltageRegulation.getTerminal().getConnectable().getId();
    }

    private static void assertDoubleEquals(double expected, double actual) {
        if (Double.isNaN(expected)) {
            assertTrue(Double.isNaN(actual));
        } else {
            assertEquals(expected, actual);
        }
    }
}

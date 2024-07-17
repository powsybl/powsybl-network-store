/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.tck.extensions.AbstractSlackTerminalTest;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SlackTerminalTest extends AbstractSlackTerminalTest {

    /* This test is overwritten because SlackTerminal.isEmpty doesn't do the same in our implementation.
       In this implementation the isEmpty return true if the slackTerminal is empty for the current variant only
       whereas it returns true on the core implementation when the slackterminal is empty on all variants
     */
    @Test
    public void variantsResetTest() {
        String variant1 = "variant1";
        String variant2 = "variant2";

        // Creates 2 variants before creating the extension
        Network network = EurostagTutorialExample1Factory.create();
        VariantManager variantManager = network.getVariantManager();
        List<String> targetVariantIds = Arrays.asList(variant1, variant2);
        variantManager.cloneVariant(INITIAL_VARIANT_ID, targetVariantIds);
        variantManager.setWorkingVariant(variant2);

        // Creates the extension
        VoltageLevel vlgen = network.getVoltageLevel("VLGEN");
        SlackTerminal.attach(network.getBusBreakerView().getBus("NGEN"));
        SlackTerminal stGen = vlgen.getExtension(SlackTerminal.class);
        assertNotNull(stGen);
        final Terminal tGen = stGen.getTerminal();

        // Testing that only current variant was set
        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertNull(stGen.getTerminal());
        stGen.setTerminal(tGen);

        variantManager.setWorkingVariant(variant1);
        assertNull(stGen.getTerminal());
        stGen.setTerminal(tGen);

        // Testing the empty property of the slackTerminal
        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertTrue(stGen.setTerminal(null).isEmpty());

        variantManager.setWorkingVariant(variant2);
        assertTrue(stGen.setTerminal(null).isEmpty());

        variantManager.setWorkingVariant(variant1);
        assertTrue(stGen.setTerminal(null).isEmpty());
        assertFalse(stGen.setTerminal(tGen).isEmpty());

        // Testing the cleanIfEmpty boolean
        stGen.setTerminal(null, false);
        assertNull(vlgen.getExtension(SlackTerminal.class));
        stGen.setTerminal(null, true);
        assertNull(vlgen.getExtension(SlackTerminal.class));

        // Creates an extension on another voltageLevel
        VoltageLevel vlhv1 = network.getVoltageLevel("VLLOAD");
        SlackTerminal.attach(network.getBusBreakerView().getBus("NLOAD"));
        SlackTerminal stLoad = vlhv1.getExtension(SlackTerminal.class);
        assertNotNull(stLoad);
        assertEquals("NLOAD", stLoad.getTerminal().getBusBreakerView().getBus().getId());
        assertFalse(stLoad.isEmpty());

        // Reset the SlackTerminal of VLGEN voltageLevel to its previous value
        SlackTerminal.reset(vlgen, tGen);
        stGen = vlgen.getExtension(SlackTerminal.class);
        assertNotNull(stGen);
        assertEquals(tGen, stGen.getTerminal());
        variantManager.setWorkingVariant(variant2);
        assertNull(stGen.getTerminal());

        // Removes all SlackTerminals from network
        variantManager.setWorkingVariant(variant1);
        SlackTerminal.reset(network);
        assertNull(vlgen.getExtension(SlackTerminal.class));
        assertNull(vlhv1.getExtension(SlackTerminal.class));

    }

    @Test
    @Override
    public void testWithSubnetwork() {
        // Network merging unavailable
    }
}

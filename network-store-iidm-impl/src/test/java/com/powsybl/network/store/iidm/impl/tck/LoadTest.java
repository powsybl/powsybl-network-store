/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.tck.AbstractLoadTest;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import com.powsybl.network.store.iidm.impl.LoadImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static com.powsybl.network.store.iidm.impl.CreateNetworksUtil.createNodeBreakerNetworkWithLine;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LoadTest extends AbstractLoadTest {

    //TODO remove this test when ZipLoadModelAdder is implemented
    @Override
    @Test
    public void testZipLoadModel() { }

    //TODO remove this test when ZipLoadModelAdder is implemented
    @Override
    @Test
    public void testExponentialLoadModel() {
        // FIXME
        Network network = FictitiousSwitchFactory.create();
        VoltageLevel voltageLevel = network.getVoltageLevel("C");
        assertNull(voltageLevel.newLoad().newExponentialModel().setNp(0.0).setNq(0.0).add());
    }

    @Test
    void testTerminals() {
        Network network = createNodeBreakerNetworkWithLine();
        Load load = network.getLoad("L");
        assertNotNull(load);
        assertInstanceOf(LoadImpl.class, load);

        Terminal terminal = load.getTerminal();
        assertEquals(List.of(terminal), ((LoadImpl) load).getTerminals(null));
        assertEquals(List.of(terminal), ((LoadImpl) load).getTerminals(ThreeSides.ONE));
        assertEquals(Collections.emptyList(), ((LoadImpl) load).getTerminals(ThreeSides.TWO));
        assertEquals(Collections.emptyList(), ((LoadImpl) load).getTerminals(ThreeSides.THREE));
    }

    @Override
    @Test
    public void setNameTest() {
        // This is an adaptation of the same test method in powsybl-core
        // to be removed when changing the name will notify the update with the variant id provided (instead of null) in powsybl-core

        Network network = FictitiousSwitchFactory.create();
        NetworkListener mockedListener = Mockito.mock(DefaultNetworkListener.class);
        network.addListener(mockedListener);
        Load load = network.getLoad("CE");
        assertNotNull(load);
        assertTrue(load.getOptionalName().isEmpty());
        load.setName("FOO");
        assertEquals("FOO", load.getOptionalName().orElseThrow());

        // Here is the change from the powsybl-core version
        //Mockito.verify(mockedListener, Mockito.times(1)).onUpdate(load, "name", null, null, "FOO");
        Mockito.verify(mockedListener, Mockito.times(1)).onUpdate(load, "name", INITIAL_VARIANT_ID, null, "FOO");
    }
}

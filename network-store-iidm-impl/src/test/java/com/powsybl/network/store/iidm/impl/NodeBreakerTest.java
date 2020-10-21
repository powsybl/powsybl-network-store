/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.tck.AbstractNodeBreakerTest;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeBreakerTest extends AbstractNodeBreakerTest {

    /**
     * !!!! TO REMOVE to use TCK method when remove method will be implemented.
     */
    @Test
    public void connectDisconnectRemove() {
        Network network;
        try {
            Method createNetwork = AbstractNodeBreakerTest.class.getDeclaredMethod("createNetwork");
            createNetwork.setAccessible(true);
            network = (Network) createNetwork.invoke(this);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new PowsyblException(e);
        }

        VoltageLevel.NodeBreakerView topo = network.getVoltageLevel("VL").getNodeBreakerView();
        Load l = network.getLoad("L");
        Generator g = network.getGenerator("G");

        // generator is connected, load is disconnected
        assertTrue(topo.getOptionalTerminal(2).isPresent());
        assertTrue(topo.getOptionalTerminal(3).isPresent());
        assertNotNull(g.getTerminal().getBusView().getBus());
        assertNull(l.getTerminal().getBusView().getBus());
        assertTrue(g.getTerminal().isConnected());
        assertFalse(l.getTerminal().isConnected());

        // connect the load
        assertTrue(l.getTerminal().connect());

        // check load is connected
        assertTrue(topo.getOptionalTerminal(2).isPresent());
        assertNotNull(l.getTerminal().getBusView().getBus());
        assertTrue(l.getTerminal().isConnected());

        // disconnect the generator
        g.getTerminal().disconnect();

        // check generator is disconnected
        assertTrue(topo.getOptionalTerminal(3).isPresent());
        assertNull(g.getTerminal().getBusView().getBus());
        assertFalse(g.getTerminal().isConnected());
    }
}

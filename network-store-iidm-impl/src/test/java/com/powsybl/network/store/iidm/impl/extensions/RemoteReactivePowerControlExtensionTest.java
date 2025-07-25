/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControlAdder;
import com.powsybl.network.store.iidm.impl.CreateNetworksUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
class RemoteReactivePowerControlExtensionTest {
    @Test
    void testRemoteReactivePowerControlExtension() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();

        Generator g = network.getGenerator("G");
        assertNotNull(g);
        assertNull(g.getExtension(RemoteReactivePowerControl.class));

        Load l = network.getLoad("L");
        assertNotNull(l);

        g.newExtension(RemoteReactivePowerControlAdder.class)
            .withTargetQ(50.)
            .withEnabled(true)
            .withRegulatingTerminal(l.getTerminal())
            .add();

        RemoteReactivePowerControl remoteReactivePowerControl = g.getExtension(RemoteReactivePowerControl.class);
        assertNotNull(remoteReactivePowerControl);
        assertEquals(50., remoteReactivePowerControl.getTargetQ());
        assertTrue(remoteReactivePowerControl.isEnabled());
        assertNotNull(remoteReactivePowerControl.getRegulatingTerminal());
        assertEquals("L", remoteReactivePowerControl.getRegulatingTerminal().getConnectable().getId());

        Load ld = network.getLoad("LD");
        assertNotNull(ld);
        remoteReactivePowerControl.setRegulatingTerminal(ld.getTerminal());
        assertEquals("LD", remoteReactivePowerControl.getRegulatingTerminal().getConnectable().getId());

        g.removeExtension(RemoteReactivePowerControl.class);
        assertNull(g.getExtension(RemoteReactivePowerControl.class));
    }
}

/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.network.store.iidm.impl.CreateNetworksUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RemoteReactivePowerControlTest {

    @Test
    void generatorUsesReactivePowerMode() {
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        Generator generator = network.getGenerator("G");
        Load load = network.getLoad("L");

        generator.newVoltageRegulation()
            .withMode(RegulationMode.REACTIVE_POWER)
            .withRegulating(true)
            .withTerminal(load.getTerminal())
            .withTargetValue(50.)
            .build();

        assertNotNull(generator.getVoltageRegulation());
        assertEquals(RegulationMode.REACTIVE_POWER, generator.getVoltageRegulation().getMode());
        assertEquals(50., generator.getRegulatingTargetQ());
        assertEquals("L", generator.getRegulatingTerminal().getConnectable().getId());
    }
}

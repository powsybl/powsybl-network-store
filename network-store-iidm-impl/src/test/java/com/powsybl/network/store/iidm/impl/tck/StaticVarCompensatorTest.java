/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.tck.AbstractStaticVarCompensatorTest;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class StaticVarCompensatorTest extends AbstractStaticVarCompensatorTest {

    @Test
    void testRegulationWhenRegulatedElementIsRemoved() {
        Network network = SvcTestCaseFactory.create();
        network.getVoltageLevel("VL2").newLoad()
            .setId("load")
            .setP0(10)
            .setQ0(11)
            .setLoadType(LoadType.UNDEFINED)
            .setConnectableBus("B2")
            .add();
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        svc.setRegulatingTerminal(network.getLoad("load").getTerminal());
        svc.setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE);
        network.getLoad("load").remove();
        Assertions.assertEquals("SVC2", svc.getRegulatingTerminal().getConnectable().getId());
        Assertions.assertEquals(StaticVarCompensator.RegulationMode.VOLTAGE, svc.getRegulationMode());

        svc.setRegulatingTerminal(network.getGenerator("G1").getTerminal());
        Assertions.assertEquals("G1", svc.getRegulatingTerminal().getConnectable().getId());
        network.getGenerator("G1").remove();
        Assertions.assertEquals("SVC2", svc.getRegulatingTerminal().getConnectable().getId());
        Assertions.assertFalse(svc.isRegulating());
    }
}

/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.DefaultTopologyVisitor;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class NpeGetConnectableTerminalWhenRemovingLineTest {

    @Test
    void test() {
        assertDoesNotThrow(() -> {
            Network network = FourSubstationsNodeBreakerFactory.create();
            network.getLine("LINE_S2S3").remove();
            VoltageLevel vl = network.getVoltageLevel("S2VL1");
            for (Bus b : vl.getBusView().getBuses()) {
                b.visitConnectedEquipments(new DefaultTopologyVisitor());
            }
        });
    }
}

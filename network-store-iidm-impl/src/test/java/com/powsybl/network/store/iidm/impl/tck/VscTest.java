/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.tck.AbstractVscTest;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@ExtendWith(ExcludeTestsExtension.class)
class VscTest extends AbstractVscTest {

    @Test
    void testRemoveVoltageRegulationPerVariant() {
        Network network = HvdcTestNetwork.createVsc();
        VscConverterStation vsc = network.getVscConverterStation("C1");
        vsc.setLocalTargetQ(10);
        vsc.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(false)
            .build();

        VoltageRegulationTestUtils.assertRemoveVoltageRegulationPerVariant(network, vsc);
    }
}

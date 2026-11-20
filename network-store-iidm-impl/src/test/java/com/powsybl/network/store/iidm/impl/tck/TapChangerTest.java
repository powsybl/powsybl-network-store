/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.tck.AbstractTapChangerTest;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@ExtendWith(ExcludeTestsExtension.class)
class TapChangerTest extends AbstractTapChangerTest {

    @Test
    void testRemoveVoltageRegulationPerVariant() {
        Network network = NoEquipmentNetworkFactory.create();
        TwoWindingsTransformer transformer = network.getSubstation("sub").newTwoWindingsTransformer()
            .setId("twt")
            .setR(1.0)
            .setX(2.0)
            .setG(3.0)
            .setB(4.0)
            .setRatedU1(5.0)
            .setRatedU2(6.0)
            .setVoltageLevel1("vl1")
            .setVoltageLevel2("vl2")
            .setConnectableBus1("busA")
            .setConnectableBus2("busB")
            .add();
        RatioTapChanger ratioTapChanger = transformer.newRatioTapChanger()
            .setLowTapPosition(0)
            .setTapPosition(1)
            .setLoadTapChangingCapabilities(true)
            .newVoltageRegulation()
                .withRegulating(false)
                .withMode(RegulationMode.VOLTAGE)
                .withTargetValue(10.0)
                .withTargetDeadband(1.0)
                .withTerminal(transformer.getTerminal(TwoSides.ONE))
                .add()
            .beginStep()
                .setR(39.78473)
                .setX(39.784725)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
            .endStep()
            .beginStep()
                .setR(39.78474)
                .setX(39.784726)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
            .endStep()
            .beginStep()
                .setR(39.78475)
                .setX(39.784727)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
            .endStep()
            .add();
        ratioTapChanger.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(false)
            .build();

        VoltageRegulationTestUtils.assertRemoveVoltageRegulationPerVariant(network, ratioTapChanger);
    }
}

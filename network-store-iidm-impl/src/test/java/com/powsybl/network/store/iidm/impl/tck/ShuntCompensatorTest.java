/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.tck.AbstractShuntCompensatorTest;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@ExtendWith(ExcludeTestsExtension.class)
class ShuntCompensatorTest extends AbstractShuntCompensatorTest {
    @Override
    public void baseLinearShuntTest() {
        // FIXME remove this test when we use the release containing this PR : https://github.com/powsybl/powsybl-core/pull/2847
    }

    //TODO: remove all this code when shunt::getVoltageRegulation throws PowsyblException
    private static final String TEST_MULTI_VARIANT = "testMultiVariant";

    private ShuntCompensatorAdder createShuntAdder(String id, String name, int sectionCount, Terminal regulatingTerminal,
                                                   boolean voltageRegulatorOn, double targetV, double targetDeadband) {
        return voltageLevel.newShuntCompensator()
                .setId(id)
                .setName(name)
                .setConnectableBus("busA")
                .setSectionCount(sectionCount)
                .setLocalTargetV(targetV)
                .newVoltageRegulation()
                .withTerminal(regulatingTerminal)
                .withRegulating(voltageRegulatorOn)
                .withTargetValue(regulatingTerminal != null ? targetV : Double.NaN)
                .withMode(RegulationMode.VOLTAGE)
                .withTargetDeadband(targetDeadband)
                .add();
    }

    private ShuntCompensator createLinearShunt(String id, String name, double bPerSection, double gPerSection,
                                               int sectionCount, int maxSectionCount, Terminal regulatingTerminal,
                                               boolean voltageRegulatorOn, double targetV, double targetDeadband) {
        return createShuntAdder(id, name, sectionCount, regulatingTerminal, voltageRegulatorOn, targetV, targetDeadband)
                .newLinearModel()
                .setBPerSection(bPerSection)
                .setGPerSection(gPerSection)
                .setMaximumSectionCount(maxSectionCount)
                .add()
                .add();
    }

    private Network network;
    private VoltageLevel voltageLevel;
    private Terminal terminal;

    @Override
    @Test
    public void testSetterGetterInMultiVariants() {
        network = NoEquipmentNetworkFactory.create();
        network.getVoltageLevel("vl2").newLoad()
                .setId("load")
                .setBus("busB")
                .setConnectableBus("busB")
                .setP0(0)
                .setQ0(0)
                .add();
        terminal = network.getLoad("load").getTerminal();
        voltageLevel = network.getVoltageLevel("vl1");
        VariantManager variantManager = network.getVariantManager();
        createLinearShunt(TEST_MULTI_VARIANT, TEST_MULTI_VARIANT, 2.0, 1.0, 5, 10, terminal, true, 200, 10);
        ShuntCompensator shunt = network.getShuntCompensator(TEST_MULTI_VARIANT);
        List<String> variantsToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantsToAdd);

        variantManager.setWorkingVariant("s4");
        // check values cloned by extend
        assertEquals(5, shunt.getSectionCount());
        assertEquals(10.0, shunt.getB(), 0.0); // 2*5
        assertEquals(5.0, shunt.getG(), 0.0); // 1*5
        // change values in s4
        VoltageRegulation voltageRegulationS4 = shunt.setSectionCount(4).getVoltageRegulation();
        voltageRegulationS4.setRegulating(false);
        shunt.setLocalTargetV(220);
        voltageRegulationS4.setTargetDeadband(5.0);

        // remove s2
        variantManager.removeVariant("s2");

        variantManager.cloneVariant("s4", "s2b");
        variantManager.setWorkingVariant("s2b");
        // check values cloned by allocate
        assertEquals(4, shunt.getSectionCount());
        assertEquals(8.0, shunt.getB(), 0.0); // 2*4
        assertEquals(4.0, shunt.getG(), 0.0); // 1*4
        assertFalse(shunt.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertFalse(shunt.isRegulating());
        assertEquals(220, shunt.getLocalTargetV(), 0.0);
        assertEquals(5.0, shunt.getVoltageRegulation().getTargetDeadband(), 0.0);

        // recheck initial variant value
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(5, shunt.getSectionCount());
        assertEquals(10.0, shunt.getB(), 0.0); // 2*5
        assertEquals(5.0, shunt.getG(), 0.0); // 1*5
        assertTrue(shunt.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertEquals(200, shunt.getRegulatingTargetV(), 0.0);
        assertEquals(10, shunt.getVoltageRegulation().getTargetDeadband(), 0.0);

        // remove working variant s4
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        assertThrows(PowsyblException.class, shunt::getSectionCount);
        assertThrows(PowsyblException.class, () -> shunt.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertThrows(PowsyblException.class, shunt::getRegulatingTargetV);
        assertThrows(PowsyblException.class, shunt::getVoltageRegulation);

        // check we delete a single variant's values
        variantManager.setWorkingVariant("s3");
        assertEquals(5, shunt.getSectionCount());
    }

    @Test
    void testRemoveVoltageRegulationPerVariant() {
        network = NoEquipmentNetworkFactory.create();
        voltageLevel = network.getVoltageLevel("vl1");
        ShuntCompensator shunt = createLinearShunt("shunt", "shunt", 2.0, 1.0, 1, 2, null, false, 200, 10);
        shunt.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(false)
            .build();

        VoltageRegulationTestUtils.assertRemoveVoltageRegulationPerVariant(network, shunt);
    }
}

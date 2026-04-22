/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import com.powsybl.iidm.network.tck.extensions.AbstractBusbarSectionPositionTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class BusbarSectionPositionTest extends AbstractBusbarSectionPositionTest {
    @Override
    @Test
    //
    // Test overrided from AbstractBusbarSectionPositionTest, because powsybl has a unique execption message for a bad index,
    // whereas it is a bad busbar index or a bad section index
    // In network store, we distinguish the both cases with a specific execption message
    //
    public void testExtension() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
            .setId("S")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl = s.newVoltageLevel()
            .setId("VL")
            .setNominalV(400.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        BusbarSection bbs = vl.getNodeBreakerView().newBusbarSection()
            .setId("BBS")
            .setNode(0)
            .add();

        BusbarSectionPosition busbarSectionPosition = bbs.newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(1)
                .withSectionIndex(2)
                .add();
        assertEquals(1, busbarSectionPosition.getBusbarIndex());
        assertEquals(2, busbarSectionPosition.getSectionIndex());

        // Wrong Busbar index
        ValidationException e0 = assertThrows(ValidationException.class, () -> busbarSectionPosition.setBusbarIndex(-1));
        assertEquals("Busbar section 'BBS': Busbar index has to be greater or equals to zero", e0.getMessage());

        // Wrong Section index
        e0 = assertThrows(ValidationException.class, () -> busbarSectionPosition.setBusbarIndex(0).setSectionIndex(-1));
        assertEquals("Busbar section 'BBS': Section index has to be greater or equals to zero", e0.getMessage());

        // Right busbar and section index
        busbarSectionPosition.setBusbarIndex(10);
        busbarSectionPosition.setSectionIndex(5);
        assertEquals(10, busbarSectionPosition.getBusbarIndex());
        assertEquals(5, busbarSectionPosition.getSectionIndex());
    }
}

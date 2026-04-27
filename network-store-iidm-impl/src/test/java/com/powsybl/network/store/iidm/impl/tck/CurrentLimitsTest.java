/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.tck.AbstractCurrentLimitsTest;
import static org.junit.Assert.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CurrentLimitsTest extends AbstractCurrentLimitsTest {
    // TODO: this test is overridden because the validation rule has changed:temporary limit name duplication is no longer allowed.

    private static Network createNetwork() {
        Network network = Network.create("test", "test");
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        Substation s2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        Line l = network.newLine()
                .setId("L")
                .setVoltageLevel1("VL1")
                .setConnectableBus1("B1")
                .setBus1("B1")
                .setVoltageLevel2("VL2")
                .setConnectableBus2("B2")
                .setBus2("B2")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0)
                .add();
        l.getOrCreateSelectedOperationalLimitsGroup1().newCurrentLimits()
                .setPermanentLimit(1000.0)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("5'")
                .setAcceptableDuration(5 * 60)
                .setValue(1400.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("1'")
                .setAcceptableDuration(60)
                .setValue(1600.0)
                .endTemporaryLimit()
                .add();
        return network;
    }

    private void createLimitsWithDuplicateName(Line line) {
        line.getOrCreateSelectedOperationalLimitsGroup1()
                .newCurrentLimits()
                .setPermanentLimit(100.0)
                .beginTemporaryLimit()
                .setName("TL")
                .setAcceptableDuration(1200)
                .setValue(1200.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("TL") // duplication
                .setAcceptableDuration(600)
                .setValue(1400.0)
                .endTemporaryLimit();
    }

    @Override
    @Test
    public void testNameDuplicationIsAllowed() { // testNameDuplicationIsNotAllowed To RENAME
        Line line = createNetwork().getLine("L");
        String message = Assertions.assertThrows(ValidationException.class, () ->
                createLimitsWithDuplicateName(line)
        ).getMessage();
        Assertions.assertEquals("AC line 'L': temporary limit name 'TL' should be unique within limit set 'DEFAULT'", message);
    }

    @Test
    void testTemporaryLimitValueIsNotSetMessage() {
        Line line = createNetwork().getLine("L");
        var temporaryLimitAdder = line.getOrCreateSelectedOperationalLimitsGroup1()
                .newCurrentLimits()
                .setPermanentLimit(100.0)
                .beginTemporaryLimit()
                .setName("TL")
                .setAcceptableDuration(1200);
        ValidationException e = Assertions.assertThrows(ValidationException.class, temporaryLimitAdder::endTemporaryLimit);
        Assertions.assertEquals("AC line 'L': temporary limit value is not set for 'TL' within limit set 'DEFAULT'", e.getMessage());
    }

    @Test
    void testTemporaryLimitValueIsNegativeMessage() {
        Line line = createNetwork().getLine("L");
        var temporaryLimitAdder = line.getOrCreateSelectedOperationalLimitsGroup1()
                .newCurrentLimits()
                .setPermanentLimit(100.0)
                .beginTemporaryLimit()
                .setName("TL")
                .setAcceptableDuration(1200)
                .setValue(-1.0);
        ValidationException e = Assertions.assertThrows(ValidationException.class, temporaryLimitAdder::endTemporaryLimit);
        Assertions.assertEquals("AC line 'L': temporary limit value must be >= 0 for 'TL' within limit set 'DEFAULT'", e.getMessage());
    }

    @Test
    void testTemporaryLimitAcceptableDurationIsNotSetMessage() {
        Line line = createNetwork().getLine("L");
        var temporaryLimitAdder = line.getOrCreateSelectedOperationalLimitsGroup1()
                .newCurrentLimits()
                .setPermanentLimit(100.0)
                .beginTemporaryLimit()
                .setName("TL")
                .setValue(1200.0);
        ValidationException e = Assertions.assertThrows(ValidationException.class, temporaryLimitAdder::endTemporaryLimit);
        Assertions.assertEquals("AC line 'L': acceptable duration is not set for 'TL' within limit set 'DEFAULT'", e.getMessage());
    }

    @Test
    void testTemporaryLimitAcceptableDurationIsNegativeMessage() {
        Line line = createNetwork().getLine("L");
        var temporaryLimitAdder = line.getOrCreateSelectedOperationalLimitsGroup1()
                .newCurrentLimits()
                .setPermanentLimit(100.0)
                .beginTemporaryLimit()
                .setName("TL")
                .setAcceptableDuration(-1)
                .setValue(1200.0);
        ValidationException e = Assertions.assertThrows(ValidationException.class, temporaryLimitAdder::endTemporaryLimit);
        Assertions.assertEquals("AC line 'L': acceptable duration must be >= 0 for 'TL' within limit set 'DEFAULT'", e.getMessage());
    }

    @Test
    void testTemporaryLimitNameIsNotSetMessage() {
        Line line = createNetwork().getLine("L");
        var temporaryLimitAdder = line.getOrCreateSelectedOperationalLimitsGroup1()
                        .newCurrentLimits()
                        .setPermanentLimit(100.0)
                        .beginTemporaryLimit()
                        .setAcceptableDuration(1200)
                        .setValue(1200.0);
        ValidationException e = Assertions.assertThrows(ValidationException.class, temporaryLimitAdder::endTemporaryLimit);
        Assertions.assertEquals("AC line 'L': name is not set within limit set 'DEFAULT'", e.getMessage());
    }
}

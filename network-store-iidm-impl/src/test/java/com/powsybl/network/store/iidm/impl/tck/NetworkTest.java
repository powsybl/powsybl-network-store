/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.OperationalLimitsGroup;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationLevel;
import com.powsybl.iidm.network.tck.AbstractNetworkTest;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkTest extends AbstractNetworkTest {

    @Override
    public void testWith() {
        // FIXME
    }

    @Override
    public void testScadaNetwork() {
        // FIXME
    }

    @Test
    public void testStreams() {
        // FIXME remove this test when we use the release containing this PR : https://github.com/powsybl/powsybl-core/pull/3020
    }

    @Override
    public void testNetwork1() {
        // FIXME remove this test when getFictitiousP0 and getFictitiousQ0 of CalculatedBus are implemented
    }

    // The following test methods are overrided from AbstractNetworkTest, because of a different message header
    // used in validation exception messages :
    // 'AC line' in powsybl
    // 'AC Line' in network store
    @Override
    public void testPermanentLimitOnSelectedOperationalLimitsGroup() {
        Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        Line line = network.getLine("NHV1_NHV2_1");
        line.getCurrentLimits2().ifPresent(currentLimits -> {
            ValidationException e = assertThrows(ValidationException.class, () -> currentLimits.setPermanentLimit(Double.NaN));
            assertTrue(e.getMessage().contains("AC Line 'NHV1_NHV2_1': permanent limit must be defined if temporary limits are present"));
        });
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        network.getLine("NHV1_NHV2_1").getCurrentLimits2().orElseThrow().setPermanentLimit(Double.NaN);
        assertTrue(Double.isNaN(network.getLine("NHV1_NHV2_1").getCurrentLimits2().orElseThrow().getPermanentLimit()));
        assertEquals(ValidationLevel.EQUIPMENT, network.getValidationLevel());
    }

    @Override
    public void testPermanentLimitOnUnselectedOperationalLimitsGroup() {
        Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        Assertions.assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        OperationalLimitsGroup unselectedGroup = network.getLine("NHV1_NHV2_1").newOperationalLimitsGroup1("unselectedGroup");
        Assertions.assertNotEquals("unselectedGroup", network.getLine("NHV1_NHV2_1").getSelectedOperationalLimitsGroupId1().orElseThrow());
        CurrentLimitsAdder adder = unselectedGroup.newCurrentLimits().setPermanentLimit(Double.NaN).beginTemporaryLimit().setName("5'").setAcceptableDuration(300).setValue(1000.0).endTemporaryLimit();
        Objects.requireNonNull(adder);
        ValidationException e = Assertions.assertThrows(ValidationException.class, adder::add);
        Assertions.assertTrue(e.getMessage().contains("AC Line 'NHV1_NHV2_1': permanent limit must be defined if temporary limits are present"));
        CurrentLimits currentLimits = adder.setPermanentLimit(1000.0).add();
        Assertions.assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        e = Assertions.assertThrows(ValidationException.class, () -> currentLimits.setPermanentLimit(Double.NaN));
        Assertions.assertTrue(e.getMessage().contains("AC Line 'NHV1_NHV2_1': permanent limit must be defined if temporary limits are present"));
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        currentLimits.setPermanentLimit(Double.NaN);
        Assertions.assertTrue(Double.isNaN(currentLimits.getPermanentLimit()));
        Assertions.assertEquals(ValidationLevel.EQUIPMENT, network.getValidationLevel());
    }

    @Override
    public void testPermanentLimitViaAdder() {
        Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        OperationalLimitsGroup unselectedGroup = network.getLine("NHV1_NHV2_1").newOperationalLimitsGroup1("unselectedGroup");
        assertNotEquals("unselectedGroup", network.getLine("NHV1_NHV2_1").getSelectedOperationalLimitsGroupId1().orElseThrow());
        CurrentLimitsAdder adder = unselectedGroup.newCurrentLimits()
            .setPermanentLimit(Double.NaN)
            .beginTemporaryLimit()
            .setName("5'")
            .setAcceptableDuration(300)
            .setValue(1000)
            .endTemporaryLimit();
        ValidationException e = assertThrows(ValidationException.class, adder::add);
        assertTrue(e.getMessage().contains("AC Line 'NHV1_NHV2_1': permanent limit must be defined if temporary limits are present"));
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        adder.add();
        assertEquals(ValidationLevel.EQUIPMENT, network.getValidationLevel());
    }
}

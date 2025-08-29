/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.tck.AbstractOperationalLimitsGroupsTest;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class OperationalLimitsGroupsTest extends AbstractOperationalLimitsGroupsTest {

    @Override
    public void testForOperationalLimitsGroupsOnLine() {
        // Remove operational limit group not implemented yet for branches with lazy loading
    }

    @Test
    void testRemoveOperationalLimitsGroup() {
        Network network = EurostagTutorialExample1Factory.create();
        Line line = network.getLine("NHV1_NHV2_1");

        UnsupportedOperationException exception = assertThrows(
                UnsupportedOperationException.class,
                () -> line.removeOperationalLimitsGroup1("testGroup")
        );
        assertEquals("Remove operational limits groups from a branch not implemented", exception.getMessage());

        exception = assertThrows(
                UnsupportedOperationException.class,
                () -> line.removeOperationalLimitsGroup2("testGroup")
        );

        assertEquals("Remove operational limits groups from a branch not implemented", exception.getMessage());
    }

}

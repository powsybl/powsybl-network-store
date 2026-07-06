/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck.extensions;

import com.powsybl.iidm.network.tck.extensions.AbstractLineCouplingsTest;
import com.powsybl.network.store.iidm.impl.tck.ExcludeTestsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ExcludeTestsExtension.class)
public class LineCouplingsTest extends AbstractLineCouplingsTest {

    @Override
    @Test
    public void test() {
    }

    @Test
    void testSameLine() {
    }

    @Test
    void testNullLine() {
    }

    @Test
    void testDuplicateCoupling() {
    }

    @Test
    void testFindSymmetric() {
    }

    @Test
    void testRemoveByLines() {
    }

    @Test
    void testRemoveByMutualCoupling() {
    }

    @Test
    void testSetters() {
    }

    @Test
    void testInvalidLineSegment() {
    }

    @Test
    void testInvalidRAndX() {
    }

    @Test
    void testListener() {
    }
}

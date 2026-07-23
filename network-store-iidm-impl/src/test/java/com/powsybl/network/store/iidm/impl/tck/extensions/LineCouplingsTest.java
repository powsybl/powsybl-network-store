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

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@ExtendWith(ExcludeTestsExtension.class)
class LineCouplingsTest extends AbstractLineCouplingsTest {

    @Override
    @Test
    public void test() {
        // FIXME: to be removed when homopolar shorticurcuit is implemented
    }

    @Test
    void testSameLine() {
        // FIXME: to be removed when homopolar shorticurcuit is implemented
    }

    @Test
    void testNullLine() {
        // FIXME: to be removed when homopolar shorticurcuit is implemented
    }

    @Test
    void testDuplicateCoupling() {
        // FIXME: to be removed when homopolar shorticurcuit is implemented
    }

    @Test
    void testFindSymmetric() {
        // FIXME: to be removed when homopolar shorticurcuit is implemented
    }

    @Test
    void testRemoveByLines() {
        // FIXME: to be removed when homopolar shorticurcuit is implemented
    }

    @Test
    void testRemoveByMutualCoupling() {
        // FIXME: to be removed when homopolar shorticurcuit is implemented
    }

    @Test
    void testSetters() {
        // FIXME: to be removed when homopolar shorticurcuit is implemented
    }

    @Test
    void testInvalidLineSegment() {
        // FIXME: to be removed when homopolar shorticurcuit is implemented
    }

    @Test
    void testInvalidRAndX() {
        // FIXME: to be removed when homopolar shorticurcuit is implemented
    }

    @Test
    void testListener() {
        // FIXME: to be removed when homopolar shorticurcuit is implemented
    }
}

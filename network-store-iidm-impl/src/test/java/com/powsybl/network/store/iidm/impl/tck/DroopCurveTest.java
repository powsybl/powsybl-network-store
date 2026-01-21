/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.tck.AbstractDroopCurveTest;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class DroopCurveTest extends AbstractDroopCurveTest {
    @Override
    public void testAdder() {
        // FIXME setup() uses DcNode that is not implemented in network store
    }

    @Override
    public void invalidMaxV() {
        // FIXME setup() uses DcNode that is not implemented in network store
    }

    @Override
    public void invalidMinV() {
        // FIXME setup() uses DcNode that is not implemented in network store
    }

    @Override
    public void overlapping() {
        // FIXME setup() uses DcNode that is not implemented in network store
    }

    @Override
    public void invalidK() {
        // FIXME setup() uses DcNode that is not implemented in network store
    }

    @Override
    public void discontinuous() {
        // FIXME setup() uses DcNode that is not implemented in network store
    }
}

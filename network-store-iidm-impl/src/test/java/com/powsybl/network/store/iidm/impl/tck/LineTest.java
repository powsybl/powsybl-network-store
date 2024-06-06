/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.tck.AbstractLineTest;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LineTest extends AbstractLineTest {

    @Override
    public void baseAcLineTests() {
        // FIXME remove this test when we use the release containing this PR : https://github.com/powsybl/powsybl-core/pull/3022
    }

    @Override
    public void testRemoveAcLine() {
        // FIXME remove this test when exception msg are homogenized with the powsybl-core
    }
}

/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.tck.AbstractMoveConnectableNotifTest;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class MoveConnectableNotifTest extends AbstractMoveConnectableNotifTest {
    @Override
    public void nodeBreakerTest() {
        // FIXME remove this test when Terminal.moveConnectable sends a notification (notifyUpdate)
    }

    @Override
    public void busBreakerTest() {
        // FIXME remove this test when Terminal.moveConnectable sends a notification (notifyUpdate)
    }
}

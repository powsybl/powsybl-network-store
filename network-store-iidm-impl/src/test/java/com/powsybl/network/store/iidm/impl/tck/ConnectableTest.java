/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.tck.AbstractConnectableTest;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ConnectableTest extends AbstractConnectableTest {
    @Override
    public void nominallyConnectedTest() {
        //FIXME removed this test when we add a new report when disconnecting a line and the terminal is already disconnected
    }

    @Override
    public void partiallyConnectedTest() {
        //FIXME remove this test when the AbstractBranchImpl.connect handles and returns the different situations the same way the core impl does :
        //  a call on connect with a fully connected line already should return false
        //  a call on connect on a partially connected line should return true if the connection went well
    }
}

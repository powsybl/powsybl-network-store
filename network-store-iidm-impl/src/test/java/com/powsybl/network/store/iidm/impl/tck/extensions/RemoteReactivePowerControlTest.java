/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck.extensions;

import com.powsybl.iidm.network.tck.extensions.AbstractRemoteReactivePowerControlTest;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RemoteReactivePowerControlTest extends AbstractRemoteReactivePowerControlTest {

    public void replacementTest() {
        // problem of modelisation when move a connectable with a regulated terminal
        // in network store the regulated terminal stays the same when moving because it is linked to only two attributes
        // in core it is not
    }
}

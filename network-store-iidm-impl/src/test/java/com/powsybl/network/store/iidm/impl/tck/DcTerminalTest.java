/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.tck.AbstractDcTerminalTest;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class DcTerminalTest extends AbstractDcTerminalTest {

    // These methods will allow for more detailed modeling of HVDCs.
    // This is a long-term work on the powsybl side.
    // It is too early to implement it on the network-store side and the need remains to be verified.

    @Override
    public void testDcLineDcTerminal() {
        // FIXME: implement the new DC model
    }

    @Override
    public void testDcConverterDcTerminal() {
        // FIXME: implement the new DC model
    }
}

/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.tck.extensions;

import com.powsybl.iidm.network.tck.extensions.AbstractSecondaryVoltageControlTest;
import org.junit.jupiter.api.Test;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecondaryVoltageControlTest extends AbstractSecondaryVoltageControlTest {

    @Test
    public void pilotPointTargetVoltageNotificationTest() {
        // notify is not implemented in powsybl-network-store-iidm-impl
    }

    @Test
    public void controlUnitParticipateNotificationTest() {
        // notify is not implemented in powsybl-network-store-iidm-impl
    }

    @Test
    public void extensionRemovalAndCreationNotificationTest() {
        // notify is not implemented in powsybl-network-store-iidm-impl
    }
}

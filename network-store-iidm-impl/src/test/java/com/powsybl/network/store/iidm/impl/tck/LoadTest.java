/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.tck.AbstractLoadTest;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadTest extends AbstractLoadTest {
    @Override
    public void testSetterGetterInMultiVariants() {
        // FIXME
    }

    @Override
    public void testZipLoadModel() {
        // FIXME
    }

    @Test
    @Override
    public void testExponentialLoadModel() {
        // FIXME
        Network network = FictitiousSwitchFactory.create();
        VoltageLevel voltageLevel = network.getVoltageLevel("C");
        assertNull(voltageLevel.newLoad().newExponentialModel().setNp(0.0).setNq(0.0).add());
    }
}

/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.tck.AbstractLoadTest;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import com.powsybl.network.store.iidm.impl.LoadImpl;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.powsybl.network.store.iidm.impl.CreateNetworksUtil.createNodeBreakerNetworkWithLine;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LoadTest extends AbstractLoadTest {

    //TODO remove this test when ZipLoadModelAdder is implemented
    @Override
    public void testZipLoadModel() { }

    //TODO remove this test when ZipLoadModelAdder is implemented
    @Override
    public void testExponentialLoadModel() {
        // FIXME
        Network network = FictitiousSwitchFactory.create();
        VoltageLevel voltageLevel = network.getVoltageLevel("C");
        assertNull(voltageLevel.newLoad().newExponentialModel().setNp(0.0).setNq(0.0).add());
    }

    @Test
    void testTerminals() {
        Network network = createNodeBreakerNetworkWithLine();
        Load load = network.getLoad("L");
        assertNotNull(load);
        assertInstanceOf(LoadImpl.class, load);

        Terminal terminal = load.getTerminal();
        assertEquals(List.of(terminal), ((LoadImpl) load).getTerminals(null));
        assertEquals(List.of(terminal), ((LoadImpl) load).getTerminals(ThreeSides.ONE));
        assertEquals(Collections.emptyList(), ((LoadImpl) load).getTerminals(ThreeSides.TWO));
        assertEquals(Collections.emptyList(), ((LoadImpl) load).getTerminals(ThreeSides.THREE));
    }
}

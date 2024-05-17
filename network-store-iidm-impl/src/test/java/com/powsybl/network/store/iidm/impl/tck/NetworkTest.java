/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.powsybl.iidm.network.tck.AbstractNetworkTest;
import org.junit.jupiter.api.Test;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkTest extends AbstractNetworkTest {

    @Override
    public void testWith() {
        // FIXME
    }

    @Override
    public void testScadaNetwork() {
        // FIXME
    }

    @Test
    public void testStreams() {
        // FIXME remove this test when we use the release containing this PR : https://github.com/powsybl/powsybl-core/pull/3020
    }

    @Override
    public void testNetwork1() {
        // FIXME remove this test when getFictitiousP0 and getFictitiousQ0 of CalculatedBus are implemented
    }
}

/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkGetBranchTest {

    @Test
    public void test() {
        var network = EurostagTutorialExample1Factory.create();
        assertNotNull(network.getBranch("NHV1_NHV2_1"));
        assertNotNull(network.getBranch("NGEN_NHV1"));
        assertNull(network.getBranch("foo"));
        assertNull(network.getBranch("LOAD"));
    }
}

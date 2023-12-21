/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class IdentifiableImplTest {

    @Test
    void test() {
        Network network = EurostagTutorialExample1Factory.create();
        var gen = network.getGenerator("GEN");
        assertEquals("def", gen.getProperty("foo", "def"));
        gen.setProperty("bar", "aaa");
        assertEquals("def", gen.getProperty("foo", "def")); // properties now exists but there is still no value for foo
    }
}

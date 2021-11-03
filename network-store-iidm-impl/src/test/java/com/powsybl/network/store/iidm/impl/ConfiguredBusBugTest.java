/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ConfiguredBusBugTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create(new NetworkFactoryImpl());
        List<String> ids = network.getIdentifiables().stream().map(Identifiable::getId).collect(Collectors.toList());
        assertEquals(List.of("P1", "P2", "VLGEN", "VLHV1", "VLHV2", "VLLOAD", "GEN", "LOAD", "NGEN_NHV1", "NHV2_NLOAD",
                "NHV1_NHV2_1", "NHV1_NHV2_2", "NGEN", "NHV1", "NHV2", "NLOAD"), ids);
        assertNotNull(network.getIdentifiable("NGEN"));
    }
}

/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GetConnectablesTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Generator> generators = network.getConnectableStream(Generator.class).toList();
        assertEquals(1, generators.size());
        List<Line> lines = network.getConnectableStream(Line.class).toList();
        assertEquals(2, lines.size());
        List<TwoWindingsTransformer> transformers = network.getConnectableStream(TwoWindingsTransformer.class).toList();
        assertEquals(2, transformers.size());
        List<Load> loads = network.getConnectableStream(Load.class).toList();
        assertEquals(1, loads.size());
        List<Injection> injections = network.getConnectableStream(Injection.class).toList();
        assertEquals(2, injections.size());
        List<HvdcConverterStation> converterStations = network.getConnectableStream(HvdcConverterStation.class).toList();
        assertTrue(converterStations.isEmpty());
    }
}

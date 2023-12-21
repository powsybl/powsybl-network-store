/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.extensions.CgmesTapChangers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class CgmesTapChangersTest {

    @Test
    void test() {
        Network network = Network.read(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource());
        for (TwoWindingsTransformer t : network.getTwoWindingsTransformers()) {
            System.out.println(t.getId());
        }
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("e482b89a-fa84-4ea9-8e70-a83d44790957");
        CgmesTapChangers<TwoWindingsTransformer> cgmesTapChangers = twt.getExtension(CgmesTapChangers.class);
        assertNotNull(cgmesTapChangers.getTapChanger("83cc66dd-8d93-4a2c-8103-f1f5a9cf7e2e"));
        assertNull(cgmesTapChangers.getTapChanger("xxx"));
    }
}

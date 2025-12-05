/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.extensions.CgmesTapChangers;
import com.powsybl.cgmes.extensions.CgmesTapChangersAdder;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class CgmesTapChangersTest {

    @Test
    void test() {
        Network network = Network.read(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource());
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("e482b89a-fa84-4ea9-8e70-a83d44790957");
        CgmesTapChangers<TwoWindingsTransformer> cgmesTapChangers = twt.getExtension(CgmesTapChangers.class);
        assertNotNull(cgmesTapChangers.getTapChanger("83cc66dd-8d93-4a2c-8103-f1f5a9cf7e2e"));
        assertNull(cgmesTapChangers.getTapChanger("xxx"));
    }

    @Test
    void testOnNonHiddenTapChanger() {
        Network network = EurostagTutorialExample1Factory.createWith3wTransformer();
        TwoWindingsTransformer twoWT = network.getTwoWindingsTransformer("NGEN_NHV1");

        CgmesTapChangers<TwoWindingsTransformer> ctc2wt = ((CgmesTapChangersAdder<TwoWindingsTransformer>) twoWT.newExtension(CgmesTapChangersAdder.class)).add();
        PowsyblException e = assertThrows(PowsyblException.class, () -> ctc2wt.newTapChanger().setId("tc1").setControlId("control1").setStep(1).setType("type1").setHiddenStatus(true).add());
        assertEquals("Hidden tap changers should have an ID for the combined tap changer", e.getMessage());

        e = assertThrows(PowsyblException.class, () -> ctc2wt.newTapChanger().setId("tc1").setControlId("control1").setStep(1).setType("type1").setHiddenStatus(false).setCombinedTapChangerId("a").add());
        assertEquals("Non-hidden tap changers do not have a different ID for the combined tap changer", e.getMessage());

        ctc2wt.newTapChanger().setId("tc1").setControlId("control1").setStep(1).setType("type1").setHiddenStatus(true).setCombinedTapChangerId("a").add();
        CgmesTapChangers<TwoWindingsTransformer> cgmesTapChangers = twoWT.getExtension(CgmesTapChangers.class);
        assertNotNull(cgmesTapChangers.getTapChanger("tc1"));
    }
}

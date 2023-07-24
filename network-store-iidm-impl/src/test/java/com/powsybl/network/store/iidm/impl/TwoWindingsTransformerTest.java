/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.iidm.network.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Ayoub LABIDI <ayoub.labidi at rte-france.com>
 */
public class TwoWindingsTransformerTest {

    @Test
    public void testTapChangerRemoval() {
        Network network = Importer.find("CGMES")
                .importData(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(), new NetworkFactoryImpl(), null);
        //test remove RatioTapChanger
        TwoWindingsTransformer twtWithRatioTapChanger = network.getTwoWindingsTransformer("b94318f6-6d24-4f56-96b9-df2531ad6543");
        twtWithRatioTapChanger.getRatioTapChanger().remove();
        assertNull(twtWithRatioTapChanger.getRatioTapChanger());

        //test remove PhaseTapChanger
        TwoWindingsTransformer twtWithPhaseTapChanger = network.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0");
        twtWithPhaseTapChanger.getPhaseTapChanger().remove();
        assertNull(twtWithPhaseTapChanger.getPhaseTapChanger());
    }
}

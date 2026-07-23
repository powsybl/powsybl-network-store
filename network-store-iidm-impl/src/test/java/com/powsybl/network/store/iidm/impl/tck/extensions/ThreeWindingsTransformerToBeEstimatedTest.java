/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimated;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimatedAdder;
import com.powsybl.iidm.network.tck.extensions.AbstractThreeWindingsTransformerToBeEstimatedTest;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ThreeWindingsTransformerToBeEstimatedTest extends AbstractThreeWindingsTransformerToBeEstimatedTest {

    @Test
    void test2() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2019-05-27T12:17:02.504+02:00"));

        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        ThreeWindingsTransformerToBeEstimated ext = twt.newExtension(ThreeWindingsTransformerToBeEstimatedAdder.class)
                .withRatioTapChanger1Status(false)
                .withRatioTapChanger2Status(true)
                .withRatioTapChanger3Status(true)
                .withPhaseTapChanger1Status(false)
                .withPhaseTapChanger2Status(false)
                .withPhaseTapChanger3Status(false)
                .add();

        assertNotNull(ext);

        ext.shouldEstimateRatioTapChanger1(false);
        assertFalse(ext.shouldEstimateRatioTapChanger1());
        ext.shouldEstimateRatioTapChanger1(true);
        assertTrue(ext.shouldEstimateRatioTapChanger1());
        ext.shouldEstimateRatioTapChanger(false, ThreeSides.ONE);
        assertFalse(ext.shouldEstimateRatioTapChanger1());

        ext.shouldEstimateRatioTapChanger2(true);
        assertTrue(ext.shouldEstimateRatioTapChanger2());
        ext.shouldEstimateRatioTapChanger2(false);
        assertFalse(ext.shouldEstimateRatioTapChanger2());
        ext.shouldEstimateRatioTapChanger(true, ThreeSides.TWO);
        assertTrue(ext.shouldEstimateRatioTapChanger2());

        ext.shouldEstimateRatioTapChanger3(true);
        assertTrue(ext.shouldEstimateRatioTapChanger3());
        ext.shouldEstimateRatioTapChanger3(false);
        assertFalse(ext.shouldEstimateRatioTapChanger3());
        ext.shouldEstimateRatioTapChanger(false, ThreeSides.THREE);
        assertFalse(ext.shouldEstimateRatioTapChanger3());

        ext.shouldEstimatePhaseTapChanger1(false);
        assertFalse(ext.shouldEstimatePhaseTapChanger1());
        ext.shouldEstimatePhaseTapChanger1(true);
        assertTrue(ext.shouldEstimatePhaseTapChanger1());
        ext.shouldEstimatePhaseTapChanger(false, ThreeSides.ONE);
        assertFalse(ext.shouldEstimatePhaseTapChanger1());

        ext.shouldEstimatePhaseTapChanger2(false);
        assertFalse(ext.shouldEstimatePhaseTapChanger2());
        ext.shouldEstimatePhaseTapChanger2(true);
        assertTrue(ext.shouldEstimatePhaseTapChanger2());
        ext.shouldEstimatePhaseTapChanger(true, ThreeSides.TWO);
        assertTrue(ext.shouldEstimatePhaseTapChanger2());

        ext.shouldEstimatePhaseTapChanger3(false);
        assertFalse(ext.shouldEstimatePhaseTapChanger3());
        ext.shouldEstimatePhaseTapChanger3(true);
        assertTrue(ext.shouldEstimatePhaseTapChanger3());
        ext.shouldEstimatePhaseTapChanger(false, ThreeSides.THREE);
        assertFalse(ext.shouldEstimatePhaseTapChanger3());
    }
}

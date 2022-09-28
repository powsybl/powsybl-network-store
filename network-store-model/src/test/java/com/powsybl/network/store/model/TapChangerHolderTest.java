/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import org.junit.Before;
import org.junit.Test;

import com.powsybl.iidm.network.PhaseTapChanger.RegulationMode;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Sylvain Bouzols <sylvain.bouzols at rte-france.com>
 */
public class TapChangerHolderTest {

    List<PhaseTapChangerStepAttributes> tapChangerStepsA;
    List<RatioTapChangerStepAttributes> tapChangerStepsB;

    @Before
    public void setUp() {
        tapChangerStepsA = new ArrayList<>();
        tapChangerStepsA.add(PhaseTapChangerStepAttributes.builder()
            .rho(1.)
            .r(2.)
            .g(3.)
            .b(4.)
            .x(5.)
            .alpha(6.)
            .build()
        );
        tapChangerStepsA.add(PhaseTapChangerStepAttributes.builder()
            .rho(1.1)
            .r(2.1)
            .g(3.1)
            .b(4.1)
            .x(5.1)
            .alpha(6.1)
            .build()
        );
        tapChangerStepsA.add(PhaseTapChangerStepAttributes.builder()
            .rho(1.2)
            .r(2.2)
            .g(3.2)
            .b(4.2)
            .x(5.2)
            .alpha(6.2)
            .build()
        );

        tapChangerStepsB = new ArrayList<>();
        tapChangerStepsB.add(RatioTapChangerStepAttributes.builder()
            .rho(10.)
            .r(20.)
            .g(30.)
            .b(40.)
            .x(50.)
            .build()
        );
        tapChangerStepsB.add(RatioTapChangerStepAttributes.builder()
            .rho(10.1)
            .r(20.1)
            .g(30.1)
            .b(40.1)
            .x(50.1)
            .build()
        );
    }

    @Test
    public void twoWindingsTransformertapChangerAttributesTest() {
        Resource<TwoWindingsTransformerAttributes> resourceTransformer = Resource.twoWindingsTransformerBuilder()
                .id("id2WT")
                .attributes(TwoWindingsTransformerAttributes.builder()
                        .voltageLevelId1("vl1")
                        .voltageLevelId2("vl2")
                        .name("id2WT")
                        .node1(1)
                        .node2(1)
                        .bus1("bus1")
                        .bus2("bus2")
                        .r(1)
                        .x(1)
                        .b(1)
                        .g(1)
                        .ratedU1(1.)
                        .ratedU2(1.)
                        .branchStatus("IN_OPERATION")
                        .phaseTapChangerAttributes(PhaseTapChangerAttributes.builder()
                                .lowTapPosition(1)
                                .regulating(true)
                                .regulatingTerminal(TerminalRefAttributes.builder()
                                        .connectableId("connectableId")
                                        .side("this side")
                                        .build()
                                )
                                .regulationMode(RegulationMode.ACTIVE_POWER_CONTROL)
                                .regulationValue(10.)
                                .steps(tapChangerStepsA)
                                .build()
                        )
                        .ratioTapChangerAttributes(RatioTapChangerAttributes.builder()
                                .lowTapPosition(1)
                                .regulating(true)
                                .regulatingTerminal(TerminalRefAttributes.builder()
                                        .connectableId("connectableId")
                                        .side("this side")
                                        .build()
                                )
                                .loadTapChangingCapabilities(true)
                                .targetDeadband(1.)
                                .targetV(10.)
                                .steps(tapChangerStepsB)
                                .build()
                        )
                        .build())
                .build();

        TwoWindingsTransformerAttributes twoWTAttributes = resourceTransformer.getAttributes();

        assertEquals(3, twoWTAttributes.getPhaseTapChangerAttributes(0).getSteps().size());
        assertEquals(2, twoWTAttributes.getRatioTapChangerAttributes(0).getSteps().size());

        assertThrows(IllegalArgumentException.class, () -> {
            twoWTAttributes.setPhaseTapChangerAttributes(1, null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            twoWTAttributes.setRatioTapChangerAttributes(1, null);
        });
        assertThrows(IllegalArgumentException.class, () -> twoWTAttributes.getPhaseTapChangerAttributes(1));
        assertThrows(IllegalArgumentException.class, () -> twoWTAttributes.getRatioTapChangerAttributes(1));

        twoWTAttributes.getPhaseTapChangerAttributes(0).getSteps().add(PhaseTapChangerStepAttributes.builder()
            .rho(1.3)
            .r(2.3)
            .g(3.3)
            .b(4.3)
            .x(5.3)
            .alpha(6.3)
            .build()
        );
        twoWTAttributes.getRatioTapChangerAttributes(0).getSteps().add(RatioTapChangerStepAttributes.builder()
            .rho(10.2)
            .r(20.2)
            .g(30.2)
            .b(40.2)
            .x(50.2)
            .build()
        );

        assertEquals(4, twoWTAttributes.getPhaseTapChangerAttributes(0).getSteps().size());
        assertEquals(3, twoWTAttributes.getRatioTapChangerAttributes(0).getSteps().size());

    }

    @Test
    public void threeWindingsTransformertapChangerAttributesTest() {
        Resource<ThreeWindingsTransformerAttributes> resourceTransformer = Resource.threeWindingsTransformerBuilder()
                .id("id3WT")
                .attributes(ThreeWindingsTransformerAttributes.builder()
                        .name("id3WT")
                        .ratedU0(1)
                        .branchStatus("IN_OPERATION")
                        .leg1(LegAttributes.builder()
                            .phaseTapChangerAttributes(PhaseTapChangerAttributes.builder()
                                .lowTapPosition(1)
                                .regulating(true)
                                .regulatingTerminal(TerminalRefAttributes.builder()
                                        .connectableId("connectableId")
                                        .side("this side")
                                        .build()
                                )
                                .regulationMode(RegulationMode.ACTIVE_POWER_CONTROL)
                                .regulationValue(10.)
                                .steps(tapChangerStepsA)
                                .build()
                            )
                            .build()
                        )
                        .leg2(LegAttributes.builder()
                            .ratioTapChangerAttributes(RatioTapChangerAttributes.builder()
                                .lowTapPosition(1)
                                .regulating(true)
                                .regulatingTerminal(TerminalRefAttributes.builder()
                                        .connectableId("connectableId")
                                        .side("this side")
                                        .build()
                                )
                                .loadTapChangingCapabilities(true)
                                .targetDeadband(1.)
                                .targetV(10.)
                                .steps(tapChangerStepsB)
                                .build()
                            )
                            .build()
                        )
                        .leg3(LegAttributes.builder()
                            .build()
                        )
                        .build())
                .build();

        ThreeWindingsTransformerAttributes threeWTAttributes = resourceTransformer.getAttributes();

        assertEquals(3, threeWTAttributes.getPhaseTapChangerAttributes(1).getSteps().size());
        assertNull(threeWTAttributes.getPhaseTapChangerAttributes(2));
        assertNull(threeWTAttributes.getPhaseTapChangerAttributes(3));
        assertEquals(2, threeWTAttributes.getRatioTapChangerAttributes(2).getSteps().size());
        assertNull(threeWTAttributes.getRatioTapChangerAttributes(1));
        assertNull(threeWTAttributes.getRatioTapChangerAttributes(3));
    }
}

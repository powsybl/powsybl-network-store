/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.integration;

import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

import java.util.Objects;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class NetworkStorageTestCaseFactory {

    private NetworkStorageTestCaseFactory() {
    }

    public static Network create() {
        return create(NetworkFactory.findDefault());
    }

    public static Network create(NetworkFactory networkFactory) {
        Objects.requireNonNull(networkFactory);

        Network network = networkFactory.createNetwork("networkTestCase", "code");
        network.setCaseDate(DateTime.parse("2016-06-29T14:54:03.427+02:00"));
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        Substation s2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("VL2")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        StaticVarCompensator svc = vl2.newStaticVarCompensator()
                .setId("SVC2")
                .setNode(0)
                .setBmin(0.0002)
                .setBmax(0.0008)
                .setReactivePowerSetPoint(200)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetPoint(390)
                .add();
        svc.getTerminal().setP(435);
        svc.getTerminal().setQ(315);
        VscConverterStation vscConverterStation1 = vl1.newVscConverterStation()
                .setId("VSC1")
                .setNode(1)
                .setLossFactor(24)
                .setReactivePowerSetpoint(300)
                .setVoltageRegulatorOn(true)
                .setVoltageSetpoint(290)
                .add();
        VscConverterStation vscConverterStation2 = vl2.newVscConverterStation()
                .setId("VSC2")
                .setNode(2)
                .setLossFactor(17)
                .setReactivePowerSetpoint(227)
                .setVoltageRegulatorOn(false)
                .setVoltageSetpoint(213)
                .add();
        DanglingLine danglingLine1 = vl1.newDanglingLine()
                .setId("DL1")
                .setNode(1)
                .setName("Dangling line 1")
                .setP0(533)
                .setQ0(242)
                .setR(27)
                .setX(44)
                .setG(89)
                .setB(11)
                .setUcteXnodeCode("UCTE_DL1")
                .newGeneration()
                .setTargetP(100)
                .setTargetQ(200)
                .setTargetV(300)
                .setMinP(10)
                .setMaxP(500)
                .setVoltageRegulationOn(true)
                .add()
                .add();
        danglingLine1.getGeneration()
                .newMinMaxReactiveLimits()
                .setMinQ(200)
                .setMaxQ(800)
                .add();
        danglingLine1.newCurrentLimits()
                .setPermanentLimit(256)
                .beginTemporaryLimit()
                .setName("TL1")
                .setValue(432)
                .setAcceptableDuration(20)
                .setFictitious(false)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("TL2")
                .setValue(289)
                .setAcceptableDuration(40)
                .setFictitious(true)
                .endTemporaryLimit()
                .add();

        DanglingLine danglingLine2 = vl1.newDanglingLine()
                .setId("DL2")
                .setNode(1)
                .setName("Dangling line 2")
                .setP0(533)
                .setQ0(242)
                .setR(27)
                .setX(44)
                .setG(89)
                .setB(11)
                .setUcteXnodeCode("UCTE_DL2")
                .newGeneration()
                .setTargetP(100)
                .setTargetQ(200)
                .setTargetV(300)
                .setMinP(10)
                .setMaxP(500)
                .setVoltageRegulationOn(true)
                .add()
                .add();
        danglingLine2.newCurrentLimits()
                .setPermanentLimit(256)
                .beginTemporaryLimit()
                .setName("TL2")
                .setValue(432)
                .setAcceptableDuration(20)
                .setFictitious(false)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("TL3")
                .setValue(289)
                .setAcceptableDuration(40)
                .setFictitious(true)
                .endTemporaryLimit()
                .add();

        HvdcLine hvdc1 = network.newHvdcLine()
                .setId("HVDC1")
                .setR(256)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)
                .setActivePowerSetpoint(330)
                .setNominalV(335)
                .setMaxP(390)
                .setConverterStationId1("VSC1")
                .setConverterStationId2("VSC2")
                .add();
        vscConverterStation1.getTerminal().setP(445);
        vscConverterStation1.getTerminal().setQ(325);
        vscConverterStation1.newReactiveCapabilityCurve().beginPoint()
                .setP(5)
                .setMinQ(1)
                .setMaxQ(10)
                .endPoint()
                .beginPoint()
                .setP(10)
                .setMinQ(-10)
                .setMaxQ(1)
                .endPoint()
                .add();
        vscConverterStation2.getTerminal().setP(254);
        vscConverterStation2.getTerminal().setQ(117);
        vscConverterStation2.newMinMaxReactiveLimits()
                .setMaxQ(127)
                .setMinQ(103)
                .add();
        LccConverterStation lccConverterStation = vl2.newLccConverterStation()
                .setId("LCC2")
                .setNode(1)
                .setPowerFactor(0.5F)
                .setLossFactor(20)
                .add();
        lccConverterStation.getTerminal().setP(440);
        lccConverterStation.getTerminal().setQ(320);

        VoltageLevel vl3 = s2.newVoltageLevel()
                .setId("VL3")
                .setNominalV(225)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        VoltageLevel vl4 = s2.newVoltageLevel()
                .setId("VL4")
                .setNominalV(65)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        ThreeWindingsTransformer threeWindingsTransformer = s2.newThreeWindingsTransformer()
                .setId("TWT1")
                .setName("Three windings transformer 1")
                .setRatedU0(234)
                .newLeg1()
                .setVoltageLevel("VL2")
                .setNode(1)
                .setR(45)
                .setX(35)
                .setG(25)
                .setB(15)
                .setRatedU(5)
                .add()
                .newLeg2()
                .setVoltageLevel("VL3")
                .setNode(2)
                .setR(47)
                .setX(37)
                .setG(27)
                .setB(17)
                .setRatedU(7)
                .add()
                .newLeg3()
                .setVoltageLevel("VL4")
                .setNode(3)
                .setR(49)
                .setX(39)
                .setG(29)
                .setB(19)
                .setRatedU(9)
                .add()
                .add();
        threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.ONE).setP(375);
        threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.TWO).setP(225);
        threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.THREE).setP(200);
        threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.ONE).setQ(48);
        threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.TWO).setQ(28);
        threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.THREE).setQ(18);

        threeWindingsTransformer.getLeg1().newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .setRegulating(true)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationValue(25)
                .setRegulationTerminal(threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.ONE))
                .setTargetDeadband(22)
                .beginStep()
                .setAlpha(-10)
                .setRho(0.99)
                .setR(1.)
                .setX(4.)
                .setG(0.5)
                .setB(1.5)
                .endStep()
                .beginStep()
                .setAlpha(0)
                .setRho(1)
                .setR(1.1)
                .setX(4.1)
                .setG(0.6)
                .setB(1.6)
                .endStep()
                .beginStep()
                .setAlpha(10)
                .setRho(1.01)
                .setR(1.2)
                .setX(4.2)
                .setG(0.7)
                .setB(1.7)
                .endStep()
                .add();
        threeWindingsTransformer.getLeg2().newRatioTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .setRegulating(false)
                .setRegulationTerminal(threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.ONE))
                .setTargetDeadband(22)
                .setTargetV(220)
                .beginStep()
                .setRho(0.99)
                .setR(1.)
                .setX(4.)
                .setG(0.5)
                .setB(1.5)
                .endStep()
                .beginStep()
                .setRho(1)
                .setR(1.1)
                .setX(4.1)
                .setG(0.6)
                .setB(1.6)
                .endStep()
                .beginStep()
                .setRho(1.01)
                .setR(1.2)
                .setX(4.2)
                .setG(0.7)
                .setB(1.7)
                .endStep()
            .add();

        threeWindingsTransformer.getLeg1()
                .newCurrentLimits()
                .setPermanentLimit(25)
                .add();

        ShuntCompensator shunt1 = vl1.newShuntCompensator()
                .setId("SHUNT1")
                .setNode(0)
                .setVoltageRegulatorOn(true)
                .setTargetDeadband(10)
                .setTargetV(380)
                .newLinearModel().setBPerSection(1).setGPerSection(2).setMaximumSectionCount(10).add()
                .setSectionCount(5)
                .add();
        shunt1.getTerminal().setQ(200);

        ShuntCompensator shunt2 = vl2.newShuntCompensator()
                .setId("SHUNT2")
                .setNode(0)
                .setVoltageRegulatorOn(false)
                .setTargetDeadband(20)
                .setTargetV(420)
                .newNonLinearModel()
                .beginSection()
                .setB(1).setG(2)
                .endSection()
                .beginSection()
                .setB(3).setG(4)
                .endSection()
                .beginSection()
                .setB(5).setG(6)
                .endSection()
                .beginSection()
                .setB(7).setG(8)
                .endSection()
                .add()
                .setSectionCount(3)
                .add();
        shunt2.getTerminal().setQ(600);

        return network;
    }
}

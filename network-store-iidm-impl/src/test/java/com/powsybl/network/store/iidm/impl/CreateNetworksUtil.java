/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
final class CreateNetworksUtil {

    static String BUS_UNKNOW_ID = "unknown";

    private CreateNetworksUtil() {
    }

    private static int initAdder(InjectionAdder adder, TopologyKind topologyKind, int invalidNode) {
        int node = invalidNode;
        if (topologyKind == TopologyKind.NODE_BREAKER) {
            adder.setNode(node--);
        } else {
            adder.setConnectableBus(topologyKind == TopologyKind.BUS_BREAKER ? BUS_UNKNOW_ID : null);
        }
        return node;
    }

    private static int initAdder(ThreeWindingsTransformerAdder.LegAdder adder, TopologyKind topologyKind, int invalidNode) {
        int node = invalidNode;
        if (topologyKind == TopologyKind.NODE_BREAKER) {
            adder.setNode(node--);
        } else {
            adder.setConnectableBus(topologyKind == TopologyKind.BUS_BREAKER ? BUS_UNKNOW_ID : null);
        }
        return node;
    }

    private static int initAdder(BranchAdder adder, TopologyKind topologyKind, int invalidNode) {
        int node = invalidNode;
        if (topologyKind == TopologyKind.NODE_BREAKER) {
            adder.setNode1(node--);
            adder.setNode2(node--);
        } else {
            adder.setConnectableBus1(topologyKind == TopologyKind.BUS_BREAKER ? BUS_UNKNOW_ID : null);
            adder.setConnectableBus2(topologyKind == TopologyKind.BUS_BREAKER ? BUS_UNKNOW_ID : null);
        }
        return node;
    }

    private static VoltageLevel addVoltageLevel(Substation s, String id, TopologyKind topologyKind, double nominalV) {
        VoltageLevel vl = s.newVoltageLevel()
                .setId(id)
                .setTopologyKind(topologyKind)
                .setNominalV(nominalV)
                .add();

        if (topologyKind == TopologyKind.BUS_BREAKER && vl.getNetwork().getBusBreakerView().getBus(BUS_UNKNOW_ID) == null) {
            vl.getBusBreakerView().newBus()
                    .setId(BUS_UNKNOW_ID)
                    .add();
        }

        return vl;
    }

    static void connectAllTerminalsVoltageLevel(VoltageLevel vl) {
        vl.getConnectableStream()
                .map(Connectable::getTerminals)
                .flatMap(List<Terminal>::stream)
                .filter(t -> t.getVoltageLevel().getId().equals(vl.getId()))
                .forEach(Terminal::connect);
    }

    static void disconnectAllTerminalsVoltageLevel(VoltageLevel vl) {
        vl.getConnectableStream()
                .map(Connectable::getTerminals)
                .flatMap(List<Terminal>::stream)
                .filter(t -> t.getVoltageLevel().getId().equals(vl.getId()))
                .forEach(Terminal::disconnect);
    }

    static List<String> recordVisited(Bus bus, boolean connectedEquipmentsOnly) {
        SortedSet<String> visited = new TreeSet<>();
        TopologyVisitor tv = new DefaultTopologyVisitor() {
            @Override
            public void visitBusbarSection(BusbarSection section) {
                visited.add(section.getId());
            }

            @Override
            public void visitLine(Line line, TwoSides side) {
                visited.add(line.getId());
            }

            @Override
            public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, TwoSides side) {
                visited.add(transformer.getId());
            }

            @Override
            public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeSides side) {
                visited.add(transformer.getId());
            }

            @Override
            public void visitGenerator(Generator generator) {
                visited.add(generator.getId());
            }

            @Override
            public void visitBattery(Battery battery) {
                visited.add(battery.getId());
            }

            @Override
            public void visitLoad(Load load) {
                visited.add(load.getId());
            }

            @Override
            public void visitShuntCompensator(ShuntCompensator sc) {
                visited.add(sc.getId());
            }

            @Override
            public void visitDanglingLine(DanglingLine danglingLine) {
                visited.add(danglingLine.getId());
            }

            @Override
            public void visitGround(Ground ground) {
                visited.add(ground.getId());
            }

            @Override
            public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {
                visited.add(staticVarCompensator.getId());
            }

            @Override
            public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
                visited.add(converterStation.getId());
            }
        };

        if (connectedEquipmentsOnly) {
            bus.visitConnectedEquipments(tv);
        } else {
            bus.visitConnectedOrConnectableEquipments(tv);
        }

        return visited.stream().collect(Collectors.toList());
    }

    private static Network createNetwokWithMultipleEquipments(TopologyKind topologyKind) {
        Network network = Network.create("test", "test");

        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add();

        Substation s2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .add();

        Substation s3 = network.newSubstation()
                .setId("S3")
                .setCountry(Country.FR)
                .add();

        VoltageLevel vl1 = addVoltageLevel(s1, "VL1", topologyKind, 380);
        VoltageLevel vl2 = addVoltageLevel(s2, "VL2", topologyKind, 380);
        addVoltageLevel(s2, "VL3", topologyKind, 225);
        addVoltageLevel(s2, "VL4", topologyKind, 65);
        addVoltageLevel(s3, "VL5", topologyKind, 225);
        addVoltageLevel(s3, "VL6", topologyKind, 380);

        int invalidNode = -1;

        InjectionAdder adder = vl1.newShuntCompensator();
        invalidNode = initAdder(adder, topologyKind, invalidNode);
        ShuntCompensator shunt1 = ((ShuntCompensatorAdder) adder)
                .setId("SHUNT1")
                .setVoltageRegulatorOn(true)
                .setTargetDeadband(10)
                .setTargetV(380)
                .newLinearModel().setBPerSection(1).setGPerSection(2).setMaximumSectionCount(10).add()
                .setSectionCount(5)
                .add();
        shunt1.getTerminal().setQ(200);

        adder = vl2.newShuntCompensator();
        invalidNode = initAdder(adder, topologyKind, invalidNode);
        ShuntCompensator shunt2 = ((ShuntCompensatorAdder) adder)
                .setId("SHUNT2")
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

        adder = vl1.newBattery();
        invalidNode = initAdder(adder, topologyKind, invalidNode);
        Battery battery = ((BatteryAdder) adder)
                .setId("battery")
                .setTargetP(50)
                .setTargetQ(10)
                .setMinP(40)
                .setMaxP(70)
                .add();
        battery.newReactiveCapabilityCurve()
                .beginPoint()
                .setMaxQ(1)
                .setMinQ(-1)
                .setP(2)
                .endPoint()
                .beginPoint()
                .setMaxQ(2)
                .setMinQ(-2)
                .setP(1)
                .endPoint()
                .add();
        battery.getTerminal().setQ(250);
        battery.getTerminal().setP(650);

        adder = vl1.newGround();
        invalidNode = initAdder(adder, topologyKind, invalidNode);
        Ground ground = ((GroundAdder) adder)
                .setId("ground")
                .add();
        ground.getTerminal().setP(500);
        battery.getTerminal().setQ(250);

        adder = vl2.newStaticVarCompensator();
        invalidNode = initAdder(adder, topologyKind, invalidNode);
        StaticVarCompensator svc = ((StaticVarCompensatorAdder) adder)
                .setId("SVC2")
                .setBmin(0.0002)
                .setBmax(0.0008)
                .setReactivePowerSetpoint(200)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetpoint(390)
                .add();
        svc.getTerminal().setP(435);
        svc.getTerminal().setQ(315);

        adder = vl1.newVscConverterStation();
        invalidNode = initAdder(adder, topologyKind, invalidNode);
        VscConverterStation vscConverterStation1 = ((VscConverterStationAdder) adder)
                .setId("VSC1")
                .setLossFactor(24)
                .setReactivePowerSetpoint(300)
                .setVoltageRegulatorOn(true)
                .setVoltageSetpoint(290)
                .add();

        adder = vl2.newVscConverterStation();
        invalidNode = initAdder(adder, topologyKind, invalidNode);
        VscConverterStation vscConverterStation2 = ((VscConverterStationAdder) adder)
                .setId("VSC2")
                .setLossFactor(17)
                .setReactivePowerSetpoint(227)
                .setVoltageRegulatorOn(false)
                .setVoltageSetpoint(213)
                .add();

        adder = vl1.newDanglingLine();
        invalidNode = initAdder(adder, topologyKind, invalidNode);
        DanglingLine danglingLine1 = ((DanglingLineAdder) adder)
                .setId("DL1")
                .setName("Dangling line 1")
                .setP0(533)
                .setQ0(242)
                .setR(27)
                .setX(44)
                .setG(89)
                .setB(11)
                .setPairingKey("UCTE_DL1")
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

        adder = vl1.newDanglingLine();
        invalidNode = initAdder(adder, topologyKind, invalidNode);
        DanglingLine danglingLine2 = ((DanglingLineAdder) adder)
                .setId("DL2")
                .setName("Dangling line 2")
                .setP0(533)
                .setQ0(242)
                .setR(27)
                .setX(44)
                .setG(89)
                .setB(11)
                .setPairingKey("UCTE_DL2")
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

        adder = vl2.newLccConverterStation();
        invalidNode = initAdder(adder, topologyKind, invalidNode);
        LccConverterStation lccConverterStation = ((LccConverterStationAdder) adder)
                .setId("LCC2")
                .setPowerFactor(0.5F)
                .setLossFactor(20)
                .add();
        lccConverterStation.getTerminal().setP(440);
        lccConverterStation.getTerminal().setQ(320);

        BranchAdder branchAdder = network.newLine();
        invalidNode = initAdder(branchAdder, topologyKind, invalidNode);
        Line l1 = ((LineAdder) branchAdder)
                .setId("LINE1")
                .setVoltageLevel1("VL1")
                .setVoltageLevel2("VL2")
                .setR(50)
                .setX(20)
                .setG1(12)
                .setG2(24)
                .setB1(45)
                .setB2(32)
                .add();

        branchAdder = s3.newTwoWindingsTransformer();
        invalidNode = initAdder(branchAdder, topologyKind, invalidNode);
        TwoWindingsTransformer twoWindingsTransformer = ((TwoWindingsTransformerAdder) branchAdder)
                .setId("TwoWT1")
                .setName("Two windings transformer 1")
                .setVoltageLevel1("VL5")
                .setVoltageLevel2("VL6")
                .setR(250)
                .setX(100)
                .setG(52)
                .setB(12)
                .setRatedU1(65)
                .setRatedU2(90)
                .add();

        twoWindingsTransformer.getTerminal(TwoSides.ONE).setP(375);
        twoWindingsTransformer.getTerminal(TwoSides.TWO).setP(225);
        twoWindingsTransformer.getTerminal(TwoSides.ONE).setQ(48);
        twoWindingsTransformer.getTerminal(TwoSides.TWO).setQ(28);

        ThreeWindingsTransformerAdder threeWindingsTransformerAdder = s2.newThreeWindingsTransformer();

        ThreeWindingsTransformerAdder.LegAdder legAdder = threeWindingsTransformerAdder.newLeg1();
        invalidNode = initAdder(legAdder, topologyKind, invalidNode);
        legAdder.setVoltageLevel("VL2")
                .setR(45)
                .setX(35)
                .setG(25)
                .setB(15)
                .setRatedU(5)
                .add();
        legAdder = threeWindingsTransformerAdder.newLeg2();
        invalidNode = initAdder(legAdder, topologyKind, invalidNode);
        legAdder.setVoltageLevel("VL3")
                .setR(47)
                .setX(37)
                .setG(27)
                .setB(17)
                .setRatedU(7)
                .add();
        legAdder = threeWindingsTransformerAdder.newLeg3();
        initAdder(legAdder, topologyKind, invalidNode);
        legAdder.setVoltageLevel("VL4")
                .setR(49)
                .setX(39)
                .setG(29)
                .setB(19)
                .setRatedU(9)
                .add();
        ThreeWindingsTransformer threeWindingsTransformer = threeWindingsTransformerAdder
                .setId("TWT1")
                .setName("Three windings transformer 1")
                .setRatedU0(234)
                .add();
        threeWindingsTransformer.getTerminal(ThreeSides.ONE).setP(375);
        threeWindingsTransformer.getTerminal(ThreeSides.TWO).setP(225);
        threeWindingsTransformer.getTerminal(ThreeSides.THREE).setP(200);
        threeWindingsTransformer.getTerminal(ThreeSides.ONE).setQ(48);
        threeWindingsTransformer.getTerminal(ThreeSides.TWO).setQ(28);
        threeWindingsTransformer.getTerminal(ThreeSides.THREE).setQ(18);

        threeWindingsTransformer.getLeg1().newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .setRegulating(true)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationValue(25)
                .setRegulationTerminal(threeWindingsTransformer.getTerminal(ThreeSides.ONE))
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
                .setRegulationTerminal(threeWindingsTransformer.getTerminal(ThreeSides.ONE))
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

        return network;
    }

    static void addBusBarSection(VoltageLevel vl) {
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS12")
                .setNode(10)
                .add();
        vl.newGenerator()
                .setId("G12")
                .setNode(11)
                .setMaxP(100.0)
                .setMinP(50.0)
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("BRS12")
                .setNode1(0)
                .setNode2(10)
                .setOpen(false)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("BR12")
                .setNode1(10)
                .setNode2(11)
                .setOpen(false)
                .add();
    }

    static Network createEmptyNodeBreakerNetwork() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("SW1")
                .setNode1(0)
                .setNode2(1)
                .setOpen(false)
                .setRetained(true)
                .add();

        return network;
    }

    static Network createBusBreakerNetworkWithOneBus() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .setCountry(Country.FR)
                .setTso("TSO1")
                .setGeographicalTags("region1")
                .add();
        VoltageLevel vl1 = s.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        vl1.newLoad()
                .setId("LD1")
                .setConnectableBus("B1")
                .setBus("B1")
                .setP0(1.0)
                .setQ0(1.0)
                .add();
        vl1.newGenerator()
                .setId("G")
                .setBus("B1")
                .setMaxP(100.0)
                .setMinP(50.0)
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
                .add();

        return network;
    }

    static Network createBusBreakerNetworkWithTwoBuses() {
        Network network = createBusBreakerNetworkWithOneBus();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        network.getVoltageLevel("VL1").getBusBreakerView().newBus()
                .setId("B2")
                .add();
        vl1.getBusBreakerView().newSwitch()
                .setId("BR1")
                .setBus1("B2")
                .setBus2("B1")
                .setOpen(false)
                .add();
        vl1.newLoad()
                .setId("LD2")
                .setConnectableBus("B2")
                .setBus("B2")
                .setP0(1.0)
                .setQ0(1.0)
                .add();

        return network;
    }

    static Network createBusBreakerNetworkWithMultiBuses() {
        Network network = createBusBreakerNetworkWithTwoBuses();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        network.getVoltageLevel("VL1").getBusBreakerView().newBus()
                .setId("B3")
                .add();
        vl1.getBusBreakerView().newSwitch()
                .setId("BR2")
                .setBus1("B3")
                .setBus2("B2")
                .setOpen(false)
                .add();
        vl1.newLoad()
                .setId("LD3")
                .setConnectableBus("B3")
                .setBus("B3")
                .setP0(1.0)
                .setQ0(1.0)
                .add();

        return network;
    }

    static Network createBusBreakerNetworkWithLine() {
        Network network = createBusBreakerNetworkWithMultiBuses();
        Substation s = network.getSubstation("S");

        VoltageLevel vl2 = s.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B21")
                .add();
        vl2.newGenerator()
                .setId("G2")
                .setBus("B21")
                .setMaxP(100.0)
                .setMinP(50.0)
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
                .add();

        network.newLine()
                .setId("L1")
                .setVoltageLevel1("VL1")
                .setBus1("B1")
                .setVoltageLevel2("VL2")
                .setBus2("B21")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();

        return network;
    }

    static Network createNodeBreakerNetworkWithTwoVoltageLevelsAndBusBarSections() {
        Network network = Network.create("test", "test");
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .setTso("TSO1")
                .setGeographicalTags("region11", "region12")
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl1.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setNode(0)
                .add();

        Substation s2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .setTso("TSO2")
                .setGeographicalTags("region11", "region12")
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("VL2")
                .setNominalV(225.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl2.getNodeBreakerView().newBusbarSection()
                .setId("BBS2")
                .setNode(0)
                .add();

        return network;
    }

    static Network createNodeBreakerNetworkWithLine() {
        Network network = Network.create("test", "test");
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .setTso("TSO1")
                .setGeographicalTags("region11", "region12")
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl1.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setNode(0)
                .add();
        vl1.newGenerator()
                .setId("G")
                .setNode(4)
                .setMaxP(100.0)
                .setMinP(50.0)
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
                .add();
        vl1.newLoad()
                .setId("L")
                .setNode(5)
                .setP0(1)
                .setQ0(1)
                .add();
        vl1.getNodeBreakerView().newDisconnector()
                .setId("D1")
                .setNode1(0)
                .setNode2(5)
                .setOpen(false)
                .add();
        vl1.getNodeBreakerView().newInternalConnection()
                .setNode1(1)
                .setNode2(4)
                .add();
        vl1.getNodeBreakerView().newBreaker()
                .setId("BR1")
                .setNode1(0)
                .setNode2(1)
                .setOpen(false)
                .add();
        vl1.getNodeBreakerView().newDisconnector()
                .setId("D2")
                .setNode1(0)
                .setNode2(2)
                .setOpen(false)
                .add();
        vl1.getNodeBreakerView().newBreaker()
                .setId("BR2")
                .setNode1(2)
                .setNode2(3)
                .setOpen(false)
                .add();

        Substation s2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .setTso("TSO2")
                .setGeographicalTags("region2")
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl2.getNodeBreakerView().newBusbarSection()
                .setId("BBS2")
                .setNode(0)
                .add();
        vl2.newLoad()
                .setId("LD")
                .setNode(1)
                .setP0(1)
                .setQ0(1)
                .add();
        vl2.getNodeBreakerView().newBreaker()
                .setId("BR3")
                .setNode1(0)
                .setNode2(1)
                .setOpen(false)
                .add();
        vl2.getNodeBreakerView().newDisconnector()
                .setId("D21")
                .setNode1(0)
                .setNode2(2)
                .setOpen(false)
                .add();
        vl2.getNodeBreakerView().newBreaker()
                .setId("BR4")
                .setNode1(2)
                .setNode2(3)
                .setOpen(false)
                .add();
        network.newLine()
                .setId("L1")
                .setVoltageLevel1("VL1")
                .setNode1(3)
                .setVoltageLevel2("VL2")
                .setNode2(3)
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        return network;
    }

    static Network createNodeBreakerNetwokWithMultipleEquipments() {
        Network network = createNetwokWithMultipleEquipments(TopologyKind.NODE_BREAKER);

        network.getSubstation("S1");
        network.getSubstation("S2");
        network.getSubstation("S3");

        network.getVoltageLevel("VL1");
        network.getVoltageLevel("VL2");
        network.getVoltageLevel("VL3");
        network.getVoltageLevel("VL4");
        network.getVoltageLevel("VL5");
        network.getVoltageLevel("VL6");

        VscConverterStationImpl vsc1 = (VscConverterStationImpl) network.getVscConverterStation("VSC1");
        vsc1.getResource().getAttributes().setNode(1);

        VscConverterStationImpl vsc2 = (VscConverterStationImpl) network.getVscConverterStation("VSC2");
        vsc2.getResource().getAttributes().setNode(2);

        StaticVarCompensatorImpl svc2 = (StaticVarCompensatorImpl) network.getStaticVarCompensator("SVC2");
        svc2.getResource().getAttributes().setNode(0);

        LccConverterStationImpl lcc2 = (LccConverterStationImpl) network.getLccConverterStation("LCC2");
        lcc2.getResource().getAttributes().setNode(1);

        DanglingLineImpl dl1 = (DanglingLineImpl) network.getDanglingLine("DL1");
        dl1.getResource().getAttributes().setNode(2);

        DanglingLineImpl dl2 = (DanglingLineImpl) network.getDanglingLine("DL2");
        dl2.getResource().getAttributes().setNode(3);

        LineImpl l1 = (LineImpl) network.getLine("LINE1");
        l1.getResource().getAttributes().setNode1(5);
        l1.getResource().getAttributes().setNode2(4);

        ThreeWindingsTransformerImpl twt1 = (ThreeWindingsTransformerImpl) network.getThreeWindingsTransformer("TWT1");
        twt1.getResource().getAttributes().getLeg1().setNode(1);
        twt1.getResource().getAttributes().getLeg2().setNode(2);
        twt1.getResource().getAttributes().getLeg3().setNode(3);

        TwoWindingsTransformerImpl twowt1 = (TwoWindingsTransformerImpl) network.getTwoWindingsTransformer("TwoWT1");
        twowt1.getResource().getAttributes().setNode1(1);
        twowt1.getResource().getAttributes().setNode2(2);

        ShuntCompensatorImpl shunt1 = (ShuntCompensatorImpl) network.getShuntCompensator("SHUNT1");
        shunt1.getResource().getAttributes().setNode(0);

        ShuntCompensatorImpl shunt2 = (ShuntCompensatorImpl) network.getShuntCompensator("SHUNT2");
        shunt2.getResource().getAttributes().setNode(3);

        BatteryImpl battery = (BatteryImpl) network.getBattery("battery");
        battery.getResource().getAttributes().setNode(4);

        GroundImpl ground = (GroundImpl) network.getGround("ground");
        ground.getResource().getAttributes().setNode(5);

        return network;
    }

    static Network createBusBreakerNetwokWithMultipleEquipments() {
        Network network = createNetwokWithMultipleEquipments(TopologyKind.BUS_BREAKER);

        VoltageLevelImpl vl1 = (VoltageLevelImpl) network.getVoltageLevel("VL1");
        VoltageLevelImpl vl2 = (VoltageLevelImpl) network.getVoltageLevel("VL2");
        VoltageLevelImpl vl3 = (VoltageLevelImpl) network.getVoltageLevel("VL3");
        VoltageLevelImpl vl4 = (VoltageLevelImpl) network.getVoltageLevel("VL4");
        VoltageLevelImpl vl5 = (VoltageLevelImpl) network.getVoltageLevel("VL5");
        VoltageLevelImpl vl6 = (VoltageLevelImpl) network.getVoltageLevel("VL6");

        vl1.getBusBreakerView().newBus().setId("BUS1").add();
        vl2.getBusBreakerView().newBus().setId("BUS2").add();
        vl3.getBusBreakerView().newBus().setId("BUS3").add();
        vl4.getBusBreakerView().newBus().setId("BUS4").add();
        vl5.getBusBreakerView().newBus().setId("BUS5").add();
        vl6.getBusBreakerView().newBus().setId("BUS6").add();

        VscConverterStationImpl vsc1 = (VscConverterStationImpl) network.getVscConverterStation("VSC1");
        vsc1.getResource().getAttributes().setConnectableBus("BUS1");

        VscConverterStationImpl vsc2 = (VscConverterStationImpl) network.getVscConverterStation("VSC2");
        vsc2.getResource().getAttributes().setConnectableBus("BUS2");

        StaticVarCompensatorImpl svc2 = (StaticVarCompensatorImpl) network.getStaticVarCompensator("SVC2");
        svc2.getResource().getAttributes().setConnectableBus("BUS2");

        LccConverterStationImpl lcc2 = (LccConverterStationImpl) network.getLccConverterStation("LCC2");
        lcc2.getResource().getAttributes().setConnectableBus("BUS2");

        DanglingLineImpl dl1 = (DanglingLineImpl) network.getDanglingLine("DL1");
        dl1.getResource().getAttributes().setConnectableBus("BUS1");

        DanglingLineImpl dl2 = (DanglingLineImpl) network.getDanglingLine("DL2");
        dl2.getResource().getAttributes().setConnectableBus("BUS1");

        LineImpl l1 = (LineImpl) network.getLine("LINE1");
        l1.getResource().getAttributes().setConnectableBus1("BUS1");
        l1.getResource().getAttributes().setConnectableBus2("BUS2");

        ThreeWindingsTransformerImpl twt1 = (ThreeWindingsTransformerImpl) network.getThreeWindingsTransformer("TWT1");
        twt1.getResource().getAttributes().getLeg1().setConnectableBus("BUS2");
        twt1.getResource().getAttributes().getLeg2().setConnectableBus("BUS3");
        twt1.getResource().getAttributes().getLeg3().setConnectableBus("BUS4");

        TwoWindingsTransformerImpl twowt1 = (TwoWindingsTransformerImpl) network.getTwoWindingsTransformer("TwoWT1");
        twowt1.getResource().getAttributes().setConnectableBus1("BUS5");
        twowt1.getResource().getAttributes().setConnectableBus2("BUS6");

        ShuntCompensatorImpl shunt1 = (ShuntCompensatorImpl) network.getShuntCompensator("SHUNT1");
        shunt1.getResource().getAttributes().setConnectableBus("BUS1");

        ShuntCompensatorImpl shunt2 = (ShuntCompensatorImpl) network.getShuntCompensator("SHUNT2");
        shunt2.getResource().getAttributes().setConnectableBus("BUS2");

        BatteryImpl battery = (BatteryImpl) network.getBattery("battery");
        battery.getResource().getAttributes().setConnectableBus("BUS1");

        return network;
    }

    static Network createDummyNodeBreakerWithTieLineNetwork() {
        Network network = Network.create("test", "test");
        Substation s1 = network.newSubstation().setId("S1").add();
        Substation s2 = network.newSubstation().setId("S2").add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL").setNominalV(1f).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        VoltageLevel vl2 = s2.newVoltageLevel().setId("VL2").setNominalV(1f).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        DanglingLine dl1 = vl1.newDanglingLine().setId("DL1").setNode(0).setP0(0.0).setQ0(0.0).setR(1.5).setX(13.0).setG(0.0).setB(1e-6).add();
        DanglingLine dl2 = vl2.newDanglingLine().setId("DL2").setNode(0).setP0(0.0).setQ0(0.0).setR(1.5).setX(13.0).setG(0.0).setB(1e-6).add();
        network.newTieLine().setId("TL").setDanglingLine1(dl1.getId()).setDanglingLine2(dl2.getId()).add();
        return network;
    }
}

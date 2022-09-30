/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.integration;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.iidm.impl.ConfiguredBusImpl;
import com.powsybl.network.store.iidm.impl.ShuntCompensatorLinearModelImpl;
import com.powsybl.network.store.iidm.impl.ShuntCompensatorNonLinearModelImpl;
import com.powsybl.network.store.server.NetworkStoreApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextHierarchy({
        @ContextConfiguration(classes = {NetworkStoreApplication.class, NetworkStoreService.class})
})
public class NetworkStoreValidationTest {

    @LocalServerPort
    private int randomServerPort;

    private NetworkStoreService service;

    private String getBaseUrl() {
        return "http://localhost:" + randomServerPort + "/";
    }

    @Before
    public void setup() {
        service = new NetworkStoreService(getBaseUrl());
    }

    @Test
    public void testVoltageLevel() {
        Network network = service.getNetworkFactory().createNetwork("Validation network", "test");
        Substation s1 = network.newSubstation().setId("S1").setCountry(Country.FR).add();

        assertTrue(assertThrows(PowsyblException.class, () -> s1.newVoltageLevel().add()).getMessage().contains("Voltage level id is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newVoltageLevel().setId("VL1").setNominalV(-10).add()).getMessage().contains("nominal voltage is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newVoltageLevel().setId("VL1").setNominalV(380).setLowVoltageLimit(-10).add())
                .getMessage().contains("low voltage limit is < 0"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newVoltageLevel().setId("VL1").setNominalV(380).setLowVoltageLimit(320).setHighVoltageLimit(-10).add())
                .getMessage().contains("high voltage limit is < 0"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newVoltageLevel().setId("VL1").setNominalV(380).setLowVoltageLimit(320).setHighVoltageLimit(280).add())
                .getMessage().contains("Inconsistent voltage limit range"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newVoltageLevel().setId("VL1").setNominalV(380).setLowVoltageLimit(320).setHighVoltageLimit(420).add())
                .getMessage().contains("topology kind is invalid"));

        VoltageLevel vl = s1.newVoltageLevel().setId("VL1").setNominalV(380).setLowVoltageLimit(320).setHighVoltageLimit(420).setTopologyKind(TopologyKind.NODE_BREAKER).add();

        assertTrue(assertThrows(PowsyblException.class, () -> vl.setNominalV(-100)).getMessage().contains("nominal voltage is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl.setLowVoltageLimit(450)).getMessage().contains("Inconsistent voltage limit range"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl.setHighVoltageLimit(-50)).getMessage().contains("high voltage limit is < 0"));

        assertTrue(assertThrows(PowsyblException.class, () -> vl.getNodeBreakerView().newSwitch().add()).getMessage().contains("Switch id is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl.getNodeBreakerView().newSwitch().setId("b").add())
                .getMessage().contains("first connection node is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl.getNodeBreakerView().newSwitch().setId("b").setNode1(1).add())
                .getMessage().contains("second connection node is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl.getNodeBreakerView().newSwitch().setId("b").setNode1(1).setNode2(2).add())
                .getMessage().contains("kind is not set"));

        vl.getNodeBreakerView().newSwitch().setId("b").setKind(SwitchKind.LOAD_BREAK_SWITCH).setNode1(1).setNode2(2).add();

        VoltageLevel vl2 = s1.newVoltageLevel().setId("VL2").setNominalV(380).setLowVoltageLimit(320).setHighVoltageLimit(420).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        Switch sw = vl2.getBusBreakerView().newSwitch().setId("s").setBus1("b1").setBus2("b2").add();
        assertTrue(assertThrows(PowsyblException.class, () -> sw.setRetained(true)).getMessage().contains("retain status is not modifiable in a non node/breaker voltage level"));

        BusbarSection section = vl.getNodeBreakerView().newBusbarSection().setId("bbs").setNode(0).add();
        assertTrue(assertThrows(PowsyblException.class, () -> section.getTerminal().setQ(100)).getMessage().contains("cannot set reactive power on a busbar section"));

        ConfiguredBusImpl bus = (ConfiguredBusImpl) vl2.getBusBreakerView().newBus().setId("b1").add();
        assertTrue(assertThrows(PowsyblException.class, () -> bus.setV(-100)).getMessage().contains("voltage cannot be < 0"));
    }

    @Test
    public void testGenerator() {
        Network network = service.getNetworkFactory().createNetwork("Validation network", "test");
        Substation s1 = network.newSubstation().setId("S1").setCountry(Country.FR).add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL1").setNominalV(380).setLowVoltageLimit(320).setHighVoltageLimit(420).setTopologyKind(TopologyKind.NODE_BREAKER).add();

        VoltageLevel vl2 = s1.newVoltageLevel().setId("VL2").setNominalV(380).setLowVoltageLimit(320).setHighVoltageLimit(420).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        assertTrue(assertThrows(PowsyblException.class, () -> vl2.newGenerator().setId("G").setNode(0).add())
                .getMessage().contains("node only used in a node breaker topology"));

        Generator gen = vl1.newGenerator().setId("G").setNode(0).setMinP(100).setMaxP(800).setTargetP(700).setVoltageRegulatorOn(true).setTargetV(380).setRatedS(5).add();

        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newGenerator().add()).getMessage().contains("Generator id is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newGenerator().setId("G1").setBus("b1").setConnectableBus("B1").add())
                .getMessage().contains("connection bus is different to connectable bus"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newGenerator().setId("G1").setBus("B1").setConnectableBus("B1").add())
                .getMessage().contains("bus only used in a bus breaker topology"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newGenerator().setId("G1").setNode(1).setConnectableBus("B1").add())
                .getMessage().contains("connection node and connection bus are exclusives"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newGenerator().setId("G1").add()).getMessage().contains("connectable bus is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newGenerator().setId("G1").setNode(0).add())
                .getMessage().contains("G is already connected to the node 0"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newGenerator().setId("G1").setNode(1).setEnergySource(null).add())
                .getMessage().contains("energy source is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newGenerator().setId("G1").setNode(1).add())
                .getMessage().contains("minimum P"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newGenerator().setId("G1").setNode(1).setMinP(100).add())
                .getMessage().contains("maximum P"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newGenerator().setId("G1").setNode(1).setMinP(100).setMaxP(800).add())
                .getMessage().contains("active power setpoint"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newGenerator().setId("G1").setNode(1).setMinP(100).setMaxP(800).setTargetP(700).add())
                .getMessage().contains("voltage regulator status is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newGenerator().setId("G1").setNode(1).setMinP(100).setMaxP(800).setTargetP(700).setVoltageRegulatorOn(true).add())
                .getMessage().contains("voltage setpoint"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newGenerator().setId("G1").setNode(1).setMinP(100).setMaxP(800).setTargetP(700).setVoltageRegulatorOn(false).add())
                .getMessage().contains("reactive power setpoint"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newGenerator().setId("G1").setNode(1).setMinP(100).setMaxP(50).setTargetP(700).setVoltageRegulatorOn(false).setTargetQ(100).add())
                .getMessage().contains("invalid active limits"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newGenerator().setId("G1").setNode(1).setMinP(100).setMaxP(800).setTargetP(700).setVoltageRegulatorOn(false).setTargetQ(100).setRatedS(-5).add())
                .getMessage().contains("Invalid value of rated S"));

        assertTrue(assertThrows(PowsyblException.class, () -> gen.setEnergySource(null)).getMessage().contains("energy source is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> gen.setMinP(1000)).getMessage().contains("invalid active limits"));
        assertTrue(assertThrows(PowsyblException.class, () -> gen.setMaxP(Double.NaN)).getMessage().matches("(.*)invalid value(.*)maximum P(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> gen.setVoltageRegulatorOn(false)).getMessage().matches("(.*)reactive power setpoint(.*)voltage regulator is off(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> gen.setTargetP(Double.NaN)).getMessage().matches("(.*)invalid value(.*)active power setpoint(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> gen.setTargetV(-100)).getMessage().matches("(.*)voltage setpoint(.*)voltage regulator is on(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> gen.setVoltageRegulatorOn(false).setTargetQ(Double.NaN)).getMessage().matches("(.*)reactive power setpoint(.*)voltage regulator is off(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> gen.setRatedS(-1)).getMessage().contains("Invalid value of rated S"));
    }

    @Test
    public void testLoad() {
        Network network = service.getNetworkFactory().createNetwork("Validation network", "test");
        Substation s1 = network.newSubstation().setId("S1").setCountry(Country.FR).add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL1").setNominalV(380).setLowVoltageLimit(320).setHighVoltageLimit(420).setTopologyKind(TopologyKind.NODE_BREAKER).add();

        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newLoad().add()).getMessage().contains("Load id is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newLoad().setId("L1").setBus("b1").setConnectableBus("B1").add())
                .getMessage().contains("connection bus is different to connectable bus"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newLoad().setId("L1").setNode(1).setConnectableBus("B1").add())
                .getMessage().contains("connection node and connection bus are exclusives"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newLoad().setId("L1").setNode(1).setLoadType(null).add())
                .getMessage().contains("load type is null"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newLoad().setId("L1").setNode(1).add())
                .getMessage().contains("p0 is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newLoad().setId("L1").setNode(1).setP0(100).add())
                .getMessage().contains("q0 is invalid"));

        Load load = vl1.newLoad().setId("L1").setNode(1).setP0(100).setQ0(100).add();
        assertTrue(assertThrows(PowsyblException.class, () -> load.setLoadType(null)).getMessage().contains("load type is null"));
        assertTrue(assertThrows(PowsyblException.class, () -> load.setP0(Double.NaN)).getMessage().contains("p0 is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> load.setQ0(Double.NaN)).getMessage().contains("q0 is invalid"));

        load.setLoadType(LoadType.AUXILIARY);
    }

    @Test
    public void testShuntCompensator() {
        Network network = service.getNetworkFactory().createNetwork("Validation network", "test");
        Substation s1 = network.newSubstation().setId("S1").setCountry(Country.FR).add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL1").setNominalV(380).setLowVoltageLimit(320).setHighVoltageLimit(420).setTopologyKind(TopologyKind.NODE_BREAKER).add();

        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newShuntCompensator().add()).getMessage().contains("Shunt compensator id is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newShuntCompensator().setId("SC1").setBus("b1").setConnectableBus("B1").add())
                .getMessage().contains("connection bus is different to connectable bus"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newShuntCompensator().setId("SC1").setNode(1).setConnectableBus("B1").add())
                .getMessage().contains("connection node and connection bus are exclusives"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newShuntCompensator().setId("SC1").add())
                .getMessage().contains("connectable bus is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newShuntCompensator().setId("SC1").setNode(1).add())
                .getMessage().contains("the shunt compensator model has not been defined"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newShuntCompensator().setId("SC1").setNode(1)
                .newLinearModel().setGPerSection(1).setBPerSection(2).setMaximumSectionCount(5).add()
                .setSectionCount(-1)
                .add())
                .getMessage().matches("(.*)the current number of section(.*)should be greater than or equal to 0(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newShuntCompensator().setId("SC1").setNode(1)
                .newLinearModel().setGPerSection(1).setBPerSection(2).setMaximumSectionCount(0).add()
                .setSectionCount(1)
                .add())
                .getMessage().matches("(.*)the maximum number of section(.*)should be greater than 0(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newShuntCompensator().setId("SC1").setNode(1)
                .newLinearModel().setGPerSection(1).setBPerSection(2).setMaximumSectionCount(3).add()
                .setSectionCount(5)
                .add())
                .getMessage().matches("(.*)the current number(.*)of section should be lesser than the maximum number of section(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newShuntCompensator().setId("SC1").setNode(1)
                .newLinearModel().setGPerSection(1).setBPerSection(2).setMaximumSectionCount(5).add()
                .setSectionCount(3)
                .setVoltageRegulatorOn(true)
                .setTargetV(-10)
                .add())
                .getMessage().matches("(.*)invalid value(.*)for voltage setpoint(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newShuntCompensator().setId("SC1").setNode(1)
                .newLinearModel().setGPerSection(1).setBPerSection(2).setMaximumSectionCount(5).add()
                .setSectionCount(3)
                .setVoltageRegulatorOn(true)
                .setTargetV(380)
                .setTargetDeadband(Double.NaN)
                .add())
                .getMessage().matches("(.*)Undefined value for target deadband of regulating shunt compensator(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newShuntCompensator().setId("SC1").setNode(1)
                .newLinearModel().setGPerSection(1).setBPerSection(2).setMaximumSectionCount(5).add()
                .setSectionCount(3)
                .setVoltageRegulatorOn(true)
                .setTargetV(380)
                .setTargetDeadband(-200)
                .add())
                .getMessage().matches("(.*)Unexpected value for target deadband of shunt compensator(.*)< 0(.*)"));

        ShuntCompensator shuntCompensator1 = vl1.newShuntCompensator().setId("SC1").setNode(1)
                .newLinearModel().setGPerSection(1).setBPerSection(2).setMaximumSectionCount(5).add()
                .setSectionCount(3)
                .setVoltageRegulatorOn(true)
                .setTargetV(380)
                .setTargetDeadband(10)
                .add();

        assertTrue(assertThrows(PowsyblException.class, () -> ((ShuntCompensatorLinearModelImpl) shuntCompensator1.getModel()).setMaximumSectionCount(2))
                .getMessage().matches("(.*)the current number(.*)of section should be lesser than the maximum number of section(.*)"));

        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newShuntCompensator().setId("SC1").setNode(1)
                .newNonLinearModel().add()
                .setSectionCount(3)
                .setVoltageRegulatorOn(true)
                .setTargetV(380)
                .setTargetDeadband(10)
                .add())
                .getMessage().matches("(.*)a shunt compensator must have at least one section(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newShuntCompensator().setId("SC1").setNode(1)
                .newNonLinearModel().beginSection().endSection().add()
                .setSectionCount(3)
                .setVoltageRegulatorOn(true)
                .setTargetV(380)
                .setTargetDeadband(10)
                .add())
                .getMessage().matches("(.*)section susceptance is invalid(.*)"));

        ShuntCompensator shuntCompensator2 = vl1.newShuntCompensator().setId("SC2").setNode(2)
                .newNonLinearModel()
                .beginSection().setB(10).setG(20).endSection()
                .beginSection().setB(30).setG(40).endSection()
                .add()
                .setSectionCount(1)
                .setVoltageRegulatorOn(true)
                .setTargetV(380)
                .setTargetDeadband(10)
                .add();

        assertTrue(assertThrows(PowsyblException.class, () -> ((ShuntCompensatorNonLinearModelImpl) shuntCompensator2.getModel()).getAllSections().get(0).setB(Double.NaN))
                .getMessage().contains("b is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> ((ShuntCompensatorNonLinearModelImpl) shuntCompensator2.getModel()).getAllSections().get(1).setG(Double.NaN))
                .getMessage().contains("g is invalid"));

        assertTrue(assertThrows(PowsyblException.class, () -> shuntCompensator2.setSectionCount(-5)).getMessage().matches("(.*)the current number of section(.*)should be greater than or equal to 0(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> shuntCompensator2.setTargetV(-50).setVoltageRegulatorOn(true)).getMessage().matches("(.*)voltage setpoint(.*)voltage regulator is on(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> shuntCompensator2.setTargetDeadband(Double.NaN)).getMessage().matches("(.*)Undefined value for target deadband of regulating(.*)"));
    }

    @Test
    public void testStaticVarCompensator() {
        Network network = service.getNetworkFactory().createNetwork("Validation network", "test");
        Substation s1 = network.newSubstation().setId("S1").setCountry(Country.FR).add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL1").setNominalV(380).setLowVoltageLimit(320).setHighVoltageLimit(420).setTopologyKind(TopologyKind.NODE_BREAKER).add();

        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newStaticVarCompensator().add()).getMessage().contains("Static var compensator id is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newStaticVarCompensator().setId("SVC1").setBus("b1").setConnectableBus("B1").add())
                .getMessage().contains("connection bus is different to connectable bus"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newStaticVarCompensator().setId("SVC1").setNode(1).setConnectableBus("B1").add())
                .getMessage().contains("connection node and connection bus are exclusives"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newStaticVarCompensator().setId("SVC1").add())
                .getMessage().contains("connectable bus is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newStaticVarCompensator().setId("SVC1").setNode(1).add())
                .getMessage().contains("bmin is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newStaticVarCompensator().setId("SVC1").setNode(1).setBmin(1).add())
                .getMessage().contains("bmax is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newStaticVarCompensator().setId("SVC1").setNode(1).setBmin(1).setBmax(10).add())
                .getMessage().contains("Regulation mode is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newStaticVarCompensator().setId("SVC1").setNode(1).setBmin(1).setBmax(10).setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE).add())
                .getMessage().matches("(.*)voltage setpoint(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newStaticVarCompensator().setId("SVC1").setNode(1).setBmin(1).setBmax(10).setRegulationMode(StaticVarCompensator.RegulationMode.REACTIVE_POWER).add())
                .getMessage().matches("(.*)reactive power setpoint(.*)"));

        StaticVarCompensator svc = vl1.newStaticVarCompensator().setId("SVC1").setNode(1).setBmin(1).setBmax(10).setRegulationMode(StaticVarCompensator.RegulationMode.REACTIVE_POWER).setReactivePowerSetPoint(10).add();

        assertTrue(assertThrows(PowsyblException.class, () -> svc.setBmin(Double.NaN)).getMessage().contains("bmin is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> svc.setBmax(Double.NaN)).getMessage().contains("bmax is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> svc.setRegulationMode(StaticVarCompensator.RegulationMode.REACTIVE_POWER).setReactivePowerSetpoint(Double.NaN)).getMessage().matches("(.*)invalid value(.*)reactive power setpoint(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> svc.setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE).setVoltageSetpoint(Double.NaN)).getMessage().matches("(.*)invalid value(.*)voltage setpoint(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> svc.setRegulationMode(null)).getMessage().contains("Regulation mode is invalid"));
    }

    @Test
    public void testDanglingLine() {
        Network network = service.getNetworkFactory().createNetwork("Validation network", "test");
        Substation s1 = network.newSubstation().setId("S1").setCountry(Country.FR).add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL1").setNominalV(380).setLowVoltageLimit(320).setHighVoltageLimit(420).setTopologyKind(TopologyKind.NODE_BREAKER).add();

        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newDanglingLine().add()).getMessage().contains("Dangling line id is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newDanglingLine().setId("DL1").setBus("b1").setConnectableBus("B1").add())
                .getMessage().contains("connection bus is different to connectable bus"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newDanglingLine().setId("DL1").setNode(1).setConnectableBus("B1").add())
                .getMessage().contains("connection node and connection bus are exclusives"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newDanglingLine().setId("DL1").add())
                .getMessage().contains("connectable bus is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newDanglingLine().setId("DL1").setNode(1).add())
                .getMessage().contains("p0 is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newDanglingLine().setId("DL1").setNode(1).setP0(1).add())
                .getMessage().contains("q0 is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newDanglingLine().setId("DL1").setNode(1).setP0(1).setQ0(1).add())
                .getMessage().contains("r is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newDanglingLine().setId("DL1").setNode(1).setP0(1).setQ0(1).setR(1).add())
                .getMessage().contains("x is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newDanglingLine().setId("DL1").setNode(1).setP0(1).setQ0(1).setR(1).setX(1).add())
                .getMessage().contains("g is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newDanglingLine().setId("DL1").setNode(1).setP0(1).setQ0(1).setR(1).setX(1).setG(1).add())
                .getMessage().contains("b is invalid"));

        DanglingLine danglingLine1 = vl1.newDanglingLine().setId("DL1").setNode(1).setP0(1).setQ0(1).setR(1).setX(1).setG(1).setB(1).add();

        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newDanglingLine().setId("DL1").setNode(1).setP0(1).setQ0(1).setR(1).setX(1).setG(1).setB(1)
                .newGeneration().setMinP(200).setMaxP(100).add()
                .add())
                .getMessage().contains("invalid active limits"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newDanglingLine().setId("DL1").setNode(1).setP0(1).setQ0(1).setR(1).setX(1).setG(1).setB(1)
                .newGeneration().setMinP(100).setMaxP(200).add()
                .add())
                .getMessage().contains("active power setpoint"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newDanglingLine().setId("DL1").setNode(1).setP0(1).setQ0(1).setR(1).setX(1).setG(1).setB(1)
                .newGeneration().setMinP(100).setMaxP(200).setTargetP(500).setVoltageRegulationOn(true).add()
                .add())
                .getMessage().contains("voltage setpoint"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newDanglingLine().setId("DL1").setNode(1).setP0(1).setQ0(1).setR(1).setX(1).setG(1).setB(1)
                .newGeneration().setMinP(100).setMaxP(200).setTargetP(500).setVoltageRegulationOn(false).setTargetV(300).add()
                .add())
                .getMessage().contains("reactive power setpoint"));

        assertTrue(assertThrows(PowsyblException.class, () -> danglingLine1.newCurrentLimits().setPermanentLimit(-5).add())
                .getMessage().contains("permanent limit must be defined and be > 0"));
        assertTrue(assertThrows(PowsyblException.class, () -> danglingLine1.newCurrentLimits().setPermanentLimit(10)
                .beginTemporaryLimit().endTemporaryLimit().add())
                .getMessage().contains("temporary limit value is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> danglingLine1.newCurrentLimits().setPermanentLimit(10)
                .beginTemporaryLimit().setValue(-1).endTemporaryLimit().add())
                .getMessage().contains("temporary limit value must be > 0"));
        assertTrue(assertThrows(PowsyblException.class, () -> danglingLine1.newCurrentLimits().setPermanentLimit(10)
                .beginTemporaryLimit().setValue(10).setAcceptableDuration(-1).endTemporaryLimit().add())
                .getMessage().contains("acceptable duration must be >= 0"));
        assertTrue(assertThrows(PowsyblException.class, () -> danglingLine1.newCurrentLimits().setPermanentLimit(10)
                .beginTemporaryLimit().setValue(10).setAcceptableDuration(20).endTemporaryLimit().add())
                .getMessage().contains("name is not set"));

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

        assertTrue(assertThrows(PowsyblException.class, () -> danglingLine1.getCurrentLimits().orElseThrow().setPermanentLimit(-50)).getMessage().contains("permanent limit must be defined and be > 0"));

        DanglingLine danglingLine2 = vl1.newDanglingLine().setId("DL2").setNode(2).setP0(1).setQ0(1).setR(1).setX(1).setG(1).setB(1)
                .newGeneration().setMinP(100).setMaxP(200).setTargetP(500).setVoltageRegulationOn(false).setTargetV(300).setTargetQ(100).add()
                .add();

        assertTrue(assertThrows(PowsyblException.class, () -> danglingLine2.getGeneration().setMinP(300)).getMessage().contains("invalid active limits"));
        assertTrue(assertThrows(PowsyblException.class, () -> danglingLine2.getGeneration().setMaxP(Double.NaN)).getMessage().matches("(.*)invalid value(.*)maximum P(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> danglingLine2.getGeneration().setTargetP(Double.NaN)).getMessage().contains("active power setpoint"));
        assertTrue(assertThrows(PowsyblException.class, () -> danglingLine2.getGeneration().setVoltageRegulationOn(true).setTargetV(-100)).getMessage().matches("(.*)voltage setpoint(.*)voltage regulator is on(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> danglingLine2.getGeneration().setVoltageRegulationOn(false).setTargetQ(Double.NaN)).getMessage().matches("(.*)reactive power setpoint(.*)voltage regulator is off(.*)"));
    }

    @Test
    public void testTwoWindingsTransformer() {
        Network network = service.getNetworkFactory().createNetwork("Validation network", "test");
        Substation s1 = network.newSubstation().setId("S1").setCountry(Country.FR).add();
        s1.newVoltageLevel().setId("VL1").setNominalV(380).setLowVoltageLimit(320).setHighVoltageLimit(420).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        s1.newVoltageLevel().setId("VL2").setNominalV(225).setLowVoltageLimit(180).setHighVoltageLimit(250).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        Substation s2 = network.newSubstation().setId("S2").setCountry(Country.FR).add();
        s2.newVoltageLevel().setId("VL3").setNominalV(225).setLowVoltageLimit(180).setHighVoltageLimit(250).setTopologyKind(TopologyKind.NODE_BREAKER).add();

        assertTrue(assertThrows(PowsyblException.class, () -> s1.newTwoWindingsTransformer().add()).getMessage().contains("2 windings transformer id is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newTwoWindingsTransformer().setId("2WT").add())
                .getMessage().contains("first voltage level is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newTwoWindingsTransformer().setId("2WT").setVoltageLevel1("1").add())
                .getMessage().matches("(.*)first voltage level(.*)not found(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newTwoWindingsTransformer().setId("2WT").setVoltageLevel1("VL1").add())
                .getMessage().contains("second voltage level is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newTwoWindingsTransformer().setId("2WT").setVoltageLevel1("VL1").setVoltageLevel2("2").add())
                .getMessage().matches("(.*)second voltage level(.*)not found(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newTwoWindingsTransformer().setId("2WT").setVoltageLevel1("VL1").setVoltageLevel2("VL3").add())
                .getMessage().matches("(.*)the 2 windings of the transformer shall belong to the substation(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newTwoWindingsTransformer().setId("2WT").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setBus1("b1").setConnectableBus1("B1").add())
                .getMessage().contains("connection bus 1 is different to connectable bus 1"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newTwoWindingsTransformer().setId("2WT").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setConnectableBus1("B1").add())
                .getMessage().contains("connection node 1 and connection bus 1 are exclusives"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newTwoWindingsTransformer().setId("2WT").setVoltageLevel1("VL1").setVoltageLevel2("VL2").add())
                .getMessage().contains("connectable bus 1 is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newTwoWindingsTransformer().setId("2WT").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setBus2("b2").setConnectableBus2("B2").add())
                .getMessage().contains("connection bus 2 is different to connectable bus 2"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newTwoWindingsTransformer().setId("2WT").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setConnectableBus2("B2").add())
                .getMessage().contains("connection node 2 and connection bus 2 are exclusives"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newTwoWindingsTransformer().setId("2WT").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).add())
                .getMessage().contains("connectable bus 2 is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newTwoWindingsTransformer().setId("2WT").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).add())
                .getMessage().contains("r is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newTwoWindingsTransformer().setId("2WT").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setR(1).add())
                .getMessage().contains("x is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newTwoWindingsTransformer().setId("2WT").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setR(1).setX(1).add())
                .getMessage().contains("g is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newTwoWindingsTransformer().setId("2WT").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setR(1).setX(1).setG(1).add())
                .getMessage().contains("b is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newTwoWindingsTransformer().setId("2WT").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setR(1).setX(1).setG(1).setB(1).add())
                .getMessage().contains("rated U1 is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newTwoWindingsTransformer().setId("2WT").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setR(1).setX(1).setG(1).setB(1).setRatedU1(1).add())
                .getMessage().contains("rated U2 is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> s1.newTwoWindingsTransformer().setId("2WT").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setR(1).setX(1).setG(1).setB(1).setRatedU1(1).setRatedU2(1).setRatedS(0).add())
                .getMessage().contains("Invalid value of rated S"));

        TwoWindingsTransformer t2e = s1.newTwoWindingsTransformer().setId("2WT").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setR(1).setX(1).setG(1).setB(1).setRatedU1(1).setRatedU2(1).add();

        assertTrue(assertThrows(PowsyblException.class, () -> t2e.setR(Double.NaN)).getMessage().contains("r is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.setX(Double.NaN)).getMessage().contains("x is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.setG(Double.NaN)).getMessage().contains("g is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.setB(Double.NaN)).getMessage().contains("b is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.setRatedU1(Double.NaN)).getMessage().contains("rated U1 is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.setRatedU2(Double.NaN)).getMessage().contains("rated U2 is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.setRatedS(-1)).getMessage().contains("Invalid value of rated S"));

        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newRatioTapChanger().add()).getMessage().contains("tap position is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newRatioTapChanger().setTapPosition(3).add())
                .getMessage().contains("ratio tap changer should have at least one step"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newRatioTapChanger().setTapPosition(3).beginStep().endStep().add())
                .getMessage().contains("step rho is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newRatioTapChanger().setTapPosition(3).beginStep().setRho(1.0).endStep().add())
                .getMessage().contains("step r is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newRatioTapChanger().setTapPosition(3).beginStep().setRho(1.0).setR(1.0).endStep().add())
                .getMessage().contains("step x is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newRatioTapChanger().setTapPosition(3).beginStep().setRho(1.0).setR(1.0).setX(1.0).endStep().add())
                .getMessage().contains("step g is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newRatioTapChanger().setTapPosition(3).beginStep().setRho(1.0).setR(1.0).setX(1.0).setG(1.0).endStep().add())
                .getMessage().contains("step b is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newRatioTapChanger()
                .setTapPosition(3)
                .beginStep().setR(10).setX(10).setG(10).setB(10).setRho(10).endStep().add())
                .getMessage().contains("incorrect tap position"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newRatioTapChanger()
                .setTapPosition(1)
                .beginStep().setR(10).setX(10).setG(10).setB(10).setRho(10).endStep()
                .beginStep().setR(20).setX(20).setG(20).setB(20).setRho(20).endStep()
                .beginStep().setR(30).setX(30).setG(30).setB(30).setRho(30).endStep()
                .setRegulating(true)
            .setLoadTapChangingCapabilities(true)
            .add())
                .getMessage().contains("a target voltage has to be set for a regulating ratio tap changer"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newRatioTapChanger()
                .setTapPosition(1)
                .beginStep().setR(10).setX(10).setG(10).setB(10).setRho(10).endStep()
                .beginStep().setR(20).setX(20).setG(20).setB(20).setRho(20).endStep()
                .beginStep().setR(30).setX(30).setG(30).setB(30).setRho(30).endStep()
                .setRegulating(true)
            .setLoadTapChangingCapabilities(true)
            .setRegulationTerminal(t2e.getTerminal1())
                .setTargetV(-10)
                .add())
                .getMessage().contains("bad target voltage"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newRatioTapChanger()
                .setTapPosition(1)
                .beginStep().setR(10).setX(10).setG(10).setB(10).setRho(10).endStep()
                .beginStep().setR(20).setX(20).setG(20).setB(20).setRho(20).endStep()
                .beginStep().setR(30).setX(30).setG(30).setB(30).setRho(30).endStep()
                .setRegulating(true)
                .setRegulationTerminal(t2e.getTerminal1())
                .setTargetV(100)
                .add())
                .getMessage().contains("Undefined value for target deadband of regulating"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newRatioTapChanger()
                .setTapPosition(1)
                .beginStep().setR(10).setX(10).setG(10).setB(10).setRho(10).endStep()
                .beginStep().setR(20).setX(20).setG(20).setB(20).setRho(20).endStep()
                .beginStep().setR(30).setX(30).setG(30).setB(30).setRho(30).endStep()
                .setRegulating(true)
                .setRegulationTerminal(t2e.getTerminal1())
                .setTargetV(100)
                .setTargetDeadband(-10)
                .add())
                .getMessage().contains("Unexpected value for target deadband of"));

        RatioTapChanger ratioTapChanger = t2e.newRatioTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(1)
                .setLoadTapChangingCapabilities(false)
                .setRegulating(true)
            .setLoadTapChangingCapabilities(true)
                .setTargetDeadband(1.0)
                .setTargetV(220.0)
                .setRegulationTerminal(t2e.getTerminal1())
                .beginStep()
                .setR(39.78473)
                .setX(39.784725)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .endStep()
                .beginStep()
                .setR(39.78474)
                .setX(39.784726)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .endStep()
                .beginStep()
                .setR(39.78475)
                .setX(39.784727)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .endStep()
                .add();

        assertTrue(assertThrows(PowsyblException.class, () -> ratioTapChanger.setTargetV(Double.NaN)).getMessage().contains("a target voltage has to be set for a regulating ratio tap changer"));
        assertTrue(assertThrows(PowsyblException.class, () -> ratioTapChanger.setTargetV(-50).setRegulating(true).setLoadTapChangingCapabilities(true)).getMessage().contains("bad target voltage "));
        assertTrue(assertThrows(PowsyblException.class, () -> ratioTapChanger.setTargetDeadband(Double.NaN)).getMessage().contains("Undefined value for target deadband of regulating"));
        assertTrue(assertThrows(PowsyblException.class, () -> ratioTapChanger.setTapPosition(-1)).getMessage().contains("incorrect tap position "));

        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newPhaseTapChanger().add()).getMessage().contains("tap position is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newPhaseTapChanger().setTapPosition(3).add())
                .getMessage().contains("a phase tap changer shall have at least one step"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newPhaseTapChanger().setTapPosition(3).beginStep().endStep().add())
                .getMessage().contains("step alpha is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newPhaseTapChanger().setTapPosition(3).beginStep().setAlpha(1.0).endStep().add())
                .getMessage().contains("step rho is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newPhaseTapChanger().setTapPosition(3).beginStep().setAlpha(1.0).setRho(1.0).endStep().add())
                .getMessage().contains("step r is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newPhaseTapChanger().setTapPosition(3).beginStep().setAlpha(1.0).setRho(1.0).setR(1.0).endStep().add())
                .getMessage().contains("step x is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newPhaseTapChanger().setTapPosition(3).beginStep().setAlpha(1.0).setRho(1.0).setR(1.0).setX(1.0).endStep().add())
                .getMessage().contains("step g is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newPhaseTapChanger().setTapPosition(3).beginStep().setAlpha(1.0).setRho(1.0).setR(1.0).setX(1.0).setG(1.0).endStep().add())
                .getMessage().contains("step b is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newPhaseTapChanger()
                .setTapPosition(3)
                .beginStep().setAlpha(1.0).setR(10).setX(10).setG(10).setB(10).setRho(10).endStep().add())
                .getMessage().contains("incorrect tap position"));

        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newPhaseTapChanger()
                .setTapPosition(1)
                .beginStep().setAlpha(1.0).setR(10).setX(10).setG(10).setB(10).setRho(10).endStep()
                .beginStep().setAlpha(1.0).setR(20).setX(20).setG(20).setB(20).setRho(20).endStep()
                .beginStep().setAlpha(1.0).setR(30).setX(30).setG(30).setB(30).setRho(30).endStep()
                .setRegulating(true)
                .setRegulationMode(null)
                .add())
                .getMessage().contains("phase regulation mode is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newPhaseTapChanger()
                .setTapPosition(1)
                .beginStep().setAlpha(1.0).setR(10).setX(10).setG(10).setB(10).setRho(10).endStep()
                .beginStep().setAlpha(1.0).setR(20).setX(20).setG(20).setB(20).setRho(20).endStep()
                .beginStep().setAlpha(1.0).setR(30).setX(30).setG(30).setB(30).setRho(30).endStep()
                .setRegulating(true)
                .setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .add())
                .getMessage().contains("phase regulation is on and threshold/setpoint value is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newPhaseTapChanger()
                .setTapPosition(1)
                .beginStep().setAlpha(1.0).setR(10).setX(10).setG(10).setB(10).setRho(10).endStep()
                .beginStep().setAlpha(1.0).setR(20).setX(20).setG(20).setB(20).setRho(20).endStep()
                .beginStep().setAlpha(1.0).setR(30).setX(30).setG(30).setB(30).setRho(30).endStep()
                .setRegulating(true)
                .setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP)
                .add())
                .getMessage().contains("phase regulation cannot be on if mode is FIXED"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newPhaseTapChanger()
                .setTapPosition(1)
                .beginStep().setAlpha(1.0).setR(10).setX(10).setG(10).setB(10).setRho(10).endStep()
                .beginStep().setAlpha(1.0).setR(20).setX(20).setG(20).setB(20).setRho(20).endStep()
                .beginStep().setAlpha(1.0).setR(30).setX(30).setG(30).setB(30).setRho(30).endStep()
                .setRegulating(true)
                .setRegulationTerminal(t2e.getTerminal1())
                .setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .setRegulationValue(10)
                .add())
                .getMessage().contains("Undefined value for target deadband of regulating"));
        assertTrue(assertThrows(PowsyblException.class, () -> t2e.newPhaseTapChanger()
                .setTapPosition(1)
                .beginStep().setAlpha(1.0).setR(10).setX(10).setG(10).setB(10).setRho(10).endStep()
                .beginStep().setAlpha(1.0).setR(20).setX(20).setG(20).setB(20).setRho(20).endStep()
                .beginStep().setAlpha(1.0).setR(30).setX(30).setG(30).setB(30).setRho(30).endStep()
                .setRegulating(true)
                .setRegulationTerminal(t2e.getTerminal1())
                .setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .setRegulationValue(10)
                .setTargetDeadband(-10)
                .add())
                .getMessage().contains("Unexpected value for target deadband of"));

        ratioTapChanger.setRegulating(false);
        PhaseTapChanger phaseTapChanger = t2e.newPhaseTapChanger()
                .setTapPosition(1)
                .setLowTapPosition(0)
                .setRegulating(true)
                .setTargetDeadband(1.0)
                .setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .setRegulationValue(10.0)
                .setRegulationTerminal(t2e.getTerminal1())
                .beginStep()
                .setR(1.0)
                .setX(2.0)
                .setG(3.0)
                .setB(4.0)
                .setAlpha(5.0)
                .setRho(6.0)
                .endStep()
                .beginStep()
                .setR(1.0)
                .setX(2.0)
                .setG(3.0)
                .setB(4.0)
                .setAlpha(5.0)
                .setRho(6.0)
                .endStep()
                .add();

        assertTrue(assertThrows(PowsyblException.class, () -> phaseTapChanger.setRegulationMode(null)).getMessage().contains("phase regulation mode is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> phaseTapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL).setRegulationValue(Double.NaN)).getMessage().contains("phase regulation is on and threshold/setpoint value is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> phaseTapChanger.setTargetDeadband(-10)).getMessage().contains("Unexpected value for target deadband"));
        assertTrue(assertThrows(PowsyblException.class, () -> phaseTapChanger.setTapPosition(-1)).getMessage().matches("(.*)incorrect tap position(.*)"));

        t2e.setR(1);
        t2e.setX(1);
        t2e.setG(1);
        t2e.setB(1);
        t2e.setRatedU1(1);
        t2e.setRatedU2(1);
        t2e.setRatedS(1);
    }

    @Test
    public void testTieLine() {
        Network network = service.getNetworkFactory().createNetwork("Validation network", "test");
        Substation s1 = network.newSubstation().setId("S1").setCountry(Country.FR).add();
        s1.newVoltageLevel().setId("VL1").setNominalV(380).setLowVoltageLimit(320).setHighVoltageLimit(420).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        s1.newVoltageLevel().setId("VL2").setNominalV(225).setLowVoltageLimit(180).setHighVoltageLimit(250).setTopologyKind(TopologyKind.NODE_BREAKER).add();

        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().add()).getMessage().contains("AC Line id is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().setId("TL").add())
                .getMessage().contains("first voltage level is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().setId("TL").setVoltageLevel1("1").add())
                .getMessage().matches("(.*)first voltage level(.*)not found(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().setId("TL").setVoltageLevel1("VL1").add())
                .getMessage().contains("second voltage level is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().setId("TL").setVoltageLevel1("VL1").setVoltageLevel2("2").add())
                .getMessage().matches("(.*)second voltage level(.*)not found(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().setId("TL").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setBus1("b1").setConnectableBus1("B1").add())
                .getMessage().contains("connection bus 1 is different to connectable bus 1"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().setId("TL").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setConnectableBus1("B1").add())
                .getMessage().contains("connection node 1 and connection bus 1 are exclusives"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().setId("TL").setVoltageLevel1("VL1").setVoltageLevel2("VL2").add())
                .getMessage().contains("connectable bus 1 is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().setId("TL").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setBus2("b2").setConnectableBus2("B2").add())
                .getMessage().contains("connection bus 2 is different to connectable bus 2"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().setId("TL").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setConnectableBus2("B2").add())
                .getMessage().contains("connection node 2 and connection bus 2 are exclusives"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().setId("TL").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).add())
                .getMessage().contains("connectable bus 2 is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().setId("TL").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).add())
                .getMessage().contains("ucteXnodeCode is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().setId("TL").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setUcteXnodeCode("1").add())
                .getMessage().contains("half line 1 is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().setId("TL").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setUcteXnodeCode("1").newHalfLine1().add().add())
                .getMessage().contains("half line 2 is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().setId("TL").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setUcteXnodeCode("1").newHalfLine2().add().newHalfLine1().add().add())
                .getMessage().contains("id is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().setId("TL").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setUcteXnodeCode("1").newHalfLine2().add().newHalfLine1().setId("h1").add().add())
                .getMessage().contains("r is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().setId("TL").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setUcteXnodeCode("1").newHalfLine2().add().newHalfLine1().setId("h1").setR(1).add().add())
                .getMessage().contains("x is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().setId("TL").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setUcteXnodeCode("1").newHalfLine2().add().newHalfLine1().setId("h1").setR(1).setX(1).add().add())
                .getMessage().contains("g1 is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().setId("TL").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setUcteXnodeCode("1").newHalfLine2().add().newHalfLine1().setId("h1").setR(1).setX(1).setG1(1).add().add())
                .getMessage().contains("b1 is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().setId("TL").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setUcteXnodeCode("1").newHalfLine2().add().newHalfLine1().setId("h1").setR(1).setX(1).setG1(1).setB1(1).add().add())
                .getMessage().contains("g2 is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newTieLine().setId("TL").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setUcteXnodeCode("1").newHalfLine2().add().newHalfLine1().setId("h1").setR(1).setX(1).setG1(1).setB1(1).setG2(1).add().add())
                .getMessage().contains("b2 is not set"));

        network.newTieLine()
                .setId("TL")
                .setVoltageLevel1("VL1")
                .setVoltageLevel2("VL2")
                .setNode1(1)
                .setNode2(1)
                .setUcteXnodeCode("1")
                .newHalfLine1()
                .setId("h1")
                .setR(1)
                .setX(1)
                .setG1(1)
                .setB1(1)
                .setG2(1)
                .setB2(1)
                .add()
                .newHalfLine2()
                .setId("h2")
                .setR(1)
                .setX(1)
                .setG1(1)
                .setB1(1)
                .setG2(1)
                .setB2(1)
                .add()
                .add();
    }

    @Test
    public void testHvdcLine() {
        Network network = service.getNetworkFactory().createNetwork("Validation network", "test");
        Substation s1 = network.newSubstation().setId("S1").setCountry(Country.FR).add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL1").setNominalV(380).setLowVoltageLimit(320).setHighVoltageLimit(420).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        VoltageLevel vl2 = s1.newVoltageLevel().setId("VL2").setNominalV(225).setLowVoltageLimit(180).setHighVoltageLimit(250).setTopologyKind(TopologyKind.NODE_BREAKER).add();

        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newVscConverterStation().add()).getMessage().contains("VSC converter station id is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newVscConverterStation().setId("VSC1").setBus("b1").setConnectableBus("B1").add())
                .getMessage().contains("connection bus is different to connectable bus"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newVscConverterStation().setId("VSC1").setNode(1).setConnectableBus("B1").add())
                .getMessage().contains("connection node and connection bus are exclusives"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newVscConverterStation().setId("VSC1").add())
                .getMessage().contains("connectable bus is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newVscConverterStation().setId("VSC1").setNode(1).add())
                .getMessage().contains("loss factor is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newVscConverterStation().setId("VSC1").setNode(1).setLossFactor(200).add())
                .getMessage().contains("loss factor must be >= 0 and <= 100"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newVscConverterStation().setId("VSC1").setNode(1).setLossFactor(20).add())
                .getMessage().contains("voltage regulator status is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newVscConverterStation().setId("VSC1").setNode(1).setLossFactor(20).setVoltageRegulatorOn(true).add())
                .getMessage().contains("voltage setpoint"));
        assertTrue(assertThrows(PowsyblException.class, () -> vl1.newVscConverterStation().setId("VSC1").setNode(1).setLossFactor(20).setVoltageRegulatorOn(false).add())
                .getMessage().contains("reactive power setpoint"));

        VscConverterStation vscConverterStation1 = vl1.newVscConverterStation().setId("VSC1").setNode(1).setLossFactor(24).setReactivePowerSetpoint(300).setVoltageRegulatorOn(true).setVoltageSetpoint(290).add();
        VscConverterStation vscConverterStation2 = vl2.newVscConverterStation().setId("VSC2").setNode(2).setLossFactor(17).setReactivePowerSetpoint(227).setVoltageRegulatorOn(false).setVoltageSetpoint(213).add();

        assertTrue(assertThrows(PowsyblException.class, () -> vscConverterStation1.setVoltageRegulatorOn(true).setVoltageSetpoint(-50)).getMessage().matches("(.*)voltage setpoint(.*)voltage regulator is on(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> vscConverterStation1.setVoltageRegulatorOn(false).setReactivePowerSetpoint(Double.NaN)).getMessage().matches("(.*)reactive power setpoint(.*)voltage regulator is off(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> vscConverterStation1.setLossFactor(150)).getMessage().contains("loss factor must be >= 0 and <= 100"));

        assertTrue(assertThrows(PowsyblException.class, () -> vscConverterStation1.newReactiveCapabilityCurve().add())
                .getMessage().contains("a reactive capability curve should have at least two points"));
        assertTrue(assertThrows(PowsyblException.class, () -> vscConverterStation1.newReactiveCapabilityCurve().beginPoint().endPoint().add())
                .getMessage().contains("P is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vscConverterStation1.newReactiveCapabilityCurve().beginPoint().setP(1).endPoint().add())
                .getMessage().contains("min Q is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vscConverterStation1.newReactiveCapabilityCurve().beginPoint().setP(1).setMinQ(2).endPoint().add())
                .getMessage().contains("max Q is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vscConverterStation1.newReactiveCapabilityCurve().beginPoint().setP(1).setMinQ(2).setMaxQ(5).endPoint().beginPoint().setP(1).setMinQ(5).setMaxQ(5).endPoint().add())
                .getMessage().matches("(.*)a point already exists for active power(.*)with a different reactive power range(.*)"));

        vscConverterStation1.newReactiveCapabilityCurve().beginPoint().setP(5).setMinQ(1).setMaxQ(10).endPoint().beginPoint().setP(10).setMinQ(-10).setMaxQ(1).endPoint().add();

        assertTrue(assertThrows(PowsyblException.class, () -> vscConverterStation2.newMinMaxReactiveLimits().add())
                .getMessage().contains("minimum reactive power is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vscConverterStation2.newMinMaxReactiveLimits().setMinQ(10).add())
                .getMessage().contains("maximum reactive power is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> vscConverterStation2.newMinMaxReactiveLimits().setMinQ(10).setMaxQ(5).add())
                .getMessage().contains("maximum reactive power is expected to be greater than or equal to minimum reactive power"));

        vscConverterStation2.newMinMaxReactiveLimits().setMaxQ(127).setMinQ(103).add();

        assertTrue(assertThrows(PowsyblException.class, () -> network.newHvdcLine().add()).getMessage().contains("HVDC line id is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newHvdcLine().setId("HVDC1").add()).getMessage().contains("r is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newHvdcLine().setId("HVDC1").setR(1).add())
                .getMessage().contains("converter mode is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newHvdcLine().setId("HVDC1").setR(1).setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER).add())
                .getMessage().contains("nominal voltage is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newHvdcLine().setId("HVDC1").setR(1).setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER).setNominalV(-10).add())
                .getMessage().contains("nominal voltage is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newHvdcLine().setId("HVDC1").setR(1).setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER).setNominalV(100).add())
                .getMessage().matches("(.*)active power setpoint(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newHvdcLine().setId("HVDC1").setR(1).setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER).setNominalV(100).setActivePowerSetpoint(-10).add())
                .getMessage().matches("(.*)active power setpoint should not be negative(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newHvdcLine().setId("HVDC1").setR(1).setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER).setNominalV(100).setActivePowerSetpoint(100).add())
                .getMessage().contains("maximum P"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newHvdcLine().setId("HVDC1").setR(1).setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER).setNominalV(100).setActivePowerSetpoint(100).setMaxP(-100).add())
                .getMessage().contains("maximum P"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newHvdcLine().setId("HVDC1").setR(1).setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER).setNominalV(100).setActivePowerSetpoint(100).setMaxP(100).setConverterStationId1("1").add())
                .getMessage().matches("(.*)Side 1 converter station(.*)not found(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newHvdcLine().setId("HVDC1").setR(1).setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER).setNominalV(100).setActivePowerSetpoint(100).setMaxP(100).setConverterStationId1("VSC1").setConverterStationId2("2").add())
                .getMessage().matches("(.*)Side 2 converter station(.*)not found(.*)"));

        HvdcLine line = network.newHvdcLine().setId("HVDC1").setR(256).setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER).setActivePowerSetpoint(330).setNominalV(335).setMaxP(390).setConverterStationId1("VSC1").setConverterStationId2("VSC2").add();

        assertTrue(assertThrows(PowsyblException.class, () -> line.setConvertersMode(null)).getMessage().contains("converter mode is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> line.setR(Double.NaN)).getMessage().contains("r is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> line.setNominalV(-50)).getMessage().contains("nominal voltage is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> line.setActivePowerSetpoint(-10)).getMessage().contains("active power setpoint should not be negative"));
        assertTrue(assertThrows(PowsyblException.class, () -> line.setMaxP(Double.NaN)).getMessage().matches("(.*)invalid value(.*)maximum P(.*)"));

        VoltageLevel vl3 = s1.newVoltageLevel()
                .setId("VL3")
                .setNominalV(380)
                .setLowVoltageLimit(320)
                .setHighVoltageLimit(420)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl3.getBusBreakerView().newBus()
                .setId("B3")
                .add();
        LccConverterStation lccConverterStation = vl3.newLccConverterStation()
                .setId("LCC1")
                .setName("Converter1")
                .setConnectableBus("B3")
                .setBus("B3")
                .setLossFactor(1.1f)
                .setPowerFactor(0.5f)
                .add();

        assertTrue(assertThrows(PowsyblException.class, () -> lccConverterStation.setPowerFactor(1.5F)).getMessage().contains("power factor is invalid, it should be between -1 and 1"));
        assertTrue(assertThrows(PowsyblException.class, () -> lccConverterStation.setLossFactor(150)).getMessage().contains("loss factor must be >= 0 and <= 100"));
    }

    @Test
    public void testLine() {
        Network network = service.getNetworkFactory().createNetwork("Validation network", "test");
        Substation s1 = network.newSubstation().setId("S1").setCountry(Country.FR).add();
        s1.newVoltageLevel().setId("VL1").setNominalV(380).setLowVoltageLimit(320).setHighVoltageLimit(420).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        s1.newVoltageLevel().setId("VL2").setNominalV(225).setLowVoltageLimit(180).setHighVoltageLimit(250).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        Substation s2 = network.newSubstation().setId("S2").setCountry(Country.FR).add();
        s2.newVoltageLevel().setId("VL3").setNominalV(225).setLowVoltageLimit(180).setHighVoltageLimit(250).setTopologyKind(TopologyKind.NODE_BREAKER).add();

        assertTrue(assertThrows(PowsyblException.class, () -> network.newLine().add()).getMessage().contains("AC Line id is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newLine().setId("Line").add())
                .getMessage().contains("first voltage level is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newLine().setId("Line").setVoltageLevel1("1").add())
                .getMessage().matches("(.*)first voltage level(.*)not found(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newLine().setId("Line").setVoltageLevel1("VL1").add())
                .getMessage().contains("second voltage level is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newLine().setId("Line").setVoltageLevel1("VL1").setVoltageLevel2("2").add())
                .getMessage().matches("(.*)second voltage level(.*)not found(.*)"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newLine().setId("Line").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setBus1("b1").setConnectableBus1("B1").add())
                .getMessage().contains("connection bus 1 is different to connectable bus 1"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newLine().setId("Line").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setConnectableBus1("B1").add())
                .getMessage().contains("connection node 1 and connection bus 1 are exclusives"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newLine().setId("Line").setVoltageLevel1("VL1").setVoltageLevel2("VL2").add())
                .getMessage().contains("connectable bus 1 is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newLine().setId("Line").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setBus2("b2").setConnectableBus2("B2").add())
                .getMessage().contains("connection bus 2 is different to connectable bus 2"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newLine().setId("Line").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setConnectableBus2("B2").add())
                .getMessage().contains("connection node 2 and connection bus 2 are exclusives"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newLine().setId("Line").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).add())
                .getMessage().contains("connectable bus 2 is not set"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newLine().setId("Line").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).add())
                .getMessage().contains("r is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newLine().setId("Line").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setR(1).add())
                .getMessage().contains("x is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newLine().setId("Line").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setR(1).setX(1).add())
                .getMessage().contains("g1 is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newLine().setId("Line").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setR(1).setX(1).setG1(1).add())
                .getMessage().contains("g2 is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newLine().setId("Line").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setR(1).setX(1).setG1(1).setG2(1).add())
                .getMessage().contains("b1 is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> network.newLine().setId("Line").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setR(1).setX(1).setG1(1).setG2(1).setB1(1).add())
                .getMessage().contains("b2 is invalid"));

        Line line = network.newLine().setId("Line").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(1).setNode2(1).setR(1).setX(1).setG1(1).setG2(1).setB1(1).setB2(1).add();

        assertTrue(assertThrows(PowsyblException.class, () -> line.setR(Double.NaN)).getMessage().contains("r is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> line.setX(Double.NaN)).getMessage().contains("x is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> line.setG1(Double.NaN)).getMessage().contains("g1 is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> line.setG2(Double.NaN)).getMessage().contains("g2 is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> line.setB1(Double.NaN)).getMessage().contains("b1 is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> line.setB2(Double.NaN)).getMessage().contains("b2 is invalid"));
    }

    @Test
    public void testThreeWindingsTransformer() {
        Network network = service.getNetworkFactory().createNetwork("Validation network", "test");
        Substation s1 = network.newSubstation().setId("S1").setCountry(Country.FR).add();
        s1.newVoltageLevel().setId("VL1").setNominalV(380).setLowVoltageLimit(320).setHighVoltageLimit(420).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        s1.newVoltageLevel().setId("VL2").setNominalV(225).setLowVoltageLimit(180).setHighVoltageLimit(250).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        s1.newVoltageLevel().setId("VL3").setNominalV(225).setLowVoltageLimit(180).setHighVoltageLimit(250).setTopologyKind(TopologyKind.NODE_BREAKER).add();

        ThreeWindingsTransformer twt3 = s1.newThreeWindingsTransformer().setId("TWT3")
                .setName("Three windings transformer 1").setRatedU0(234).newLeg1().setVoltageLevel("VL1").setNode(1)
                .setR(45).setX(35).setG(25).setB(15).setRatedU(5).add().newLeg2().setVoltageLevel("VL2").setNode(1)
                .setR(47).setX(37).setG(27).setB(17).setRatedU(7).add().newLeg3().setVoltageLevel("VL3").setNode(1)
                .setR(49).setX(39).setG(29).setB(19).setRatedU(9).add().add();

        assertTrue(assertThrows(PowsyblException.class, () -> twt3.getLeg1().setR(Double.NaN)).getMessage().contains("r is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> twt3.getLeg2().setX(Double.NaN)).getMessage().contains("x is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> twt3.getLeg3().setG(Double.NaN)).getMessage().contains("g is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> twt3.getLeg1().setB(Double.NaN)).getMessage().contains("b is invalid"));
        assertTrue(assertThrows(PowsyblException.class, () -> twt3.getLeg1().setRatedU(Double.NaN)).getMessage().contains("rated U is invalid"));

        twt3.getLeg1().setR(1);
        twt3.getLeg1().setX(1);
        twt3.getLeg1().setG(1);
        twt3.getLeg1().setB(1);
        twt3.getLeg1().setRatedU(1);
    }
}

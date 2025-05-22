/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
class RegulatingTest {
    @Test
    void testRegulatingTerminalGeneratorDuplicatedId() {
        Network network = Network.create("test", "test");
        Substation s1 = network.newSubstation().setId("S1").setCountry(Country.FR).add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL1").setNominalV(400f).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl1.newGenerator().setId("G1").setEnsureIdUnicity(true).setNode(0).setMinP(0).setMaxP(1).setTargetP(1).setTargetQ(0).setVoltageRegulatorOn(false).add();
        vl1.newGenerator().setId("G1").setEnsureIdUnicity(true).setNode(1).setMinP(0).setMaxP(1).setTargetP(1).setTargetQ(0).setVoltageRegulatorOn(false).add();

        Assertions.assertEquals("G1", network.getGenerator("G1").getRegulatingTerminal().getConnectable().getId());
        Assertions.assertEquals("G1#0", network.getGenerator("G1#0").getRegulatingTerminal().getConnectable().getId());
    }

    @Test
    void testRegulatingTerminalSCDuplicatedId() {
        Network network = Network.create("test", "test");
        Substation s1 = network.newSubstation().setId("S1").setCountry(Country.FR).add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL1").setNominalV(400f).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl1.newShuntCompensator().setId("SC1").setEnsureIdUnicity(true).setNode(0)
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
        vl1.newShuntCompensator().setId("SC1").setEnsureIdUnicity(true).setNode(1)
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

        Assertions.assertEquals("SC1", network.getShuntCompensator("SC1").getRegulatingTerminal().getConnectable().getId());
        Assertions.assertEquals("SC1#0", network.getShuntCompensator("SC1#0").getRegulatingTerminal().getConnectable().getId());
    }

    @Test
    void testRegulatingTerminalSVCDuplicatedId() {
        Network network = Network.create("test", "test");
        Substation s1 = network.newSubstation().setId("S1").setCountry(Country.FR).add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL1").setNominalV(400f).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl1.newStaticVarCompensator().setId("SVC1").setEnsureIdUnicity(true).setNode(0)
                .setBmin(0.0002)
                .setBmax(0.0008)
                .setReactivePowerSetpoint(200)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetpoint(390)
                .add();
        vl1.newStaticVarCompensator().setId("SVC1").setEnsureIdUnicity(true).setNode(1)
                .setBmin(0.0002)
                .setBmax(0.0008)
                .setReactivePowerSetpoint(200)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetpoint(390)
                .add();

        Assertions.assertEquals("SVC1", network.getStaticVarCompensator("SVC1").getRegulatingTerminal().getConnectable().getId());
        Assertions.assertEquals("SVC1#0", network.getStaticVarCompensator("SVC1#0").getRegulatingTerminal().getConnectable().getId());
    }

    @Test
    void testRegulatingTerminalVSCDuplicatedId() {
        Network network = Network.create("test", "test");
        Substation s1 = network.newSubstation().setId("S1").setCountry(Country.FR).add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL1").setNominalV(400f).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl1.newVscConverterStation().setId("VSC1").setEnsureIdUnicity(true).setNode(0)
                .setLossFactor(24)
                .setReactivePowerSetpoint(300)
                .setVoltageRegulatorOn(true)
                .setVoltageSetpoint(290)
                .add();
        vl1.newVscConverterStation().setId("VSC1").setEnsureIdUnicity(true).setNode(1)
                .setLossFactor(24)
                .setReactivePowerSetpoint(300)
                .setVoltageRegulatorOn(true)
                .setVoltageSetpoint(290)
                .add();

        Assertions.assertEquals("VSC1", network.getVscConverterStation("VSC1").getRegulatingTerminal().getConnectable().getId());
        Assertions.assertEquals("VSC1#0", network.getVscConverterStation("VSC1#0").getRegulatingTerminal().getConnectable().getId());
    }
}

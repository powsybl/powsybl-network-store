/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck.extensions;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.StandbyAutomatonAdder;
import com.powsybl.iidm.network.tck.extensions.AbstractStandbyAutomatonTest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StandbyAutomatonTest extends AbstractStandbyAutomatonTest {
    @Override
    public void variantsCloneTest() {
        // FIXME
    }

    @Test
    public void testStandbyAutomatonCheckVoltageConfig() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl.getBusBreakerView().newBus()
                .setId("BUS")
                .add();
        StaticVarCompensator svc = vl.newStaticVarCompensator()
                .setId("SVC")
                .setConnectableBus("BUS")
                .setBmin(12.2)
                .setBmax(32.2)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetpoint(23.8)
                .add();

        StandbyAutomatonAdder standbyAutomatonAdder = svc.newExtension(StandbyAutomatonAdder.class)
                .withHighVoltageSetpoint(21.3)
                .withLowVoltageSetpoint(1.7)
                .withHighVoltageThreshold(0.0)
                .withLowVoltageThreshold(0.0);
        assertEquals("Static var compensator 'SVC': Inconsistent low (0.0) and high (0.0) voltage thresholds", assertThrows(ValidationException.class, standbyAutomatonAdder::add).getMessage());

        standbyAutomatonAdder = svc.newExtension(StandbyAutomatonAdder.class).withLowVoltageSetpoint(Double.NaN);
        assertEquals("Static var compensator 'SVC': lowVoltageSetpoint is invalid", assertThrows(ValidationException.class, standbyAutomatonAdder::add).getMessage());

        standbyAutomatonAdder = svc.newExtension(StandbyAutomatonAdder.class).withHighVoltageSetpoint(Double.NaN);
        assertEquals("Static var compensator 'SVC': highVoltageSetpoint is invalid", assertThrows(ValidationException.class, standbyAutomatonAdder::add).getMessage());

        standbyAutomatonAdder = svc.newExtension(StandbyAutomatonAdder.class).withHighVoltageThreshold(Double.NaN);
        assertEquals("Static var compensator 'SVC': highVoltageThreshold is invalid", assertThrows(ValidationException.class, standbyAutomatonAdder::add).getMessage());

        standbyAutomatonAdder = svc.newExtension(StandbyAutomatonAdder.class).withLowVoltageThreshold(Double.NaN);
        assertEquals("Static var compensator 'SVC': lowVoltageThreshold is invalid", assertThrows(ValidationException.class, standbyAutomatonAdder::add).getMessage());
    }

    @Test
    public void testStandbyAutomatonCheckB0() {

        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl.getBusBreakerView().newBus()
                .setId("BUS")
                .add();
        StaticVarCompensator svc = vl.newStaticVarCompensator()
                .setId("SVC")
                .setConnectableBus("BUS")
                .setBmin(12.2)
                .setBmax(32.2)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetpoint(23.8)
                .add();

        StandbyAutomatonAdder standbyAutomatonAdder = svc.newExtension(StandbyAutomatonAdder.class)
                .withHighVoltageSetpoint(21.3)
                .withLowVoltageSetpoint(1.7)
                .withHighVoltageThreshold(10.0)
                .withLowVoltageThreshold(3.0)
                .withB0(Double.NaN);

        assertEquals("Static var compensator 'SVC': b0 is invalid", assertThrows(ValidationException.class, standbyAutomatonAdder::add).getMessage());
    }
}

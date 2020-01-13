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
 */
public final class NetworkStorageTestCaseFactory {

    private NetworkStorageTestCaseFactory() {
    }

    public static Network create() {
        return create(NetworkFactory.findDefault());
    }

    public static Network create(NetworkFactory networkFactory) {
        Objects.requireNonNull(networkFactory);

        Network network = networkFactory.createNetwork("svcTestCase", "code");
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
        VscConverterStation vscConverterStation2 = vl2.newVscConverterStation()
                .setId("VSC2")
                .setNode(2)
                .setLossFactor(17)
                .setReactivePowerSetpoint(227)
                .setVoltageRegulatorOn(false)
                .setVoltageSetpoint(213)
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
                .setPowerFactor(35)
                .add();
        lccConverterStation.getTerminal().setP(440);
        lccConverterStation.getTerminal().setQ(320);
        return network;
    }
}

/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.tck;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.tck.AbstractNetworkTest;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import com.powsybl.iidm.network.util.Networks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkTest extends AbstractNetworkTest {

    private static final String REGION1 = "region1";
    private static final String VOLTAGE_LEVEL1_BUSBAR_SECTION2 = "voltageLevel1BusbarSection2";
    private static final String SUBSTATION12 = "substation1";
    private static final String VOLTAGE_LEVEL1 = "voltageLevel1";
    private static final String VOLTAGE_LEVEL1_BUSBAR_SECTION1 = "voltageLevel1BusbarSection1";
    private static final String VOLTAGE_LEVEL1_BREAKER1 = "voltageLevel1Breaker1";

    @Override
    public void testWith() {
        // FIXME
    }

    @Override
    public void testScadaNetwork() {
        // FIXME
    }

    @Test
    public void testStreams() {
        // FIXME remove this test when we use the release containing this PR : https://github.com/powsybl/powsybl-core/pull/3020
    }

    // see TODO-Override below
    @Override
    @Test
    public void testNetwork1() {
        Network network = NetworkTest1Factory.create();
        assertSame(network, network.getNetwork());
        assertEquals(1, Iterables.size(network.getCountries()));
        assertEquals(1, network.getCountryCount());
        Country country1 = network.getCountries().iterator().next();

        assertEquals(1, Iterables.size(network.getSubstations()));
        assertEquals(1, Iterables.size(network.getSubstations(Country.FR, "TSO1", REGION1)));
        assertEquals(1, network.getSubstationCount());
        assertEquals(2, network.getBusBreakerView().getBusCount());

        Substation substation1 = network.getSubstation(SUBSTATION12);
        assertNotNull(substation1);
        assertEquals(SUBSTATION12, substation1.getId());
        assertSame(country1, substation1.getCountry().orElse(null));
        assertEquals(1, substation1.getGeographicalTags().size());
        assertTrue(substation1.getGeographicalTags().contains(REGION1));
        assertEquals(1, Iterables.size(network.getVoltageLevels()));
        assertEquals(1, network.getVoltageLevelCount());

        VoltageLevel voltageLevel1 = network.getVoltageLevel(VOLTAGE_LEVEL1);
        assertNotNull(voltageLevel1);
        assertEquals(VOLTAGE_LEVEL1, voltageLevel1.getId());
        assertEquals(400.0, voltageLevel1.getNominalV(), 0.0);
        assertSame(substation1, voltageLevel1.getSubstation().orElse(null));
        assertSame(TopologyKind.NODE_BREAKER, voltageLevel1.getTopologyKind());

        VoltageLevel.NodeBreakerView topology1 = voltageLevel1.getNodeBreakerView();

        assertEquals(0.0, topology1.getFictitiousP0(0), 0.0);
        assertEquals(0.0, topology1.getFictitiousQ0(0), 0.0);
        topology1.setFictitiousP0(0, 1.0).setFictitiousQ0(0, 2.0);
        assertEquals(1.0, topology1.getFictitiousP0(0), 0.0);
        assertEquals(2.0, topology1.getFictitiousQ0(0), 0.0);
        Map<String, Set<Integer>> nodesByBus = Networks.getNodesByBus(voltageLevel1);
        nodesByBus.forEach((busId, nodes) -> {
            if (nodes.contains(0)) {
                assertEquals(1.0, voltageLevel1.getBusView().getBus(busId).getFictitiousP0(), 0.0);
            } else if (nodes.contains(1)) {
                assertEquals(2.0, voltageLevel1.getBusView().getBus(busId).getFictitiousP0(), 0.0);
            }
        });

        assertEquals(6, topology1.getMaximumNodeIndex());
        assertEquals(2, Iterables.size(topology1.getBusbarSections()));
        assertEquals(2, topology1.getBusbarSectionCount());

        assertEquals(2, Iterables.size(network.getBusbarSections()));
        assertEquals(2, network.getBusbarSectionCount());
        assertEquals(2, network.getBusbarSectionStream().count());

        BusbarSection voltageLevel1BusbarSection1 = topology1.getBusbarSection(VOLTAGE_LEVEL1_BUSBAR_SECTION1);
        assertNotNull(voltageLevel1BusbarSection1);
        assertEquals(VOLTAGE_LEVEL1_BUSBAR_SECTION1, voltageLevel1BusbarSection1.getId());

        BusbarSection voltageLevel1BusbarSection2 = topology1.getBusbarSection(VOLTAGE_LEVEL1_BUSBAR_SECTION2);
        assertNotNull(voltageLevel1BusbarSection2);
        assertEquals(VOLTAGE_LEVEL1_BUSBAR_SECTION2, voltageLevel1BusbarSection2.getId());
        assertEquals(5, Iterables.size(topology1.getSwitches()));
        assertEquals(5, topology1.getSwitchCount());

        VoltageLevel voltageLevel2 = substation1.newVoltageLevel().setId("VL2").setNominalV(320).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        assertNull(voltageLevel2.getNodeBreakerView().getBusbarSection(VOLTAGE_LEVEL1_BUSBAR_SECTION1));

        assertEquals(Arrays.asList(network.getSwitch("generator1Disconnector1"), network.getSwitch("generator1Breaker1")),
                topology1.getSwitches(6));
        assertEquals(Arrays.asList(network.getSwitch("load1Disconnector1"), network.getSwitch("load1Breaker1")),
                topology1.getSwitchStream(3).collect(Collectors.toList()));
        assertEquals(Collections.singletonList(network.getSwitch("load1Disconnector1")), topology1.getSwitches(2));

        assertEquals(5, Iterables.size(network.getSwitches()));
        assertEquals(5, network.getSwitchCount());
        assertEquals(5, network.getSwitchStream().count());

        Switch voltageLevel1Breaker1 = topology1.getSwitch(VOLTAGE_LEVEL1_BREAKER1);
        assertNotNull(voltageLevel1Breaker1);
        assertEquals(VOLTAGE_LEVEL1_BREAKER1, voltageLevel1Breaker1.getId());
        assertFalse(voltageLevel1Breaker1.isOpen());
        assertTrue(voltageLevel1Breaker1.isRetained());
        assertSame(SwitchKind.BREAKER, voltageLevel1Breaker1.getKind());
        assertSame(voltageLevel1BusbarSection1.getTerminal().getNodeBreakerView().getNode(), topology1.getNode1(voltageLevel1Breaker1.getId()));
        assertSame(voltageLevel1BusbarSection2.getTerminal().getNodeBreakerView().getNode(), topology1.getNode2(voltageLevel1Breaker1.getId()));
        assertEquals(1, Iterables.size(voltageLevel1.getLoads()));
        assertEquals(1, voltageLevel1.getLoadCount());

        Load load1 = network.getLoad("load1");
        assertNotNull(load1);
        assertEquals("load1", load1.getId());
        assertEquals(2, load1.getTerminal().getNodeBreakerView().getNode());
        assertEquals(10.0, load1.getP0(), 0.0);
        assertEquals(3.0, load1.getQ0(), 0.0);

        Generator generator1 = network.getGenerator("generator1");
        assertNotNull(generator1);
        assertEquals("generator1", generator1.getId());
        assertEquals(5, generator1.getTerminal().getNodeBreakerView().getNode());
        assertEquals(200.0, generator1.getMinP(), 0.0);
        assertEquals(900.0, generator1.getMaxP(), 0.0);
        assertSame(EnergySource.NUCLEAR, generator1.getEnergySource());
        assertTrue(generator1.isVoltageRegulatorOn());
        assertEquals(900.0, generator1.getTargetP(), 0.0);
        assertEquals(380.0, generator1.getTargetV(), 0.0);
        ReactiveCapabilityCurve rcc1 = generator1.getReactiveLimits(ReactiveCapabilityCurve.class);
        assertEquals(2, rcc1.getPointCount());
        assertEquals(500.0, rcc1.getMaxQ(500), 0.0);
        assertEquals(300.0, rcc1.getMinQ(500), 0.0);

        assertEquals(2, Iterables.size(voltageLevel1.getBusBreakerView().getBuses()));
        assertEquals(2, voltageLevel1.getBusBreakerView().getBusCount());
        Bus busCalc1 = voltageLevel1BusbarSection1.getTerminal().getBusBreakerView().getBus();
        Bus busCalc2 = voltageLevel1BusbarSection2.getTerminal().getBusBreakerView().getBus();
        // TODO-Override dont use assertSame but asserEquals on CalculatedBus (id vs ptr equality)
        assertEquals(busCalc1, load1.getTerminal().getBusBreakerView().getBus());
        assertEquals(busCalc2, generator1.getTerminal().getBusBreakerView().getBus());
        assertEquals(0, busCalc1.getConnectedComponent().getNum());
        assertEquals(0, busCalc2.getConnectedComponent().getNum());

        assertEquals(1, Iterables.size(voltageLevel1.getBusView().getBuses()));
        Bus busCalc = voltageLevel1BusbarSection1.getTerminal().getBusView().getBus();
        assertEquals(busCalc, voltageLevel1BusbarSection2.getTerminal().getBusView().getBus());
        assertEquals(busCalc, load1.getTerminal().getBusView().getBus());
        assertEquals(busCalc, generator1.getTerminal().getBusView().getBus());
        // TODO-Override KO assertEquals(0, busCalc.getConnectedComponent().getNum());

        // Changes listener
        NetworkListener exceptionListener = mock(DefaultNetworkListener.class);
        doThrow(new UnsupportedOperationException()).when(exceptionListener).onElementAdded(any(), anyString(), any());
        doThrow(new UnsupportedOperationException()).when(exceptionListener).onElementReplaced(any(), anyString(),
                any(), any());
        NetworkListener mockedListener = mock(DefaultNetworkListener.class);

        // Identifiable properties
        // TODO-Override properties assertions made on voltageLevel1 rather than busCalc, cause CalculatedBus is NOT AbstractIdentifiable in network-store
        String key = "keyTest";
        String value = "ValueTest";
        assertFalse(voltageLevel1.hasProperty());
        assertTrue(voltageLevel1.getPropertyNames().isEmpty());
        // Test without listeners registered
        voltageLevel1.setProperty("listeners", "no listeners");
        // Test without listeners registered & same values
        voltageLevel1.setProperty("listeners", "no listeners");
        verifyNoMoreInteractions(mockedListener);
        verifyNoMoreInteractions(exceptionListener);
        // Add observer changes to current network
        network.addListener(mockedListener);
        network.addListener(exceptionListener);
        // Test with listeners registered
        voltageLevel1.setProperty(key, value);
        assertTrue(voltageLevel1.hasProperty());
        assertTrue(voltageLevel1.hasProperty(key));
        assertEquals(value, voltageLevel1.getProperty(key));
        assertEquals("default", voltageLevel1.getProperty("invalid", "default"));
        assertEquals(2, voltageLevel1.getPropertyNames().size());

        // Check notification done
        verify(mockedListener, times(1))
                .onElementAdded(voltageLevel1, "properties[" + key + "]", value);
        // Check no notification on same property
        String value2 = "ValueTest2";
        voltageLevel1.setProperty(key, value2);
        verify(mockedListener, times(1))
                .onElementReplaced(voltageLevel1, "properties[" + key + "]", value, value2);
        // Check no notification on same property
        voltageLevel1.setProperty(key, value2);
        verifyNoMoreInteractions(mockedListener);
        // Remove changes observer
        network.removeListener(mockedListener);
        // Adding same property without listener registered
        voltageLevel1.setProperty(key, value);
        // Check no notification
        verifyNoMoreInteractions(mockedListener);

        // validation
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        network.runValidationChecks();
        // TODO-Override cannot use EQUIPMENT level (Validation level below STEADY_STATE_HYPOTHESIS not supported)
        /*network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        network.getLoad("load1").setP0(0.0);
        voltageLevel1.newLoad()
                .setId("unchecked")
                .setP0(1.0)
                .setQ0(1.0)
                .setNode(3)
                .add();
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        Load unchecked2 = voltageLevel1.newLoad()
                .setId("unchecked2")
                .setNode(10)
                .add();
        assertEquals(ValidationLevel.EQUIPMENT, network.getValidationLevel());
        unchecked2.setP0(0.0).setQ0(0.0);
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        network.setMinimumAcceptableValidationLevel(ValidationLevel.STEADY_STATE_HYPOTHESIS);*/
    }

    // The following test methods are overrided from AbstractNetworkTest, because of a different message header
    // used in validation exception messages :
    // 'AC line' in powsybl
    // 'AC Line' in network store
    @Override
    public void testPermanentLimitOnSelectedOperationalLimitsGroup() {
        Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        Line line = network.getLine("NHV1_NHV2_1");
        line.getCurrentLimits2().ifPresent(currentLimits -> {
            ValidationException e = assertThrows(ValidationException.class, () -> currentLimits.setPermanentLimit(Double.NaN));
            assertTrue(e.getMessage().contains("AC Line 'NHV1_NHV2_1': permanent limit must be defined if temporary limits are present"));
        });
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        network.getLine("NHV1_NHV2_1").getCurrentLimits2().orElseThrow().setPermanentLimit(Double.NaN);
        assertTrue(Double.isNaN(network.getLine("NHV1_NHV2_1").getCurrentLimits2().orElseThrow().getPermanentLimit()));
        assertEquals(ValidationLevel.EQUIPMENT, network.getValidationLevel());
    }

    @Override
    public void testPermanentLimitOnUnselectedOperationalLimitsGroup() {
        Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        Assertions.assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        OperationalLimitsGroup unselectedGroup = network.getLine("NHV1_NHV2_1").newOperationalLimitsGroup1("unselectedGroup");
        Assertions.assertNotEquals("unselectedGroup", network.getLine("NHV1_NHV2_1").getSelectedOperationalLimitsGroupId1().orElseThrow());
        CurrentLimitsAdder adder = unselectedGroup.newCurrentLimits().setPermanentLimit(Double.NaN).beginTemporaryLimit().setName("5'").setAcceptableDuration(300).setValue(1000.0).endTemporaryLimit();
        Objects.requireNonNull(adder);
        ValidationException e = Assertions.assertThrows(ValidationException.class, adder::add);
        Assertions.assertTrue(e.getMessage().contains("AC Line 'NHV1_NHV2_1': permanent limit must be defined if temporary limits are present"));
        CurrentLimits currentLimits = adder.setPermanentLimit(1000.0).add();
        Assertions.assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        e = Assertions.assertThrows(ValidationException.class, () -> currentLimits.setPermanentLimit(Double.NaN));
        Assertions.assertTrue(e.getMessage().contains("AC Line 'NHV1_NHV2_1': permanent limit must be defined if temporary limits are present"));
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        currentLimits.setPermanentLimit(Double.NaN);
        Assertions.assertTrue(Double.isNaN(currentLimits.getPermanentLimit()));
        Assertions.assertEquals(ValidationLevel.EQUIPMENT, network.getValidationLevel());
    }

    @Override
    public void testPermanentLimitViaAdder() {
        Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        OperationalLimitsGroup unselectedGroup = network.getLine("NHV1_NHV2_1").newOperationalLimitsGroup1("unselectedGroup");
        assertNotEquals("unselectedGroup", network.getLine("NHV1_NHV2_1").getSelectedOperationalLimitsGroupId1().orElseThrow());
        CurrentLimitsAdder adder = unselectedGroup.newCurrentLimits()
            .setPermanentLimit(Double.NaN)
            .beginTemporaryLimit()
            .setName("5'")
            .setAcceptableDuration(300)
            .setValue(1000)
            .endTemporaryLimit();
        ValidationException e = assertThrows(ValidationException.class, adder::add);
        assertTrue(e.getMessage().contains("AC Line 'NHV1_NHV2_1': permanent limit must be defined if temporary limits are present"));
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        adder.add();
        assertEquals(ValidationLevel.EQUIPMENT, network.getValidationLevel());
    }
}

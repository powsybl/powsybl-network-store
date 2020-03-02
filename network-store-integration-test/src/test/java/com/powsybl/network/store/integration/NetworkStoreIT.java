/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.integration;

import com.google.common.collect.ImmutableSet;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.entsoe.util.MergedXnode;
import com.powsybl.entsoe.util.Xnode;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.VoltageLevel.NodeBreakerView.InternalConnection;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.ReactiveCapabilityCurveImpl;
import com.powsybl.network.store.server.CassandraConfig;
import com.powsybl.network.store.server.CassandraConstants;
import com.powsybl.network.store.server.NetworkStoreApplication;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.ucte.converter.UcteImporter;
import org.apache.commons.io.FilenameUtils;
import org.cassandraunit.spring.CassandraDataSet;
import org.cassandraunit.spring.EmbeddedCassandra;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {NetworkStoreApplication.class, CassandraConfig.class, NetworkStoreService.class})
@TestExecutionListeners(listeners = CustomCassandraUnitTestExecutionListener.class,
                        mergeMode = MERGE_WITH_DEFAULTS)
@CassandraDataSet(value = "iidm.cql", keyspace = CassandraConstants.KEYSPACE_IIDM)
@EmbeddedCassandra(timeout = 60000L)
public class NetworkStoreIT {

    @LocalServerPort
    private int randomServerPort;

    private String getBaseUrl() {
        return "http://localhost:" + randomServerPort + "/";
    }

    // This method is provided to avoid timeout when dropping tables
    @Before
    public void initialize() {
        EmbeddedCassandraServerHelper.getCluster().getConfiguration().getSocketOptions().setReadTimeoutMillis(60000);
    }

    @Test
    public void test() {
        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {
            // import new network in the store
            assertTrue(service.getNetworkIds().isEmpty());
            Network network = service.importNetwork(new ResourceDataSource("test", new ResourceSet("/", "test.xiidm")));
            service.flush(network);

            assertEquals(1, service.getNetworkIds().size());

            testNetwork(network);
        }
    }

    private static void testNetwork(Network network) {
        assertEquals("sim1", network.getId());
        assertEquals("sim1", network.getName());
        assertEquals("test", network.getSourceFormat());
        assertEquals("2019-05-27T11:31:41.109+02:00", network.getCaseDate().toString());
        assertEquals(0, network.getForecastDistance());
        assertEquals(1, network.getSubstationStream().count());
        Substation p1 = network.getSubstation("P1");
        assertNotNull(p1);
        assertEquals("P1", p1.getId());
        assertEquals(Country.FR, p1.getCountry().orElse(null));
        assertEquals(Country.FR, p1.getNullableCountry());
        assertEquals("RTE", p1.getTso());
        assertSame(network, p1.getNetwork());
        assertSame(p1, network.getSubstation("P1"));
        assertEquals(1, network.getSubstationCount());
        assertSame(p1, network.getSubstationStream().findFirst().orElseThrow(AssertionError::new));
        assertEquals(1, network.getCountryCount());
        assertEquals(ImmutableSet.of(Country.FR), network.getCountries());
    }

    @Test
    public void nodeBreakerTest() {
        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {
            Network network = NetworkTest1Factory.create(service.getNetworkFactory());
            service.flush(network);

            assertEquals("network1", network.getId());

            assertEquals(1, network.getGeneratorCount());
            assertEquals("generator1", network.getGeneratorStream().findFirst().orElseThrow(AssertionError::new).getId());
            assertNotNull(network.getGenerator("generator1"));
            assertEquals(5, network.getGenerator("generator1").getTerminal().getNodeBreakerView().getNode());

            assertEquals(1, network.getLoadCount());
            assertEquals("load1", network.getLoadStream().findFirst().orElseThrow(AssertionError::new).getId());
            assertNotNull(network.getLoad("load1"));
            assertEquals(2, network.getLoad("load1").getTerminal().getNodeBreakerView().getNode());

            // try to emulate voltage level diagram generation use case

            for (Substation s : network.getSubstations()) {
                assertEquals("substation1", s.getId());
                for (VoltageLevel vl : s.getVoltageLevels()) {
                    assertEquals("voltageLevel1", vl.getId());
                    vl.visitEquipments(new DefaultTopologyVisitor() {
                        @Override
                        public void visitBusbarSection(BusbarSection section) {
                            assertTrue(section.getId().equals("voltageLevel1BusbarSection1") || section.getId().equals("voltageLevel1BusbarSection2"));
                        }

                        @Override
                        public void visitLoad(Load load) {
                            assertEquals("load1", load.getId());
                        }

                        @Override
                        public void visitGenerator(Generator generator) {
                            assertEquals("generator1", generator.getId());
                        }
                    });
                }
            }
        }
    }

    @Test
    public void svcTest() {
        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("networkTestCase", readNetwork.getId());

            assertEquals(1, readNetwork.getStaticVarCompensatorCount());

            Stream<StaticVarCompensator> svcs = readNetwork.getStaticVarCompensatorStream();
            StaticVarCompensator svc = svcs.findFirst().get();
            assertEquals(0.0002, svc.getBmin(), 0.00001);
            assertEquals(0.0008, svc.getBmax(), 0.00001);
            assertEquals(StaticVarCompensator.RegulationMode.VOLTAGE, svc.getRegulationMode());
            assertEquals(390, svc.getVoltageSetPoint(), 0.1);
            assertEquals(200, svc.getReactivePowerSetPoint(), 0.1);
            assertEquals(435, svc.getTerminal().getP(), 0.1);
            assertEquals(315, svc.getTerminal().getQ(), 0.1);
        }
    }

    @Test
    public void vscConverterStationTest() {
        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("networkTestCase", readNetwork.getId());

            assertEquals(2, readNetwork.getVscConverterStationCount());

            Stream<VscConverterStation> vscConverterStationsStream = readNetwork.getVscConverterStationStream();
            VscConverterStation vscConverterStation = vscConverterStationsStream.filter(vsc -> vsc.getId().equals("VSC1")).findFirst().get();
            assertEquals("VSC1", vscConverterStation.getId());
            assertEquals(24, vscConverterStation.getLossFactor(), 0.1);
            assertEquals(300, vscConverterStation.getReactivePowerSetpoint(), 0.1);
            assertTrue(vscConverterStation.isVoltageRegulatorOn());
            assertEquals(290, vscConverterStation.getVoltageSetpoint(), 0.1);
            assertEquals(445, vscConverterStation.getTerminal().getP(), 0.1);
            assertEquals(325, vscConverterStation.getTerminal().getQ(), 0.1);
            assertEquals(ReactiveLimitsKind.CURVE, vscConverterStation.getReactiveLimits().getKind());
            ReactiveCapabilityCurve limits = vscConverterStation.getReactiveLimits(ReactiveCapabilityCurve.class);
            assertEquals(10, limits.getMaxQ(5), 0.1);
            assertEquals(1, limits.getMinQ(5), 0.1);
            assertEquals(1, limits.getMaxQ(10), 0.1);
            assertEquals(-10, limits.getMinQ(10), 0.1);
            assertEquals(2, limits.getPointCount());
            assertEquals(2, limits.getPoints().size());

            VscConverterStation vscConverterStation2 = readNetwork.getVscConverterStation("VSC2");
            assertEquals("VSC2", vscConverterStation2.getId());
            assertEquals(17, vscConverterStation2.getLossFactor(), 0.1);
            assertEquals(227, vscConverterStation2.getReactivePowerSetpoint(), 0.1);
            assertFalse(vscConverterStation2.isVoltageRegulatorOn());
            assertEquals(213, vscConverterStation2.getVoltageSetpoint(), 0.1);
            assertEquals(254, vscConverterStation2.getTerminal().getP(), 0.1);
            assertEquals(117, vscConverterStation2.getTerminal().getQ(), 0.1);
            assertEquals(ReactiveLimitsKind.MIN_MAX, vscConverterStation2.getReactiveLimits().getKind());
            MinMaxReactiveLimits minMaxLimits = vscConverterStation2.getReactiveLimits(MinMaxReactiveLimits.class);
            assertEquals(127, minMaxLimits.getMaxQ(), 0.1);
            assertEquals(103, minMaxLimits.getMinQ(), 0.1);
        }
    }

    @Test
    public void lccConverterStationTest() {
        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("networkTestCase", readNetwork.getId());

            assertEquals(1, readNetwork.getLccConverterStationCount());

            Stream<LccConverterStation> lccConverterStations = readNetwork.getLccConverterStationStream();
            LccConverterStation lccConverterStation = lccConverterStations.findFirst().get();
            assertEquals("LCC2", lccConverterStation.getId());
            assertEquals(35, lccConverterStation.getPowerFactor(), 0.1);
            assertEquals(440, lccConverterStation.getTerminal().getP(), 0.1);
            assertEquals(320, lccConverterStation.getTerminal().getQ(), 0.1);
        }
    }

    @Test
    public void danglingLineTest() {
        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("networkTestCase", readNetwork.getId());

            assertEquals(1, readNetwork.getDanglingLineCount());

            Stream<DanglingLine> danglingLines = readNetwork.getDanglingLineStream();
            DanglingLine danglingLine = danglingLines.findFirst().get();
            assertEquals("DL1", danglingLine.getId());
            assertEquals("Dangling line 1", danglingLine.getName());
            assertEquals(533, danglingLine.getP0(), 0.1);
            assertEquals(242, danglingLine.getQ0(), 0.1);
            assertEquals(27, danglingLine.getR(), 0.1);
            assertEquals(44, danglingLine.getX(), 0.1);
            assertEquals(89, danglingLine.getG(), 0.1);
            assertEquals(11, danglingLine.getB(), 0.1);
            assertEquals("UCTE_DL1", danglingLine.getUcteXnodeCode());

            CurrentLimits currentLimits = danglingLine.getCurrentLimits();
            assertEquals(256, currentLimits.getPermanentLimit(), 0.1);
            assertEquals(432, currentLimits.getTemporaryLimitValue(20), 0.1);
            CurrentLimits.TemporaryLimit temporaryLimit = currentLimits.getTemporaryLimit(20);
            assertEquals(432, temporaryLimit.getValue(), 0.1);
            assertEquals("TL1", temporaryLimit.getName());
            assertFalse(temporaryLimit.isFictitious());
            assertEquals(289, currentLimits.getTemporaryLimitValue(40), 0.1);
            temporaryLimit = currentLimits.getTemporaryLimit(40);
            assertEquals(289, temporaryLimit.getValue(), 0.1);
            assertEquals("TL2", temporaryLimit.getName());
            assertTrue(temporaryLimit.isFictitious());
        }
    }

    @Test
    public void hvdcLineTest() {
        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("networkTestCase", readNetwork.getId());

            assertEquals(1, readNetwork.getHvdcLineCount());

            Stream<HvdcLine> hvdcLines = readNetwork.getHvdcLineStream();
            HvdcLine hvdcLine = hvdcLines.findFirst().get();
            assertEquals(256, hvdcLine.getR(), 0.1);
            assertEquals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, hvdcLine.getConvertersMode());
            assertEquals(330, hvdcLine.getActivePowerSetpoint(), 0.1);
            assertEquals(335, hvdcLine.getNominalV(), 0.1);
            assertEquals(390, hvdcLine.getMaxP(), 0.1);
            assertEquals("VSC1", hvdcLine.getConverterStation1().getId());
            assertEquals("VSC2", hvdcLine.getConverterStation2().getId());
        }
    }

    @Test
    public void threeWindingsTransformerTest() {
        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("networkTestCase", readNetwork.getId());

            assertEquals(1, readNetwork.getThreeWindingsTransformerCount());

            Stream<ThreeWindingsTransformer> threeWindingsTransformerStream = readNetwork.getThreeWindingsTransformerStream();
            ThreeWindingsTransformer threeWindingsTransformer = threeWindingsTransformerStream.findFirst().get();
            assertEquals(234, threeWindingsTransformer.getRatedU0(), 0.1);
            assertEquals(45, threeWindingsTransformer.getLeg1().getR(), 0.1);
            assertEquals(35, threeWindingsTransformer.getLeg1().getX(), 0.1);
            assertEquals(25, threeWindingsTransformer.getLeg1().getG(), 0.1);
            assertEquals(15, threeWindingsTransformer.getLeg1().getB(), 0.1);
            assertEquals(5, threeWindingsTransformer.getLeg1().getRatedU(), 0.1);
            assertEquals(47, threeWindingsTransformer.getLeg2().getR(), 0.1);
            assertEquals(37, threeWindingsTransformer.getLeg2().getX(), 0.1);
            assertEquals(27, threeWindingsTransformer.getLeg2().getG(), 0.1);
            assertEquals(17, threeWindingsTransformer.getLeg2().getB(), 0.1);
            assertEquals(7, threeWindingsTransformer.getLeg2().getRatedU(), 0.1);
            assertEquals(49, threeWindingsTransformer.getLeg3().getR(), 0.1);
            assertEquals(39, threeWindingsTransformer.getLeg3().getX(), 0.1);
            assertEquals(29, threeWindingsTransformer.getLeg3().getG(), 0.1);
            assertEquals(19, threeWindingsTransformer.getLeg3().getB(), 0.1);
            assertEquals(9, threeWindingsTransformer.getLeg3().getRatedU(), 0.1);

            assertEquals(375, threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.ONE).getP(), 0.1);
            assertEquals(225, threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.TWO).getP(), 0.1);
            assertEquals(200, threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.THREE).getP(), 0.1);

            assertEquals(48, threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.ONE).getQ(), 0.1);
            assertEquals(28, threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.TWO).getQ(), 0.1);
            assertEquals(18, threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.THREE).getQ(), 0.1);

            assertEquals(1, threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.ONE).getNodeBreakerView().getNode());
            assertEquals(2, threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.TWO).getNodeBreakerView().getNode());
            assertEquals(3, threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.THREE).getNodeBreakerView().getNode());

            assertEquals(3, threeWindingsTransformer.getTerminals().size());
            assertTrue(threeWindingsTransformer.getTerminals().contains(threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.ONE)));
            assertTrue(threeWindingsTransformer.getTerminals().contains(threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.TWO)));
            assertTrue(threeWindingsTransformer.getTerminals().contains(threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.THREE)));

            PhaseTapChanger phaseTapChanger = threeWindingsTransformer.getLeg1().getPhaseTapChanger();
            assertEqualsPhaseTapChangerStep(phaseTapChanger.getStep(0), -10, 1.5, 0.5, 1., 0.99, 4.);
            assertEqualsPhaseTapChangerStep(phaseTapChanger.getStep(1), 0, 1.6, 0.6, 1.1, 1., 4.1);
            assertEqualsPhaseTapChangerStep(phaseTapChanger.getStep(2), 10, 1.7, 0.7, 1.2, 1.01, 4.2);
            assertEqualsPhaseTapChangerStep(phaseTapChanger.getCurrentStep(), -10, 1.5, 0.5, 1., 0.99, 4.);

            RatioTapChanger ratioTapChanger = threeWindingsTransformer.getLeg2().getRatioTapChanger();
            assertEqualsRatioTapChangerStep(ratioTapChanger.getStep(0), 1.5, 0.5, 1., 0.99, 4.);
            assertEqualsRatioTapChangerStep(ratioTapChanger.getStep(1), 1.6, 0.6, 1.1, 1., 4.1);
            assertEqualsRatioTapChangerStep(ratioTapChanger.getStep(2), 1.7, 0.7, 1.2, 1.01, 4.2);
            assertEqualsRatioTapChangerStep(ratioTapChanger.getCurrentStep(), 1.5, 0.5, 1., 0.99, 4.);

            assertEquals(25, threeWindingsTransformer.getLeg1().getCurrentLimits().getPermanentLimit(), .0001);
        }
    }

    @Test
    public void internalConnectionsFromCgmesTest() {
        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {
            // import new network in the store
            Network network = service.importNetwork(CgmesConformity1Catalog.miniNodeBreaker().dataSource());
            service.flush(network);
        }

        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            Map<String, Integer> nbInternalConnectionsPerVL = new HashMap();
            readNetwork.getVoltageLevels().forEach(vl -> nbInternalConnectionsPerVL.put(vl.getId(), vl.getNodeBreakerView().getInternalConnectionCount()));

            assertEquals(9, nbInternalConnectionsPerVL.get("_b2707f00-2554-41d2-bde2-7dd80a669e50"), .0001);
            assertEquals(11, nbInternalConnectionsPerVL.get("_8d4a8238-5b31-4c16-8692-0265dae5e132"), .0001);
            assertEquals(23, nbInternalConnectionsPerVL.get("_0d68ac81-124d-4d21-afa8-6c503feef5b8"), .0001);
            assertEquals(9, nbInternalConnectionsPerVL.get("_6f8ef715-bc0a-47d7-a74e-27f17234f590"), .0001);
            assertEquals(29, nbInternalConnectionsPerVL.get("_347fb7af-642f-4c60-97d9-c03d440b6a82"), .0001);
            assertEquals(22, nbInternalConnectionsPerVL.get("_051b93ae-9c15-4490-8cea-33395298f031"), .0001);
            assertEquals(22, nbInternalConnectionsPerVL.get("_5d9d9d87-ce6b-4213-b4ec-d50de9790a59"), .0001);
            assertEquals(16, nbInternalConnectionsPerVL.get("_93778e52-3fd5-456d-8b10-987c3e6bc47e"), .0001);
            assertEquals(50, nbInternalConnectionsPerVL.get("_a43d15db-44a6-4fda-a525-2402ff43226f"), .0001);
            assertEquals(36, nbInternalConnectionsPerVL.get("_cd28a27e-8b17-4f23-b9f5-03b6de15203f"), .0001);

            InternalConnection ic = readNetwork.getVoltageLevel("_b2707f00-2554-41d2-bde2-7dd80a669e50").getNodeBreakerView().getInternalConnections().iterator().next();
            assertEquals(4, ic.getNode1());
            assertEquals(0, ic.getNode2());
        }
    }

    @Test
    public void moreComplexNodeBreakerTest() {
        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {
            Network network = FictitiousSwitchFactory.create(service.getNetworkFactory());
            service.flush(network);
        }
    }

    @Test
    public void testPhaseTapChanger() {
        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {
            service.flush(createTapChangerNetwork(service.getNetworkFactory()));
        }

        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("Phase tap changer", readNetwork.getId());

            assertEquals(1, readNetwork.getTwoWindingsTransformerCount());

            TwoWindingsTransformer twoWindingsTransformer = readNetwork.getTwoWindingsTransformer("TWT2");
            PhaseTapChanger phaseTapChanger = twoWindingsTransformer.getPhaseTapChanger();

            assertEquals(3, phaseTapChanger.getStepCount());
            assertEquals(PhaseTapChanger.RegulationMode.CURRENT_LIMITER, phaseTapChanger.getRegulationMode());
            assertEquals(25, phaseTapChanger.getRegulationValue(), .0001);
            assertEquals(-1, phaseTapChanger.getLowTapPosition());
            assertEquals(22, phaseTapChanger.getTargetDeadband(), .0001);
            assertEquals(1, phaseTapChanger.getHighTapPosition());
            assertEquals(0, phaseTapChanger.getTapPosition());
            assertTrue(phaseTapChanger.isRegulating());
            assertEqualsPhaseTapChangerStep(phaseTapChanger.getStep(0), -10, 1.5, 0.5, 1., 0.99, 4.);
            assertEqualsPhaseTapChangerStep(phaseTapChanger.getStep(1), 0, 1.6, 0.6, 1.1, 1., 4.1);
            assertEqualsPhaseTapChangerStep(phaseTapChanger.getStep(2), 10, 1.7, 0.7, 1.2, 1.01, 4.2);
            assertEqualsPhaseTapChangerStep(phaseTapChanger.getCurrentStep(), -10, 1.5, 0.5, 1., 0.99, 4.);

            phaseTapChanger.setLowTapPosition(-2);
            phaseTapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL);
            phaseTapChanger.setRegulationValue(12);
            phaseTapChanger.setRegulating(false);
            phaseTapChanger.setTapPosition(2);
            phaseTapChanger.setTargetDeadband(13);
            assertEquals(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, phaseTapChanger.getRegulationMode());
            assertEquals(12, phaseTapChanger.getRegulationValue(), .0001);
            assertEquals(-2, phaseTapChanger.getLowTapPosition());
            assertEquals(13, phaseTapChanger.getTargetDeadband(), .0001);
            assertEquals(2, phaseTapChanger.getTapPosition());
            assertFalse(phaseTapChanger.isRegulating());

            PhaseTapChangerStep phaseTapChangerStep = phaseTapChanger.getStep(0);
            phaseTapChangerStep.setAlpha(20);
            phaseTapChangerStep.setB(21);
            phaseTapChangerStep.setG(22);
            phaseTapChangerStep.setR(23);
            phaseTapChangerStep.setRho(24);
            phaseTapChangerStep.setX(25);
            assertEquals(20, phaseTapChanger.getStep(0).getAlpha(), .0001);
            assertEquals(21, phaseTapChanger.getStep(0).getB(), .0001);
            assertEquals(22, phaseTapChanger.getStep(0).getG(), .0001);
            assertEquals(23, phaseTapChanger.getStep(0).getR(), .0001);
            assertEquals(24, phaseTapChanger.getStep(0).getRho(), .0001);
            assertEquals(25, phaseTapChanger.getStep(0).getX(), .0001);

            RatioTapChanger ratioTapChanger = twoWindingsTransformer.getRatioTapChanger();

            assertEquals(3, ratioTapChanger.getStepCount());
            assertEquals(-1, ratioTapChanger.getLowTapPosition());
            assertEquals(22, ratioTapChanger.getTargetDeadband(), .0001);
            assertEquals(1, ratioTapChanger.getHighTapPosition());
            assertEquals(0, ratioTapChanger.getTapPosition());
            assertTrue(ratioTapChanger.isRegulating());
            assertEqualsRatioTapChangerStep(ratioTapChanger.getStep(0), 1.5, 0.5, 1., 0.99, 4.);
            assertEqualsRatioTapChangerStep(ratioTapChanger.getStep(1), 1.6, 0.6, 1.1, 1., 4.1);
            assertEqualsRatioTapChangerStep(ratioTapChanger.getStep(2), 1.7, 0.7, 1.2, 1.01, 4.2);
            assertEqualsRatioTapChangerStep(ratioTapChanger.getCurrentStep(), 1.5, 0.5, 1., 0.99, 4.);

            ratioTapChanger.setLowTapPosition(-2);
            ratioTapChanger.setRegulating(false);
            ratioTapChanger.setTapPosition(2);
            ratioTapChanger.setTargetDeadband(13);
            ratioTapChanger.setLoadTapChangingCapabilities(false);
            ratioTapChanger.setTargetV(27);
            assertEquals(-2, ratioTapChanger.getLowTapPosition());
            assertEquals(13, ratioTapChanger.getTargetDeadband(), .0001);
            assertEquals(2, ratioTapChanger.getTapPosition());
            assertFalse(ratioTapChanger.hasLoadTapChangingCapabilities());
            assertFalse(ratioTapChanger.isRegulating());
            assertEquals(27, ratioTapChanger.getTargetV(), .0001);

            RatioTapChangerStep ratioTapChangerStep = ratioTapChanger.getStep(0);
            ratioTapChangerStep.setB(21);
            ratioTapChangerStep.setG(22);
            ratioTapChangerStep.setR(23);
            ratioTapChangerStep.setRho(24);
            ratioTapChangerStep.setX(25);
            assertEquals(21, ratioTapChanger.getStep(0).getB(), .0001);
            assertEquals(22, ratioTapChanger.getStep(0).getG(), .0001);
            assertEquals(23, ratioTapChanger.getStep(0).getR(), .0001);
            assertEquals(24, ratioTapChanger.getStep(0).getRho(), .0001);
            assertEquals(25, ratioTapChanger.getStep(0).getX(), .0001);
            assertEquals(25, ratioTapChanger.getStep(0).getX(), .0001);

        }
    }

    @Test
    public void testGeneratorMinMaxReactiveLimits() {
        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {
            service.flush(createGeneratorNetwork(service.getNetworkFactory(), ReactiveLimitsKind.MIN_MAX));
        }

        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {

            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals("Generator network", readNetwork.getId());

            Generator generator = readNetwork.getGeneratorStream().findFirst().get();
            assertEquals("GEN", generator.getId());

            ReactiveLimits reactiveLimits = generator.getReactiveLimits();

            assertEquals(ReactiveLimitsKind.MIN_MAX, reactiveLimits.getKind());
            MinMaxReactiveLimits minMaxReactiveLimits = (MinMaxReactiveLimits) reactiveLimits;
            assertEquals(2, minMaxReactiveLimits.getMaxQ(), .0001);
            assertEquals(-2, minMaxReactiveLimits.getMinQ(), .0001);

            generator.setEnergySource(EnergySource.HYDRO);
            generator.setMaxP(5);
            generator.setMinP(-5);
            generator.setRatedS(2);
            generator.setTargetP(3);
            generator.setTargetQ(4);
            generator.setTargetV(6);
            generator.setVoltageRegulatorOn(false);

            assertEquals(5, generator.getMaxP(), .0001);
            assertEquals(-5, generator.getMinP(), .0001);
            assertEquals(2, generator.getRatedS(), .0001);
            assertEquals(3, generator.getTargetP(), .0001);
            assertEquals(4, generator.getTargetQ(), .0001);
            assertEquals(6, generator.getTargetV(), .0001);
            assertFalse(generator.isVoltageRegulatorOn());
        }
    }

    @Test
    public void testGeneratorCurveReactiveLimits() {
        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {
            service.flush(createGeneratorNetwork(service.getNetworkFactory(), ReactiveLimitsKind.CURVE));
        }

        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {

            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals("Generator network", readNetwork.getId());

            Generator generator = readNetwork.getGeneratorStream().findFirst().get();
            assertEquals("GEN", generator.getId());

            ReactiveLimits reactiveLimits = generator.getReactiveLimits();

            assertEquals(ReactiveLimitsKind.CURVE, reactiveLimits.getKind());
            ReactiveCapabilityCurve reactiveCapabilityCurve = (ReactiveCapabilityCurve) reactiveLimits;
            ReactiveCapabilityCurveImpl.Point point = reactiveCapabilityCurve.getPoints().iterator().next();
            assertEquals(1, point.getMaxQ(), .0001);
            assertEquals(-1, point.getMinQ(), .0001);
            assertEquals(2, point.getP(), .0001);

            assertEquals(reactiveCapabilityCurve.getPointCount(), generator.getReactiveLimits(ReactiveCapabilityCurve.class).getPointCount());
        }
    }

    @Test
    public void testBusBreakerNetwork() {
        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {
            service.flush(BusBreakerNetworkFactory.create(service.getNetworkFactory()));
        }

        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {

            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            ArrayList<Bus> buses = new ArrayList<>();

            readNetwork.getBusBreakerView().getBuses().forEach(buses::add);
            assertEquals(4, buses.size());
            assertEquals(4, readNetwork.getBusBreakerView().getBusStream().count());

            ArrayList<Bus> votlageLevelBuses = new ArrayList<>();
            readNetwork.getVoltageLevel("VLLOAD").getBusBreakerView().getBuses().forEach(votlageLevelBuses::add);
            assertEquals(1, votlageLevelBuses.size());
            assertEquals("NLOAD", votlageLevelBuses.get(0).getId());
            assertNull(readNetwork.getVoltageLevel("VLLOAD").getBusBreakerView().getBus("NHV2"));
            assertNotNull(readNetwork.getVoltageLevel("VLLOAD").getBusBreakerView().getBus("NLOAD"));
        }
    }

    @Test
    public void testUcteNetwork() {
        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {
            service.flush(loadUcteNetwork(service.getNetworkFactory()));
        }

        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(1, readNetwork.getDanglingLineCount());
            assertEquals("XG__F_21", readNetwork.getDanglingLineStream().findFirst().get().getUcteXnodeCode());
            Xnode xnode = (Xnode) readNetwork.getDanglingLineStream().findFirst().get().getExtensionByName("xnode");
            assertEquals("XG__F_21", xnode.getCode());
            Xnode sameXnode = (Xnode) readNetwork.getDanglingLineStream().findFirst().get().getExtension(Xnode.class);
            assertEquals("XG__F_21", sameXnode.getCode());
            ConnectablePosition connectablePosition = (ConnectablePosition) readNetwork.getDanglingLineStream().findFirst().get().getExtension(ConnectablePosition.class);
            assertNull(connectablePosition);
            ConnectablePosition connectablePosition2 = (ConnectablePosition) readNetwork.getDanglingLineStream().findFirst().get().getExtensionByName("");
            assertNull(connectablePosition2);
            assertEquals(4, readNetwork.getLineCount());
            assertNotNull(readNetwork.getLine("XB__F_21 F_SU1_21 1"));
            assertNotNull(readNetwork.getLine("XB__F_11 F_SU1_11 1"));
            Line line = readNetwork.getLine("XB__F_21 F_SU1_21 1");
            assertTrue(line.isTieLine());
            assertNotNull(line.getExtension(MergedXnode.class));
            MergedXnode mergedXnode = line.getExtension(MergedXnode.class);
            assertEquals("XB__F_21", mergedXnode.getCode());
            assertEquals("XB__F_21 B_SU1_21 1", mergedXnode.getLine1Name());
            assertEquals("XB__F_21 F_SU1_21 1", mergedXnode.getLine2Name());
            assertNotNull(line.getExtensionByName("mergedXnode"));
            TieLine tieLine2 = readNetwork.newTieLine()
                    .setId("id")
                    .setName("name")
                    .setB1(1)
                    .setB2(2)
                    .setG1(3)
                    .setG2(4)
                    .setR(5)
                    .setX(6)
                    .setUcteXnodeCode("test")
                    .setXnodeP(7)
                    .setXnodeQ(8)
                    .add();
            assertEquals("id", tieLine2.getId());
            assertEquals("test", tieLine2.getUcteXnodeCode());
            assertEquals("name", tieLine2.getName());
            assertEquals(1, tieLine2.getHalf1().getB1(), .0001);
            assertEquals(2, tieLine2.getHalf1().getB2(), .0001);
            assertEquals(3, tieLine2.getHalf1().getG1(), .0001);
            assertEquals(4, tieLine2.getHalf1().getG2(), .0001);
            assertEquals(5, tieLine2.getHalf1().getR(), .0001);
            assertEquals(6, tieLine2.getHalf1().getX(), .0001);
            assertEquals(7, tieLine2.getHalf1().getXnodeP(), .0001);
            assertEquals(8, tieLine2.getHalf1().getXnodeQ(), .0001);
            assertEquals(1, tieLine2.getHalf2().getB1(), .0001);
            assertEquals(2, tieLine2.getHalf2().getB2(), .0001);
            assertEquals(3, tieLine2.getHalf2().getG1(), .0001);
            assertEquals(4, tieLine2.getHalf2().getG2(), .0001);
            assertEquals(5, tieLine2.getHalf2().getR(), .0001);
            assertEquals(6, tieLine2.getHalf2().getX(), .0001);
            assertEquals(7, tieLine2.getHalf2().getXnodeP(), .0001);
            assertEquals(8, tieLine2.getHalf2().getXnodeQ(), .0001);

            tieLine2.getHalf1().setB1(10);
            tieLine2.getHalf1().setB2(11);
            tieLine2.getHalf1().setG1(12);
            tieLine2.getHalf1().setG2(13);
            tieLine2.getHalf1().setR(14);
            tieLine2.getHalf1().setX(15);
            tieLine2.getHalf1().setXnodeP(16);
            tieLine2.getHalf1().setXnodeQ(17);
            tieLine2.getHalf2().setB1(18);
            tieLine2.getHalf2().setB2(19);
            tieLine2.getHalf2().setG1(20);
            tieLine2.getHalf2().setG2(21);
            tieLine2.getHalf2().setR(22);
            tieLine2.getHalf2().setX(23);
            tieLine2.getHalf2().setXnodeP(24);
            tieLine2.getHalf2().setXnodeQ(25);

            assertEquals(18, tieLine2.getHalf1().getB1(), .0001);
            assertEquals(19, tieLine2.getHalf1().getB2(), .0001);
            assertEquals(20, tieLine2.getHalf1().getG1(), .0001);
            assertEquals(21, tieLine2.getHalf1().getG2(), .0001);
            assertEquals(22, tieLine2.getHalf1().getR(), .0001);
            assertEquals(23, tieLine2.getHalf1().getX(), .0001);
            assertEquals(16, tieLine2.getHalf1().getXnodeP(), .0001);
            assertEquals(17, tieLine2.getHalf1().getXnodeQ(), .0001);
            assertEquals(18, tieLine2.getHalf2().getB1(), .0001);
            assertEquals(19, tieLine2.getHalf2().getB2(), .0001);
            assertEquals(20, tieLine2.getHalf2().getG1(), .0001);
            assertEquals(21, tieLine2.getHalf2().getG2(), .0001);
            assertEquals(22, tieLine2.getHalf2().getR(), .0001);
            assertEquals(23, tieLine2.getHalf2().getX(), .0001);
            assertEquals(24, tieLine2.getHalf2().getXnodeP(), .0001);
            assertEquals(25, tieLine2.getHalf2().getXnodeQ(), .0001);

            Line regularLine = readNetwork.getLine("F_SU1_12 F_SU2_11 2");
            assertNull(regularLine.getExtension(MergedXnode.class));
            regularLine.addExtension(MergedXnode.class,
                    new MergedXnode(regularLine, 1, 1, 1, 1,
                            1, 1, "", "", ""));
            assertNotNull(regularLine.getExtension(MergedXnode.class));
            assertEquals(1, regularLine.getExtension(MergedXnode.class).getRdp(), .0001);
        }
    }

    public Network loadUcteNetwork(NetworkFactory networkFactory) {
        String filePath = "/uctNetwork.uct";
        ReadOnlyDataSource dataSource = new ResourceDataSource(
                FilenameUtils.getBaseName(filePath),
                new ResourceSet(FilenameUtils.getPath(filePath),
                        FilenameUtils.getName(filePath)));
        return new UcteImporter().importData(dataSource, networkFactory, null);
    }

    private void assertEqualsPhaseTapChangerStep(PhaseTapChangerStep phaseTapChangerStep, double alpha, double b, double g, double r, double rho, double x) {
        assertEquals(alpha, phaseTapChangerStep.getAlpha(), .0001);
        assertEquals(b, phaseTapChangerStep.getB(), .0001);
        assertEquals(g, phaseTapChangerStep.getG(), .0001);
        assertEquals(r, phaseTapChangerStep.getR(), .0001);
        assertEquals(rho, phaseTapChangerStep.getRho(), .0001);
        assertEquals(x, phaseTapChangerStep.getX(), .0001);
    }

    private void assertEqualsRatioTapChangerStep(RatioTapChangerStep ratioTapChangerStep, double b, double g, double r, double rho, double x) {
        assertEquals(b, ratioTapChangerStep.getB(), .0001);
        assertEquals(g, ratioTapChangerStep.getG(), .0001);
        assertEquals(r, ratioTapChangerStep.getR(), .0001);
        assertEquals(rho, ratioTapChangerStep.getRho(), .0001);
        assertEquals(x, ratioTapChangerStep.getX(), .0001);
    }

    private Network createTapChangerNetwork(NetworkFactory networkFactory) {
        Network network = networkFactory.createNetwork("Phase tap changer", "test");
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.ES)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400f)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl1.getNodeBreakerView().setNodeCount(3);
        VoltageLevel vl2 = s1.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400f)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl2.getNodeBreakerView().setNodeCount(3);
        TwoWindingsTransformer twt = s1.newTwoWindingsTransformer()
                .setId("TWT2")
                .setName("My two windings transformer")
                .setVoltageLevel1("VL1")
                .setVoltageLevel2("VL2")
                .setNode1(1)
                .setNode2(2)
                .setR(0.5)
                .setX(4.)
                .setG(0)
                .setB(0)
                .setRatedU1(24)
                .setRatedU2(385)
                .add();
        twt.newPhaseTapChanger()
                .setLowTapPosition(-1)
                .setTapPosition(0)
                .setRegulating(true)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationValue(25)
                .setRegulationTerminal(twt.getTerminal2())
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
        twt.newRatioTapChanger()
                .setLowTapPosition(-1)
                .setTapPosition(0)
                .setRegulating(true)
                .setRegulationTerminal(twt.getTerminal2())
                .setTargetDeadband(22)
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
        return network;
    }

    private Network createGeneratorNetwork(NetworkFactory networkFactory, ReactiveLimitsKind kind) {
        Network network = networkFactory.createNetwork("Generator network", "test");
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.ES)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400f)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl1.getNodeBreakerView().setNodeCount(2);
        Generator generator = vl1.newGenerator()
                .setId("GEN")
                .setNode(1)
                .setMaxP(20)
                .setMinP(-20)
                .setVoltageRegulatorOn(true)
                .add();
        if (kind.equals(ReactiveLimitsKind.CURVE)) {
            generator.newReactiveCapabilityCurve()
                    .beginPoint()
                    .setMaxQ(1)
                    .setMinQ(-1)
                    .setP(2)
                    .endPoint()
                    .add();
        } else {
            generator.newMinMaxReactiveLimits()
                    .setMaxQ(2)
                    .setMinQ(-2)
                    .add();
        }
        return network;
    }
}

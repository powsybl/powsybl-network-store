/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.integration;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.extensions.*;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.entsoe.util.*;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.test.*;
import com.powsybl.math.graph.TraverseResult;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import com.powsybl.network.store.client.RestClient;
import com.powsybl.network.store.client.RestClientImpl;
import com.powsybl.network.store.iidm.impl.ConfiguredBusImpl;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;
import com.powsybl.network.store.iidm.impl.NetworkFactoryImpl;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.iidm.impl.extensions.CgmesSshMetadataImpl;
import com.powsybl.network.store.iidm.impl.extensions.CgmesSvMetadataImpl;
import com.powsybl.network.store.iidm.impl.extensions.CimCharacteristicsImpl;
import com.powsybl.network.store.iidm.impl.extensions.*;
import com.powsybl.network.store.model.BaseVoltageSourceAttribute;
import com.powsybl.network.store.model.CgmesSshMetadataAttributes;
import com.powsybl.network.store.model.CgmesSvMetadataAttributes;
import com.powsybl.network.store.model.CimCharacteristicsAttributes;
import com.powsybl.network.store.server.NetworkStoreApplication;
import com.powsybl.ucte.converter.UcteImporter;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextHierarchy({
    @ContextConfiguration(classes = {NetworkStoreApplication.class, NetworkStoreService.class})
})
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class NetworkStoreIT {

    @DynamicPropertySource
    static void makeTestDbSuffix(DynamicPropertyRegistry registry) {
        UUID uuid = UUID.randomUUID();
        registry.add("testDbSuffix", () -> uuid);
    }

    public static final double ESP = 0.000001;
    @LocalServerPort
    private int randomServerPort;

    private String getBaseUrl() {
        return "http://localhost:" + randomServerPort + "/";
    }

    private NetworkStoreService createNetworkStoreService() {
        return createNetworkStoreService(null);
    }

    private NetworkStoreService createNetworkStoreService(RestClientMetrics metrics) {
        RestClient restClient = new RestClientImpl(getBaseUrl());
        if (metrics != null) {
            restClient = new TestRestClient(restClient, metrics);
        }
        return new NetworkStoreService(restClient, PreloadingStrategy.NONE);
    }

    @Test
    public void test() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            // import new network in the store
            assertTrue(service.getNetworkIds().isEmpty());
            Network network = service.importNetwork(new ResourceDataSource("test", new ResourceSet("/", "test.xiidm")));
            service.flush(network);

            assertEquals(1, service.getNetworkIds().size());

            testNetwork(network);
        }
    }

    private static void testNetwork(Network network) {
        assertEquals(false, network.isFictitious());
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
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkTest1Factory.create(service.getNetworkFactory(), "1");
            service.flush(network);

            assertEquals("n1_network", network.getId());

            assertEquals(1, network.getGeneratorCount());
            assertEquals("n1_generator1", network.getGeneratorStream().findFirst().orElseThrow(AssertionError::new).getId());
            assertNotNull(network.getGenerator("n1_generator1"));
            assertEquals(5, network.getGenerator("n1_generator1").getTerminal().getNodeBreakerView().getNode());

            assertEquals(1, network.getLoadCount());
            assertEquals("n1_load1", network.getLoadStream().findFirst().orElseThrow(AssertionError::new).getId());
            assertNotNull(network.getLoad("n1_load1"));
            assertEquals(2, network.getLoad("n1_load1").getTerminal().getNodeBreakerView().getNode());

            // try to emulate voltage level diagram generation use case

            for (Substation s : network.getSubstations()) {
                assertEquals("n1_substation1", s.getId());
                for (VoltageLevel vl : s.getVoltageLevels()) {
                    assertEquals("n1_voltageLevel1", vl.getId());
                    vl.visitEquipments(new DefaultTopologyVisitor() {
                        @Override
                        public void visitBusbarSection(BusbarSection section) {
                            assertTrue(section.getId().equals("n1_voltageLevel1BusbarSection1") || section.getId().equals("n1_voltageLevel1BusbarSection2"));
                        }

                        @Override
                        public void visitLoad(Load load) {
                            assertEquals("n1_load1", load.getId());
                        }

                        @Override
                        public void visitGenerator(Generator generator) {
                            assertEquals("n1_generator1", generator.getId());
                        }
                    });
                }
            }

            List<Bus> buses = network.getVoltageLevel("n1_voltageLevel1").getBusView().getBusStream().collect(Collectors.toList());
            assertEquals(1, buses.size());
            assertEquals("n1_voltageLevel1_0", buses.get(0).getId());
            assertEquals("n1_voltageLevel1_0", buses.get(0).getId());
            assertEquals("n1_voltageLevel1_0", buses.get(0).getName());
            List<BusbarSection> busbarSections = new ArrayList<>();
            List<Generator> generators = new ArrayList<>();
            List<Load> loads = new ArrayList<>();
            buses.get(0).visitConnectedEquipments(new DefaultTopologyVisitor() {
                @Override
                public void visitBusbarSection(BusbarSection section) {
                    busbarSections.add(section);
                }

                @Override
                public void visitLoad(Load load) {
                    loads.add(load);
                }

                @Override
                public void visitGenerator(Generator generator) {
                    generators.add(generator);
                }
            });
            assertEquals(2, busbarSections.size());
            assertEquals(1, generators.size());
            assertEquals(1, loads.size());
            List<Terminal> connectedTerminals = StreamSupport.stream(buses.get(0).getConnectedTerminals().spliterator(), false)
                .collect(Collectors.toList());
            assertEquals(4, connectedTerminals.size());

            assertNotNull(network.getGenerator("n1_generator1").getTerminal().getBusView().getBus());
            assertEquals("n1_voltageLevel1_0", buses.get(0).getId());

            VoltageLevel voltageLevel1 = network.getVoltageLevel("n1_voltageLevel1");
            assertEquals(6, voltageLevel1.getNodeBreakerView().getMaximumNodeIndex());
            assertArrayEquals(new int[] {5, 2, 0, 1, 3, 6}, voltageLevel1.getNodeBreakerView().getNodes());
            assertNotNull(voltageLevel1.getNodeBreakerView().getTerminal(2));
            assertNull(voltageLevel1.getNodeBreakerView().getTerminal(4));
            List<Integer> traversedNodes = new ArrayList<>();
            voltageLevel1.getNodeBreakerView().traverse(2, (node1, sw, node2) -> {
                traversedNodes.add(node1);
                return TraverseResult.CONTINUE;
            });
            assertEquals(Arrays.asList(2, 3, 0, 1, 6), traversedNodes);
        }
    }

    @Test
    public void svcTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("networkTestCase", readNetwork.getId());

            assertEquals(1, readNetwork.getStaticVarCompensatorCount());

            Stream<StaticVarCompensator> svcs = readNetwork.getStaticVarCompensatorStream();
            StaticVarCompensator svc = svcs.findFirst().get();
            assertFalse(svc.isFictitious());
            assertEquals(0.0002, svc.getBmin(), 0.00001);
            assertEquals(0.0008, svc.getBmax(), 0.00001);
            assertEquals(StaticVarCompensator.RegulationMode.VOLTAGE, svc.getRegulationMode());
            assertEquals(390, svc.getVoltageSetPoint(), 0.1);
            assertEquals(200, svc.getReactivePowerSetPoint(), 0.1);
            assertEquals(435, svc.getTerminal().getP(), 0.1);
            assertEquals(315, svc.getTerminal().getQ(), 0.1);

            svc.setFictitious(true);
            svc.setBmin(0.5);
            svc.setBmax(0.7);
            svc.setRegulationMode(StaticVarCompensator.RegulationMode.REACTIVE_POWER);
            svc.setVoltageSetPoint(400);
            svc.setReactivePowerSetPoint(220);
            svc.getTerminal().setP(450);
            svc.getTerminal().setQ(300);

            service.flush(readNetwork);  // flush the network
        }

        // reload modified network
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            StaticVarCompensator svc = readNetwork.getStaticVarCompensatorStream().findFirst().get();
            assertNotNull(svc);

            assertTrue(svc.isFictitious());
            assertEquals(0.5, svc.getBmin(), 0.00001);
            assertEquals(0.7, svc.getBmax(), 0.00001);
            assertEquals(StaticVarCompensator.RegulationMode.REACTIVE_POWER, svc.getRegulationMode());
            assertEquals(400, svc.getVoltageSetPoint(), 0.1);
            assertEquals(220, svc.getReactivePowerSetPoint(), 0.1);
            assertEquals(450, svc.getTerminal().getP(), 0.1);
            assertEquals(300, svc.getTerminal().getQ(), 0.1);
        }
    }

    @Test
    public void testSvcRemove() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(1, readNetwork.getStaticVarCompensatorCount());
            readNetwork.getStaticVarCompensator("SVC2").remove();
            assertEquals(0, readNetwork.getStaticVarCompensatorCount());
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(0, readNetwork.getStaticVarCompensatorCount());
        }
    }

    @Test
    public void vscConverterStationTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("networkTestCase", readNetwork.getId());

            assertEquals(2, readNetwork.getVscConverterStationCount());

            Stream<VscConverterStation> vscConverterStationsStream = readNetwork.getVscConverterStationStream();
            VscConverterStation vscConverterStation = vscConverterStationsStream.filter(vsc -> vsc.getId().equals("VSC1")).findFirst().get();
            assertFalse(vscConverterStation.isFictitious());
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
            assertFalse(vscConverterStation2.isFictitious());
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

            vscConverterStation.setLossFactor(26);
            vscConverterStation.setReactivePowerSetpoint(320);
            vscConverterStation.setVoltageRegulatorOn(false);
            vscConverterStation.setVoltageSetpoint(300);
            vscConverterStation.getTerminal().setP(452);
            vscConverterStation.getTerminal().setQ(318);

            vscConverterStation2.setFictitious(true);

            service.flush(readNetwork);  // flush the network
        }

        // reload modified network
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            VscConverterStation vscConverterStation = readNetwork.getVscConverterStation("VSC1");
            assertNotNull(vscConverterStation);

            assertEquals(26, vscConverterStation.getLossFactor(), 0.1);
            assertEquals(320, vscConverterStation.getReactivePowerSetpoint(), 0.1);
            assertFalse(vscConverterStation.isVoltageRegulatorOn());
            assertEquals(300, vscConverterStation.getVoltageSetpoint(), 0.1);
            assertEquals(452, vscConverterStation.getTerminal().getP(), 0.1);
            assertEquals(318, vscConverterStation.getTerminal().getQ(), 0.1);

            VscConverterStation vscConverterStation2 = readNetwork.getVscConverterStation("VSC2");
            assertNotNull(vscConverterStation2);
            assertTrue(vscConverterStation2.isFictitious());
        }
    }

    @Test
    public void testVscConverterRemove() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(2, readNetwork.getVscConverterStationCount());
            VscConverterStation vsc1 = readNetwork.getVscConverterStation("VSC1");
            VscConverterStation vsc2 = readNetwork.getVscConverterStation("VSC2");
            assertThrows(PowsyblException.class, () -> vsc1.remove())
                .getMessage().contains("Impossible to remove this converter station (still attached to 'HVDC1')");
            assertTrue(assertThrows(PowsyblException.class, () -> vsc2.remove())
                .getMessage().contains("Impossible to remove this converter station (still attached to 'HVDC1')"));
            assertEquals(1, readNetwork.getHvdcLineCount());
            readNetwork.getHvdcLine("HVDC1").remove();
            assertEquals(0, readNetwork.getHvdcLineCount());
            vsc2.remove();
            assertEquals(1, readNetwork.getVscConverterStationCount());
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(0, readNetwork.getHvdcLineCount());
            assertEquals(1, readNetwork.getVscConverterStationCount());
        }
    }

    @Test
    public void lccConverterStationTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("networkTestCase", readNetwork.getId());

            assertEquals(1, readNetwork.getLccConverterStationCount());

            Stream<LccConverterStation> lccConverterStations = readNetwork.getLccConverterStationStream();
            LccConverterStation lccConverterStation = lccConverterStations.findFirst().get();
            assertFalse(lccConverterStation.isFictitious());
            assertEquals("LCC2", lccConverterStation.getId());
            assertEquals(0.5, lccConverterStation.getPowerFactor(), 0.1);
            assertEquals(440, lccConverterStation.getTerminal().getP(), 0.1);
            assertEquals(320, lccConverterStation.getTerminal().getQ(), 0.1);

            lccConverterStation.setFictitious(true);
            lccConverterStation.setPowerFactor(0.5F);
            lccConverterStation.setLossFactor(50);
            lccConverterStation.getTerminal().setP(423);
            lccConverterStation.getTerminal().setQ(330);

            service.flush(readNetwork);  // flush the network
        }

        // reload modified network
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            LccConverterStation lccConverterStation = readNetwork.getLccConverterStationStream().findFirst().get();
            assertNotNull(lccConverterStation);

            assertTrue(lccConverterStation.isFictitious());
            assertEquals(0.5F, lccConverterStation.getPowerFactor(), 0.1);
            assertEquals(50, lccConverterStation.getLossFactor(), 0.1);
            assertEquals(423, lccConverterStation.getTerminal().getP(), 0.1);
            assertEquals(330, lccConverterStation.getTerminal().getQ(), 0.1);
        }
    }

    @Test
    public void testLccConverterRemove() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(1, readNetwork.getLccConverterStationCount());
            readNetwork.getLccConverterStation("LCC2").remove();
            assertEquals(0, readNetwork.getLccConverterStationCount());
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(0, readNetwork.getLccConverterStationCount());
        }
    }

    @Test
    public void testLineRemove() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(1, readNetwork.getLineCount());
            readNetwork.getLine("LINE1").remove();
            assertEquals(0, readNetwork.getLineCount());
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(0, readNetwork.getLineCount());
        }
    }

    @Test
    public void testLoadRemove() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            readNetwork.getSubstation("S1").newVoltageLevel()
                .setId("vl1")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
            readNetwork.getVoltageLevel("vl1").getBusBreakerView().newBus()
                .setId("BUS1")
                .add();
            readNetwork.getVoltageLevel("vl1").newLoad()
                .setId("LD1")
                .setP0(200.0)
                .setQ0(-200.0)
                .setLoadType(LoadType.AUXILIARY)
                .setConnectableBus("BUS1")
                .add();

            assertEquals(2, readNetwork.getLoadCount());
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(2, readNetwork.getLoadCount());
            readNetwork.getLoad("LD1").remove();
            assertEquals(1, readNetwork.getLoadCount());
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(1, readNetwork.getLoadCount());
        }
    }

    @Test
    public void testBusBarSectionRemove() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            readNetwork.getVoltageLevel("VL1").getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setEnsureIdUnicity(true)
                .setFictitious(false)
                .setName("bbs1")
                .setNode(0)
                .add();
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(1, readNetwork.getVoltageLevel("VL1").getNodeBreakerView().getBusbarSectionCount());
            readNetwork.getBusbarSection("BBS1").remove();
            assertEquals(0, readNetwork.getVoltageLevel("VL1").getNodeBreakerView().getBusbarSectionCount());
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(0, readNetwork.getVoltageLevel("VL1").getNodeBreakerView().getBusbarSectionCount());
        }
    }

    @Test
    public void testSubstationRemove() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(3, readNetwork.getSubstationCount());
            assertEquals(6, readNetwork.getVoltageLevelCount());
            assertEquals(2, readNetwork.getVscConverterStationCount());
            assertEquals(2, readNetwork.getDanglingLineCount());
            assertEquals(2, readNetwork.getShuntCompensatorCount());

            assertTrue(assertThrows(PowsyblException.class, () -> readNetwork.getSubstation("S1").remove())
                .getMessage().contains("The substation S1 is still connected to another substation"));

            readNetwork.getHvdcLine("HVDC1").remove();

            assertTrue(assertThrows(PowsyblException.class, () -> readNetwork.getSubstation("S1").remove())
                .getMessage().contains("The substation S1 is still connected to another substation"));

            readNetwork.getLine("LINE1").remove();
            readNetwork.getSubstation("S1").remove();

            assertEquals(2, readNetwork.getSubstationCount());
            assertEquals(5, readNetwork.getVoltageLevelCount());
            assertEquals(1, readNetwork.getVscConverterStationCount());
            assertEquals(0, readNetwork.getDanglingLineCount());
            assertEquals(1, readNetwork.getShuntCompensatorCount());
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(2, readNetwork.getSubstationCount());
            assertEquals(5, readNetwork.getVoltageLevelCount());
            assertEquals(1, readNetwork.getVscConverterStationCount());
            assertEquals(0, readNetwork.getDanglingLineCount());
            assertEquals(1, readNetwork.getShuntCompensatorCount());

            assertEquals(1, readNetwork.getThreeWindingsTransformerCount());

            readNetwork.getSubstation("S2").remove();

            assertEquals(0, readNetwork.getThreeWindingsTransformerCount());
            assertEquals(1, readNetwork.getSubstationCount());
            assertEquals(0, readNetwork.getShuntCompensatorCount());
        }
    }

    @Test
    public void testSubstationUpdate() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            NetworkListener mockedListener = mock(DefaultNetworkListener.class);
            // Add observer changes to current network
            readNetwork.addListener(mockedListener);

            Substation s = readNetwork.getSubstations().iterator().next();
            s.setCountry(Country.BB);
            s.setTso("New TSO");
            s.addGeographicalTag("paris");

            verify(mockedListener, times(1)).onUpdate(s, "country", Country.FR, Country.BB);
            verify(mockedListener, times(1)).onUpdate(s, "tso", null, "New TSO");
            verify(mockedListener, times(1)).onElementAdded(s, "geographicalTags", "paris");

            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            Substation s = readNetwork.getSubstations().iterator().next();
            assertEquals(Country.BB, s.getCountry().get());
            assertEquals("New TSO", s.getTso());
            assertTrue(s.getGeographicalTags().contains("paris"));
        }

    }

    @Test
    public void substationTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.createNetwork("test", "test");

            NetworkListener mockedListener = mock(DefaultNetworkListener.class);
            // Add observer changes to current network
            network.addListener(mockedListener);

            Substation s1 = network.newSubstation()
                .setId("S1")
                .setFictitious(true)
                .setCountry(Country.FR)
                .setTso("TSO_FR")
                .add();

            verify(mockedListener, times(1)).onCreation(s1);

            assertTrue(s1.isFictitious());
            s1.setFictitious(false);
            assertFalse(s1.isFictitious());

            assertEquals(Country.FR, s1.getCountry().get());
            assertEquals("TSO_FR", s1.getTso());

            s1.setCountry(Country.BE);
            s1.setTso("TSO_BE");
            s1.addGeographicalTag("BELGIUM");

            assertEquals(Country.BE, s1.getCountry().get());
            assertEquals("TSO_BE", s1.getTso());

            verify(mockedListener, times(1)).onUpdate(s1, "country", Country.FR, Country.BE);
            verify(mockedListener, times(1)).onUpdate(s1, "tso", "TSO_FR", "TSO_BE");
            verify(mockedListener, times(1)).onElementAdded(s1, "geographicalTags", "BELGIUM");

            s1.setProperty("testProperty", "original");
            verify(mockedListener, times(1)).onElementAdded(s1, "properties[testProperty]", "original");
            s1.setProperty("testProperty", "modified");
            verify(mockedListener, times(1)).onElementReplaced(s1, "properties[testProperty]", "original", "modified");
        }
    }

    @Test
    public void voltageLevelTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.createNetwork("test", "test");

            NetworkListener mockedListener = mock(DefaultNetworkListener.class);
            // Add observer changes to current network
            network.addListener(mockedListener);

            Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .setTso("TSO_FR")
                .add();

            VoltageLevel vl1 = s1.newVoltageLevel()
                .setFictitious(true)
                .setId("vl1")
                .setNominalV(400)
                .setLowVoltageLimit(385)
                .setHighVoltageLimit(415)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();

            verify(mockedListener, times(1)).onCreation(vl1);

            assertTrue(vl1.isFictitious());
            assertEquals(400, vl1.getNominalV(), 0.1);
            assertEquals(385, vl1.getLowVoltageLimit(), 0.1);
            assertEquals(415, vl1.getHighVoltageLimit(), 0.1);

            vl1.setFictitious(false);
            vl1.setNominalV(380);
            vl1.setLowVoltageLimit(370);
            vl1.setHighVoltageLimit(390);

            assertFalse(vl1.isFictitious());
            assertEquals(380, vl1.getNominalV(), 0.1);
            assertEquals(370, vl1.getLowVoltageLimit(), 0.1);
            assertEquals(390, vl1.getHighVoltageLimit(), 0.1);

            verify(mockedListener, times(1)).onUpdate(vl1, "nominalV", 400d, 380d);
            verify(mockedListener, times(1)).onUpdate(vl1, "lowVoltageLimit", 385d, 370d);
            verify(mockedListener, times(1)).onUpdate(vl1, "highVoltageLimit", 415d, 390d);
        }
    }

    @Test
    public void lineTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.createNetwork("test", "test");

            NetworkListener mockedListener = mock(DefaultNetworkListener.class);
            // Add observer changes to current network
            network.addListener(mockedListener);

            Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
            VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
            vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();

            Substation s2 = network.newSubstation()
                .setId("S2")
                .add();
            VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("vl2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
            vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();

            Line line = network.newLine()
                .setId("line")
                .setFictitious(true)
                .setVoltageLevel1("vl1")
                .setBus1("b1")
                .setVoltageLevel2("vl2")
                .setBus2("b2")
                .setR(1)
                .setX(3)
                .setG1(4)
                .setG2(8)
                .setB1(2)
                .setB2(4)
                .add();

            verify(mockedListener, times(1)).onCreation(line);

            assertTrue(line.isFictitious());
            assertEquals(1, line.getR(), 0.1);
            assertEquals(3, line.getX(), 0.1);
            assertEquals(4, line.getG1(), 0.1);
            assertEquals(8, line.getG2(), 0.1);
            assertEquals(2, line.getB1(), 0.1);
            assertEquals(4, line.getB2(), 0.1);

            line.setFictitious(false);
            line.setR(5);
            line.setX(6);
            line.setG1(12);
            line.setG2(24);
            line.setB1(8);
            line.setB2(16);

            assertFalse(line.isFictitious());
            assertEquals(5, line.getR(), 0.1);
            assertEquals(6, line.getX(), 0.1);
            assertEquals(12, line.getG1(), 0.1);
            assertEquals(24, line.getG2(), 0.1);
            assertEquals(8, line.getB1(), 0.1);
            assertEquals(16, line.getB2(), 0.1);

            verify(mockedListener, times(1)).onUpdate(line, "r", 1d, 5d);
            verify(mockedListener, times(1)).onUpdate(line, "x", 3d, 6d);
            verify(mockedListener, times(1)).onUpdate(line, "g1", 4d, 12d);
            verify(mockedListener, times(1)).onUpdate(line, "g2", 8d, 24d);
            verify(mockedListener, times(1)).onUpdate(line, "b1", 2d, 8d);
            verify(mockedListener, times(1)).onUpdate(line, "b2", 4d, 16d);
        }
    }

    @Test
    public void batteryTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.createNetwork("test", "test");

            NetworkListener mockedListener = mock(DefaultNetworkListener.class);
            // Add observer changes to current network
            network.addListener(mockedListener);

            Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
            VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
            vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();

            Battery battery = vl1.newBattery()
                .setFictitious(true)
                .setId("battery")
                .setConnectableBus("b1")
                .setBus("b1")
                .setTargetP(50)
                .setTargetQ(10)
                .setMinP(40)
                .setMaxP(70)
                .add();

            verify(mockedListener, times(1)).onCreation(battery);

            assertTrue(battery.isFictitious());
            assertEquals(50, battery.getTargetP(), 0.1);
            assertEquals(10, battery.getTargetQ(), 0.1);
            assertEquals(40, battery.getMinP(), 0.1);
            assertEquals(70, battery.getMaxP(), 0.1);

            battery.setFictitious(false);
            battery.setTargetP(65);
            battery.setTargetQ(20);

            assertFalse(battery.isFictitious());
            assertEquals(65, battery.getTargetP(), 0.1);
            assertEquals(20, battery.getTargetQ(), 0.1);

            verify(mockedListener, times(1)).onUpdate(battery, "targetP", INITIAL_VARIANT_ID, 50d, 65d);
            verify(mockedListener, times(1)).onUpdate(battery, "targetQ", INITIAL_VARIANT_ID, 10d, 20d);

            battery.setMaxP(90);
            battery.setMinP(50);
            verify(mockedListener, times(1)).onUpdate(battery, "maxP", 70d, 90d);
            verify(mockedListener, times(1)).onUpdate(battery, "minP", 40d, 50d);

            assertTrue(assertThrows(ValidationException.class, () -> battery.setMaxP(60)).getMessage().contains("invalid active power p > maxP"));
        }
    }

    @Test
    public void testBatteryRemove() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(1, readNetwork.getBatteryCount());
            readNetwork.getBattery("battery").remove();
            assertEquals(0, readNetwork.getBatteryCount());
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(0, readNetwork.getBatteryCount());
        }
    }

    @Test
    public void loadTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.createNetwork("test", "test");

            NetworkListener mockedListener = mock(DefaultNetworkListener.class);
            // Add observer changes to current network
            network.addListener(mockedListener);

            Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
            VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
            vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
            Load load = vl1.newLoad()
                .setId("load")
                .setFictitious(true)
                .setConnectableBus("b1")
                .setBus("b1")
                .setP0(50)
                .setQ0(10)
                .add();

            verify(mockedListener, times(1)).onCreation(load);

            assertTrue(load.isFictitious());
            assertEquals(50, load.getP0(), 0.1);
            assertEquals(10, load.getQ0(), 0.1);

            load.setFictitious(false);
            load.setP0(70);
            load.setQ0(20);

            assertFalse(load.isFictitious());
            assertEquals(70, load.getP0(), 0.1);
            assertEquals(20, load.getQ0(), 0.1);

            verify(mockedListener, times(1)).onUpdate(load, "p0", INITIAL_VARIANT_ID, 50d, 70d);
            verify(mockedListener, times(1)).onUpdate(load, "q0", INITIAL_VARIANT_ID, 10d, 20d);
        }
    }

    @Test
    public void danglingLineTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("networkTestCase", readNetwork.getId());

            assertEquals(2, readNetwork.getDanglingLineCount());

            Stream<DanglingLine> danglingLines = readNetwork.getDanglingLineStream();
            DanglingLine danglingLine = danglingLines.findFirst().get();
            assertFalse(danglingLine.isFictitious());
            assertEquals("DL1", danglingLine.getId());
            assertEquals("Dangling line 1", danglingLine.getName());
            assertEquals(533, danglingLine.getP0(), 0.1);
            assertEquals(242, danglingLine.getQ0(), 0.1);
            assertEquals(27, danglingLine.getR(), 0.1);
            assertEquals(44, danglingLine.getX(), 0.1);
            assertEquals(89, danglingLine.getG(), 0.1);
            assertEquals(11, danglingLine.getB(), 0.1);
            assertEquals("UCTE_DL1", danglingLine.getUcteXnodeCode());
            assertEquals(100, danglingLine.getGeneration().getTargetP(), 0.1);
            assertEquals(200, danglingLine.getGeneration().getTargetQ(), 0.1);
            assertEquals(300, danglingLine.getGeneration().getTargetV(), 0.1);
            assertEquals(10, danglingLine.getGeneration().getMinP(), 0.1);
            assertEquals(500, danglingLine.getGeneration().getMaxP(), 0.1);
            assertTrue(danglingLine.getGeneration().isVoltageRegulationOn());
            assertEquals(ReactiveLimitsKind.MIN_MAX, danglingLine.getGeneration().getReactiveLimits().getKind());
            assertEquals(200, ((MinMaxReactiveLimits) danglingLine.getGeneration().getReactiveLimits()).getMinQ(), 0.1);
            assertEquals(800, ((MinMaxReactiveLimits) danglingLine.getGeneration().getReactiveLimits()).getMaxQ(), 0.1);
            MinMaxReactiveLimits minMaxLimits = danglingLine.getGeneration().getReactiveLimits(MinMaxReactiveLimits.class);
            assertEquals(200, minMaxLimits.getMinQ(), 0.1);
            assertEquals(800, minMaxLimits.getMaxQ(), 0.1);

            CurrentLimits currentLimits = danglingLine.getCurrentLimits().orElseThrow();
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

            NetworkListener mockedListener = mock(DefaultNetworkListener.class);
            // Add observer changes to current network
            readNetwork.addListener(mockedListener);

            danglingLine.setR(25);
            danglingLine.setX(48);
            danglingLine.setG(83);
            danglingLine.setB(15);
            danglingLine.setP0(520);
            danglingLine.setQ0(250);
            danglingLine.getTerminal().setP(60);
            danglingLine.getTerminal().setQ(90);
            danglingLine.getGeneration().setMinP(20);
            danglingLine.getGeneration().setMaxP(900);
            danglingLine.getGeneration().setTargetP(300);
            danglingLine.getGeneration().setTargetV(350);
            danglingLine.getGeneration().setTargetQ(1100);
            danglingLine.getGeneration().setVoltageRegulationOn(false);

            // Check update notification
            verify(mockedListener, times(1)).onUpdate(danglingLine, "r", 27d, 25d);
            verify(mockedListener, times(1)).onUpdate(danglingLine, "x", 44d, 48d);
            verify(mockedListener, times(1)).onUpdate(danglingLine, "g", 89d, 83d);
            verify(mockedListener, times(1)).onUpdate(danglingLine, "b", 11d, 15d);
            verify(mockedListener, times(1)).onUpdate(danglingLine, "p0", INITIAL_VARIANT_ID, 533d, 520d);
            verify(mockedListener, times(1)).onUpdate(danglingLine, "q0", INITIAL_VARIANT_ID, 242d, 250d);
            verify(mockedListener, times(1)).onUpdate(danglingLine, "minP", 10d, 20d);
            verify(mockedListener, times(1)).onUpdate(danglingLine, "maxP", 500d, 900d);
            verify(mockedListener, times(1)).onUpdate(danglingLine, "targetP", INITIAL_VARIANT_ID, 100d, 300d);
            verify(mockedListener, times(1)).onUpdate(danglingLine, "targetQ", INITIAL_VARIANT_ID, 200d, 1100d);
            verify(mockedListener, times(1)).onUpdate(danglingLine, "targetV", INITIAL_VARIANT_ID, 300d, 350d);
            verify(mockedListener, times(1)).onUpdate(danglingLine, "voltageRegulationOn", true, false);

            readNetwork.removeListener(mockedListener);

            danglingLine.getGeneration().newReactiveCapabilityCurve().beginPoint()
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

            DanglingLine danglingLine2 = readNetwork.getDanglingLineStream().skip(1).findFirst().get();
            assertFalse(danglingLine2.isFictitious());
            assertEquals("DL2", danglingLine2.getId());
            assertEquals(ReactiveLimitsKind.MIN_MAX, danglingLine2.getGeneration().getReactiveLimits().getKind());

            danglingLine2.setFictitious(true);
            danglingLine2.setR(50);
            danglingLine2.getGeneration().newReactiveCapabilityCurve().beginPoint()
                .setP(25)
                .setMinQ(7)
                .setMaxQ(13)
                .endPoint()
                .beginPoint()
                .setP(10)
                .setMinQ(-10)
                .setMaxQ(1)
                .endPoint()
                .add();

            service.flush(readNetwork);  // flush the network
        }

        // reload modified network
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            DanglingLine danglingLine = readNetwork.getDanglingLineStream().findFirst().get();

            assertEquals(520, danglingLine.getP0(), 0.1);
            assertEquals(250, danglingLine.getQ0(), 0.1);
            assertEquals(25, danglingLine.getR(), 0.1);
            assertEquals(48, danglingLine.getX(), 0.1);
            assertEquals(83, danglingLine.getG(), 0.1);
            assertEquals(15, danglingLine.getB(), 0.1);
            assertEquals(20, danglingLine.getGeneration().getMinP(), 0.1);
            assertEquals(900, danglingLine.getGeneration().getMaxP(), 0.1);
            assertEquals(300, danglingLine.getGeneration().getTargetP(), 0.1);
            assertEquals(350, danglingLine.getGeneration().getTargetV(), 0.1);
            assertEquals(1100, danglingLine.getGeneration().getTargetQ(), 0.1);
            assertFalse(danglingLine.getGeneration().isVoltageRegulationOn());

            assertEquals(ReactiveLimitsKind.CURVE, danglingLine.getGeneration().getReactiveLimits().getKind());
            assertEquals(2, ((ReactiveCapabilityCurve) danglingLine.getGeneration().getReactiveLimits()).getPointCount());
            ReactiveCapabilityCurve curveLimits = danglingLine.getGeneration().getReactiveLimits(ReactiveCapabilityCurve.class);
            assertEquals(2, curveLimits.getPointCount());

            DanglingLine danglingLine2 = readNetwork.getDanglingLineStream().skip(1).findFirst().get();
            assertTrue(danglingLine2.isFictitious());
            assertEquals("DL2", danglingLine2.getId());
            assertEquals(ReactiveLimitsKind.CURVE, danglingLine2.getGeneration().getReactiveLimits().getKind());
            assertEquals(2, ((ReactiveCapabilityCurve) danglingLine2.getGeneration().getReactiveLimits()).getPointCount());
            ReactiveCapabilityCurve curveLimits2 = danglingLine2.getGeneration().getReactiveLimits(ReactiveCapabilityCurve.class);
            assertEquals(2, curveLimits2.getPointCount());
        }
    }

    @Test
    public void hvdcLineTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("networkTestCase", readNetwork.getId());

            assertEquals(1, readNetwork.getHvdcLineCount());

            Stream<HvdcLine> hvdcLines = readNetwork.getHvdcLineStream();
            HvdcLine hvdcLine = hvdcLines.findFirst().get();
            assertFalse(hvdcLine.isFictitious());
            assertEquals(256, hvdcLine.getR(), 0.1);
            assertEquals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, hvdcLine.getConvertersMode());
            assertEquals(330, hvdcLine.getActivePowerSetpoint(), 0.1);
            assertEquals(335, hvdcLine.getNominalV(), 0.1);
            assertEquals(390, hvdcLine.getMaxP(), 0.1);
            assertEquals("VSC1", hvdcLine.getConverterStation1().getId());
            assertEquals("VSC2", hvdcLine.getConverterStation2().getId());
            assertEquals("HVDC1", hvdcLine.getConverterStation1().getHvdcLine().getId());
            assertEquals("HVDC1", hvdcLine.getConverterStation2().getHvdcLine().getId());

            hvdcLine.setFictitious(true);
            hvdcLine.setR(240);
            hvdcLine.setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER);
            hvdcLine.setActivePowerSetpoint(350);
            hvdcLine.setNominalV(360);
            hvdcLine.setMaxP(370);

            service.flush(readNetwork);  // flush the network
        }

        // reload modified network
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            HvdcLine hvdcLine = readNetwork.getHvdcLineStream().findFirst().get();

            assertTrue(hvdcLine.isFictitious());
            assertEquals(240, hvdcLine.getR(), 0.1);
            assertEquals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, hvdcLine.getConvertersMode());
            assertEquals(350, hvdcLine.getActivePowerSetpoint(), 0.1);
            assertEquals(360, hvdcLine.getNominalV(), 0.1);
            assertEquals(370, hvdcLine.getMaxP(), 0.1);
        }
    }

    @Test
    public void testHvdcLineRemove() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(1, readNetwork.getHvdcLineCount());
            readNetwork.getHvdcLine("HVDC1").remove();
            readNetwork.newHvdcLine()
                .setName("HVDC1")
                .setId("HVDC1")
                .setR(27)
                .setActivePowerSetpoint(350.0)
                .setMaxP(400.0)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setNominalV(220)
                .setConverterStationId1("VSC1")
                .setConverterStationId2("VSC2")
                .add();
            readNetwork.newHvdcLine()
                .setName("HVDC2")
                .setId("HVDC2")
                .setR(27)
                .setActivePowerSetpoint(350.0)
                .setMaxP(400.0)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setNominalV(220)
                .setConverterStationId1("VSC1")
                .setConverterStationId2("VSC2")
                .add();
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(2, readNetwork.getHvdcLineCount());
            readNetwork.getHvdcLine("HVDC2").remove();

            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(1, readNetwork.getHvdcLineCount());
            assertNotNull(readNetwork.getHvdcLine("HVDC1"));
        }
    }

    @Test
    public void threeWindingsTransformerTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("networkTestCase", readNetwork.getId());

            assertEquals(1, readNetwork.getThreeWindingsTransformerCount());

            Stream<ThreeWindingsTransformer> threeWindingsTransformerStream = readNetwork.getThreeWindingsTransformerStream();
            ThreeWindingsTransformer threeWindingsTransformer = threeWindingsTransformerStream.findFirst().get();
            assertFalse(threeWindingsTransformer.isFictitious());
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

            assertEquals(25, threeWindingsTransformer.getLeg1().getCurrentLimits().orElseThrow().getPermanentLimit(), .0001);

            threeWindingsTransformer.setFictitious(true);
            threeWindingsTransformer.getLeg1().getTerminal().setP(1000.);
            threeWindingsTransformer.getLeg2().getTerminal().setQ(2000.);
            threeWindingsTransformer.getLeg3().getTerminal().setP(3000.);

            service.flush(readNetwork);  // flush the network
        }

        // reload modified network
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            ThreeWindingsTransformer transformer = readNetwork.getThreeWindingsTransformer("TWT1");
            assertNotNull(transformer);

            assertTrue(transformer.isFictitious());
            assertEquals(1000., transformer.getLeg1().getTerminal().getP(), 0.);
            assertEquals(2000., transformer.getLeg2().getTerminal().getQ(), 0.);
            assertEquals(3000., transformer.getLeg3().getTerminal().getP(), 0.);
        }
    }

    @Test
    public void testThreeWindingsTransformerRemove() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(1, readNetwork.getThreeWindingsTransformerCount());
            readNetwork.getThreeWindingsTransformer("TWT1").remove();
            assertEquals(0, readNetwork.getThreeWindingsTransformerCount());
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(0, readNetwork.getThreeWindingsTransformerCount());
        }
    }

    @Test
    public void twoWindingsTransformerTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("networkTestCase", readNetwork.getId());

            assertEquals(1, readNetwork.getTwoWindingsTransformerCount());

            Stream<TwoWindingsTransformer> twoWindingsTransformerStream = readNetwork.getTwoWindingsTransformerStream();
            TwoWindingsTransformer twoWindingsTransformer = twoWindingsTransformerStream.findFirst().get();
            assertFalse(twoWindingsTransformer.isFictitious());
            assertEquals(250, twoWindingsTransformer.getR(), 0.1);
            assertEquals(100, twoWindingsTransformer.getX(), 0.1);
            assertEquals(52, twoWindingsTransformer.getG(), 0.1);
            assertEquals(12, twoWindingsTransformer.getB(), 0.1);
            assertEquals(65, twoWindingsTransformer.getRatedU1(), 0.1);
            assertEquals(90, twoWindingsTransformer.getRatedU2(), 0.1);
            assertEquals(50, twoWindingsTransformer.getRatedS(), 0.1);

            assertEquals(375, twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.ONE).getP(), 0.1);
            assertEquals(225, twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.TWO).getP(), 0.1);

            assertEquals(48, twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.ONE).getQ(), 0.1);
            assertEquals(28, twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.TWO).getQ(), 0.1);

            assertEquals(2, twoWindingsTransformer.getTerminals().size());
            assertTrue(twoWindingsTransformer.getTerminals().contains(twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.ONE)));
            assertTrue(twoWindingsTransformer.getTerminals().contains(twoWindingsTransformer.getTerminal(TwoWindingsTransformer.Side.TWO)));

            service.flush(readNetwork);  // flush the network
        }

        // reload modified network
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            TwoWindingsTransformer twoWindingsTransformer = readNetwork.getTwoWindingsTransformer("TwoWT1");
            assertNotNull(twoWindingsTransformer);

            NetworkListener mockedListener = mock(DefaultNetworkListener.class);
            // Add observer changes to current network
            readNetwork.addListener(mockedListener);

            twoWindingsTransformer.setFictitious(true);
            twoWindingsTransformer.setR(280);
            twoWindingsTransformer.setX(130);
            twoWindingsTransformer.setG(82);
            twoWindingsTransformer.setB(42);
            twoWindingsTransformer.setRatedU1(95);
            twoWindingsTransformer.setRatedU2(120);
            twoWindingsTransformer.setRatedS(100);

            assertTrue(twoWindingsTransformer.isFictitious());
            assertEquals(280, twoWindingsTransformer.getR(), 0.1);
            assertEquals(130, twoWindingsTransformer.getX(), 0.1);
            assertEquals(82, twoWindingsTransformer.getG(), 0.1);
            assertEquals(42, twoWindingsTransformer.getB(), 0.1);
            assertEquals(95, twoWindingsTransformer.getRatedU1(), 0.1);
            assertEquals(120, twoWindingsTransformer.getRatedU2(), 0.1);
            assertEquals(100, twoWindingsTransformer.getRatedS(), 0.1);

            verify(mockedListener, times(1)).onUpdate(twoWindingsTransformer, "r", 250d, 280d);
            verify(mockedListener, times(1)).onUpdate(twoWindingsTransformer, "x", 100d, 130d);
            verify(mockedListener, times(1)).onUpdate(twoWindingsTransformer, "g", 52d, 82d);
            verify(mockedListener, times(1)).onUpdate(twoWindingsTransformer, "b", 12d, 42d);
            verify(mockedListener, times(1)).onUpdate(twoWindingsTransformer, "ratedU1", 65d, 95d);
            verify(mockedListener, times(1)).onUpdate(twoWindingsTransformer, "ratedU2", 90d, 120d);
            verify(mockedListener, times(1)).onUpdate(twoWindingsTransformer, "ratedS", 50d, 100d);
            verify(mockedListener, times(1)).onUpdate(twoWindingsTransformer, "fictitious", false, true);

            readNetwork.removeListener(mockedListener);
        }
    }

    @Test
    public void testTwoWindingsTransformerRemove() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(1, readNetwork.getTwoWindingsTransformerCount());
            readNetwork.getTwoWindingsTransformer("TwoWT1").remove();
            assertEquals(0, readNetwork.getTwoWindingsTransformerCount());
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(0, readNetwork.getTwoWindingsTransformerCount());
        }
    }

    @Test
    public void internalConnectionsFromCgmesTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            // import new network in the store
            Network network = service.importNetwork(CgmesConformity1Catalog.miniNodeBreaker().dataSource());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            Map<String, Integer> nbInternalConnectionsPerVL = new HashMap();
            readNetwork.getVoltageLevels().forEach(vl -> nbInternalConnectionsPerVL.put(vl.getId(), vl.getNodeBreakerView().getInternalConnectionCount()));

            assertEquals(9, nbInternalConnectionsPerVL.get("b2707f00-2554-41d2-bde2-7dd80a669e50"), .0001);
            assertEquals(11, nbInternalConnectionsPerVL.get("8d4a8238-5b31-4c16-8692-0265dae5e132"), .0001);
            assertEquals(23, nbInternalConnectionsPerVL.get("0d68ac81-124d-4d21-afa8-6c503feef5b8"), .0001);
            assertEquals(9, nbInternalConnectionsPerVL.get("6f8ef715-bc0a-47d7-a74e-27f17234f590"), .0001);
            assertEquals(29, nbInternalConnectionsPerVL.get("347fb7af-642f-4c60-97d9-c03d440b6a82"), .0001);
            assertEquals(22, nbInternalConnectionsPerVL.get("051b93ae-9c15-4490-8cea-33395298f031"), .0001);
            assertEquals(22, nbInternalConnectionsPerVL.get("5d9d9d87-ce6b-4213-b4ec-d50de9790a59"), .0001);
            assertEquals(16, nbInternalConnectionsPerVL.get("93778e52-3fd5-456d-8b10-987c3e6bc47e"), .0001);
            assertEquals(50, nbInternalConnectionsPerVL.get("a43d15db-44a6-4fda-a525-2402ff43226f"), .0001);
            assertEquals(36, nbInternalConnectionsPerVL.get("cd28a27e-8b17-4f23-b9f5-03b6de15203f"), .0001);

            VoltageLevel.NodeBreakerView.InternalConnection ic = readNetwork.getVoltageLevel("b2707f00-2554-41d2-bde2-7dd80a669e50").getNodeBreakerView().getInternalConnections().iterator().next();
            assertEquals(4, ic.getNode1());
            assertEquals(0, ic.getNode2());
        }
    }

    @Test
    public void cgmesExtensionsTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            // import new network in the store
            Network network = service.importNetwork(CgmesConformity1Catalog.miniNodeBreaker().dataSource());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            CgmesSvMetadata cgmesSvMetadata = readNetwork.getExtensionByName("cgmesSvMetadata");
            CgmesSshMetadata cgmesSshMetadata = readNetwork.getExtensionByName("cgmesSshMetadata");
            CimCharacteristics cimCharacteristics = readNetwork.getExtensionByName("cimCharacteristics");
            assertEquals(573, cgmesSvMetadata.getDescription().length());
            assertTrue(cgmesSvMetadata.getDescription().contains("CGMES Conformity Assessment"));
            assertEquals(4, cgmesSvMetadata.getSvVersion());
            assertEquals("http://A1.de/Planning/ENTSOE/2", cgmesSvMetadata.getModelingAuthoritySet());
            assertEquals(3, cgmesSvMetadata.getDependencies().size());
            assertEquals(573, cgmesSshMetadata.getDescription().length());
            assertTrue(cgmesSshMetadata.getDescription().contains("CGMES Conformity Assessment"));
            assertEquals(4, cgmesSshMetadata.getSshVersion());
            assertEquals("http://A1.de/Planning/ENTSOE/2", cgmesSshMetadata.getModelingAuthoritySet());
            assertEquals(1, cgmesSshMetadata.getDependencies().size());
            assertEquals(CgmesTopologyKind.NODE_BREAKER, cimCharacteristics.getTopologyKind());
            assertEquals(16, cimCharacteristics.getCimVersion());

            cgmesSvMetadata = readNetwork.getExtension(CgmesSvMetadata.class);
            cimCharacteristics = readNetwork.getExtension(CimCharacteristics.class);
            cgmesSshMetadata = readNetwork.getExtension(CgmesSshMetadata.class);
            assertEquals(573, cgmesSvMetadata.getDescription().length());
            assertTrue(cgmesSvMetadata.getDescription().contains("CGMES Conformity Assessment"));
            assertEquals(4, cgmesSvMetadata.getSvVersion());
            assertEquals("http://A1.de/Planning/ENTSOE/2", cgmesSvMetadata.getModelingAuthoritySet());
            assertEquals(3, cgmesSvMetadata.getDependencies().size());
            assertEquals(573, cgmesSshMetadata.getDescription().length());
            assertTrue(cgmesSshMetadata.getDescription().contains("CGMES Conformity Assessment"));
            assertEquals(4, cgmesSshMetadata.getSshVersion());
            assertEquals("http://A1.de/Planning/ENTSOE/2", cgmesSshMetadata.getModelingAuthoritySet());
            assertEquals(1, cgmesSshMetadata.getDependencies().size());
            assertEquals(CgmesTopologyKind.NODE_BREAKER, cimCharacteristics.getTopologyKind());
            assertEquals(16, cimCharacteristics.getCimVersion());

            Collection<Extension<Network>> cgmesExtensions = readNetwork.getExtensions();
            Iterator<Extension<Network>> it = cgmesExtensions.iterator();
            cgmesSvMetadata = (CgmesSvMetadata) it.next();
            cgmesSshMetadata = (CgmesSshMetadata) it.next();
            cimCharacteristics = (CimCharacteristics) it.next();
            assertEquals(573, cgmesSvMetadata.getDescription().length());
            assertTrue(cgmesSvMetadata.getDescription().contains("CGMES Conformity Assessment"));
            assertEquals(4, cgmesSvMetadata.getSvVersion());
            assertEquals("http://A1.de/Planning/ENTSOE/2", cgmesSvMetadata.getModelingAuthoritySet());
            assertEquals(3, cgmesSvMetadata.getDependencies().size());
            assertEquals(573, cgmesSshMetadata.getDescription().length());
            assertTrue(cgmesSshMetadata.getDescription().contains("CGMES Conformity Assessment"));
            assertEquals(4, cgmesSshMetadata.getSshVersion());
            assertEquals("http://A1.de/Planning/ENTSOE/2", cgmesSshMetadata.getModelingAuthoritySet());
            assertEquals(1, cgmesSshMetadata.getDependencies().size());
            assertEquals(CgmesTopologyKind.NODE_BREAKER, cimCharacteristics.getTopologyKind());
            assertEquals(16, cimCharacteristics.getCimVersion());

            CgmesSvMetadataAttributes cgmesSvMetadataAttributes = CgmesSvMetadataAttributes.builder()
                .description("Description")
                .svVersion(6)
                .dependencies(new ArrayList<>())
                .modelingAuthoritySet("modelingAuthoritySet")
                .build();

            ((NetworkImpl) readNetwork).getResource().getAttributes().setCgmesSvMetadata(cgmesSvMetadataAttributes);

            CgmesSshMetadataAttributes cgmesSshMetadataAttributes = CgmesSshMetadataAttributes.builder()
                .description("DescriptionSsh")
                .sshVersion(7)
                .dependencies(new ArrayList<>())
                .modelingAuthoritySet("modelingAuthoritySetSsh")
                .build();

            ((NetworkImpl) readNetwork).getResource().getAttributes().setCgmesSshMetadata(cgmesSshMetadataAttributes);

            CimCharacteristicsAttributes cimCharacteristicsAttributes = CimCharacteristicsAttributes.builder()
                .cimVersion(5)
                .cgmesTopologyKind(CgmesTopologyKind.BUS_BRANCH)
                .build();

            ((NetworkImpl) readNetwork).getResource().getAttributes().setCimCharacteristics(cimCharacteristicsAttributes);
            ((NetworkImpl) readNetwork).updateResource();

            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            CgmesSvMetadata cgmesSvMetadata = readNetwork.getExtensionByName("cgmesSvMetadata");
            CgmesSshMetadata cgmesSshMetadata = readNetwork.getExtensionByName("cgmesSshMetadata");
            CimCharacteristics cimCharacteristics = readNetwork.getExtensionByName("cimCharacteristics");

            assertEquals(CgmesTopologyKind.BUS_BRANCH, cimCharacteristics.getTopologyKind());
            assertEquals(5, cimCharacteristics.getCimVersion());
            assertEquals("Description", cgmesSvMetadata.getDescription());
            assertEquals(6, cgmesSvMetadata.getSvVersion());
            assertEquals("modelingAuthoritySet", cgmesSvMetadata.getModelingAuthoritySet());
            assertEquals(0, cgmesSvMetadata.getDependencies().size());
            assertEquals("DescriptionSsh", cgmesSshMetadata.getDescription());
            assertEquals(7, cgmesSshMetadata.getSshVersion());
            assertEquals("modelingAuthoritySetSsh", cgmesSshMetadata.getModelingAuthoritySet());
            assertEquals(0, cgmesSshMetadata.getDependencies().size());

            cgmesSvMetadata = new CgmesSvMetadataImpl((NetworkImpl) readNetwork,
                "Description2",
                7,
                new ArrayList<>(),
                "modelingAuthoritySet2");

            cgmesSshMetadata = new CgmesSshMetadataImpl((NetworkImpl) readNetwork,
                "DescriptionSsh2",
                8,
                new ArrayList<>(),
                "modelingAuthoritySetSsh2");

            cimCharacteristics = new CimCharacteristicsImpl((NetworkImpl) readNetwork,
                CgmesTopologyKind.NODE_BREAKER,
                6);

            readNetwork.addExtension(CgmesSvMetadata.class, cgmesSvMetadata);
            readNetwork.addExtension(CgmesSshMetadata.class, cgmesSshMetadata);
            readNetwork.addExtension(CimCharacteristics.class, cimCharacteristics);
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            CgmesSvMetadata cgmesSvMetadata = readNetwork.getExtensionByName("cgmesSvMetadata");
            CgmesSshMetadata cgmesSshMetadata = readNetwork.getExtensionByName("cgmesSshMetadata");
            CimCharacteristics cimCharacteristics = readNetwork.getExtensionByName("cimCharacteristics");

            assertEquals(CgmesTopologyKind.NODE_BREAKER, cimCharacteristics.getTopologyKind());
            assertEquals(6, cimCharacteristics.getCimVersion());
            assertEquals("Description2", cgmesSvMetadata.getDescription());
            assertEquals(7, cgmesSvMetadata.getSvVersion());
            assertEquals("modelingAuthoritySet2", cgmesSvMetadata.getModelingAuthoritySet());
            assertEquals(0, cgmesSvMetadata.getDependencies().size());
            assertEquals("DescriptionSsh2", cgmesSshMetadata.getDescription());
            assertEquals(8, cgmesSshMetadata.getSshVersion());
            assertEquals("modelingAuthoritySetSsh2", cgmesSshMetadata.getModelingAuthoritySet());
            assertEquals(0, cgmesSshMetadata.getDependencies().size());
        }
    }

    @Test
    public void aliasesTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            // import new network in the store
            service.importNetwork(CgmesConformity1Catalog.miniNodeBreaker().dataSource());
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());

            NetworkImpl readNetwork = (NetworkImpl) service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow());

            assertEquals(256, readNetwork.getIdByAlias().size());

            TwoWindingsTransformer twoWT = readNetwork.getTwoWindingsTransformer("7fe566b9-6bac-4cd3-8b52-8f46e9ba237d");
            assertEquals("813365c3-5be7-4ef0-a0a7-abd1ae6dc174", readNetwork.getTwoWindingsTransformer("813365c3-5be7-4ef0-a0a7-abd1ae6dc174").getId());
            assertEquals("813365c3-5be7-4ef0-a0a7-abd1ae6dc174", readNetwork.getTwoWindingsTransformer("7fe566b9-6bac-4cd3-8b52-8f46e9ba237d").getId());
            assertEquals("813365c3-5be7-4ef0-a0a7-abd1ae6dc174", readNetwork.getTwoWindingsTransformer("0522ca48-e644-4d3a-9721-22bb0abd1c8b").getId());

            assertEquals("7fe566b9-6bac-4cd3-8b52-8f46e9ba237d", twoWT.getAliasFromType("CGMES.Terminal2").orElseThrow());
            assertEquals("82611054-72b9-4cb0-8621-e418b8962cb1", twoWT.getAliasFromType("CGMES.Terminal1").orElseThrow());
            assertEquals("0522ca48-e644-4d3a-9721-22bb0abd1c8b", twoWT.getAliasFromType("CGMES.RatioTapChanger2").orElseThrow());
            assertEquals(Optional.empty(), twoWT.getAliasFromType("non_existing_type"));

            twoWT.removeAlias("0522ca48-e644-4d3a-9721-22bb0abd1c8b");

            readNetwork.addAlias("7fe566b9-6bac-4cd3-8b52-8f46e9ba237d", "two");
            assertThrows(PowsyblException.class, () -> readNetwork.addAlias("813365c3-5be7-4ef0-a0a7-abd1ae6dc174", "two", false));
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            NetworkImpl readNetwork = (NetworkImpl) service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow());

            assertEquals(255, readNetwork.getIdByAlias().size());

            TwoWindingsTransformer twoWT = readNetwork.getTwoWindingsTransformer("813365c3-5be7-4ef0-a0a7-abd1ae6dc174");
            assertEquals(4, twoWT.getAliases().size());

            assertNull(readNetwork.getTwoWindingsTransformer("0522ca48-e644-4d3a-9721-22bb0abd1c8b"));

            ThreeWindingsTransformer threeWT = readNetwork.getThreeWindingsTransformer("5d38b7ed-73fd-405a-9cdb-78425e003773");
            threeWT.addAlias("alias_without_type");
            threeWT.addAlias("alias_with_type", "typeA");
            service.flush(readNetwork);
        }
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            NetworkImpl readNetwork = (NetworkImpl) service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow());

            assertEquals(257, readNetwork.getIdByAlias().size());

            ThreeWindingsTransformer threeWT = readNetwork.getThreeWindingsTransformer("5d38b7ed-73fd-405a-9cdb-78425e003773");
            assertEquals("5d38b7ed-73fd-405a-9cdb-78425e003773", readNetwork.getThreeWindingsTransformer("alias_without_type").getId());
            assertEquals("5d38b7ed-73fd-405a-9cdb-78425e003773", readNetwork.getThreeWindingsTransformer("alias_with_type").getId());
            assertEquals(Optional.empty(), threeWT.getAliasType("alias_without_type"));
            assertEquals("alias_with_type", threeWT.getAliasFromType("typeA").orElseThrow());
            assertEquals(9, threeWT.getAliases().size());
            threeWT.removeAlias("alias_without_type");
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            NetworkImpl readNetwork = (NetworkImpl) service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow());

            assertEquals(256, readNetwork.getIdByAlias().size());

            ThreeWindingsTransformer threeWT = readNetwork.getThreeWindingsTransformer("5d38b7ed-73fd-405a-9cdb-78425e003773");
            assertEquals(8, threeWT.getAliases().size());
        }
    }

    @Test
    public void connectablesTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = FourSubstationsNodeBreakerFactory.create(service.getNetworkFactory());
            assertEquals(26, network.getConnectableCount());
            assertEquals(26, IterableUtils.size(network.getConnectables()));

            assertEquals(2, network.getConnectableCount(Line.class));
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals(26, readNetwork.getConnectableCount());
            assertEquals(26, IterableUtils.size(readNetwork.getConnectables()));

            assertEquals(2, readNetwork.getConnectableCount(Line.class));
        }

    }

    @Test
    public void moreComplexNodeBreakerTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = FictitiousSwitchFactory.create(service.getNetworkFactory());
            service.flush(network);
        }
    }

    @Test
    public void testPhaseTapChanger() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            service.flush(createTapChangerNetwork(service.getNetworkFactory()));
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

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
            assertEquals(0, phaseTapChanger.getLowTapPosition());
            assertEquals(22, phaseTapChanger.getTargetDeadband(), .0001);
            assertEquals(2, phaseTapChanger.getHighTapPosition());
            assertEquals(0, phaseTapChanger.getTapPosition());
            assertFalse(phaseTapChanger.isRegulating());
            assertEqualsPhaseTapChangerStep(phaseTapChanger.getStep(0), -10, 1.5, 0.5, 1., 0.99, 4.);
            assertEqualsPhaseTapChangerStep(phaseTapChanger.getStep(1), 0, 1.6, 0.6, 1.1, 1., 4.1);
            assertEqualsPhaseTapChangerStep(phaseTapChanger.getStep(2), 10, 1.7, 0.7, 1.2, 1.01, 4.2);
            assertEqualsPhaseTapChangerStep(phaseTapChanger.getCurrentStep(), -10, 1.5, 0.5, 1., 0.99, 4.);

            phaseTapChanger.setLowTapPosition(-2);
            phaseTapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL);
            phaseTapChanger.setRegulationValue(12);
            phaseTapChanger.setRegulating(false);
            phaseTapChanger.setTapPosition(-1);
            phaseTapChanger.setTargetDeadband(13);
            assertEquals(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, phaseTapChanger.getRegulationMode());
            assertEquals(12, phaseTapChanger.getRegulationValue(), .0001);
            assertEquals(-2, phaseTapChanger.getLowTapPosition());
            assertEquals(13, phaseTapChanger.getTargetDeadband(), .0001);
            assertEquals(-1, phaseTapChanger.getTapPosition());
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

            assertEquals(phaseTapChanger.getRegulationTerminal().getP(), twoWindingsTransformer.getTerminal2().getP(), 0);
            assertEquals(phaseTapChanger.getRegulationTerminal().getQ(), twoWindingsTransformer.getTerminal2().getQ(), 0);
            phaseTapChanger.setRegulationTerminal(twoWindingsTransformer.getTerminal1());
            service.flush(readNetwork);
            assertEquals(phaseTapChanger.getRegulationTerminal().getP(), twoWindingsTransformer.getTerminal1().getP(), 0);
            assertEquals(phaseTapChanger.getRegulationTerminal().getQ(), twoWindingsTransformer.getTerminal1().getQ(), 0);

            RatioTapChanger ratioTapChanger = twoWindingsTransformer.getRatioTapChanger();

            assertEquals(3, ratioTapChanger.getStepCount());
            assertEquals(0, ratioTapChanger.getLowTapPosition());
            assertEquals(22, ratioTapChanger.getTargetDeadband(), .0001);
            assertEquals(2, ratioTapChanger.getHighTapPosition());
            assertEquals(0, ratioTapChanger.getTapPosition());
            assertTrue(ratioTapChanger.isRegulating());
            assertEqualsRatioTapChangerStep(ratioTapChanger.getStep(0), 1.5, 0.5, 1., 0.99, 4.);
            assertEqualsRatioTapChangerStep(ratioTapChanger.getStep(1), 1.6, 0.6, 1.1, 1., 4.1);
            assertEqualsRatioTapChangerStep(ratioTapChanger.getStep(2), 1.7, 0.7, 1.2, 1.01, 4.2);
            assertEqualsRatioTapChangerStep(ratioTapChanger.getCurrentStep(), 1.5, 0.5, 1., 0.99, 4.);

            ratioTapChanger.setLowTapPosition(-2);
            ratioTapChanger.setRegulating(false);
            ratioTapChanger.setTapPosition(0);
            ratioTapChanger.setTargetDeadband(13);
            ratioTapChanger.setLoadTapChangingCapabilities(false);
            ratioTapChanger.setTargetV(27);
            assertEquals(-2, ratioTapChanger.getLowTapPosition());
            assertEquals(13, ratioTapChanger.getTargetDeadband(), .0001);
            assertEquals(0, ratioTapChanger.getTapPosition());
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

            assertEquals(ratioTapChanger.getRegulationTerminal().getP(), twoWindingsTransformer.getTerminal2().getP(), 0);
            assertEquals(ratioTapChanger.getRegulationTerminal().getQ(), twoWindingsTransformer.getTerminal2().getQ(), 0);
            ratioTapChanger.setRegulationTerminal(twoWindingsTransformer.getTerminal1());
            service.flush(readNetwork);
            assertEquals(ratioTapChanger.getRegulationTerminal().getP(), twoWindingsTransformer.getTerminal1().getP(), 0);
            assertEquals(ratioTapChanger.getRegulationTerminal().getQ(), twoWindingsTransformer.getTerminal1().getQ(), 0);

            twoWindingsTransformer.getTerminal1().setP(100.);

            service.flush(readNetwork);  // flush the network
        }

        // reload modified network
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            TwoWindingsTransformer transformer = readNetwork.getTwoWindingsTransformer("TWT2");
            assertNotNull(transformer);

            assertEquals(100., transformer.getTerminal1().getP(), 0.);  // P1 must be the modified value
        }
    }

    @Test
    public void testGeneratorMinMaxReactiveLimits() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            service.flush(createGeneratorNetwork(service.getNetworkFactory(), ReactiveLimitsKind.MIN_MAX));
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals("Generator network", readNetwork.getId());

            Generator generator = readNetwork.getGeneratorStream().findFirst().get();
            assertFalse(generator.isFictitious());
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

            generator.setFictitious(true);
            generator.setEnergySource(EnergySource.NUCLEAR);
            generator.setMaxP(1200);
            generator.setMinP(100);
            generator.setRatedS(4);
            generator.setTargetP(1000);
            generator.setTargetQ(300);
            generator.setTargetV(389);
            generator.setVoltageRegulatorOn(true);

            service.flush(readNetwork);  // flush the network
        }

        // reload modified network
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            Generator generator = readNetwork.getGeneratorStream().findFirst().get();
            assertNotNull(generator);
            assertTrue(generator.isFictitious());

            assertEquals(EnergySource.NUCLEAR, generator.getEnergySource());
            assertEquals(1200, generator.getMaxP(), .0001);
            assertEquals(100, generator.getMinP(), .0001);
            assertEquals(4, generator.getRatedS(), .0001);
            assertEquals(1000, generator.getTargetP(), .0001);
            assertEquals(300, generator.getTargetQ(), .0001);
            assertEquals(389, generator.getTargetV(), .0001);
            assertTrue(generator.isVoltageRegulatorOn());

            ReactiveLimits reactiveLimits = generator.getReactiveLimits();

            assertEquals(ReactiveLimitsKind.MIN_MAX, reactiveLimits.getKind());
            MinMaxReactiveLimits minMaxReactiveLimits = (MinMaxReactiveLimits) reactiveLimits;
            assertEquals(2, minMaxReactiveLimits.getMaxQ(), .0001);
            assertEquals(-2, minMaxReactiveLimits.getMinQ(), .0001);
        }
    }

    @Test
    public void testGeneratorCurveReactiveLimits() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            service.flush(createGeneratorNetwork(service.getNetworkFactory(), ReactiveLimitsKind.CURVE));
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals("Generator network", readNetwork.getId());

            Generator generator = readNetwork.getGeneratorStream().findFirst().get();
            assertFalse(generator.isFictitious());
            assertEquals("GEN", generator.getId());

            ReactiveLimits reactiveLimits = generator.getReactiveLimits();

            assertEquals(ReactiveLimitsKind.CURVE, reactiveLimits.getKind());
            ReactiveCapabilityCurve reactiveCapabilityCurve = (ReactiveCapabilityCurve) reactiveLimits;
            assertEquals(2, reactiveCapabilityCurve.getPointCount());
            assertEquals(1, reactiveCapabilityCurve.getMinP(), .0001);
            assertEquals(2, reactiveCapabilityCurve.getMaxP(), .0001);

            Iterator<ReactiveCapabilityCurve.Point> itPoints = reactiveCapabilityCurve.getPoints().stream().sorted(Comparator.comparingDouble(ReactiveCapabilityCurve.Point::getP)).iterator();
            ReactiveCapabilityCurve.Point point = itPoints.next();
            assertEquals(2, point.getMaxQ(), .0001);
            assertEquals(-2, point.getMinQ(), .0001);
            assertEquals(1, point.getP(), .0001);
            point = itPoints.next();
            assertEquals(1, point.getMaxQ(), .0001);
            assertEquals(-1, point.getMinQ(), .0001);
            assertEquals(2, point.getP(), .0001);

            assertEquals(reactiveCapabilityCurve.getPointCount(), generator.getReactiveLimits(ReactiveCapabilityCurve.class).getPointCount());
        }
    }

    @Test
    public void testGeneratorRemove() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = createGeneratorNetwork(service.getNetworkFactory(), ReactiveLimitsKind.MIN_MAX);
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(1, readNetwork.getGeneratorCount());
            readNetwork.getGenerator("GEN").remove();
            assertEquals(0, readNetwork.getGeneratorCount());
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(0, readNetwork.getGeneratorCount());
        }
    }

    @Test
    public void testBusBreakerNetwork() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            service.flush(EurostagTutorialExample1Factory.create(service.getNetworkFactory()));
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            List<Bus> buses = new ArrayList<>();

            readNetwork.getBusBreakerView().getBuses().forEach(buses::add);
            assertEquals(4, buses.size());
            assertEquals(4, readNetwork.getBusBreakerView().getBusStream().count());

            List<Bus> votlageLevelBuses = new ArrayList<>();
            VoltageLevel vlload = readNetwork.getVoltageLevel("VLLOAD");
            vlload.getBusBreakerView().getBuses().forEach(votlageLevelBuses::add);
            assertEquals(1, votlageLevelBuses.size());
            assertEquals("NLOAD", votlageLevelBuses.get(0).getId());
            assertNull(vlload.getBusBreakerView().getBus("NHV2"));
            assertNotNull(vlload.getBusBreakerView().getBus("NLOAD"));

            Load nload = vlload.getLoadStream().findFirst().orElseThrow(IllegalStateException::new);
            assertNotNull(nload.getTerminal().getBusBreakerView().getBus());

            // bus view calculation test
            List<Bus> calculatedBuses = vlload.getBusView().getBusStream().collect(Collectors.toList());
            assertEquals(1, calculatedBuses.size());
            assertEquals("VLLOAD_0", calculatedBuses.get(0).getId());
            assertNotNull(nload.getTerminal().getBusView().getBus());
            assertEquals("VLLOAD_0", nload.getTerminal().getBusView().getBus().getId());

            Bus calculatedBus = calculatedBuses.get(0);
            assertEquals(1, calculatedBus.getLoadStream().count());
            assertEquals(0, calculatedBus.getGeneratorStream().count());
            assertEquals(0, calculatedBus.getLineStream().count());
            assertEquals(1, calculatedBus.getTwoWindingsTransformerStream().count());
            assertEquals(0, calculatedBus.getShuntCompensatorStream().count());
            assertEquals(ComponentConstants.MAIN_NUM, calculatedBus.getConnectedComponent().getNum());
            assertEquals(ComponentConstants.MAIN_NUM, calculatedBus.getSynchronousComponent().getNum());
        }
    }

    @Test
    public void testComponentCalculationNetwork() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.createNetwork("test", "test");
            Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
            VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
            vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();

            Substation s2 = network.newSubstation()
                .setId("S2")
                .add();
            VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("vl2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
            vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
            vl2.getBusBreakerView().newBus()
                .setId("b2b")
                .add();
            Switch s = vl2.getBusBreakerView().newSwitch()
                .setId("s")
                .setBus1("b2")
                .setBus2("b2b")
                .setOpen(false)
                .add();

            Substation s3 = network.newSubstation()
                .setId("S3")
                .add();
            VoltageLevel vl3 = s3.newVoltageLevel()
                .setId("vl3")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
            vl3.getBusBreakerView().newBus()
                .setId("b3")
                .add();
            vl3.newLoad()
                .setId("ld")
                .setConnectableBus("b3")
                .setBus("b3")
                .setP0(50)
                .setQ0(10)
                .add();

            network.newLine()
                .setId("l12")
                .setVoltageLevel1("vl1")
                .setBus1("b1")
                .setVoltageLevel2("vl2")
                .setBus2("b2")
                .setR(1)
                .setX(3)
                .setG1(0)
                .setG2(0)
                .setB1(0)
                .setB2(0)
                .add();

            network.newLine()
                .setId("l23")
                .setVoltageLevel1("vl2")
                .setBus1("b2b")
                .setVoltageLevel2("vl3")
                .setBus2("b3")
                .setR(1)
                .setX(3)
                .setG1(0)
                .setG2(0)
                .setB1(0)
                .setB2(0)
                .add();

            vl1.newGenerator()
                .setId("g")
                .setConnectableBus("b1")
                .setRegulatingTerminal(network.getLine("l12").getTerminal1())
                .setBus("b1")
                .setTargetP(102.56)
                .setTargetV(390)
                .setMinP(0)
                .setMaxP(500)
                .setVoltageRegulatorOn(true)
                .add();

            service.flush(network);

            Map<UUID, String> networkIds = service.getNetworkIds();
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());

            Network network = service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow(AssertionError::new));
            assertEquals(ComponentConstants.MAIN_NUM, network.getGenerator("g").getTerminal().getBusView().getBus().getConnectedComponent().getNum());
            assertEquals(ComponentConstants.MAIN_NUM, network.getGenerator("g").getTerminal().getBusView().getBus().getSynchronousComponent().getNum());
            assertEquals(ComponentConstants.MAIN_NUM, network.getLoad("ld").getTerminal().getBusView().getBus().getConnectedComponent().getNum());
            assertEquals(ComponentConstants.MAIN_NUM, network.getLoad("ld").getTerminal().getBusView().getBus().getSynchronousComponent().getNum());

            network.getSwitch("s").setOpen(true);
            assertEquals(ComponentConstants.MAIN_NUM, network.getGenerator("g").getTerminal().getBusView().getBus().getConnectedComponent().getNum());
            assertEquals(ComponentConstants.MAIN_NUM, network.getGenerator("g").getTerminal().getBusView().getBus().getSynchronousComponent().getNum());
            assertEquals(1, network.getLoad("ld").getTerminal().getBusView().getBus().getConnectedComponent().getNum());
            assertEquals(1, network.getLoad("ld").getTerminal().getBusView().getBus().getSynchronousComponent().getNum());
        }
    }

    @Test
    public void testUcteNetwork() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            service.flush(loadUcteNetwork(service.getNetworkFactory()));
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(1, readNetwork.getDanglingLineCount());
            DanglingLine dl = readNetwork.getDanglingLineStream().findFirst().orElseThrow(AssertionError::new);
            assertEquals("XG__F_21", dl.getUcteXnodeCode());
            Xnode xnode = (Xnode) dl.getExtensionByName("xnode");
            assertEquals("XG__F_21", xnode.getCode());
            assertEquals(1, dl.getExtensions().size());
            Xnode sameXnode = (Xnode) dl.getExtension(Xnode.class);
            assertEquals("XG__F_21", sameXnode.getCode());
            ConnectablePosition connectablePosition = dl.getExtension(ConnectablePosition.class);
            assertNull(connectablePosition);
            ConnectablePosition connectablePosition2 = dl.getExtensionByName("");
            assertNull(connectablePosition2);
            assertEquals(4, readNetwork.getLineCount());
            assertNotNull(readNetwork.getLine("XB__F_21 B_SU1_21 1 + XB__F_21 F_SU1_21 1"));
            assertNotNull(readNetwork.getLine("XB__F_11 B_SU1_11 1 + XB__F_11 F_SU1_11 1"));
            assertNotNull(readNetwork.getLine("F_SU1_12 F_SU2_11 2"));
            assertNotNull(readNetwork.getLine("F_SU1_12 F_SU2_11 1"));
            Line line = readNetwork.getLine("XB__F_21 B_SU1_21 1 + XB__F_21 F_SU1_21 1");
            assertTrue(line.isTieLine());
            assertNotNull(line.getExtension(MergedXnode.class));
            MergedXnode mergedXnode = line.getExtension(MergedXnode.class);
            assertEquals("XB__F_21", mergedXnode.getCode());
            assertEquals("XB__F_21 B_SU1_21 1", mergedXnode.getLine1Name());
            assertEquals("XB__F_21 F_SU1_21 1", mergedXnode.getLine2Name());
            assertNotNull(line.getExtensionByName("mergedXnode"));
            assertEquals(1, line.getExtensions().size());

            Substation s1 = readNetwork.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add();
            s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
            s1.newVoltageLevel()
                .setId("VL2")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

            TieLine tieLine2 = readNetwork.newTieLine()
                .setId("id")
                .setName("name")
                .setVoltageLevel1("VL1")
                .setNode1(1)
                .setVoltageLevel2("VL2")
                .setNode2(2)
                .newHalfLine1()
                .setId("h1")
                .setB1(1)
                .setB2(2)
                .setG1(3)
                .setG2(4)
                .setR(5)
                .setX(6)
                .setXnodeP(7)
                .setXnodeQ(8)
                .add()
                .newHalfLine2()
                .setId("h2")
                .setB1(1.5)
                .setB2(2.5)
                .setG1(3.5)
                .setG2(4.5)
                .setR(5.5)
                .setX(6.5)
                .setXnodeP(7.5)
                .setXnodeQ(8.5)
                .add()
                .setUcteXnodeCode("test")
                .add();
            assertEquals("id", tieLine2.getId());
            assertEquals("test", tieLine2.getUcteXnodeCode());
            assertEquals("name", tieLine2.getName());
            assertEquals(10.5, tieLine2.getR(), 0);
            assertEquals(12.5, tieLine2.getX(), 0);
            assertEquals(7, tieLine2.getG1(), 0);
            assertEquals(8, tieLine2.getG2(), 0);
            assertEquals(3, tieLine2.getB1(), 0);
            assertEquals(4, tieLine2.getB2(), 0);
            assertEquals("h1", tieLine2.getHalf1().getId());
            assertEquals(1.5, tieLine2.getHalf1().getB1(), 0);
            assertEquals(1.5, tieLine2.getHalf1().getB2(), 0);
            assertEquals(3.5, tieLine2.getHalf1().getG1(), 0);
            assertEquals(3.5, tieLine2.getHalf1().getG2(), 0);
            assertEquals(5, tieLine2.getHalf1().getR(), ESP);
            assertEquals(6, tieLine2.getHalf1().getX(), ESP);
            assertEquals("h2", tieLine2.getHalf2().getId());
            assertEquals(2, tieLine2.getHalf2().getB1(), 0);
            assertEquals(2, tieLine2.getHalf2().getB2(), 0);
            assertEquals(4, tieLine2.getHalf2().getG1(), 0);
            assertEquals(4, tieLine2.getHalf2().getG2(), 0);
            assertEquals(5.5, tieLine2.getHalf2().getR(), ESP);
            assertEquals(6.5, tieLine2.getHalf2().getX(), ESP);
            assertEquals("h1", tieLine2.getHalf(Branch.Side.ONE).getId());
            assertEquals("h2", tieLine2.getHalf(Branch.Side.TWO).getId());

            Line regularLine = readNetwork.getLine("F_SU1_12 F_SU2_11 2");
            assertNull(regularLine.getExtension(MergedXnode.class));
            regularLine.addExtension(MergedXnode.class,
                new MergedXnodeImpl(regularLine, 1, 1, 1, 1,
                    1, 1, "", "", ""));
            assertNotNull(regularLine.getExtension(MergedXnode.class));
            assertEquals(1, regularLine.getExtension(MergedXnode.class).getRdp(), .0001);

            tieLine2.getTerminal1().setQ(200.);
            tieLine2.getTerminal2().setP(800.);

            regularLine.getTerminal1().setP(500.);
            regularLine.getTerminal2().setQ(300.);

            Substation s2 = readNetwork.newSubstation()
                .setId("D7_TEST_SUB_EA")
                .setCountry(Country.DE)
                .add();

            assertNull(s2.getExtension(EntsoeArea.class));
            assertNull(s2.getExtensionByName("entsoeArea"));
            s2.addExtension(EntsoeArea.class,
                new EntsoeAreaImpl(s2, EntsoeGeographicalCode.D7));
            assertNotNull(s2.getExtension(EntsoeArea.class));
            assertNotNull(s2.getExtensionByName("entsoeArea"));
            assertEquals(EntsoeGeographicalCode.D7, s2.getExtension(EntsoeArea.class).getCode());

            service.flush(readNetwork);  // flush the network
        }

        // reload modified network
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            Line tieLine = readNetwork.getLine("id");
            assertNotNull(tieLine);
            assertEquals(200., tieLine.getTerminal1().getQ(), 0.);
            assertEquals(800., tieLine.getTerminal2().getP(), 0.);

            Line regularLine = readNetwork.getLine("F_SU1_12 F_SU2_11 2");
            assertNotNull(regularLine);

            assertEquals(500., regularLine.getTerminal1().getP(), 0.);
            assertEquals(300., regularLine.getTerminal2().getQ(), 0.);

            Substation substationTestEntsoeArea = readNetwork.getSubstation("D7_TEST_SUB_EA");
            assertNotNull(substationTestEntsoeArea);
            assertEquals(EntsoeGeographicalCode.D7, substationTestEntsoeArea.getExtension(EntsoeArea.class).getCode());
        }
    }

    @Test
    public void testDanglingLineRemove() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            service.flush(createRemoveDL(service.getNetworkFactory()));
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(1, readNetwork.getDanglingLineCount());
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(1, readNetwork.getDanglingLineCount());
            readNetwork.getDanglingLine("dl1").remove();
            readNetwork.getVoltageLevel("VL1").newDanglingLine()
                .setName("dl1")
                .setId("dl1")
                .setNode(1)
                .setP0(533)
                .setQ0(242)
                .setR(27)
                .setX(44)
                .setG(89)
                .setB(11)
                .add();
            readNetwork.getVoltageLevel("VL1").newDanglingLine()
                .setName("dl2")
                .setId("dl2")
                .setNode(2)
                .setP0(533)
                .setQ0(242)
                .setR(27)
                .setX(44)
                .setG(89)
                .setB(11)
                .add();
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(2, readNetwork.getDanglingLineCount());
            readNetwork.getDanglingLine("dl2").remove();
            assertEquals(1, readNetwork.getDanglingLineCount());
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(1, readNetwork.getDanglingLineCount());
            assertNotNull(readNetwork.getDanglingLine("dl1"));
        }
    }

    @Test
    public void switchesTest() {
        // create network and save it
        try (NetworkStoreService service = createNetworkStoreService()) {
            service.flush(createSwitchesNetwork(service.getNetworkFactory()));
        }

        // load saved network and modify a switch state
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals("Switches network", readNetwork.getId());

            assertEquals(7, readNetwork.getSwitchCount());

            Switch breaker = readNetwork.getSwitch("v1b1");
            assertNotNull(breaker);
            assertFalse(breaker.isFictitious());

            assertEquals(Boolean.FALSE, breaker.isOpen());

            breaker.setFictitious(true);
            breaker.setOpen(true); // open breaker switch

            service.flush(readNetwork);  // flush the network
        }

        // reload modified network
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            Switch breaker = readNetwork.getSwitch("v1b1");
            assertNotNull(breaker);
            assertTrue(breaker.isFictitious());

            assertEquals(Boolean.TRUE, breaker.isOpen());  // the breaker switch must be opened
        }
    }

    @Test
    public void testNodeBreakerVoltageLevelRemove() {
        // create network and save it
        try (NetworkStoreService service = createNetworkStoreService()) {
            service.flush(createSwitchesNetwork(service.getNetworkFactory()));
        }

        // load saved network and modify a switch state
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals("Switches network", readNetwork.getId());

            assertEquals(7, readNetwork.getSwitchCount());

            Switch breaker = readNetwork.getSwitch("v1b1");
            assertNotNull(breaker);
            breaker = readNetwork.getSwitch("v1d1");
            assertNotNull(breaker);

            readNetwork.getVoltageLevel("v1").remove();

            assertEquals(5, readNetwork.getSwitchCount());

            breaker = readNetwork.getSwitch("v1b1");
            assertNull(breaker);
            breaker = readNetwork.getSwitch("v1d1");
            assertNull(breaker);

            service.flush(readNetwork);  // flush the network
        }

        // reload modified network
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            Switch breaker = readNetwork.getSwitch("v1b1");
            assertNull(breaker);
            breaker = readNetwork.getSwitch("v1d1");
            assertNull(breaker);

            assertEquals(5, readNetwork.getSwitchCount());
        }
    }

    @Test
    public void testVoltageLevel() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = EurostagTutorialExample1Factory.createWithMultipleConnectedComponents(service.getNetworkFactory());

            VoltageLevel vl3 = network.getVoltageLevel("VLHV3");
            Iterable<Load> loadsVL3 = vl3.getConnectables(Load.class);
            assertEquals(2, Iterables.size(loadsVL3));
            Iterable<Generator> generatorsVL3 = vl3.getConnectables(Generator.class);
            assertEquals(2, Iterables.size(generatorsVL3));
            Iterable<ShuntCompensator> scsVL3 = vl3.getConnectables(ShuntCompensator.class);
            assertEquals(1, Iterables.size(scsVL3));
            Iterable<Line> linesVL3 = vl3.getConnectables(Line.class);
            assertTrue(Iterables.isEmpty(linesVL3));

            Iterable<DanglingLine> danglingLinesVL3 = vl3.getConnectables(DanglingLine.class);
            assertTrue(Iterables.isEmpty(danglingLinesVL3));

            vl3.getBusBreakerView().newBus()
                .setId("BUS")
                .add();
            vl3.newDanglingLine()
                .setId("DL")
                .setBus("BUS")
                .setR(10.0)
                .setX(1.0)
                .setB(10e-6)
                .setG(10e-5)
                .setP0(50.0)
                .setQ0(30.0)
                .add();
            danglingLinesVL3 = vl3.getConnectables(DanglingLine.class);
            assertEquals(1, Iterables.size(danglingLinesVL3));

            Iterable<StaticVarCompensator> svcsVL3 = vl3.getConnectables(StaticVarCompensator.class);
            assertTrue(Iterables.isEmpty(svcsVL3));
            vl3.newStaticVarCompensator()
                .setId("SVC2")
                .setConnectableBus("BUS")
                .setBus("BUS")
                .setBmin(0.0002)
                .setBmax(0.0008)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetPoint(390)
                .add();
            svcsVL3 = vl3.getConnectables(StaticVarCompensator.class);
            assertEquals(1, Iterables.size(svcsVL3));

            VoltageLevel vl1 = network.getVoltageLevel("VLHV1");
            Iterable<Load> loadsVL1 = vl1.getConnectables(Load.class);
            assertTrue(Iterables.isEmpty(loadsVL1));
            Iterable<Generator> generatorsVL1 = vl1.getConnectables(Generator.class);
            assertTrue(Iterables.isEmpty(generatorsVL1));
            Iterable<ShuntCompensator> scsVL1 = vl1.getConnectables(ShuntCompensator.class);
            assertTrue(Iterables.isEmpty(scsVL1));
            Iterable<Line> linesVL1 = vl1.getConnectables(Line.class);
            assertEquals(2, Iterables.size(linesVL1));
            Iterable<TwoWindingsTransformer> t2wsVL1 = vl1.getConnectables(TwoWindingsTransformer.class);
            assertEquals(1, Iterables.size(t2wsVL1));
            Iterable<Branch> branchesVL1 = vl1.getConnectables(Branch.class);
            assertEquals(3, Iterables.size(branchesVL1));

            VscConverterStation vsc = vl1.newVscConverterStation()
                .setId("VSC1")
                .setName("Converter2")
                .setConnectableBus("NHV1")
                .setLossFactor(1.1f)
                .setReactivePowerSetpoint(123)
                .setVoltageRegulatorOn(false)
                .add();

            vl1.getBusBreakerView().newBus()
                .setId("B1")
                .add();
            vl1.newLccConverterStation()
                .setId("LCC1")
                .setName("Converter1")
                .setConnectableBus("B1")
                .setBus("B1")
                .setLossFactor(1.1f)
                .setPowerFactor(0.5f)
                .add();

            vl1.getBusBreakerView().newBus()
                .setId("B2")
                .add();
            vl1.newLccConverterStation()
                .setId("LCC2")
                .setName("Converter2")
                .setConnectableBus("B2")
                .setBus("B2")
                .setLossFactor(1.1f)
                .setPowerFactor(0.5f)
                .add();

            Iterable<VscConverterStation> vscsVL1 = vl1.getConnectables(VscConverterStation.class);
            assertEquals(1, Iterables.size(vscsVL1));
            Iterable<LccConverterStation> lccsVL1 = vl1.getConnectables(LccConverterStation.class);
            assertEquals(2, Iterables.size(lccsVL1));
            Iterable<HvdcConverterStation> hvdccVL1 = vl1.getConnectables(HvdcConverterStation.class);
            assertEquals(3, Iterables.size(hvdccVL1));

            Network networkT3W = ThreeWindingsTransformerNetworkFactory.create(service.getNetworkFactory());
            VoltageLevel t3wVl1 = networkT3W.getVoltageLevel("VL_132");
            VoltageLevel t3wVl2 = networkT3W.getVoltageLevel("VL_33");
            VoltageLevel t3wVl3 = networkT3W.getVoltageLevel("VL_11");
            Iterable<ThreeWindingsTransformer> t3wsVL1 = t3wVl1.getConnectables(ThreeWindingsTransformer.class);
            assertEquals(1, Iterables.size(t3wsVL1));
            Iterable<ThreeWindingsTransformer> t3wsVL2 = t3wVl2.getConnectables(ThreeWindingsTransformer.class);
            assertEquals(1, Iterables.size(t3wsVL2));
            Iterable<ThreeWindingsTransformer> t3wsVL3 = t3wVl3.getConnectables(ThreeWindingsTransformer.class);
            assertEquals(1, Iterables.size(t3wsVL3));
        }
    }

    @Test
    public void configuredBusTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("networkTestCase", readNetwork.getId());

            assertEquals(13, readNetwork.getBusBreakerView().getBusStream().collect(Collectors.toList()).size());
            assertEquals(2, readNetwork.getBusBreakerView().getBusStream().filter(b -> b instanceof ConfiguredBusImpl).count());
            Bus bus1 = readNetwork.getBusBreakerView().getBus("BUS5");
            Bus bus2 = readNetwork.getBusBreakerView().getBus("BUS6");

            assertNotNull(bus1);
            assertNotNull(bus2);

            assertFalse(bus1.isFictitious());
            assertEquals("VL5", bus1.getVoltageLevel().getId());
            assertTrue(Double.isNaN(bus1.getV()));
            assertTrue(Double.isNaN(bus1.getAngle()));

            assertFalse(bus2.isFictitious());
            assertEquals("VL6", bus2.getVoltageLevel().getId());
            assertTrue(Double.isNaN(bus2.getV()));
            assertTrue(Double.isNaN(bus2.getAngle()));

            bus1.setFictitious(true);
            bus1.setV(0);
            bus1.setAngle(0);

            service.flush(readNetwork);  // flush the network
        }

        // reload modified network
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            Bus bus1 = readNetwork.getBusBreakerView().getBus("BUS5");

            assertTrue(bus1.isFictitious());
            assertEquals(.0, bus1.getV(), .0);
            assertEquals(.0, bus1.getAngle(), .0);
        }
    }

    @Test
    public void testConfiguredBus() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            // import new network in the store
            Network network = service.importNetwork(CgmesConformity1Catalog.smallBusBranch().dataSource());

            Set<String> visitedConnectables = new HashSet<>();
            TopologyVisitor tv = new DefaultTopologyVisitor() {

                @Override
                public void visitBusbarSection(BusbarSection section) {
                    visitedConnectables.add(section.getId());
                }

                @Override
                public void visitLine(Line line, Branch.Side side) {
                    visitedConnectables.add(line.getId());
                }

                @Override
                public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, Branch.Side side) {
                    visitedConnectables.add(transformer.getId());
                }

                @Override
                public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
                    visitedConnectables.add(transformer.getId());
                }

                @Override
                public void visitGenerator(Generator generator) {
                    visitedConnectables.add(generator.getId());
                }

                @Override
                public void visitBattery(Battery battery) {
                    visitedConnectables.add(battery.getId());
                }

                @Override
                public void visitLoad(Load load) {
                    visitedConnectables.add(load.getId());
                }

                @Override
                public void visitShuntCompensator(ShuntCompensator sc) {
                    visitedConnectables.add(sc.getId());
                }

                @Override
                public void visitDanglingLine(DanglingLine danglingLine) {
                    visitedConnectables.add(danglingLine.getId());
                }

                @Override
                public void visitStaticVarCompensator(StaticVarCompensator staticVarCompensator) {
                    visitedConnectables.add(staticVarCompensator.getId());
                }

                @Override
                public void visitHvdcConverterStation(HvdcConverterStation<?> converterStation) {
                    visitedConnectables.add(converterStation.getId());
                }
            };

            Set<String> visitedConnectablesBusView = new HashSet<>();
            Set<String> visitedConnectablesBusBreakerView = new HashSet<>();

            VoltageLevel testVl = network.getVoltageLevel("0483be8b-c766-11e1-8775-005056c00008");
            Bus busFromBusView = testVl.getBusView().getBus("0483be8b-c766-11e1-8775-005056c00008_0");
            busFromBusView.visitConnectedEquipments(tv);
            visitedConnectablesBusView.addAll(visitedConnectables);
            visitedConnectables.clear();
            Bus busFromBusBreakerView = testVl.getBusBreakerView().getBus("044e56a4-c766-11e1-8775-005056c00008");
            busFromBusBreakerView.visitConnectedEquipments(tv);
            visitedConnectablesBusBreakerView.addAll(visitedConnectables);
            visitedConnectables.clear();

            assertEquals(visitedConnectablesBusView, visitedConnectablesBusBreakerView);
            visitedConnectablesBusBreakerView.clear();
            visitedConnectablesBusView.clear();

            testVl = network.getVoltageLevel("04728079-c766-11e1-8775-005056c00008");
            busFromBusView = testVl.getBusView().getBus("04728079-c766-11e1-8775-005056c00008_0");
            busFromBusView.visitConnectedEquipments(tv);
            visitedConnectablesBusView.addAll(visitedConnectables);
            visitedConnectables.clear();
            busFromBusBreakerView = testVl.getBusBreakerView().getBus("04689567-c766-11e1-8775-005056c00008");
            busFromBusBreakerView.visitConnectedEquipments(tv);
            visitedConnectablesBusBreakerView.addAll(visitedConnectables);
            visitedConnectables.clear();

            assertEquals(visitedConnectablesBusView, visitedConnectablesBusBreakerView);
            visitedConnectablesBusBreakerView.clear();
            visitedConnectablesBusView.clear();

            StaticVarCompensator svc = network.getVoltageLevel("04664b78-c766-11e1-8775-005056c00008").newStaticVarCompensator()
                .setId("SVC1")
                .setName("SVC1")
                .setConnectableBus("04878f11-c766-11e1-8775-005056c00008")
                .setRegulationMode(StaticVarCompensator.RegulationMode.OFF)
                .setReactivePowerSetPoint(5.2f)
                .setBmax(0.5f)
                .setBmin(0.1f)
                .add();
            svc.getTerminal().connect();

            LccConverterStation lcc = network.getVoltageLevel("04664b78-c766-11e1-8775-005056c00008").newLccConverterStation()
                .setId("LCC1")
                .setName("LCC1")
                .setPowerFactor(0.2f)
                .setLossFactor(0.5f)
                .setConnectableBus("04878f11-c766-11e1-8775-005056c00008")
                .add();
            lcc.getTerminal().connect();

            VscConverterStation vsc = network.getVoltageLevel("04664b78-c766-11e1-8775-005056c00008").newVscConverterStation()
                .setId("VSC1")
                .setName("VSC1")
                .setVoltageRegulatorOn(false)
                .setReactivePowerSetpoint(4.5f)
                .setLossFactor(0.3f)
                .setConnectableBus("04878f11-c766-11e1-8775-005056c00008")
                .add();
            vsc.getTerminal().connect();

            testVl = network.getVoltageLevel("04664b78-c766-11e1-8775-005056c00008");
            busFromBusView = testVl.getBusView().getBus("04664b78-c766-11e1-8775-005056c00008_0");
            busFromBusView.visitConnectedEquipments(tv);
            visitedConnectablesBusView.addAll(visitedConnectables);
            visitedConnectables.clear();
            busFromBusBreakerView = testVl.getBusBreakerView().getBus("04878f11-c766-11e1-8775-005056c00008");
            busFromBusBreakerView.visitConnectedEquipments(tv);
            visitedConnectablesBusBreakerView.addAll(visitedConnectables);
            visitedConnectables.clear();

            assertEquals(visitedConnectablesBusView, visitedConnectablesBusBreakerView);
            visitedConnectablesBusBreakerView.clear();
            visitedConnectablesBusView.clear();
        }
    }

    @Test
    public void testBusBreakerVoltageLevelRemove() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            // import new network in the store
            Network network = service.importNetwork(CgmesConformity1Catalog.smallBusBranch().dataSource());
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            VoltageLevel vl = readNetwork.getVoltageLevel("0483be8b-c766-11e1-8775-005056c00008");
            List<Bus> busesInNetwork = readNetwork.getBusBreakerView().getBusStream().collect(Collectors.toList());
            assertEquals(115, busesInNetwork.size());
            List<Bus> busesInVL = vl.getBusBreakerView().getBusStream().collect(Collectors.toList());

            readNetwork.getLine("0460cd36-c766-11e1-8775-005056c00008").remove();
            readNetwork.getLine("04631724-c766-11e1-8775-005056c00008").remove();
            readNetwork.getLine("0457a574-c766-11e1-8775-005056c00008").remove();
            readNetwork.getLine("04569409-c766-11e1-8775-005056c00008").remove();

            service.flush(readNetwork);

            vl.remove();

            busesInNetwork = readNetwork.getBusBreakerView().getBusStream().collect(Collectors.toList());
            assertEquals(114, busesInNetwork.size());

            service.flush(readNetwork);
        }

        // reload modified network
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            List<Bus> busesInNetwork = readNetwork.getBusBreakerView().getBusStream().collect(Collectors.toList());
            assertEquals(114, busesInNetwork.size());
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
        VoltageLevel vl2 = s1.newVoltageLevel()
            .setId("VL2")
            .setNominalV(400f)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
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
            .setRatedS(100)
            .add();
        twt.newPhaseTapChanger()
            .setLowTapPosition(0)
            .setTapPosition(0)
            .setRegulating(false)
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
            .setLowTapPosition(0)
            .setTapPosition(0)
            .setRegulating(true)
            .setTargetV(200)
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
        Generator generator = vl1.newGenerator()
            .setId("GEN")
            .setNode(1)
            .setMaxP(20)
            .setMinP(-20)
            .setVoltageRegulatorOn(true)
            .setTargetP(100)
            .setTargetV(200)
            .setTargetQ(100)
            .add();
        if (kind.equals(ReactiveLimitsKind.CURVE)) {
            generator.newReactiveCapabilityCurve()
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
        } else {
            generator.newMinMaxReactiveLimits()
                .setMaxQ(2)
                .setMinQ(-2)
                .add();
        }
        return network;
    }

    private Network createRemoveDL(NetworkFactory networkFactory) {
        Network network = networkFactory.createNetwork("DL network", "test");
        Substation s1 = network.newSubstation()
            .setId("S1")
            .setCountry(Country.ES)
            .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
            .setId("VL1")
            .setNominalV(400f)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        vl1.newDanglingLine()
            .setId("dl1")
            .setName("dl1")
            .setNode(1)
            .setP0(1)
            .setQ0(1)
            .setR(1)
            .setX(1)
            .setG(1)
            .setB(1)
            .add();
        network.getDanglingLine("dl1").remove();
        vl1.newDanglingLine()
            .setId("dl1")
            .setName("dl1")
            .setNode(1)
            .setP0(1)
            .setQ0(1)
            .setR(1)
            .setX(1)
            .setG(1)
            .setB(1)
            .add();
        vl1.newGenerator()
            .setId("GEN")
            .setNode(3)
            .setMaxP(20)
            .setMinP(-20)
            .setVoltageRegulatorOn(true)
            .setTargetP(100)
            .setTargetQ(100)
            .setTargetV(220)
            .setRatedS(1)
            .add();
        return network;
    }

    private Network createSwitchesNetwork(NetworkFactory networkFactory) {
        Network network = networkFactory.createNetwork("Switches network", "test");

        Substation s1 = createSubstation(network, "s1", "s1", Country.FR);
        VoltageLevel v1 = createVoltageLevel(s1, "v1", "v1", TopologyKind.NODE_BREAKER, 380.0, 20);
        createBusBarSection(v1, "1.1", "1.1", 0, 1, 1);
        createSwitch(v1, "v1d1", "v1d1", SwitchKind.DISCONNECTOR, true, false, false, 0, 1);
        createSwitch(v1, "v1b1", "v1b1", SwitchKind.BREAKER, true, false, false, 1, 2);
        createLoad(v1, "v1load", "v1load", "v1load", 1, ConnectablePosition.Direction.TOP, 2, 0., 0.);

        VoltageLevel v2 = createVoltageLevel(s1, "v2", "v2", TopologyKind.NODE_BREAKER, 225.0, 20);
        createBusBarSection(v2, "1A", "1A", 0, 1, 1);
        createBusBarSection(v2, "1B", "1B", 1, 1, 2);
        createSwitch(v2, "v2d1", "v2d1", SwitchKind.DISCONNECTOR, true, false, false, 0, 2);
        createSwitch(v2, "v2b1", "v2b1", SwitchKind.BREAKER, true, true, false, 2, 3);
        createSwitch(v2, "v2d2", "v2d2", SwitchKind.DISCONNECTOR, true, false, false, 3, 1);
        createSwitch(v2, "v2dload", "v2dload", SwitchKind.DISCONNECTOR, true, false, false, 1, 4);
        createSwitch(v2, "v2bload", "v2bload", SwitchKind.BREAKER, true, false, false, 4, 5);
        createLoad(v2, "v2load", "v2load", "v2load", 1, ConnectablePosition.Direction.BOTTOM, 5, 0., 0.);

        return network;
    }

    private static Substation createSubstation(Network n, String id, String name, Country country) {
        return n.newSubstation()
            .setId(id)
            .setName(name)
            .setCountry(country)
            .add();
    }

    private static VoltageLevel createVoltageLevel(Substation s, String id, String name,
                                                   TopologyKind topology, double vNom, int nodeCount) {
        VoltageLevel vl = s.newVoltageLevel()
            .setId(id)
            .setName(name)
            .setTopologyKind(topology)
            .setNominalV(vNom)
            .add();
        return vl;
    }

    private static void createBusBarSection(VoltageLevel vl, String id, String name, int node, int busbarIndex, int sectionIndex) {
        BusbarSection bbs = vl.getNodeBreakerView().newBusbarSection()
            .setId(id)
            .setName(name)
            .setNode(node)
            .add();
        bbs.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(busbarIndex).withSectionIndex(sectionIndex).add();
        BusbarSectionPosition bbsp = bbs.getExtension(BusbarSectionPosition.class);
        assertNotNull(bbsp);
        assertEquals(busbarIndex, bbsp.getBusbarIndex());
        assertEquals(sectionIndex, bbsp.getSectionIndex());
        bbsp = bbs.getExtensionByName("position");
        assertNotNull(bbsp);
        assertEquals(busbarIndex, bbsp.getBusbarIndex());
        assertEquals(sectionIndex, bbsp.getSectionIndex());
    }

    private static void createSwitch(VoltageLevel vl, String id, String name, SwitchKind kind, boolean retained, boolean open, boolean fictitious, int node1, int node2) {
        vl.getNodeBreakerView().newSwitch()
            .setId(id)
            .setName(name)
            .setKind(kind)
            .setRetained(retained)
            .setOpen(open)
            .setFictitious(fictitious)
            .setNode1(node1)
            .setNode2(node2)
            .add();
    }

    private static void createLoad(VoltageLevel vl, String id, String name, String feederName, int feederOrder,
                                   ConnectablePosition.Direction direction, int node, double p0, double q0) {
        Load load = vl.newLoad()
            .setId(id)
            .setName(name)
            .setNode(node)
            .setP0(p0)
            .setQ0(q0)
            .add();
        load.newExtension(ConnectablePositionAdder.class).newFeeder()
            .withName(feederName).withOrder(feederOrder).withDirection(direction).add().add();
        ConnectablePosition cp = load.getExtension(ConnectablePosition.class);
        assertNotNull(cp);
        assertEquals(feederName, cp.getFeeder().getName());
        assertEquals(feederOrder, cp.getFeeder().getOrder().orElseThrow().intValue());
        assertEquals(direction, cp.getFeeder().getDirection());
        cp = load.getExtensionByName("position");
        assertNotNull(cp);
        assertEquals(feederName, cp.getFeeder().getName());
        assertEquals(feederOrder, cp.getFeeder().getOrder().orElseThrow().intValue());
        assertEquals(direction, cp.getFeeder().getDirection());
    }

    @Test
    public void testActivePowerControlExtension() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = EurostagTutorialExample1Factory.create(service.getNetworkFactory());
            Generator gen = network.getGenerator("GEN");
            gen.addExtension(ActivePowerControl.class, new ActivePowerControlImpl<>(gen, true, 6.3f));
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.getNetwork(service.getNetworkIds().keySet().iterator().next());
            Generator gen = network.getGenerator("GEN");
            ActivePowerControl<Generator> activePowerControl = gen.getExtension(ActivePowerControl.class);
            assertNotNull(activePowerControl);
            assertTrue(activePowerControl.isParticipate());
            assertEquals(6.3f, activePowerControl.getDroop(), 0f);
            assertNotNull(gen.getExtensionByName("activePowerControl"));
        }
    }

    @Test
    public void testGeneratorStartup() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = EurostagTutorialExample1Factory.create(service.getNetworkFactory());
            Generator gen = network.getGenerator("GEN");
            gen.addExtension(GeneratorStartup.class, new GeneratorStartupImpl((GeneratorImpl) gen, 1.0, 2.0, 3.0, 4.0, 5.0));
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.getNetwork(service.getNetworkIds().keySet().iterator().next());
            Generator gen = network.getGenerator("GEN");
            GeneratorStartup generatorStartup = gen.getExtension(GeneratorStartup.class);
            assertNotNull(generatorStartup);
            assertEquals(1.0f, generatorStartup.getPlannedActivePowerSetpoint(), 0f);
            assertEquals(2.0f, generatorStartup.getStartupCost(), 0f);
            assertEquals(3.0f, generatorStartup.getMarginalCost(), 0f);
            assertEquals(4.0f, generatorStartup.getPlannedOutageRate(), 0f);
            assertEquals(5.0f, generatorStartup.getForcedOutageRate(), 0f);
            assertNotNull(gen.getExtensionByName(GeneratorStartup.NAME));
        }
    }

    @Test
    public void testGeneratorShortCircuit() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = EurostagTutorialExample1Factory.create(service.getNetworkFactory());
            Generator gen = network.getGenerator("GEN");
            assertNull(gen.getExtension(GeneratorShortCircuit.class));
            assertNull(gen.getExtensionByName(GeneratorShortCircuit.NAME));
            assertTrue(gen.getExtensions().isEmpty());
            GeneratorShortCircuitAdder circuitAdder = gen.newExtension(GeneratorShortCircuitAdder.class).withDirectTransX(Double.NaN);
            assertThrows(PowsyblException.class, () -> circuitAdder.add());
            circuitAdder.withDirectSubtransX(20.)
                        .withDirectTransX(30.)
                        .withStepUpTransformerX(50.)
                        .add();
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.getNetwork(service.getNetworkIds().keySet().iterator().next());
            Generator gen = network.getGenerator("GEN");
            GeneratorShortCircuit generatorShortCircuit = gen.getExtension(GeneratorShortCircuit.class);
            assertNotNull(generatorShortCircuit);
            assertEquals(20., generatorShortCircuit.getDirectSubtransX(), 0);
            assertEquals(30., generatorShortCircuit.getDirectTransX(), 0);
            assertEquals(50., generatorShortCircuit.getStepUpTransformerX(), 0);
            assertNotNull(gen.getExtensionByName(GeneratorShortCircuit.NAME));
            assertEquals(GeneratorShortCircuit.NAME, generatorShortCircuit.getName());

            assertThrows(PowsyblException.class, () -> generatorShortCircuit.setDirectTransX(Double.NaN));
            generatorShortCircuit.setDirectSubtransX(23.);
            generatorShortCircuit.setDirectTransX(32.);
            generatorShortCircuit.setStepUpTransformerX(44.);
            assertEquals(23., generatorShortCircuit.getDirectSubtransX(), 0);
            assertEquals(32., generatorShortCircuit.getDirectTransX(), 0);
            assertEquals(44., generatorShortCircuit.getStepUpTransformerX(), 0);
        }
    }

    @Test
    public void testIdentifiableShortCircuit() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = EurostagTutorialExample1Factory.create(service.getNetworkFactory());
            VoltageLevel vl = network.getVoltageLevel("VLGEN");
            assertNull(vl.getExtension(IdentifiableShortCircuit.class));
            assertNull(vl.getExtensionByName(IdentifiableShortCircuit.NAME));
            assertTrue(vl.getExtensions().isEmpty());
            IdentifiableShortCircuitAdder shortCircuitAdder = vl.newExtension(IdentifiableShortCircuitAdder.class);
            shortCircuitAdder.withIpMin(80.)
                    .withIpMax(220.)
                    .add();
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.getNetwork(service.getNetworkIds().keySet().iterator().next());
            VoltageLevel vl = network.getVoltageLevel("VLGEN");
            IdentifiableShortCircuit identifiableShortCircuit = vl.getExtension(IdentifiableShortCircuit.class);
            assertNotNull(identifiableShortCircuit);
            assertEquals(80., identifiableShortCircuit.getIpMin(), 0);
            assertEquals(220., identifiableShortCircuit.getIpMax(), 0);
            assertNotNull(vl.getExtensionByName(IdentifiableShortCircuit.NAME));
            assertEquals(IdentifiableShortCircuit.NAME, identifiableShortCircuit.getName());

            identifiableShortCircuit.setIpMin(90.);
            identifiableShortCircuit.setIpMax(260.);
            assertEquals(90., identifiableShortCircuit.getIpMin(), 0);
            assertEquals(260., identifiableShortCircuit.getIpMax(), 0);
        }
    }

    @Test
    public void testGetIdentifiable() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            service.flush(EurostagTutorialExample1Factory.create(service.getNetworkFactory()));
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.getNetwork(service.getNetworkIds().keySet().iterator().next());
            Identifiable gen = network.getIdentifiable("GEN");
            assertNotNull(gen);
            assertTrue(gen instanceof Generator);

            Identifiable bus = network.getIdentifiable("NLOAD");
            assertNotNull(bus);
            assertTrue(bus instanceof Bus);

            assertEquals(16, network.getIdentifiables().size());
            assertEquals(Arrays.asList("P1", "P2", "VLHV2", "VLHV1", "VLGEN", "VLLOAD", "GEN", "LOAD", "NGEN_NHV1",
                    "NHV2_NLOAD", "NHV1_NHV2_2", "NHV1_NHV2_1", "NLOAD", "NHV1", "NHV2", "NGEN"),
                network.getIdentifiables().stream().map(Identifiable::getId).collect(Collectors.toList()));
        }
    }

    private Network createExtensionsNetwork(NetworkFactory networkFactory) {
        Network network = networkFactory.createNetwork("Extensions network", "test");

        Substation s1 = createSubstation(network, "s1", "s1", Country.FR);
        VoltageLevel v1 = createVoltageLevel(s1, "v1", "v1", TopologyKind.NODE_BREAKER, 380.0, 20);
        createBusBarSection(v1, "1.1", "1.1", 0, 1, 1);
        createLoad(v1, "v1load", "v1load", "v1load", 1, ConnectablePosition.Direction.TOP, 2, 0., 0.);

        VoltageLevel v2 = createVoltageLevel(s1, "v2", "v2", TopologyKind.NODE_BREAKER, 225.0, 20);
        createBusBarSection(v2, "2.1", "2.1", 0, 1, 1);

        VoltageLevel v3 = createVoltageLevel(s1, "v3", "v3", TopologyKind.NODE_BREAKER, 100.0, 20);
        createBusBarSection(v2, "3.1", "3.1", 0, 1, 1);

        TwoWindingsTransformer twt2 = s1.newTwoWindingsTransformer().setId("TWT2")
            .setName("My two windings transformer").setVoltageLevel1("v1").setVoltageLevel2("v2").setNode1(1)
            .setNode2(1).setR(0.5).setX(4).setG(0).setB(0).setRatedU1(24).setRatedU2(385).setRatedS(100).add();
        twt2.newExtension(ConnectablePositionAdder.class).newFeeder1().withName("twt2.1").withOrder(2)
            .withDirection(ConnectablePosition.Direction.TOP).add().newFeeder2().withName("twt2.2").withOrder(2)
            .withDirection(ConnectablePosition.Direction.TOP).add().add();
        ConnectablePosition cptwt2 = twt2.getExtension(ConnectablePosition.class);
        assertEquals("twt2.1", cptwt2.getFeeder1().getName());
        assertEquals(2, cptwt2.getFeeder1().getOrder().orElseThrow().intValue());
        assertEquals(ConnectablePosition.Direction.TOP, cptwt2.getFeeder1().getDirection());
        assertEquals("twt2.2", cptwt2.getFeeder2().getName());
        assertEquals(2, cptwt2.getFeeder2().getOrder().orElseThrow().intValue());
        assertEquals(ConnectablePosition.Direction.TOP, cptwt2.getFeeder2().getDirection());
        cptwt2 = twt2.getExtensionByName("position");
        assertEquals("twt2.1", cptwt2.getFeeder1().getName());
        assertEquals(2, cptwt2.getFeeder1().getOrder().orElseThrow().intValue());
        assertEquals(ConnectablePosition.Direction.TOP, cptwt2.getFeeder1().getDirection());
        assertEquals("twt2.2", cptwt2.getFeeder2().getName());
        assertEquals(2, cptwt2.getFeeder2().getOrder().orElseThrow().intValue());
        assertEquals(ConnectablePosition.Direction.TOP, cptwt2.getFeeder2().getDirection());

        ThreeWindingsTransformer twt3 = s1.newThreeWindingsTransformer().setId("TWT3")
            .setName("Three windings transformer 1").setRatedU0(234).newLeg1().setVoltageLevel("v1").setNode(1)
            .setR(45).setX(35).setG(25).setB(15).setRatedU(5).add().newLeg2().setVoltageLevel("v2").setNode(1)
            .setR(47).setX(37).setG(27).setB(17).setRatedU(7).add().newLeg3().setVoltageLevel("v3").setNode(1)
            .setR(49).setX(39).setG(29).setB(19).setRatedU(9).add().add();
        twt3.newExtension(ConnectablePositionAdder.class).newFeeder1().withName("twt3.1").withOrder(3)
            .withDirection(ConnectablePosition.Direction.BOTTOM).add().newFeeder2().withName("twt3.2").withOrder(3)
            .withDirection(ConnectablePosition.Direction.BOTTOM).add().newFeeder3().withName("twt3.3").withOrder(3)
            .withDirection(ConnectablePosition.Direction.BOTTOM).add().add();

        ConnectablePosition cptwt3 = twt3.getExtension(ConnectablePosition.class);
        assertEquals("twt3.1", cptwt3.getFeeder1().getName());
        assertEquals(3, cptwt3.getFeeder1().getOrder().orElseThrow().intValue());
        assertEquals(ConnectablePosition.Direction.BOTTOM, cptwt3.getFeeder1().getDirection());
        assertEquals("twt3.2", cptwt3.getFeeder2().getName());
        assertEquals(3, cptwt3.getFeeder2().getOrder().orElseThrow().intValue());
        assertEquals(ConnectablePosition.Direction.BOTTOM, cptwt3.getFeeder2().getDirection());
        assertEquals("twt3.3", cptwt3.getFeeder3().getName());
        assertEquals(3, cptwt3.getFeeder3().getOrder().orElseThrow().intValue());
        assertEquals(ConnectablePosition.Direction.BOTTOM, cptwt3.getFeeder3().getDirection());
        cptwt3 = twt3.getExtensionByName("position");
        assertEquals("twt3.1", cptwt3.getFeeder1().getName());
        assertEquals(3, cptwt3.getFeeder1().getOrder().orElseThrow().intValue());
        assertEquals(ConnectablePosition.Direction.BOTTOM, cptwt3.getFeeder1().getDirection());
        assertEquals("twt3.2", cptwt3.getFeeder2().getName());
        assertEquals(3, cptwt3.getFeeder2().getOrder().orElseThrow().intValue());
        assertEquals(ConnectablePosition.Direction.BOTTOM, cptwt3.getFeeder2().getDirection());
        assertEquals("twt3.3", cptwt3.getFeeder3().getName());
        assertEquals(3, cptwt3.getFeeder3().getOrder().orElseThrow().intValue());
        assertEquals(ConnectablePosition.Direction.BOTTOM, cptwt3.getFeeder3().getDirection());
        return network;
    }

    @Test
    public void extensionsTest() {
        // create network and save it
        try (NetworkStoreService service = createNetworkStoreService()) {
            service.flush(createExtensionsNetwork(service.getNetworkFactory()));
        }

        // load saved network
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals("Extensions network", readNetwork.getId());

            Load load = readNetwork.getLoad("v1load");
            TwoWindingsTransformer twt2 = readNetwork.getTwoWindingsTransformer("TWT2");
            ThreeWindingsTransformer twt3 = readNetwork.getThreeWindingsTransformer("TWT3");
            assertNotNull(load);
            assertNotNull(twt2);
            assertNotNull(twt3);
            ConnectablePosition cpload = load.getExtension(ConnectablePosition.class);
            assertNotNull(cpload);
            assertEquals("v1load", cpload.getFeeder().getName());
            assertEquals(1, cpload.getFeeder().getOrder().orElseThrow().intValue());
            assertEquals(ConnectablePosition.Direction.TOP, cpload.getFeeder().getDirection());
            cpload = load.getExtensionByName("position");
            assertNotNull(cpload);
            assertEquals("v1load", cpload.getFeeder().getName());
            assertEquals(1, cpload.getFeeder().getOrder().orElseThrow().intValue());
            assertEquals(ConnectablePosition.Direction.TOP, cpload.getFeeder().getDirection());

            ConnectablePosition cptwt2 = twt2.getExtension(ConnectablePosition.class);
            assertNotNull(cptwt2);
            assertEquals("twt2.1", cptwt2.getFeeder1().getName());
            assertEquals(2, cptwt2.getFeeder1().getOrder().orElseThrow().intValue());
            assertEquals(ConnectablePosition.Direction.TOP, cptwt2.getFeeder1().getDirection());
            assertEquals("twt2.2", cptwt2.getFeeder2().getName());
            assertEquals(2, cptwt2.getFeeder2().getOrder().orElseThrow().intValue());
            assertEquals(ConnectablePosition.Direction.TOP, cptwt2.getFeeder2().getDirection());
            cptwt2 = twt2.getExtensionByName("position");
            assertNotNull(cptwt2);
            assertEquals("twt2.1", cptwt2.getFeeder1().getName());
            assertEquals(2, cptwt2.getFeeder1().getOrder().orElseThrow().intValue());
            assertEquals(ConnectablePosition.Direction.TOP, cptwt2.getFeeder1().getDirection());
            assertEquals("twt2.2", cptwt2.getFeeder2().getName());
            assertEquals(2, cptwt2.getFeeder2().getOrder().orElseThrow().intValue());
            assertEquals(ConnectablePosition.Direction.TOP, cptwt2.getFeeder2().getDirection());

            ConnectablePosition cptwt3 = twt3.getExtension(ConnectablePosition.class);
            assertNotNull(cptwt3);
            assertEquals("twt3.1", cptwt3.getFeeder1().getName());
            assertEquals(3, cptwt3.getFeeder1().getOrder().orElseThrow().intValue());
            assertEquals(ConnectablePosition.Direction.BOTTOM, cptwt3.getFeeder1().getDirection());
            assertEquals("twt3.2", cptwt3.getFeeder2().getName());
            assertEquals(3, cptwt3.getFeeder2().getOrder().orElseThrow().intValue());
            assertEquals(ConnectablePosition.Direction.BOTTOM, cptwt3.getFeeder2().getDirection());
            assertEquals("twt3.3", cptwt3.getFeeder3().getName());
            assertEquals(3, cptwt3.getFeeder3().getOrder().orElseThrow().intValue());
            assertEquals(ConnectablePosition.Direction.BOTTOM, cptwt3.getFeeder3().getDirection());
            cptwt3 = twt3.getExtensionByName("position");
            assertNotNull(cptwt3);
            assertEquals("twt3.1", cptwt3.getFeeder1().getName());
            assertEquals(3, cptwt3.getFeeder1().getOrder().orElseThrow().intValue());
            assertEquals(ConnectablePosition.Direction.BOTTOM, cptwt3.getFeeder1().getDirection());
            assertEquals("twt3.2", cptwt3.getFeeder2().getName());
            assertEquals(3, cptwt3.getFeeder2().getOrder().orElseThrow().intValue());
            assertEquals(ConnectablePosition.Direction.BOTTOM, cptwt3.getFeeder2().getDirection());
            assertEquals("twt3.3", cptwt3.getFeeder3().getName());
            assertEquals(3, cptwt3.getFeeder3().getOrder().orElseThrow().intValue());
            assertEquals(ConnectablePosition.Direction.BOTTOM, cptwt3.getFeeder3().getDirection());
        }
    }

    @Test
    public void shuntCompensatorTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals("networkTestCase", readNetwork.getId());

            assertEquals(2, readNetwork.getShuntCompensatorCount());

            ShuntCompensator shunt1 = readNetwork.getShuntCompensatorStream().findFirst().get();
            assertEquals("SHUNT1", shunt1.getId());
            assertFalse(shunt1.isFictitious());
            assertTrue(shunt1.isVoltageRegulatorOn());
            assertEquals(5, shunt1.getSectionCount());
            assertEquals(10, shunt1.getMaximumSectionCount());
            assertEquals(5, shunt1.getB(), 0.01);
            assertEquals(10, shunt1.getG(), 0.01);
            assertEquals(1, shunt1.getB(1), 0.01);
            assertEquals(2, shunt1.getG(1), 0.01);
            assertEquals(380, shunt1.getTargetV(), 0.1);
            assertEquals(10, shunt1.getTargetDeadband(), 0.1);
            assertEquals(200, shunt1.getTerminal().getQ(), 0.1);
            assertEquals(ShuntCompensatorModelType.LINEAR, shunt1.getModelType());
            ShuntCompensatorModel shuntModel = shunt1.getModel();
            ShuntCompensatorLinearModel shuntLinearModel = shunt1.getModel(ShuntCompensatorLinearModel.class);
            assertEquals(1, ((ShuntCompensatorLinearModel) shuntModel).getBPerSection(), 0.001);
            assertEquals(2, shuntLinearModel.getGPerSection(), 0.001);

            shunt1.setTargetV(420);
            shunt1.setVoltageRegulatorOn(false);
            shunt1.setSectionCount(8);
            shunt1.setTargetDeadband(20);
            shunt1.getTerminal().setQ(600);
            ((ShuntCompensatorLinearModel) shunt1.getModel()).setBPerSection(3);
            ((ShuntCompensatorLinearModel) shunt1.getModel()).setGPerSection(4);
            ((ShuntCompensatorLinearModel) shunt1.getModel()).setMaximumSectionCount(10);

            ShuntCompensator shunt2 = readNetwork.getShuntCompensatorStream().skip(1).findFirst().get();
            assertEquals("SHUNT2", shunt2.getId());
            assertFalse(shunt2.isFictitious());
            assertFalse(shunt2.isVoltageRegulatorOn());
            assertEquals(3, shunt2.getSectionCount());
            assertEquals(420, shunt2.getTargetV(), 0.1);
            assertEquals(20, shunt2.getTargetDeadband(), 0.1);
            assertEquals(600, shunt2.getTerminal().getQ(), 0.1);
            assertEquals(ShuntCompensatorModelType.NON_LINEAR, shunt2.getModelType());
            assertEquals(1, ((ShuntCompensatorNonLinearModel) shunt2.getModel()).getAllSections().get(0).getB(), 0.001);
            assertEquals(2, ((ShuntCompensatorNonLinearModel) shunt2.getModel()).getAllSections().get(0).getG(), 0.001);
            assertEquals(3, ((ShuntCompensatorNonLinearModel) shunt2.getModel()).getAllSections().get(1).getB(), 0.001);
            assertEquals(4, ((ShuntCompensatorNonLinearModel) shunt2.getModel()).getAllSections().get(1).getG(), 0.001);
            assertEquals(5, ((ShuntCompensatorNonLinearModel) shunt2.getModel()).getAllSections().get(2).getB(), 0.001);
            assertEquals(6, ((ShuntCompensatorNonLinearModel) shunt2.getModel()).getAllSections().get(2).getG(), 0.001);
            assertEquals(7, ((ShuntCompensatorNonLinearModel) shunt2.getModel()).getAllSections().get(3).getB(), 0.001);
            assertEquals(8, ((ShuntCompensatorNonLinearModel) shunt2.getModel()).getAllSections().get(3).getG(), 0.001);
            ((ShuntCompensatorNonLinearModel) shunt2.getModel()).getAllSections().get(0).setB(11);
            ((ShuntCompensatorNonLinearModel) shunt2.getModel()).getAllSections().get(0).setG(12);

            shunt2.setFictitious(true);
            shunt2.setTargetV(450);
            shunt2.setVoltageRegulatorOn(true);
            shunt2.setSectionCount(1);
            shunt2.setTargetDeadband(80);
            shunt2.getTerminal().setQ(800);

            service.flush(readNetwork);  // flush the network
        }

        // reload modified network
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            ShuntCompensator shunt1 = readNetwork.getShuntCompensatorStream().findFirst().get();
            assertNotNull(shunt1);
            assertFalse(shunt1.isFictitious());
            assertFalse(shunt1.isVoltageRegulatorOn());
            assertEquals(8, shunt1.getSectionCount());
            assertEquals(420, shunt1.getTargetV(), 0.1);
            assertEquals(20, shunt1.getTargetDeadband(), 0.1);
            assertEquals(600, shunt1.getTerminal().getQ(), 0.1);

            ShuntCompensator shunt2 = readNetwork.getShuntCompensatorStream().skip(1).findFirst().get();
            assertNotNull(shunt2);
            assertTrue(shunt2.isFictitious());
            assertTrue(shunt2.isVoltageRegulatorOn());
            assertEquals(1, shunt2.getSectionCount());
            assertEquals(450, shunt2.getTargetV(), 0.1);
            assertEquals(80, shunt2.getTargetDeadband(), 0.1);
            assertEquals(800, shunt2.getTerminal().getQ(), 0.1);
        }
    }

    @Test
    public void testShuntCompensatorRemove() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(2, readNetwork.getShuntCompensatorCount());
            readNetwork.getShuntCompensator("SHUNT1").remove();
            assertEquals(1, readNetwork.getShuntCompensatorCount());
            service.flush(readNetwork);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals(1, readNetwork.getShuntCompensatorCount());
        }
    }

    @Test
    public void getIdentifiableNetworkTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = EurostagTutorialExample1Factory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network network = service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow(AssertionError::new));
            // network is itself an identifiable
            assertSame(network, network.getIdentifiable(network.getId()));
        }
    }

    @Test
    public void coordinatedReactiveControlTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = EurostagTutorialExample1Factory.create(service.getNetworkFactory());
            Generator gen = network.getGenerator("GEN");
            assertNull(gen.getExtension(CoordinatedReactiveControl.class));
            assertNull(gen.getExtensionByName("coordinatedReactiveControl"));
            assertTrue(gen.getExtensions().isEmpty());
            gen.newExtension(CoordinatedReactiveControlAdder.class)
                .withQPercent(50)
                .add();
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network network = service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow(AssertionError::new));
            Generator gen = network.getGenerator("GEN");
            CoordinatedReactiveControl extension = gen.getExtension(CoordinatedReactiveControl.class);
            assertNotNull(extension);
            assertEquals(50, extension.getQPercent(), 0);
            assertNotNull(gen.getExtensionByName("coordinatedReactiveControl"));
            assertEquals(1, gen.getExtensions().size());
        }
    }

    @Test
    public void generatorEntsoeCategoryTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = EurostagTutorialExample1Factory.create(service.getNetworkFactory());
            Generator gen = network.getGenerator("GEN");
            assertNull(gen.getExtension(GeneratorEntsoeCategory.class));
            assertNull(gen.getExtensionByName("entsoeCategory"));
            assertTrue(gen.getExtensions().isEmpty());
            gen.newExtension(GeneratorEntsoeCategoryAdder.class)
                .withCode(50)
                .add();
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network network = service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow(AssertionError::new));
            Generator gen = network.getGenerator("GEN");
            GeneratorEntsoeCategory extension = gen.getExtension(GeneratorEntsoeCategory.class);
            assertNotNull(extension);
            assertEquals(50, extension.getCode());
            assertNotNull(gen.getExtensionByName("entsoeCategory"));
            assertEquals(1, gen.getExtensions().size());
        }
    }

    @Test
    public void voltagePerReactivePowerControlTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = SvcTestCaseFactory.create(service.getNetworkFactory());
            StaticVarCompensator svc2 = network.getStaticVarCompensator("SVC2");
            assertNull(svc2.getExtension(VoltagePerReactivePowerControl.class));
            assertNull(svc2.getExtensionByName("voltagePerReactivePowerControl"));
            assertTrue(svc2.getExtensions().isEmpty());
            svc2.newExtension(VoltagePerReactivePowerControlAdder.class)
                .withSlope(0.3)
                .add();
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network network = service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow(AssertionError::new));
            StaticVarCompensator svc2 = network.getStaticVarCompensator("SVC2");
            VoltagePerReactivePowerControl extension = svc2.getExtension(VoltagePerReactivePowerControl.class);
            assertNotNull(extension);
            assertEquals(0.3, extension.getSlope(), 0);
            assertNotNull(svc2.getExtensionByName("voltagePerReactivePowerControl"));
            assertEquals(1, svc2.getExtensions().size());
        }
    }

    @Test
    public void regulatingShuntTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = ShuntTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network network = service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow(AssertionError::new));
            ShuntCompensator sc = network.getShuntCompensator("SHUNT");
            assertNotNull(sc);
            assertTrue(sc.isVoltageRegulatorOn());
            assertEquals(200.0, sc.getTargetV(), 0);
            assertEquals(5.0, sc.getTargetDeadband(), 0);
            assertEquals("LOAD", sc.getRegulatingTerminal().getConnectable().getId());

            sc.setVoltageRegulatorOn(false);
            sc.setTargetV(210.0);
            sc.setTargetDeadband(3.0);

            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network network = service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow(AssertionError::new));
            assertEquals(1, network.getShuntCompensatorCount());
            assertEquals(1, network.getVoltageLevel("VL1").getShuntCompensatorCount());
            ShuntCompensator sc = network.getShuntCompensator("SHUNT");
            assertNotNull(sc);
            assertFalse(sc.isVoltageRegulatorOn());
            assertEquals(210.0, sc.getTargetV(), 0);
            assertEquals(3.0, sc.getTargetDeadband(), 0);
        }
    }

    @Test
    public void propertiesTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = EurostagTutorialExample1Factory.create(service.getNetworkFactory());
            Generator gen = network.getGenerator("GEN");

            assertFalse(gen.hasProperty());
            assertFalse(gen.hasProperty("foo"));
            assertNull(gen.getProperty("foo"));
            assertTrue(gen.getPropertyNames().isEmpty());
            assertTrue(gen.getProperties().isEmpty());

            gen.setProperty("foo", "bar");
            assertEquals("bar", gen.getProperty("foo"));
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network network = service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow(AssertionError::new));
            Generator gen = network.getGenerator("GEN");
            assertTrue(gen.hasProperty());
            assertTrue(gen.hasProperty("foo"));
            assertEquals("bar", gen.getProperty("foo"));
            assertEquals(Collections.singleton("foo"), gen.getPropertyNames());
            assertEquals(1, gen.getProperties().size());
        }
    }

    @Test
    public void ratedSTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = ThreeWindingsTransformerNetworkFactory.create(service.getNetworkFactory());
            ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
            assertTrue(Double.isNaN(twt.getLeg1().getRatedS()));
            twt.getLeg1().setRatedS(101);
            assertEquals(101, twt.getLeg1().getRatedS(), 0);
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network network = service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow(AssertionError::new));
            ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
            assertEquals(101, twt.getLeg1().getRatedS(), 0);
        }
    }

    @Test
    public void loadDetailExtensionTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = SvcTestCaseFactory.create(service.getNetworkFactory());
            Load load2 = network.getLoad("L2");
            assertNull(load2.getExtension(LoadDetail.class));
            assertNull(load2.getExtensionByName("loadDetail"));
            assertTrue(load2.getExtensions().isEmpty());
            load2.newExtension(LoadDetailAdder.class)
                .withFixedActivePower(5.5f)
                .withFixedReactivePower(2.5f)
                .withVariableActivePower(3.2f)
                .withVariableReactivePower(2.1f)
                .add();
            assertNotNull(load2.getExtension(LoadDetail.class));
            assertNotNull(load2.getExtensionByName("loadDetail"));
            assertFalse(load2.getExtensions().isEmpty());
            LoadDetail loadDetail = load2.getExtension(LoadDetail.class);
            assertEquals(5.5f, loadDetail.getFixedActivePower(), 0.1f);
            assertEquals(2.5f, loadDetail.getFixedReactivePower(), 0.1f);
            assertEquals(3.2f, loadDetail.getVariableActivePower(), 0.1f);
            assertEquals(2.1f, loadDetail.getVariableReactivePower(), 0.1f);
            service.flush(network);
            loadDetail.setFixedActivePower(7.5f);
            loadDetail.setFixedReactivePower(4.5f);
            loadDetail.setVariableActivePower(5.2f);
            loadDetail.setVariableReactivePower(4.1f);
            assertEquals(7.5f, loadDetail.getFixedActivePower(), 0.1f);
            assertEquals(4.5f, loadDetail.getFixedReactivePower(), 0.1f);
            assertEquals(5.2f, loadDetail.getVariableActivePower(), 0.1f);
            assertEquals(4.1f, loadDetail.getVariableReactivePower(), 0.1f);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network network = service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow(AssertionError::new));
            Load load2 = network.getLoad("L2");
            assertNotNull(load2.getExtension(LoadDetail.class));
            assertNotNull(load2.getExtensionByName("loadDetail"));
            assertFalse(load2.getExtensions().isEmpty());
            LoadDetail loadDetail = load2.getExtension(LoadDetail.class);
            assertEquals(5.5f, loadDetail.getFixedActivePower(), 0.1f);
            assertEquals(2.5f, loadDetail.getFixedReactivePower(), 0.1f);
            assertEquals(3.2f, loadDetail.getVariableActivePower(), 0.1f);
            assertEquals(2.1f, loadDetail.getVariableReactivePower(), 0.1f);
        }
    }

    @Test
    public void slackTerminalExtensionTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = SvcTestCaseFactory.create(service.getNetworkFactory());
            VoltageLevel vl = network.getVoltageLevel("VL1");
            assertNull(vl.getExtension(SlackTerminal.class));
            assertNull(vl.getExtensionByName("slackTerminal"));
            assertTrue(vl.getExtensions().isEmpty());
            assertThrows(PowsyblException.class, () -> vl.newExtension(SlackTerminalAdder.class)
                .withTerminal(null)
                .add());
            assertNull(vl.getExtension(SlackTerminal.class));
            assertNull(vl.getExtensionByName("slackTerminal"));
            assertTrue(vl.getExtensions().isEmpty());
            Generator generator = network.getGenerator("G1");
            vl.newExtension(SlackTerminalAdder.class)
                .withTerminal(generator.getTerminal())
                .add();
            assertNotNull(vl.getExtension(SlackTerminal.class));
            assertNotNull(vl.getExtensionByName("slackTerminal"));
            assertFalse(vl.getExtensions().isEmpty());
            assertEquals(vl.getExtension(SlackTerminal.class).getTerminal(), generator.getTerminal());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network network = service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow(AssertionError::new));
            VoltageLevel vl = network.getVoltageLevel("VL1");
            Generator generator = network.getGenerator("G1");
            vl.newExtension(SlackTerminalAdder.class)
                .withTerminal(generator.getTerminal())
                .add();
            assertNotNull(vl.getExtension(SlackTerminal.class));
            assertNotNull(vl.getExtensionByName("slackTerminal"));
            assertFalse(vl.getExtensions().isEmpty());
            assertEquals(vl.getExtension(SlackTerminal.class).getTerminal(), generator.getTerminal());
        }
    }

    @Test
    public void threeWindingsTransformerPhaseAngleClockExtensionTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = ThreeWindingsTransformerNetworkFactory.create(service.getNetworkFactory());
            Substation substation = network.getSubstation("SUBSTATION");
            ThreeWindingsTransformer twt = substation.getThreeWindingsTransformerStream().findFirst().orElseThrow(AssertionError::new);
            assertNull(twt.getExtension(ThreeWindingsTransformerPhaseAngleClock.class));
            assertNull(twt.getExtensionByName("threeWindingsTransformerPhaseAngleClock"));
            assertTrue(twt.getExtensions().isEmpty());
            twt.newExtension(ThreeWindingsTransformerPhaseAngleClockAdder.class).withPhaseAngleClockLeg2(1).withPhaseAngleClockLeg3(2).add();
            assertNotNull(twt.getExtension(ThreeWindingsTransformerPhaseAngleClock.class));
            assertNotNull(twt.getExtensionByName("threeWindingsTransformerPhaseAngleClock"));
            assertFalse(twt.getExtensions().isEmpty());
            assertEquals(1, twt.getExtension(ThreeWindingsTransformerPhaseAngleClock.class).getPhaseAngleClockLeg2());
            assertEquals(2, twt.getExtension(ThreeWindingsTransformerPhaseAngleClock.class).getPhaseAngleClockLeg3());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network network = service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow(AssertionError::new));
            Substation substation = network.getSubstation("SUBSTATION");
            ThreeWindingsTransformer twt = substation.getThreeWindingsTransformerStream().findFirst().orElseThrow(AssertionError::new);
            assertNotNull(twt.getExtension(ThreeWindingsTransformerPhaseAngleClock.class));
            assertNotNull(twt.getExtensionByName("threeWindingsTransformerPhaseAngleClock"));
            assertFalse(twt.getExtensions().isEmpty());
            assertEquals(1, twt.getExtension(ThreeWindingsTransformerPhaseAngleClock.class).getPhaseAngleClockLeg2());
            assertEquals(2, twt.getExtension(ThreeWindingsTransformerPhaseAngleClock.class).getPhaseAngleClockLeg3());
        }
    }

    @Test
    public void twoWindingsTransformerPhaseAngleClockExtensionTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = EurostagTutorialExample1Factory.create(service.getNetworkFactory());

            TwoWindingsTransformer twt = network.getTwoWindingsTransformer("NGEN_NHV1");
            assertNull(twt.getExtension(TwoWindingsTransformerPhaseAngleClock.class));
            assertNull(twt.getExtensionByName("twoWindingsTransformerPhaseAngleClock"));
            assertTrue(twt.getExtensions().isEmpty());
            twt.newExtension(TwoWindingsTransformerPhaseAngleClockAdder.class).withPhaseAngleClock(1).add();
            assertNotNull(twt.getExtension(TwoWindingsTransformerPhaseAngleClock.class));
            assertNotNull(twt.getExtensionByName("twoWindingsTransformerPhaseAngleClock"));
            assertFalse(twt.getExtensions().isEmpty());
            assertEquals(1, twt.getExtension(TwoWindingsTransformerPhaseAngleClock.class).getPhaseAngleClock());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network network = service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow(AssertionError::new));
            Substation substation = network.getSubstation("SUBSTATION");
            TwoWindingsTransformer twt = network.getTwoWindingsTransformer("NGEN_NHV1");
            assertNotNull(twt.getExtension(TwoWindingsTransformerPhaseAngleClock.class));
            assertNotNull(twt.getExtensionByName("twoWindingsTransformerPhaseAngleClock"));
            assertFalse(twt.getExtensions().isEmpty());
            assertEquals(1, twt.getExtension(TwoWindingsTransformerPhaseAngleClock.class).getPhaseAngleClock());
        }
    }

    @Test
    public void svcVoltagePerReactivePowerExtensionTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = SvcTestCaseFactory.create(service.getNetworkFactory());

            StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
            assertNull(svc.getExtension(VoltagePerReactivePowerControl.class));
            assertNull(svc.getExtensionByName("voltagePerReactivePowerControl"));
            assertTrue(svc.getExtensions().isEmpty());
            svc.newExtension(VoltagePerReactivePowerControlAdder.class).withSlope(1).add();
            assertNotNull(svc.getExtension(VoltagePerReactivePowerControl.class));
            assertNotNull(svc.getExtensionByName("voltagePerReactivePowerControl"));
            assertFalse(svc.getExtensions().isEmpty());
            assertEquals(1, svc.getExtension(VoltagePerReactivePowerControl.class).getSlope(), 0.001);
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network network = service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow(AssertionError::new));
            StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
            assertNotNull(svc.getExtension(VoltagePerReactivePowerControl.class));
            assertNotNull(svc.getExtensionByName("voltagePerReactivePowerControl"));
            assertFalse(svc.getExtensions().isEmpty());
            assertEquals(1, svc.getExtension(VoltagePerReactivePowerControl.class).getSlope(), 0.001);
        }
    }

    @Test
    public void coordinatedReactiveControlExtensionTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = SvcTestCaseFactory.create(service.getNetworkFactory());

            Generator generator = network.getGenerator("G1");
            assertNull(generator.getExtension(CoordinatedReactiveControl.class));
            assertNull(generator.getExtensionByName("coordinatedReactiveControl"));
            assertTrue(generator.getExtensions().isEmpty());
            generator.newExtension(CoordinatedReactiveControlAdder.class).withQPercent(1).add();
            assertNotNull(generator.getExtension(CoordinatedReactiveControl.class));
            assertNotNull(generator.getExtensionByName("coordinatedReactiveControl"));
            assertFalse(generator.getExtensions().isEmpty());
            assertEquals(1, generator.getExtension(CoordinatedReactiveControl.class).getQPercent(), 0.001);
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network network = service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow(AssertionError::new));
            Generator generator = network.getGenerator("G1");
            assertNotNull(generator.getExtension(CoordinatedReactiveControl.class));
            assertNotNull(generator.getExtensionByName("coordinatedReactiveControl"));
            assertFalse(generator.getExtensions().isEmpty());
            assertEquals(1, generator.getExtension(CoordinatedReactiveControl.class).getQPercent(), 0.001);
        }
    }

    @Test
    public void testVisit2WTConnectedInOneVLOnlyIssue() {
        String filePath = "/BrranchConnectedInOneVLOnlyIssue.uct";
        ReadOnlyDataSource dataSource = getResource(filePath, filePath);
        Network network = new UcteImporter().importData(dataSource, new NetworkFactoryImpl(), null);
        Set<Branch.Side> visitedLineSides = new HashSet<>();
        Set<Branch.Side> visited2WTSides = new HashSet<>();
        Set<ThreeWindingsTransformer.Side> visited3WTSides = new HashSet<>();
        network.getVoltageLevelStream().findFirst().get().visitEquipments(new DefaultTopologyVisitor() {
            @Override
            public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, Branch.Side side) {
                visited2WTSides.add(side);
            }

            @Override
            public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
                visited3WTSides.add(side);
            }

            @Override
            public void visitLine(Line line, Branch.Side side) {
                visitedLineSides.add(side);
            }
        });

        assertEquals(2, visitedLineSides.size());
        assertTrue(visitedLineSides.contains(Branch.Side.ONE));
        assertTrue(visitedLineSides.contains(Branch.Side.TWO));

        assertEquals(2, visited2WTSides.size());
        assertTrue(visited2WTSides.contains(Branch.Side.ONE));
        assertTrue(visited2WTSides.contains(Branch.Side.TWO));

        assertEquals(0, visited3WTSides.size());
    }

    @Test
    public void activeAndApparentPowerLimitsTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("networkTestCase", readNetwork.getId());

            assertEquals(2, readNetwork.getDanglingLineCount());

            DanglingLine danglingLine = readNetwork.getDanglingLine("DL2");

            readNetwork.getThreeWindingsTransformer("TWT1").getLeg1().newActivePowerLimits().setPermanentLimit(10).add();
            readNetwork.getThreeWindingsTransformer("TWT1").getLeg1().newApparentPowerLimits().setPermanentLimit(20).add();

            assertEquals(10, readNetwork.getThreeWindingsTransformer("TWT1").getLeg1().getActivePowerLimits().orElseThrow().getPermanentLimit(), 0.1);
            assertEquals(20, readNetwork.getThreeWindingsTransformer("TWT1").getLeg1().getApparentPowerLimits().orElseThrow().getPermanentLimit(), 0.1);

            ApparentPowerLimits apparentPowerLimits = danglingLine.getApparentPowerLimits().orElseThrow();
            assertEquals(400, apparentPowerLimits.getPermanentLimit(), 0.1);
            assertEquals(550, apparentPowerLimits.getTemporaryLimitValue(20), 0.1);
            ApparentPowerLimits.TemporaryLimit temporaryLimit = apparentPowerLimits.getTemporaryLimit(20);
            assertEquals(550, temporaryLimit.getValue(), 0.1);
            assertEquals("APL_TL1", temporaryLimit.getName());
            assertFalse(temporaryLimit.isFictitious());
            assertEquals(450, apparentPowerLimits.getTemporaryLimitValue(40), 0.1);
            temporaryLimit = apparentPowerLimits.getTemporaryLimit(40);
            assertEquals(450, temporaryLimit.getValue(), 0.1);
            assertEquals("APL_TL2", temporaryLimit.getName());
            assertTrue(temporaryLimit.isFictitious());

            ActivePowerLimits activePowerLimits = danglingLine.getActivePowerLimits().orElseThrow();
            assertEquals(300, activePowerLimits.getPermanentLimit(), 0.1);
            assertEquals(450, activePowerLimits.getTemporaryLimitValue(20), 0.1);
            ActivePowerLimits.TemporaryLimit temporaryLimit2 = activePowerLimits.getTemporaryLimit(20);
            assertEquals(450, temporaryLimit2.getValue(), 0.1);
            assertEquals("ACL_TL1", temporaryLimit2.getName());
            assertFalse(temporaryLimit2.isFictitious());
            assertEquals(350, activePowerLimits.getTemporaryLimitValue(40), 0.1);
            temporaryLimit2 = activePowerLimits.getTemporaryLimit(40);
            assertEquals(350, temporaryLimit2.getValue(), 0.1);
            assertEquals("ACL_TL2", temporaryLimit2.getName());
            assertTrue(temporaryLimit2.isFictitious());

            Line line = readNetwork.getLine("LINE1");

            apparentPowerLimits = line.getApparentPowerLimits1().orElseThrow();
            assertEquals(1000, apparentPowerLimits.getPermanentLimit(), 0.1);
            assertEquals(500, apparentPowerLimits.getTemporaryLimitValue(20), 0.1);
            temporaryLimit = apparentPowerLimits.getTemporaryLimit(20);
            assertEquals(500, temporaryLimit.getValue(), 0.1);
            assertEquals("APL_TL1", temporaryLimit.getName());
            assertFalse(temporaryLimit.isFictitious());
            assertEquals(250, apparentPowerLimits.getTemporaryLimitValue(40), 0.1);
            temporaryLimit = apparentPowerLimits.getTemporaryLimit(40);
            assertEquals(250, temporaryLimit.getValue(), 0.1);
            assertEquals("APL_TL2", temporaryLimit.getName());
            assertTrue(temporaryLimit.isFictitious());

            apparentPowerLimits = line.getApparentPowerLimits2().orElseThrow();
            assertEquals(2000, apparentPowerLimits.getPermanentLimit(), 0.1);
            assertEquals(1000, apparentPowerLimits.getTemporaryLimitValue(20), 0.1);
            temporaryLimit = apparentPowerLimits.getTemporaryLimit(20);
            assertEquals(1000, temporaryLimit.getValue(), 0.1);
            assertEquals("APL_TL3", temporaryLimit.getName());
            assertFalse(temporaryLimit.isFictitious());
            assertEquals(500, apparentPowerLimits.getTemporaryLimitValue(40), 0.1);
            temporaryLimit = apparentPowerLimits.getTemporaryLimit(40);
            assertEquals(500, temporaryLimit.getValue(), 0.1);
            assertEquals("APL_TL4", temporaryLimit.getName());
            assertTrue(temporaryLimit.isFictitious());

            activePowerLimits = line.getActivePowerLimits1().orElseThrow();
            assertEquals(3000, activePowerLimits.getPermanentLimit(), 0.1);
            assertEquals(1500, activePowerLimits.getTemporaryLimitValue(20), 0.1);
            temporaryLimit2 = activePowerLimits.getTemporaryLimit(20);
            assertEquals(1500, temporaryLimit2.getValue(), 0.1);
            assertEquals("ACL_TL1", temporaryLimit2.getName());
            assertFalse(temporaryLimit2.isFictitious());
            assertEquals(750, activePowerLimits.getTemporaryLimitValue(40), 0.1);
            temporaryLimit2 = activePowerLimits.getTemporaryLimit(40);
            assertEquals(750, temporaryLimit2.getValue(), 0.1);
            assertEquals("ACL_TL2", temporaryLimit2.getName());
            assertTrue(temporaryLimit2.isFictitious());

            activePowerLimits = line.getActivePowerLimits2().orElseThrow();
            assertEquals(4000, activePowerLimits.getPermanentLimit(), 0.1);
            assertEquals(2000, activePowerLimits.getTemporaryLimitValue(20), 0.1);
            temporaryLimit2 = activePowerLimits.getTemporaryLimit(20);
            assertEquals(2000, temporaryLimit2.getValue(), 0.1);
            assertEquals("ACL_TL3", temporaryLimit2.getName());
            assertFalse(temporaryLimit2.isFictitious());
            assertEquals(1000, activePowerLimits.getTemporaryLimitValue(40), 0.1);
            temporaryLimit2 = activePowerLimits.getTemporaryLimit(40);
            assertEquals(1000, temporaryLimit2.getValue(), 0.1);
            assertEquals("ACL_TL4", temporaryLimit2.getName());
            assertTrue(temporaryLimit2.isFictitious());

            activePowerLimits.setPermanentLimit(5000);
            apparentPowerLimits.setPermanentLimit(5000);
            assertEquals(5000, activePowerLimits.getPermanentLimit(), 0.1);
            assertEquals(5000, apparentPowerLimits.getPermanentLimit(), 0.1);
        }
    }

    @Test
    public void activePowerLimitsAdderValidationTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("networkTestCase", readNetwork.getId());

            assertThrows(ValidationException.class, () -> readNetwork.getLine("LINE1").newActivePowerLimits1().setPermanentLimit(15).beginTemporaryLimit().endTemporaryLimit().add())
                .getMessage().contains("temporary limit value is not set");
            assertThrows(ValidationException.class, () -> readNetwork.getLine("LINE1").newActivePowerLimits1().setPermanentLimit(15).beginTemporaryLimit().setValue(-2).endTemporaryLimit().add())
                .getMessage().contains("temporary limit value must be > 0");
            assertThrows(ValidationException.class, () -> readNetwork.getLine("LINE1").newActivePowerLimits1().setPermanentLimit(15).beginTemporaryLimit().setValue(2).endTemporaryLimit().add())
                .getMessage().contains("acceptable duration is not set");
            assertThrows(ValidationException.class, () -> readNetwork.getLine("LINE1").newActivePowerLimits1().setPermanentLimit(15).beginTemporaryLimit().setValue(2).setAcceptableDuration(-2).endTemporaryLimit().add())
                .getMessage().contains("acceptable duration must be >= 0");

            assertThrows(ValidationException.class, () -> readNetwork.getLine("LINE1").newActivePowerLimits1().setPermanentLimit(15).beginTemporaryLimit().ensureNameUnicity().setValue(2).setAcceptableDuration(2).endTemporaryLimit().add())
                .getMessage().contains("name is not set");
            readNetwork.getLine("LINE1").newActivePowerLimits1().setPermanentLimit(15)
                .beginTemporaryLimit().setName("name").ensureNameUnicity().setValue(2).setAcceptableDuration(2).endTemporaryLimit()
                .beginTemporaryLimit().setName("name").ensureNameUnicity().setValue(1).setAcceptableDuration(4).endTemporaryLimit()
                .add();
            assertEquals("name#0", readNetwork.getLine("LINE1").getActivePowerLimits1().orElseThrow().getTemporaryLimit(4).getName());
        }
    }

    @Test
    public void apparentPowerLimitsAdderValidationTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("networkTestCase", readNetwork.getId());

            assertThrows(ValidationException.class, () -> readNetwork.getLine("LINE1").newApparentPowerLimits1().setPermanentLimit(15).beginTemporaryLimit().endTemporaryLimit().add())
                .getMessage().contains("temporary limit value is not set");
            assertThrows(ValidationException.class, () -> readNetwork.getLine("LINE1").newApparentPowerLimits1().setPermanentLimit(15).beginTemporaryLimit().setValue(-2).endTemporaryLimit().add())
                .getMessage().contains("temporary limit value must be > 0");
            assertThrows(ValidationException.class, () -> readNetwork.getLine("LINE1").newApparentPowerLimits1().setPermanentLimit(15).beginTemporaryLimit().setValue(2).endTemporaryLimit().add())
                .getMessage().contains("acceptable duration is not set");
            assertThrows(ValidationException.class, () -> readNetwork.getLine("LINE1").newApparentPowerLimits1().setPermanentLimit(15).beginTemporaryLimit().setValue(2).setAcceptableDuration(-2).endTemporaryLimit().add())
                .getMessage().contains("acceptable duration must be >= 0");

            assertThrows(ValidationException.class, () -> readNetwork.getLine("LINE1").newApparentPowerLimits1().setPermanentLimit(15).beginTemporaryLimit().ensureNameUnicity().setValue(2).setAcceptableDuration(2).endTemporaryLimit().add())
                .getMessage().contains("name is not set");
            readNetwork.getLine("LINE1").newApparentPowerLimits1().setPermanentLimit(15)
                .beginTemporaryLimit().setName("name").ensureNameUnicity().setValue(2).setAcceptableDuration(2).endTemporaryLimit()
                .beginTemporaryLimit().setName("name").ensureNameUnicity().setValue(1).setAcceptableDuration(4).endTemporaryLimit()
                .add();
            assertEquals("name#0", readNetwork.getLine("LINE1").getApparentPowerLimits1().orElseThrow().getTemporaryLimit(4).getName());
        }
    }

    @Test
    public void batteryActivePowerControlTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("networkTestCase", readNetwork.getId());

            assertEquals(1, readNetwork.getBatteryCount());

            Battery battery = readNetwork.getBattery("battery");

            battery.addExtension(ActivePowerControl.class, new ActivePowerControlImpl<>(battery, false, 1.0f));
            ActivePowerControl activePowerControl = battery.getExtension(ActivePowerControl.class);
            assertFalse(activePowerControl.isParticipate());
            assertEquals(1.0f, activePowerControl.getDroop(), 0.01);

            activePowerControl = battery.getExtensionByName("activePowerControl");
            assertFalse(activePowerControl.isParticipate());
            assertEquals(1.0f, activePowerControl.getDroop(), 0.01);

            Collection<Extension<Battery>> extensions = battery.getExtensions();
            assertEquals(1, extensions.size());
            activePowerControl = (ActivePowerControl) extensions.iterator().next();
            assertFalse(activePowerControl.isParticipate());
            assertEquals(1.0f, activePowerControl.getDroop(), 0.01);
        }
    }

    @Test
    public void lccActivePowerControlTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("networkTestCase", readNetwork.getId());

            LccConverterStation lccConverterStation = readNetwork.getLccConverterStation("LCC2");

            assertThrows(UnsupportedOperationException.class, () -> lccConverterStation.addExtension(ActivePowerControl.class, new ActivePowerControlImpl<>(lccConverterStation, false, 1.0f)))
                .getMessage().contains("Cannot set ActivePowerControl");
            assertNull(lccConverterStation.getExtension(ActivePowerControl.class));
        }
    }

    @Test
    public void loadActivePowerControlTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = NetworkStorageTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals("networkTestCase", readNetwork.getId());

            Load load = readNetwork.getLoad("load1");

            assertThrows(UnsupportedOperationException.class, () -> load.addExtension(ActivePowerControl.class, new ActivePowerControlImpl<>(load, false, 1.0f)))
                .getMessage().contains("Cannot set ActivePowerControl");
            assertNull(load.getExtension(ActivePowerControl.class));
        }
    }

    @Test
    public void hvdcAngleDroopActivePowerControlExtensionTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = HvdcTestNetwork.createVsc(service.getNetworkFactory());
            HvdcLine hvdcLine = network.getHvdcLine("L");
            assertNull(hvdcLine.getExtension(HvdcAngleDroopActivePowerControl.class));
            assertNull(hvdcLine.getExtensionByName("hvdcAngleDroopActivePowerControl"));
            assertTrue(hvdcLine.getExtensions().isEmpty());
            hvdcLine.newExtension(HvdcAngleDroopActivePowerControlAdder.class)
                .withP0(10.0f)
                .withDroop(5.0f)
                .withEnabled(true)
                .add();
            assertNotNull(hvdcLine.getExtension(HvdcAngleDroopActivePowerControl.class));
            assertNotNull(hvdcLine.getExtensionByName("hvdcAngleDroopActivePowerControl"));
            assertFalse(hvdcLine.getExtensions().isEmpty());
            HvdcAngleDroopActivePowerControl hvdcAngleDroopActivePowerControl = hvdcLine.getExtension(HvdcAngleDroopActivePowerControl.class);
            assertEquals(10.0f, hvdcAngleDroopActivePowerControl.getP0(), 0.1f);
            assertEquals(5.0f, hvdcAngleDroopActivePowerControl.getDroop(), 0.1f);
            assertTrue(hvdcAngleDroopActivePowerControl.isEnabled());

            service.flush(network);

            hvdcAngleDroopActivePowerControl.setP0(20.0f);
            hvdcAngleDroopActivePowerControl.setDroop(80.0f);
            hvdcAngleDroopActivePowerControl.setEnabled(false);
            assertEquals(20.0f, hvdcAngleDroopActivePowerControl.getP0(), 0.1f);
            assertEquals(80.0f, hvdcAngleDroopActivePowerControl.getDroop(), 0.1f);
            assertFalse(hvdcAngleDroopActivePowerControl.isEnabled());

            assertEquals("L", hvdcAngleDroopActivePowerControl.getExtendable().getId());
            assertThrows(IllegalArgumentException.class, () -> hvdcAngleDroopActivePowerControl.setP0(Float.NaN));
            assertThrows(IllegalArgumentException.class, () -> hvdcAngleDroopActivePowerControl.setDroop(Float.NaN));
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network network = service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow(AssertionError::new));

            HvdcLine hvdcLine = network.getHvdcLine("L");
            assertNotNull(hvdcLine.getExtension(HvdcAngleDroopActivePowerControl.class));
            assertNotNull(hvdcLine.getExtensionByName("hvdcAngleDroopActivePowerControl"));
            assertFalse(hvdcLine.getExtensions().isEmpty());

            HvdcAngleDroopActivePowerControl hvdcAngleDroopActivePowerControl = hvdcLine.getExtension(HvdcAngleDroopActivePowerControl.class);
            assertEquals(10.0f, hvdcAngleDroopActivePowerControl.getP0(), 0.1f);
            assertEquals(5.0f, hvdcAngleDroopActivePowerControl.getDroop(), 0.1f);
            assertTrue(hvdcAngleDroopActivePowerControl.isEnabled());
        }
    }

    @Test
    public void hvdcOperatorActivePowerRangeExtensionTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = HvdcTestNetwork.createVsc(service.getNetworkFactory());
            HvdcLine hvdcLine = network.getHvdcLine("L");
            assertNull(hvdcLine.getExtension(HvdcOperatorActivePowerRange.class));
            assertNull(hvdcLine.getExtensionByName("hvdcOperatorActivePowerRange"));
            assertTrue(hvdcLine.getExtensions().isEmpty());
            hvdcLine.newExtension(HvdcOperatorActivePowerRangeAdder.class)
                .withOprFromCS1toCS2(15.0f)
                .withOprFromCS2toCS1(8.0f)
                .add();
            assertNotNull(hvdcLine.getExtension(HvdcOperatorActivePowerRange.class));
            assertNotNull(hvdcLine.getExtensionByName("hvdcOperatorActivePowerRange"));
            assertFalse(hvdcLine.getExtensions().isEmpty());
            HvdcOperatorActivePowerRange hvdcOperatorActivePowerRange = hvdcLine.getExtension(HvdcOperatorActivePowerRange.class);
            assertEquals(15.0f, hvdcOperatorActivePowerRange.getOprFromCS1toCS2(), 0.1f);
            assertEquals(8.0f, hvdcOperatorActivePowerRange.getOprFromCS2toCS1(), 0.1f);

            service.flush(network);

            hvdcOperatorActivePowerRange.setOprFromCS1toCS2(30.0f);
            hvdcOperatorActivePowerRange.setOprFromCS2toCS1(22.0f);
            assertEquals(30.0f, hvdcOperatorActivePowerRange.getOprFromCS1toCS2(), 0.1f);
            assertEquals(22.0f, hvdcOperatorActivePowerRange.getOprFromCS2toCS1(), 0.1f);

            assertEquals("L", hvdcOperatorActivePowerRange.getExtendable().getId());
            assertThrows(IllegalArgumentException.class, () -> hvdcOperatorActivePowerRange.setOprFromCS1toCS2(-1.0f));
            assertThrows(IllegalArgumentException.class, () -> hvdcOperatorActivePowerRange.setOprFromCS2toCS1(-2.0f));
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network network = service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow(AssertionError::new));

            HvdcLine hvdcLine = network.getHvdcLine("L");
            assertNotNull(hvdcLine.getExtension(HvdcOperatorActivePowerRange.class));
            assertNotNull(hvdcLine.getExtensionByName("hvdcOperatorActivePowerRange"));
            assertFalse(hvdcLine.getExtensions().isEmpty());

            HvdcOperatorActivePowerRange hvdcOperatorActivePowerRange = hvdcLine.getExtension(HvdcOperatorActivePowerRange.class);
            assertEquals(15.0f, hvdcOperatorActivePowerRange.getOprFromCS1toCS2(), 0.1f);
            assertEquals(8.0f, hvdcOperatorActivePowerRange.getOprFromCS2toCS1(), 0.1f);
        }
    }

    @Test
    public void cgmesControlAreaDanglingLineTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            // import new network in the store
            Network network = service.importNetwork(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource());
            CgmesControlAreas cgmesControlAreas = network.getExtension(CgmesControlAreas.class);
            assertNotNull(cgmesControlAreas);
            assertEquals(0, cgmesControlAreas.getCgmesControlAreas().size());
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            UUID networkUuid = networkIds.keySet().iterator().next();

            Network network = service.getNetwork(networkUuid);
            CgmesControlAreas cgmesControlAreas = network.getExtension(CgmesControlAreas.class);
            assertNotNull(cgmesControlAreas);
            assertEquals(0, cgmesControlAreas.getCgmesControlAreas().size());
            CgmesControlArea cgmesControlArea = cgmesControlAreas.newCgmesControlArea()
                .setId("ca1")
                .setEnergyIdentificationCodeEic("code")
                .setNetInterchange(1000)
                .add();
            cgmesControlArea.add(network.getGenerator("550ebe0d-f2b2-48c1-991f-cebea43a21aa").getTerminal());
            cgmesControlArea.add(network.getDanglingLine("a16b4a6c-70b1-4abf-9a9d-bd0fa47f9fe4").getBoundary());
            assertEquals(1, cgmesControlAreas.getCgmesControlAreas().size());
            CgmesControlArea ca1 = cgmesControlAreas.getCgmesControlArea("ca1");
            assertNotNull(ca1);

            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            UUID networkUuid = networkIds.keySet().iterator().next();

            Network network = service.getNetwork(networkUuid);
            CgmesControlAreas cgmesControlAreas = network.getExtension(CgmesControlAreas.class);
            assertNotNull(cgmesControlAreas);
            assertEquals(1, cgmesControlAreas.getCgmesControlAreas().size());
            assertTrue(cgmesControlAreas.containsCgmesControlAreaId("ca1"));
            CgmesControlArea cgmesControlArea = cgmesControlAreas.getCgmesControlArea("ca1");
            assertEquals("ca1", cgmesControlArea.getId());
            assertNull(cgmesControlArea.getName());
            assertEquals("code", cgmesControlArea.getEnergyIdentificationCodeEIC());
            assertEquals(1000, cgmesControlArea.getNetInterchange(), 0);
            assertEquals(1, cgmesControlArea.getTerminals().size());
            assertEquals(1, cgmesControlArea.getBoundaries().size());
        }
    }

    @Test
    public void baseVoltageMappingTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            // import new network in the store
            Network network = service.importNetwork(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource());
            assertNull(network.getExtension(Object.class));
            assertNull(network.getExtensionByName(""));
            BaseVoltageMapping baseVoltageMapping = network.getExtension(BaseVoltageMapping.class);
            assertNotNull(baseVoltageMapping);
            assertEquals(7, baseVoltageMapping.getBaseVoltages().size());
            assertFalse(baseVoltageMapping.isBaseVoltageEmpty());
            var ht = baseVoltageMapping.getBaseVoltage(400.0);
            assertNotNull(ht);
            assertEquals("65dd04e792584b3b912374e35dec032e", ht.getId());
            assertEquals(Source.BOUNDARY, ht.getSource());
            assertEquals(400.0, ht.getNominalV(), .1);

            assertFalse(baseVoltageMapping.isBaseVoltageMapped(42.0));
            assertNull(baseVoltageMapping.getBaseVoltage(42.0));
            // IGN do not remplace IGM
            baseVoltageMapping.addBaseVoltage(10.5, "somethingIGM", Source.IGM);
            assertNotEquals("somethingIGM", baseVoltageMapping.getBaseVoltage(10.5).getId());
            // BOUNDARY replace IGM
            baseVoltageMapping.addBaseVoltage(10.5, "something", Source.BOUNDARY);
            var bvs = BaseVoltageSourceAttribute.builder().id("something").nominalV(10.5).source(Source.BOUNDARY).build();
            assertEquals(bvs, baseVoltageMapping.getBaseVoltage(10.5));
            // BOUNDARY do not replace BOUNDARY
            baseVoltageMapping.addBaseVoltage(10.5, "somethingAgain", Source.BOUNDARY);
            assertNotEquals("somethingAgain", baseVoltageMapping.getBaseVoltage(10.5).getId());
            // IGM do not replace BOUNDARY
            baseVoltageMapping.addBaseVoltage(10.5, "somethingIGM", Source.IGM);
            assertNotEquals("somethingIGM", baseVoltageMapping.getBaseVoltage(10.5).getId());

            baseVoltageMapping.addBaseVoltage(42.0, "somethingElse", Source.IGM);
            var ft = baseVoltageMapping.getBaseVoltage(42.0);
            assertEquals("somethingElse", ft.getId());
            assertEquals(Source.IGM, ft.getSource());

            var baseVolatge = baseVoltageMapping.baseVoltagesByNominalVoltageMap();
            assertEquals(baseVoltageMapping.getBaseVoltages().size(), baseVolatge.size());
            service.flush(network);

        }
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            UUID networkUuid = networkIds.keySet().iterator().next();

            Network network = service.getNetwork(networkUuid);
            BaseVoltageMapping baseVoltageMapping = network.getExtensionByName("baseVoltageMapping");

            var ft = baseVoltageMapping.getBaseVoltage(42.0);
            assertNotNull(ft);
        }
    }

    @Test
    public void cgmesControlAreaTieLineTest() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            // import new network in the store
            service.importNetwork(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource());
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            UUID networkUuid = networkIds.keySet().iterator().next();

            Network network = service.getNetwork(networkUuid);
            CgmesControlAreas cgmesControlAreas = network.getExtension(CgmesControlAreas.class);
            assertNotNull(cgmesControlAreas);

            assertNotNull(cgmesControlAreas);
            assertEquals(0, cgmesControlAreas.getCgmesControlAreas().size());
            CgmesControlArea cgmesControlArea = cgmesControlAreas.newCgmesControlArea()
                .setId("ca2")
                .setEnergyIdentificationCodeEic("code2")
                .setNetInterchange(800)
                .add();
            cgmesControlArea.add(((TieLine) network.getLine("b18cd1aa-7808-49b9-a7cf-605eaf07b006 + e8acf6b6-99cb-45ad-b8dc-16c7866a4ddc")).getHalf1().getBoundary());
            assertEquals(1, cgmesControlAreas.getCgmesControlAreas().size());

            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            UUID networkUuid = networkIds.keySet().iterator().next();

            Network network = service.getNetwork(networkUuid);
            CgmesControlAreas cgmesControlAreas = network.getExtension(CgmesControlAreas.class);
            assertNotNull(cgmesControlAreas);

            assertEquals(1, cgmesControlAreas.getCgmesControlAreas().size());
            CgmesControlArea cgmesControlArea = cgmesControlAreas.getCgmesControlArea("ca2");
            assertNotNull(cgmesControlArea);
            assertEquals(1, cgmesControlArea.getBoundaries().size());
        }
    }

    @Test
    public void testImportWithoutFlush() {
        try (NetworkStoreService service = createNetworkStoreService()) {

            ReporterModel report = new ReporterModel("test", "test");

            Network network = service.importNetwork(getResource("test.xiidm", "/"), report, false);
            final UUID networkUuid1 = service.getNetworkUuid(network);

            assertTrue(assertThrows(PowsyblException.class, () -> service.getNetwork(networkUuid1)).getMessage().contains(String.format("Network '%s' not found", networkUuid1)));

            network = service.importNetwork(getResource("test.xiidm", "/"), report, true);
            UUID networkUuid2 = service.getNetworkUuid(network);
            service.getNetwork(networkUuid2);
        }
    }

    @Test
    public void testImportWithProperties() {
        try (NetworkStoreService service = createNetworkStoreService()) {

            ReporterModel report = new ReporterModel("test", "test");
            Properties importParameters = new Properties();
            importParameters.put("randomImportParameters", "randomImportValue");

            Network network = service.importNetwork(getResource("test.xiidm", "/"), report, importParameters, false);
            final UUID networkUuid1 = service.getNetworkUuid(network);

            assertTrue(assertThrows(PowsyblException.class, () -> service.getNetwork(networkUuid1)).getMessage().contains(String.format("Network '%s' not found", networkUuid1)));

            network = service.importNetwork(getResource("test.xiidm", "/"), report, importParameters, true);
            UUID networkUuid2 = service.getNetworkUuid(network);
            service.getNetwork(networkUuid2);
        }
    }

    @Test
    public void testImportWithReport() {
        try (NetworkStoreService service = createNetworkStoreService()) {

            ReporterModel report = new ReporterModel("test", "test");

            service.importNetwork(getResource("test.xiidm", "/"), report);
            assertTrue(report.getSubReporters().isEmpty());

            service.importNetwork(getResource("uctNetwork.uct", "/"), report);
            assertFalse(report.getSubReporters().isEmpty());

            service.importNetwork(getResource("uctNetwork.uct", "/"));

        }
    }

    @Test
    public void testRemoteReactivePowerControl() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            // import new network in the store
            Network network = service.importNetwork(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEReactivePowerGen().dataSource());
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            Generator g = readNetwork.getGenerator("3a3b27be-b18b-4385-b557-6735d733baf0");
            RemoteReactivePowerControl ext = g.getExtension(RemoteReactivePowerControl.class);
            assertNotNull(ext);
            assertEquals(115.5, ext.getTargetQ(), 0.0);
            assertTrue(ext.isEnabled());
            assertSame(readNetwork.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0").getTerminal2(), ext.getRegulatingTerminal());
        }

    }

    private ResourceDataSource getResource(String fileName, String path) {
        return new ResourceDataSource(FilenameUtils.getBaseName(fileName),
            new ResourceSet(FilenameUtils.getPath(path),
                FilenameUtils.getName(fileName)));
    }

    @Test
    public void testVariants() {
        // import network on initial variant
        UUID networkUuid;
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = EurostagTutorialExample1Factory.createWithMoreGenerators(service.getNetworkFactory());
            networkUuid = service.getNetworkUuid(network);
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.getNetwork(networkUuid);
            assertNotNull(network);

            // check LOAD initial variant p0 value
            Load load = network.getLoad("LOAD");
            assertEquals(600, load.getP0(), 0);

            // remove a generator before clone, should be removed in both variants
            Generator gen2 = network.getGenerator("GEN2");
            gen2.remove();
            // check removal GEN2 in initial variant
            assertNull(network.getGenerator("GEN2"));
            // check that GEN2 object is not usable anymore
            PowsyblException e = assertThrows(PowsyblException.class, gen2::getId);
            assertEquals("Object has been removed in current variant", e.getMessage());

            // update a generator before clone, should be updated in both variants
            Generator gen = network.getGenerator("GEN");
            gen.setTargetP(507);
            assertEquals(507, gen.getTargetP(), 0);

            // create a load before clone, should be created in both variants
            Load load2 = network.getVoltageLevel("VLGEN").newLoad()
                .setId("LOAD2")
                .setBus("NLOAD")
                .setConnectableBus("NLOAD")
                .setP0(800.0)
                .setQ0(550.0)
                .add();
            Load load2b = network.getLoad("LOAD2");
            assertEquals(800, load2.getP0(), 0);
            assertEquals(800, load2b.getP0(), 0);

            // clone initial variant to variant "v"
            network.getVariantManager().cloneVariant(INITIAL_VARIANT_ID, "v");

            // change load p0 value on "v" variant
            network.getVariantManager().setWorkingVariant("v");
            assertNotNull(load);
            load.setP0(601);
            assertEquals(601, load.getP0(), 0);

            // check removal on "v" variant
            assertNull(network.getGenerator("GEN2"));
            // check that GENERATOR2 object is not usable anymore
            PowsyblException e1 = assertThrows(PowsyblException.class, gen2::getId);
            assertEquals("Object has been removed in current variant", e1.getMessage());

            // check that GENERATOR is modified
            Generator genb = network.getGenerator("GEN");
            assertEquals(507, gen.getTargetP(), 0);
            assertEquals(507, genb.getTargetP(), 0);

            // check that created load exists on "v" variant
            Load load2c = network.getLoad("LOAD2");
            assertEquals(800, load2.getP0(), 0);
            assertEquals(800, load2b.getP0(), 0);
            assertEquals(800, load2c.getP0(), 0);

            // save network with its new variant
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.getNetwork(networkUuid);

            Load load = network.getLoad("LOAD");
            assertEquals(600, load.getP0(), 0);

            // check gen2 removal on initial variant
            assertNull(network.getGenerator("GEN2"));
            // check that GEN is modified on initial variant
            Generator gen = network.getGenerator("GEN");
            assertEquals(507, gen.getTargetP(), 0);
            // check that LOAD2 is created on initial variant
            Load load2 = network.getLoad("LOAD2");
            assertEquals(800, load2.getP0(), 0);

            // check we can get "v" again and p0 value is correct
            network.getVariantManager().setWorkingVariant("v");
            assertEquals(601, load.getP0(), 0);

            // check gen2 removal on "v" variant
            assertNull(network.getGenerator("GEN2"));
            // check that GEN is modified "v" variant
            Generator genb = network.getGenerator("GEN");
            assertEquals(507, gen.getTargetP(), 0);
            assertEquals(507, genb.getTargetP(), 0);
            // check that LOAD2 is created "v" variant
            Load load2b = network.getLoad("LOAD2");
            assertEquals(800, load2.getP0(), 0);
            assertEquals(800, load2b.getP0(), 0);

            // remove LOAD on initial variant
            network.getVariantManager().setWorkingVariant(INITIAL_VARIANT_ID);
            load.remove();
            assertNull(network.getLoad("LOAD"));

            // check that LOAD object is not usable anymore
            PowsyblException e = assertThrows(PowsyblException.class, load::getId);
            assertEquals("Object has been removed in current variant", e.getMessage());

            // switch to "v" variant and check LOAD exists again
            network.getVariantManager().setWorkingVariant("v");
            assertNotNull(network.getLoad("LOAD"));
            assertEquals(601, load.getP0(), 0);

            // save LOAD removal on initial variant
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.getNetwork(networkUuid);

            // check LOAD still exists on initial variant
            network.getVariantManager().setWorkingVariant("v");
            assertNotNull(network.getLoad("LOAD"));

            // check LOAD is still removed on variant "v"
            network.getVariantManager().setWorkingVariant(INITIAL_VARIANT_ID);
            assertNull(network.getLoad("LOAD"));
        }

        // assert http client type call (for performance regression testing)
        var metrics = new RestClientMetrics();
        try (NetworkStoreService service = createNetworkStoreService(metrics)) {
            Network network = service.getNetwork(networkUuid);
            assertEquals(1, metrics.oneGetterCallCount);
            assertEquals(0, metrics.allGetterCallCount);
            metrics.reset();
            network.getLines();
            assertEquals(0, metrics.oneGetterCallCount);
            assertEquals(1, metrics.allGetterCallCount);
            metrics.reset();
            network.getVariantManager().setWorkingVariant("v");
            // when switch from initial variant to "v" variant, we should reuse the same loading granularity
            // (one, some, all) as loading on initial variant
            assertEquals(1, metrics.oneGetterCallCount);
            assertEquals(1, metrics.allGetterCallCount);
        }
    }

    @Test
    public void emptyCacheCloneTest() {
        UUID networkUuid;
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = EurostagTutorialExample1Factory.create(service.getNetworkFactory());
            networkUuid = service.getNetworkUuid(network);
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.getNetwork(networkUuid);
            assertNotNull(network);

            // clone initial variant to variant "v" while nothing has been cached
            network.getVariantManager().cloneVariant(INITIAL_VARIANT_ID, "v");
            network.getVariantManager().setWorkingVariant("v");

            // check LOAD initial variant exists
            Load load = network.getLoad("LOAD");
            assertNotNull(load);
            assertEquals(600, load.getP0(), 0);
        }

        // Using NetworkStoreService.cloneVariant
        // For an empty cache and buffer this should be the same as network.getVariantManager().cloneVariant()
        try (NetworkStoreService service = createNetworkStoreService()) {
            // clone initial variant to variant "v2" while nothing has been cached or modified
            service.cloneVariant(networkUuid, INITIAL_VARIANT_ID, "v2");
        }
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.getNetwork(networkUuid);
            assertNotNull(network);
            network.getVariantManager().setWorkingVariant("v2");

            // check LOAD variant v2 exists
            Load load = network.getLoad("LOAD");
            assertNotNull(load);
            assertEquals(600, load.getP0(), 0);
        }

        // Using NetworkStoreService.cloneVariant
        // With things in the buffer and cache, after a flush the clone should work.
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.getNetwork(networkUuid);
            assertNotNull(network);
            network.getVariantManager().setWorkingVariant("v2");

            // check LOAD variant v2 exists and modify
            Load load = network.getLoad("LOAD");
            assertNotNull(load);
            assertEquals(600, load.getP0(), 0);
            load.setP0(700);

            // clone initial variant after flush
            service.flush(network);
            service.cloneVariant(networkUuid, "v2", "v3");
        }
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.getNetwork(networkUuid);
            assertNotNull(network);
            network.getVariantManager().setWorkingVariant("v3");

            // check LOAD variant v3 exists and is modified
            Load load = network.getLoad("LOAD");
            assertNotNull(load);
            assertEquals(700, load.getP0(), 0);
        }

        // Using NetworkStoreService.cloneVariant, testing overwrite error
        try (NetworkStoreService service = createNetworkStoreService()) {
            // clone initial variant over existing
            PowsyblException ex = assertThrows(PowsyblException.class,
                () -> service.cloneVariant(networkUuid, INITIAL_VARIANT_ID, "v3"));
            assertTrue(ex.getMessage().contains("already exists"));
        }
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.getNetwork(networkUuid);
            assertNotNull(network);
            network.getVariantManager().setWorkingVariant("v3");

            // check LOAD variant v3 exists and is still modified
            Load load = network.getLoad("LOAD");
            assertNotNull(load);
            assertEquals(700, load.getP0(), 0);
        }

        // Using NetworkStoreService.cloneVariant, testing maybeOverwrite
        try (NetworkStoreService service = createNetworkStoreService()) {
            // clone initial variant over existing with mayOverwrite=true
            service.cloneVariant(networkUuid, INITIAL_VARIANT_ID, "v3", true);
        }
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.getNetwork(networkUuid);
            assertNotNull(network);
            network.getVariantManager().setWorkingVariant("v3");

            // check LOAD variant v3 exists and is restored to initial
            Load load = network.getLoad("LOAD");
            assertNotNull(load);
            assertEquals(600, load.getP0(), 0);
        }

        // Using NetworkStoreService.cloneVariant, testing overwrite initial
        try (NetworkStoreService service = createNetworkStoreService()) {
            // clone initial variant over existing with mayOverwrite=true
            PowsyblException ex = assertThrows(PowsyblException.class,
                () -> service.cloneVariant(networkUuid, "v2", INITIAL_VARIANT_ID, true));
            assertTrue(ex.getMessage().contains("forbidden"));
        }
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.getNetwork(networkUuid);
            assertNotNull(network);
            network.getVariantManager().setWorkingVariant("v3");

            // check LOAD variant initial exists and not modified
            Load load = network.getLoad("LOAD");
            assertNotNull(load);
            assertEquals(600, load.getP0(), 0);
        }
    }

    @Test
    public void testVariantRemove() {
        // import network on initial variant
        UUID networkUuid;
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = EurostagTutorialExample1Factory.create(service.getNetworkFactory());
            networkUuid = service.getNetworkUuid(network);
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.getNetwork(networkUuid);

            // there is only initial variant
            assertEquals(1, network.getVariantManager().getVariantIds().size());

            // clone initial variant to "v" and check there now 2 variants
            network.getVariantManager().cloneVariant(INITIAL_VARIANT_ID, "v");
            assertEquals(2, network.getVariantManager().getVariantIds().size());
            network.getVariantManager().setWorkingVariant("v");

            // remove variant "v" and check we have only one variant
            network.getVariantManager().removeVariant("v");
            assertEquals(1, network.getVariantManager().getVariantIds().size());
            assertEquals(INITIAL_VARIANT_ID, network.getVariantManager().getWorkingVariantId());

            // check that we can recreate a new variant with same id "v"
            network.getVariantManager().cloneVariant(INITIAL_VARIANT_ID, "v");
            assertEquals(2, network.getVariantManager().getVariantIds().size());

            // check that we cannot create a new variant with same id
            PowsyblException e = assertThrows(PowsyblException.class, () -> network.getVariantManager().cloneVariant(INITIAL_VARIANT_ID, "v"));
            assertEquals("Variant 'v' already exists", e.getMessage());

            // change LOAD p0 on variant "v"
            network.getVariantManager().setWorkingVariant("v");
            Load load = network.getLoad("LOAD");
            assertNotNull(load);
            load.setP0(666);

            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.getNetwork(networkUuid);

            // check that on variant "v", we still have 666 as p0 for LOAD
            network.getVariantManager().setWorkingVariant("v");
            Load load = network.getLoad("LOAD");
            assertNotNull(load);
            assertEquals(666, load.getP0(), 0);

            // overwrite variant "v" by initial variant and check that LOAD p0 has been reverted to 600
            network.getVariantManager().cloneVariant(INITIAL_VARIANT_ID, "v", true);
            assertNotNull(load);
            assertEquals(600, load.getP0(), 0);
        }
    }

    @Test
    public void testVoltageLevelWithoutSubstation() {
        UUID networkUuid;
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.createNetwork("networknosubstation", "test");
            networkUuid = service.getNetworkUuid(network);
            network.newVoltageLevel()
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setId("bbVL")
                .setName("bbVL_name")
                .setNominalV(200.0)
                .setLowVoltageLimit(100.0)
                .setHighVoltageLimit(200.0)
                .add();
            VoltageLevel voltageLevel = network.getVoltageLevel("bbVL");
            assertNotNull(voltageLevel);
            assertEquals(200.0, voltageLevel.getNominalV(), 0.0);
            assertEquals(100.0, voltageLevel.getLowVoltageLimit(), 0.0);
            assertEquals(200.0, voltageLevel.getHighVoltageLimit(), 0.0);
            assertEquals(ContainerType.VOLTAGE_LEVEL, voltageLevel.getContainerType());
            assertTrue(voltageLevel.getSubstation().isEmpty());

            assertTrue(Iterables.isEmpty(voltageLevel.getConnectables()));
            voltageLevel.getBusBreakerView().newBus().setId("bbVL_1").add();
            Load load = voltageLevel.newLoad()
                .setId("LOAD")
                .setBus("bbVL_1")
                .setP0(600.0)
                .setQ0(200.0)
                .add();
            assertEquals(1, Iterables.size(voltageLevel.getConnectables()));
            assertTrue(Iterables.contains(voltageLevel.getConnectables(), load));

            service.flush(network);
        }
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = service.getNetwork(networkUuid);
            VoltageLevel voltageLevel = network.getVoltageLevel("bbVL");
            assertNotNull(voltageLevel);
            assertEquals(200.0, voltageLevel.getNominalV(), 0.0);
            assertEquals(100.0, voltageLevel.getLowVoltageLimit(), 0.0);
            assertEquals(200.0, voltageLevel.getHighVoltageLimit(), 0.0);
            assertEquals(ContainerType.VOLTAGE_LEVEL, voltageLevel.getContainerType());
            assertTrue(voltageLevel.getSubstation().isEmpty());

            Load load = network.getLoad("LOAD");
            assertEquals(1, Iterables.size(voltageLevel.getConnectables()));
            assertTrue(Iterables.contains(voltageLevel.getConnectables(), load));
        }
    }

    @Test
    public void testNanValues() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            service.flush(createGeneratorNetwork(service.getNetworkFactory(), ReactiveLimitsKind.MIN_MAX));
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());
            assertEquals("Generator network", readNetwork.getId());

            Generator generator = readNetwork.getGeneratorStream().findFirst().get();
            assertEquals("GEN", generator.getId());

            generator.getTerminal().setP(Double.NaN);
            generator.getTerminal().setQ(Double.NaN);

            assertEquals(Double.NaN, generator.getTerminal().getP(), .0001);
            assertEquals(Double.NaN, generator.getTerminal().getQ(), .0001);

            service.flush(readNetwork);  // flush the network
        }

        // reload modified network
        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            Generator generator = readNetwork.getGeneratorStream().findFirst().get();
            assertNotNull(generator);

            assertEquals(Double.NaN, generator.getTerminal().getP(), .0001);
            assertEquals(Double.NaN, generator.getTerminal().getQ(), .0001);
        }
    }

    @Test
    public void testNpeWithTemporaryLimits() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            var network = EurostagTutorialExample1Factory.create(service.getNetworkFactory());
            var l = network.getLine("NHV1_NHV2_1");
            l.newCurrentLimits1()
                    .setPermanentLimit(1000)
                    .add();
            service.flush(network);
        }

        try (NetworkStoreService service = createNetworkStoreService()) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            assertEquals(1, networkIds.size());
            Network network = service.getNetwork(networkIds.keySet().stream().findFirst().orElseThrow());
            var l = network.getLine("NHV1_NHV2_1");
            assertTrue(l.getCurrentLimits1().orElseThrow().getTemporaryLimits().isEmpty());
            assertNull(l.getCurrentLimits1().orElseThrow().getTemporaryLimit(60 * 20));
        }
    }

    @Test
    public void testIncrementalUpdate() {
        try (NetworkStoreService service = createNetworkStoreService()) {
            Network network = EurostagTutorialExample1Factory.create(service.getNetworkFactory());
            network.getBusView().getBuses(); // force storing calculated topology and connectivity
            service.flush(network);
        }

        var metrics = new RestClientMetrics();
        try (NetworkStoreService service = createNetworkStoreService(metrics)) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            UUID networkUuid = networkIds.keySet().stream().findFirst().orElseThrow();
            Network network = service.getNetwork(networkUuid);
            Load load = network.getLoad("LOAD");
            load.getTerminal().setP(13.45d);
            Generator gen = network.getGenerator("GEN");
            gen.getTerminal().setP(1).setQ(2);
            Line l1 = network.getLine("NHV1_NHV2_1");
            l1.getTerminal1().setP(12.3);
            Line l2 = network.getLine("NHV1_NHV2_2");
            l2.getTerminal1().setQ(1.45);
            l2.setX(1.35);
            VoltageLevel vlgen = network.getVoltageLevel("VLGEN");
            for (Bus b : vlgen.getBusView().getBuses()) {
                b.setV(399).setAngle(4);
            }
            Bus nload = network.getBusBreakerView().getBus("NLOAD");
            nload.setV(25);
            TwoWindingsTransformer twt1 = network.getTwoWindingsTransformer("NGEN_NHV1");
            twt1.getTerminal2().setP(100);
            service.flush(network);

            assertEquals(Set.of("/networks/" + networkUuid + "/generators/sv",               // GEN only SV
                                "/networks/" + networkUuid + "/voltage-levels/sv",           // VLGEN only SV
                                "/networks/" + networkUuid + "/loads/sv",                    // LOAD only SV
                                "/networks/" + networkUuid + "/lines",                       // NHV1_NHV2_2 full
                                "/networks/" + networkUuid + "/configured-buses",            // NLOAD full because not optimized (useless)
                                "/networks/" + networkUuid + "/lines/sv",                    // NHV1_NHV2_1 only SV
                                "/networks/" + networkUuid + "/2-windings-transformers/sv"), // NGEN_NHV1 only SV
                    metrics.updatedUrls);
        }

        try (NetworkStoreService service = createNetworkStoreService(metrics)) {
            Map<UUID, String> networkIds = service.getNetworkIds();
            UUID networkUuid = networkIds.keySet().stream().findFirst().orElseThrow();
            Network network = service.getNetwork(networkUuid);
            Load load = network.getLoad("LOAD");
            assertEquals(13.45d, load.getTerminal().getP(), 0);
            Generator gen = network.getGenerator("GEN");
            assertEquals(1, gen.getTerminal().getP(), 0);
            assertEquals(2, gen.getTerminal().getQ(), 0);
            Line l1 = network.getLine("NHV1_NHV2_1");
            assertEquals(12.3, l1.getTerminal1().getP(), 0);
            Line l2 = network.getLine("NHV1_NHV2_2");
            assertEquals(1.45, l2.getTerminal1().getQ(), 0);
            assertEquals(1.35, l2.getX(), 0);
            VoltageLevel vlgen = network.getVoltageLevel("VLGEN");
            for (Bus b : vlgen.getBusView().getBuses()) {
                assertEquals(399, b.getV(), 0);
                assertEquals(4, b.getAngle(), 0);
            }
            Bus nload = network.getBusBreakerView().getBus("NLOAD");
            assertEquals(25, nload.getV(), 0);
            TwoWindingsTransformer twt1 = network.getTwoWindingsTransformer("NGEN_NHV1");
            assertEquals(100, twt1.getTerminal2().getP(), 0);
        }
    }
}

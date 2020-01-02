/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.integration;

import com.google.common.collect.ImmutableSet;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.server.CassandraConfig;
import com.powsybl.network.store.server.CassandraConstants;
import com.powsybl.network.store.server.NetworkStoreApplication;
import org.cassandraunit.spring.CassandraDataSet;
import org.cassandraunit.spring.CassandraUnitDependencyInjectionTestExecutionListener;
import org.cassandraunit.spring.CassandraUnitTestExecutionListener;
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
@TestExecutionListeners(listeners = {CassandraUnitDependencyInjectionTestExecutionListener.class,
                                     CassandraUnitTestExecutionListener.class},
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

            assertEquals(readNetwork.getId(), "svcTestCase");

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

            assertEquals(readNetwork.getId(), "svcTestCase");

            assertEquals(2, readNetwork.getVscConverterStationCount());

            Stream<VscConverterStation> vscConverterStationsStream = readNetwork.getVscConverterStationStream();
            VscConverterStation vscConverterStation = vscConverterStationsStream.filter(vsc -> vsc.getId().equals("VSC1")).findFirst().get();
            assertEquals("VSC1", vscConverterStation.getId());
            assertEquals(24, vscConverterStation.getLossFactor(), 0.1);
            assertEquals(300, vscConverterStation.getReactivePowerSetpoint(), 0.1);
            assertEquals(true, vscConverterStation.isVoltageRegulatorOn());
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
            assertEquals(false, vscConverterStation2.isVoltageRegulatorOn());
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

            assertEquals(readNetwork.getId(), "svcTestCase");

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
    public void hvdcLineTest() {
        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {
            Network network = SvcTestCaseFactory.create(service.getNetworkFactory());
            service.flush(network);
        }

        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {

            Map<UUID, String> networkIds = service.getNetworkIds();

            assertEquals(1, networkIds.size());

            Network readNetwork = service.getNetwork(networkIds.keySet().stream().findFirst().get());

            assertEquals(readNetwork.getId(), "svcTestCase");

            assertEquals(1, readNetwork.getHvdcLineCount());

            Stream<HvdcLine> hvdcLines = readNetwork.getHvdcLineStream();
            HvdcLine hvdcLine = hvdcLines.findFirst().get();
            assertEquals(hvdcLine.getR(), 256, 0.1);
            assertEquals(hvdcLine.getConvertersMode(), HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER);
            assertEquals(hvdcLine.getActivePowerSetpoint(), 330, 0.1);
            assertEquals(hvdcLine.getNominalV(), 335, 0.1);
            assertEquals(hvdcLine.getMaxP(), 390, 0.1);
            //assertEquals(hvdcLine.getConverterStation1(), "id536");
            //assertEquals(hvdcLine.getConverterStation2(), "id1089");
        }
    }

    @Test
    public void moreComplexNodeBreakerTest() {
        try (NetworkStoreService service = new NetworkStoreService(getBaseUrl())) {
            Network network = FictitiousSwitchFactory.create(service.getNetworkFactory());
            service.flush(network);
        }
    }
}

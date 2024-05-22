/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.OperatingStatus;
import com.powsybl.network.store.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
class ExtensionAttributesTest extends AbstractSerDeTest {

    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
    }

    private static ObjectMapper createObjectMapper() {
        return JsonUtil.createObjectMapper();
    }

    private static void write(ExtensionAttributes values, Path jsonFile) {
        try (OutputStream out = Files.newOutputStream(jsonFile)) {
            createObjectMapper().writerWithDefaultPrettyPrinter().writeValue(out, values);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static ExtensionAttributes read(Path jsonFile) {
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return createObjectMapper().readerFor(ExtensionAttributes.class).readValue(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void roundTripGeneratorStartup() throws IOException {
        GeneratorStartupAttributes generatorStartupAttributes = new GeneratorStartupAttributes(0.5, 10, 5, 3, 5);
        roundTripTest(generatorStartupAttributes, ExtensionAttributesTest::write, ExtensionAttributesTest::read, "/extensions/GeneratorStartupAttributes.json");
    }

    @Test
    void roundTripActivePowerControl() throws IOException {
        ActivePowerControlAttributes activePowerControlAttributes = new ActivePowerControlAttributes(true, 12.0, 0.5);
        roundTripTest(activePowerControlAttributes, ExtensionAttributesTest::write, ExtensionAttributesTest::read, "/extensions/ActivePowerControlAttributes.json");
    }

    @Test
    void roundTripOperatingStatus() throws IOException {
        OperatingStatusAttributes generatorStartupAttributes = new OperatingStatusAttributes(OperatingStatus.Status.FORCED_OUTAGE.toString());
        roundTripTest(generatorStartupAttributes, ExtensionAttributesTest::write, ExtensionAttributesTest::read, "/extensions/OperatingStatusAttributes.json");
    }

    @Test
    void roundTripReferencePriorities() throws IOException {
        var referencePriorityAttributes1 = new ReferencePriorityAttributes(new TerminalRefAttributes("id1", "side1"), 1);
        var referencePriorityAttributes2 = new ReferencePriorityAttributes(new TerminalRefAttributes("id2", "side2"), 2);
        ReferencePrioritiesAttributes referencePrioritiesAttributes = new ReferencePrioritiesAttributes(List.of(referencePriorityAttributes1, referencePriorityAttributes2));
        roundTripTest(referencePrioritiesAttributes, ExtensionAttributesTest::write, ExtensionAttributesTest::read, "/extensions/ReferencePriorityAttributes.json");
    }

    @Test
    void roundTripCgmesMetadataModels() throws IOException {
        var cgmesMetadataModelAttributes1 = new CgmesMetadataModelAttributes(CgmesSubset.EQUIPMENT, "id1", "descr", 1, "test1", List.of("profile1", "profile2"), List.of("dep1", "dep2"), List.of());
        var cgmesMetadataModelAttributes2 = new CgmesMetadataModelAttributes(CgmesSubset.DYNAMIC, "id2", "descr2", 2, "test2", List.of("profile2"), List.of("dep2"), List.of("ss1"));
        CgmesMetadataModelsAttributes cgmesMetadataModelsAttributes = new CgmesMetadataModelsAttributes(List.of(cgmesMetadataModelAttributes1, cgmesMetadataModelAttributes2));
        roundTripTest(cgmesMetadataModelsAttributes, ExtensionAttributesTest::write, ExtensionAttributesTest::read, "/extensions/CgmesMetadataModelAttributes.json");
    }

    @Test
    void roundTripSubstationPosition() throws IOException {
        SubstationPositionAttributes substationPositionAttributes = new SubstationPositionAttributes(new Coordinate(46.11, 49.23));
        roundTripTest(substationPositionAttributes, ExtensionAttributesTest::write, ExtensionAttributesTest::read, "/extensions/SubstationPositionAttributes.json");
    }

    @Test
    void roundTripLinePosition() throws IOException {
        LinePositionAttributes linePositionAttributes = new LinePositionAttributes(List.of(new Coordinate(46.11, 49.23), new Coordinate(50.23, 12.3)));
        roundTripTest(linePositionAttributes, ExtensionAttributesTest::write, ExtensionAttributesTest::read, "/extensions/LinePositionAttributes.json");
    }
}

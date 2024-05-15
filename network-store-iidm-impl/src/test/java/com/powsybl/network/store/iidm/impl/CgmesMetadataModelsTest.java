/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.extensions.CgmesMetadataModels;
import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.iidm.network.Network;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
class CgmesMetadataModelsTest {
    @Test
    void test() {
        Network network = Network.read(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource());
        CgmesMetadataModels cgmesMetadataModels = network.getExtension(CgmesMetadataModels.class);
        assertNotNull(cgmesMetadataModels);
        assertEquals(9, cgmesMetadataModels.getModels().size());
        Optional<CgmesMetadataModel> cgmesSvModel = cgmesMetadataModels.getModelForSubset(CgmesSubset.STATE_VARIABLES);
        assertTrue(cgmesSvModel.isPresent());
        assertEquals(CgmesSubset.STATE_VARIABLES, cgmesSvModel.get().getSubset());
        assertEquals("urn:uuid:c2960b34-0a04-4cd1-9c4d-f3112d85ec6c", cgmesSvModel.get().getId());
        assertEquals(2, cgmesSvModel.get().getVersion());
        assertEquals("http://elia.be/CGMES/2.4.15", cgmesSvModel.get().getModelingAuthoritySet());
        assertTrue(cgmesSvModel.get().getSupersedes().isEmpty());
        assertEquals(3, cgmesSvModel.get().getDependentOn().size());
        assertTrue(CollectionUtils.isEqualCollection(Set.of("urn:uuid:d400c631-75a0-4c30-8aed-832b0d282e73", "urn:uuid:f2f43818-09c8-4252-9611-7af80c398d20", "urn:uuid:2399cbd1-9a39-11e0-aa80-0800200c9a66"), cgmesSvModel.get().getDependentOn()));
        assertEquals(1, cgmesSvModel.get().getProfiles().size());
        assertTrue(CollectionUtils.isEqualCollection(Set.of("http://entsoe.eu/CIM/StateVariables/4/1"), cgmesSvModel.get().getProfiles()));

        Optional<CgmesMetadataModel> cgmesSshModel = cgmesMetadataModels.getModelForSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS);
        assertTrue(cgmesSshModel.isPresent());
        assertEquals(CgmesSubset.STEADY_STATE_HYPOTHESIS, cgmesSshModel.get().getSubset());
        assertEquals("urn:uuid:52b712d1-f3b0-4a59-9191-79f2fb1e4c4e", cgmesSshModel.get().getId());
        assertEquals(2, cgmesSshModel.get().getVersion());
        assertEquals("http://elia.be/CGMES/2.4.15", cgmesSshModel.get().getModelingAuthoritySet());
        assertTrue(cgmesSshModel.get().getSupersedes().isEmpty());
        assertEquals(1, cgmesSshModel.get().getDependentOn().size());
        assertTrue(CollectionUtils.isEqualCollection(Set.of("urn:uuid:d400c631-75a0-4c30-8aed-832b0d282e73"), cgmesSshModel.get().getDependentOn()));
        assertEquals(1, cgmesSshModel.get().getProfiles().size());
        assertTrue(CollectionUtils.isEqualCollection(Set.of("http://entsoe.eu/CIM/SteadyStateHypothesis/1/1"), cgmesSshModel.get().getProfiles()));
    }
}

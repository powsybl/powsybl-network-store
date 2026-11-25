/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.network.store.model.ActivePowerControlAttributes;
import com.powsybl.network.store.model.BusbarSectionAttributes;
import com.powsybl.network.store.model.ExtensionAttributes;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
class BusBarSectionToInjectionAdapterTest {

    @Test
    void test() {
        String name = "name";
        Map<String, String> properties = Map.of("prop1", "val1");
        Map<String, String> aliasByType = Map.of("alias1", "val1");
        Set<String> aliasWithoutType = Set.of("alias1");
        boolean isFictitious = false;
        Map<String, ExtensionAttributes> extensionAttributes = Map.of("activePowerControl", new ActivePowerControlAttributes());

        BusbarSectionAttributes bbsAttributes = new BusbarSectionAttributes();
        BusbarSectionToInjectionAdapter bbsToInjectionAdapter = new BusbarSectionToInjectionAdapter(bbsAttributes);
        bbsToInjectionAdapter.setName(name);
        bbsToInjectionAdapter.setProperties(properties);
        bbsToInjectionAdapter.setAliasByType(aliasByType);
        bbsToInjectionAdapter.setAliasesWithoutType(aliasWithoutType);
        bbsToInjectionAdapter.setFictitious(isFictitious);
        bbsToInjectionAdapter.setExtensionAttributes(extensionAttributes);

        assertEquals(name, bbsToInjectionAdapter.getName());
        assertEquals(properties, bbsToInjectionAdapter.getProperties());
        assertEquals(aliasByType, bbsToInjectionAdapter.getAliasByType());
        assertEquals(aliasWithoutType, bbsToInjectionAdapter.getAliasesWithoutType());
        assertEquals(isFictitious, bbsToInjectionAdapter.isFictitious());
        assertEquals(extensionAttributes, bbsToInjectionAdapter.getExtensionAttributes());
    }
}

/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.DefaultNetworkListener;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkListener;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author David Braquart <david.braquart at rte-france.com>
 */
public class PropertiesTest {

    @Test
    public void testPropertiesChangesNotification() {
        NetworkListener mockedListener = Mockito.mock(DefaultNetworkListener.class);
        Network network = CreateNetworksUtil.createNodeBreakerNetworkWithLine();
        network.addListener(mockedListener);
        Load load = network.getLoad("L");

        load.setProperty("k", "v");
        Mockito.verify(mockedListener, Mockito.times(1)).onPropertyAdded(load, "properties[k]", "v");
        load.setProperty("k", "v-update");
        Mockito.verify(mockedListener, Mockito.times(1)).onPropertyReplaced(load, "properties[k]", "v", "v-update");
        load.setProperty("k2", "v2");
        Mockito.verify(mockedListener, Mockito.times(1)).onPropertyAdded(load, "properties[k2]", "v2");
        assertEquals(Set.of("k", "k2"), load.getPropertyNames());

        assertTrue(load.removeProperty("k"));
        Mockito.verify(mockedListener, Mockito.times(1)).onPropertyRemoved(load, "properties[k]", "v-update");
        assertTrue(load.removeProperty("k2"));
        Mockito.verify(mockedListener, Mockito.times(1)).onPropertyRemoved(load, "properties[k2]", "v2");
        assertFalse(load.removeProperty("k"));
        Mockito.verifyNoMoreInteractions(mockedListener);
        assertFalse(load.removeProperty("unknown"));
        Mockito.verifyNoMoreInteractions(mockedListener);

        assertEquals(Set.of(), load.getPropertyNames());
    }
}

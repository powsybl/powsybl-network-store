/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.model.VariantInfos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * @author Joris Mancini <joris.mancini_externe at rte-france.com>
 */
class VariantManagerImplTest {

    private VariantManagerImpl variantManager;

    private NetworkStoreClient networkStoreClient;

    @BeforeEach
    void setUp() {
        NetworkObjectIndex networkObjectIndex = Mockito.mock(NetworkObjectIndex.class);
        networkStoreClient = Mockito.mock(NetworkStoreClient.class);
        NetworkImpl network = Mockito.mock(NetworkImpl.class);

        when(network.getUuid()).thenReturn(UUID.randomUUID());
        when(networkObjectIndex.getStoreClient()).thenReturn(networkStoreClient);
        when(networkObjectIndex.getNetwork()).thenReturn(network);
        when(networkObjectIndex.getWorkingVariantNum()).thenReturn(0);
        when(networkStoreClient.getVariantsInfos(any())).thenReturn(List.of(new VariantInfos("id", 0)));

        variantManager = new VariantManagerImpl(networkObjectIndex);
    }

    @Test
    void testCloneFailureAfterAllAttempts() {
        doThrow(DuplicateVariantNumException.class).when(networkStoreClient).cloneNetwork(any(), anyInt(), anyInt(), any());

        assertThrows(PowsyblException.class, () -> variantManager.cloneVariant("id", "id2"), "Impossible to clone variant after 3 attempts");
    }

    @Test
    void testCloneFailureButSuccessAfterAll() {
        // Throw twice then success
        doThrow(DuplicateVariantNumException.class)
            .doThrow(DuplicateVariantNumException.class)
            .doNothing()
            .when(networkStoreClient).cloneNetwork(any(), anyInt(), anyInt(), any());

        assertDoesNotThrow(() -> variantManager.cloneVariant("id", "id2"));
    }
}

/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.model.AttributeFilter;
import com.powsybl.network.store.model.LoadAttributes;
import com.powsybl.network.store.model.Resource;
import org.apache.logging.log4j.util.TriConsumer;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CollectionBufferTest {

    private static final UUID NETWORK_UUID = UUID.randomUUID();

    private CollectionBuffer<LoadAttributes> collectionBuffer;

    private Resource<LoadAttributes> l1;
    private Resource<LoadAttributes> l2;

    private final List<Resource<LoadAttributes>> created = new ArrayList<>();
    private final List<Resource<LoadAttributes>> updated = new ArrayList<>();
    private final List<String> removed = new ArrayList<>();

    private final UUID uuid = UUID.randomUUID();

    @Before
    public void setUp() throws Exception {
        created.clear();
        updated.clear();
        removed.clear();

        l1 = Resource.loadBuilder()
                .id("l1")
                .attributes(LoadAttributes.builder()
                        .voltageLevelId("vl1")
                        .build())
                .build();
        l2 = Resource.loadBuilder()
                .id("l2")
                .attributes(LoadAttributes.builder()
                        .voltageLevelId("vl1")
                        .build())
                .build();

        BiConsumer<UUID, List<Resource<LoadAttributes>>> createFct = (uuid, resources) -> created.addAll(resources);
        TriConsumer<UUID, List<Resource<LoadAttributes>>, AttributeFilter> updateFct = (uuid, resources, attributeFilter) -> updated.addAll(resources);
        TriConsumer<UUID, Integer, List<String>> removeFct = (uuid, variantNum, ids) -> removed.addAll(ids);
        collectionBuffer = new CollectionBuffer<>(createFct, updateFct, removeFct);
    }

    @Test
    public void createTest() {
        assertTrue(created.isEmpty());
        assertTrue(updated.isEmpty());
        assertTrue(removed.isEmpty());
        collectionBuffer.create(l1);
        collectionBuffer.flush(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM);
        assertEquals(1, created.size());
        assertTrue(updated.isEmpty());
        assertTrue(removed.isEmpty());
    }

    @Test
    public void createThenUpdateTest() {
        assertTrue(created.isEmpty());
        assertTrue(updated.isEmpty());
        assertTrue(removed.isEmpty());
        collectionBuffer.create(l1);
        collectionBuffer.update(l1);
        collectionBuffer.flush(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM);
        assertEquals(1, created.size());
        assertTrue(updated.isEmpty());
        assertTrue(removed.isEmpty());
    }

    @Test
    public void updateTest() {
        assertTrue(created.isEmpty());
        assertTrue(updated.isEmpty());
        assertTrue(removed.isEmpty());
        collectionBuffer.update(l1);
        collectionBuffer.flush(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM);
        assertTrue(created.isEmpty());
        assertEquals(1, updated.size());
        assertTrue(removed.isEmpty());
    }

    @Test
    public void removeTest() {
        assertTrue(created.isEmpty());
        assertTrue(updated.isEmpty());
        assertTrue(removed.isEmpty());
        collectionBuffer.remove(l1.getId());
        collectionBuffer.flush(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM);
        assertTrue(created.isEmpty());
        assertTrue(updated.isEmpty());
        assertEquals(1, removed.size());
    }

    @Test
    public void createThenRemoveTest() {
        assertTrue(created.isEmpty());
        assertTrue(updated.isEmpty());
        assertTrue(removed.isEmpty());
        collectionBuffer.create(l1);
        collectionBuffer.remove(l1.getId());
        collectionBuffer.flush(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM);
        assertTrue(created.isEmpty());
        assertTrue(updated.isEmpty());
        assertTrue(removed.isEmpty());
    }

    @Test
    public void updateThenRemoveTest() {
        assertTrue(created.isEmpty());
        assertTrue(updated.isEmpty());
        assertTrue(removed.isEmpty());
        collectionBuffer.update(l1);
        collectionBuffer.remove(l1.getId());
        collectionBuffer.flush(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM);
        assertTrue(created.isEmpty());
        assertTrue(updated.isEmpty());
        assertEquals(1, removed.size());
    }
}

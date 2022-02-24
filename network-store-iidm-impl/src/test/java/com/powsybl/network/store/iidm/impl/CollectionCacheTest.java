/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.iidm.impl.util.TriFunction;
import com.powsybl.network.store.model.LoadAttributes;
import com.powsybl.network.store.model.Resource;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.function.BiFunction;

import static org.junit.Assert.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CollectionCacheTest {

    private static final UUID NETWORK_UUID = UUID.randomUUID();

    private CollectionCache<LoadAttributes> collectionCache;

    private boolean oneLoaderCalled;
    private boolean containerLoaderCalled;
    private boolean allLoaderCalled;

    private Resource<LoadAttributes> l1;
    private Resource<LoadAttributes> l2;
    private Resource<LoadAttributes> l3;
    private Resource<LoadAttributes> l4;

    private TriFunction<UUID, Integer, String, Optional<Resource<LoadAttributes>>> oneLoader;
    private TriFunction<UUID, Integer, String, List<Resource<LoadAttributes>>> containerLoader;
    private BiFunction<UUID, Integer, List<Resource<LoadAttributes>>> allLoader;

    @Before
    public void setUp() {
        oneLoaderCalled = false;
        containerLoaderCalled = false;
        allLoaderCalled = false;

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
        l3 = Resource.loadBuilder()
                .id("l3")
                .attributes(LoadAttributes.builder()
                        .voltageLevelId("vl2")
                        .build())
                .build();
        l4 = Resource.loadBuilder()
                .id("l4")
                .attributes(LoadAttributes.builder()
                        .voltageLevelId("vl2")
                        .build())
                .build();

        oneLoader = (networkUuid, variantNum, id) -> {
            oneLoaderCalled = true;
            if (id.equals("l1")) {
                return Optional.of(l1);
            } else if (id.equals("l2")) {
                return Optional.of(l2);
            } else if (id.equals("l3")) {
                return Optional.of(l3);
            } else {
                return Optional.empty();
            }
        };

        containerLoader = (networkUuid, variantNum, containerId) -> {
            containerLoaderCalled = true;
            if (containerId.equals("vl1")) {
                return Arrays.asList(l1, l2);
            } else if (containerId.equals("vl2")) {
                return Collections.singletonList(l3);
            } else {
                return Collections.emptyList();
            }
        };

        allLoader = (networkUuid, variantNum) -> {
            allLoaderCalled = true;
            return Arrays.asList(l1, l2, l3);
        };

        collectionCache = new CollectionCache<>(oneLoader, containerLoader, allLoader);
    }

    @Test
    public void getResourcesFirstTest() {
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        assertEquals(Arrays.asList(l1, l2, l3), collectionCache.getResources(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM));
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertTrue(allLoaderCalled);
        assertEquals(l2, collectionCache.getResource(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "l2").orElse(null));
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertTrue(allLoaderCalled);
        assertEquals(Arrays.asList(l1, l2), collectionCache.getContainerResources(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "vl1"));
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertTrue(allLoaderCalled);
    }

    @Test
    public void getResourceFirstTest() {
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        assertEquals(l2, collectionCache.getResource(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "l2").orElse(null));
        assertTrue(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        assertEquals(Arrays.asList(l2, l1), collectionCache.getContainerResources(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "vl1"));
        assertTrue(oneLoaderCalled);
        assertTrue(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        assertEquals(l1, collectionCache.getResource(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "l1").orElse(null));
        assertTrue(oneLoaderCalled);
        assertTrue(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        assertEquals(Arrays.asList(l1, l2, l3), collectionCache.getResources(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM));
        assertTrue(oneLoaderCalled);
        assertTrue(containerLoaderCalled);
        assertTrue(allLoaderCalled);
        assertEquals(3, collectionCache.getResourceCount(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM));
    }

    @Test
    public void incorrectGetContainerResourcesTest() {
        CollectionCache<LoadAttributes> otherCollectionCache = new CollectionCache<>(oneLoader, null, allLoader);
        PowsyblException exception = assertThrows(PowsyblException.class, () -> otherCollectionCache.getContainerResources(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ""));
        assertEquals("it is not possible to load resources by container, if container resources loader has not been specified", exception.getMessage());
    }

    @Test
    public void getContainerResourcesFirstTest() {
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        assertEquals(Collections.singletonList(l3), collectionCache.getContainerResources(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "vl2"));
        assertFalse(oneLoaderCalled);
        assertTrue(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        assertEquals(l3, collectionCache.getResource(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "l3").orElse(null));
        assertFalse(oneLoaderCalled);
        assertTrue(containerLoaderCalled);
        assertFalse(allLoaderCalled);
    }

    @Test
    public void getResourceCountTest() {
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        assertEquals(3, collectionCache.getResourceCount(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM));
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertTrue(allLoaderCalled);
    }

    @Test
    public void createResourceTest() {
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        collectionCache.createResource(l4);
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        assertTrue(collectionCache.getResource(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "l4").isPresent());
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        assertEquals(Arrays.asList(l1, l2, l3, l4), collectionCache.getResources(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM));
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertTrue(allLoaderCalled);
    }

    @Test
    public void createThenRemoveResourceTest() {
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        collectionCache.createResource(l4);
        collectionCache.removeResource("l4");
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        assertFalse(collectionCache.getResource(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "l4").isPresent());
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
    }

    @Test
    public void removeThenCreateResourceTest() {
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        collectionCache.removeResource("l1");
        assertFalse(collectionCache.getResource(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "l1").isPresent()); // no loading because explicitly removed
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        collectionCache.createResource(l1);
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        assertTrue(collectionCache.getResource(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "l1").isPresent());
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
    }

    @Test
    public void updateResourceTest() {
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        Resource<LoadAttributes> newL1 = Resource.loadBuilder()
                .id("l1")
                .attributes(LoadAttributes.builder()
                        .voltageLevelId("vl999")
                        .build())
                .build();
        collectionCache.updateResource(newL1);
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        assertEquals("vl999", collectionCache.getResource(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "l1").orElseThrow(IllegalStateException::new).getAttributes().getVoltageLevelId());
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
    }

    @Test
    public void getThenUpdateResourceTest() {
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        assertEquals("vl1", collectionCache.getResource(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "l1").orElseThrow(IllegalStateException::new).getAttributes().getVoltageLevelId());
        assertTrue(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        Resource<LoadAttributes> newL1 = Resource.loadBuilder()
                .id("l1")
                .attributes(LoadAttributes.builder()
                        .voltageLevelId("vl999")
                        .build())
                .build();
        oneLoaderCalled = false;
        collectionCache.updateResource(newL1);
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        assertEquals("vl999", collectionCache.getResource(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "l1").orElseThrow(IllegalStateException::new).getAttributes().getVoltageLevelId());
    }

    @Test
    public void initTest() {
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        collectionCache.init(); // it means we trust cache content and no more server loading
        assertTrue(collectionCache.getResources(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM).isEmpty());
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
    }

    @Test
    public void initContainerTest() {
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        collectionCache.initContainer("vl2"); // it means we trust cache content and no more server loading
        assertTrue(collectionCache.getContainerResources(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "vl2").isEmpty());
        assertFalse(oneLoaderCalled);
        assertFalse(containerLoaderCalled);
        assertFalse(allLoaderCalled);
        assertEquals(Arrays.asList(l1, l2), collectionCache.getContainerResources(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "vl1"));
        assertFalse(oneLoaderCalled);
        assertTrue(containerLoaderCalled);
        assertFalse(allLoaderCalled);
    }

    @Test
    public void getContainerResourcesThenRemoveTest() {
        collectionCache.getContainerResources(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "vl1");
        assertEquals(2, collectionCache.getContainerResources(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "vl1").size());
        collectionCache.removeResource("l1");
        assertEquals(1, collectionCache.getContainerResources(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "vl1").size());
    }

    @Test
    public void removeResourceThenGetContainerTest() {
        collectionCache.removeResource("l1");
        collectionCache.getContainerResources(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "vl1");
        assertEquals(1, collectionCache.getContainerResources(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "vl1").size());
    }
}

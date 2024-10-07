/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.iidm.impl.util.TriFunction;
import com.powsybl.network.store.model.*;
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
    MockNetworkStoreClient mockNetworkStoreClient;

    private ActivePowerControlAttributes apc1;
    private ActivePowerControlAttributes apc2;
    private OperatingStatusAttributes os1;

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

        apc1 = ActivePowerControlAttributes.builder()
                .droop(5.2)
                .participate(true)
                .participationFactor(0.5)
                .build();

        apc2 = ActivePowerControlAttributes.builder()
                .droop(6)
                .participate(false)
                .participationFactor(0.5)
                .build();

        os1 = OperatingStatusAttributes.builder()
                .operatingStatus("foo")
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
        mockNetworkStoreClient = new MockNetworkStoreClient(apc1, apc2, os1);
        collectionCache = new CollectionCache<>(oneLoader, containerLoader, allLoader, mockNetworkStoreClient);
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
        CollectionCache<LoadAttributes> otherCollectionCache = new CollectionCache<>(oneLoader, null, allLoader, null);
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

    @Test
    public void getExtensionAttributesWithResourceNotCachedMustThrow() {
        PowsyblException exception = assertThrows(PowsyblException.class, () -> collectionCache.getExtensionAttributes(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l1", "activePowerControl"));
        assertTrue(exception.getMessage().startsWith("Cannot manipulate extensions for identifiable"));
        exception = assertThrows(PowsyblException.class, () -> collectionCache.getAllExtensionsAttributesByResourceTypeAndExtensionName(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "activePowerControl"));
        assertTrue(exception.getMessage().startsWith("Cannot manipulate extensions for identifiable"));
        exception = assertThrows(PowsyblException.class, () -> collectionCache.getAllExtensionsAttributesByIdentifiableId(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l1"));
        assertTrue(exception.getMessage().startsWith("Cannot manipulate extensions for identifiable"));
        exception = assertThrows(PowsyblException.class, () -> collectionCache.getAllExtensionsAttributesByResourceType(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD));
        assertTrue(exception.getMessage().startsWith("Cannot manipulate extensions for identifiable"));
    }

    @Test
    public void getExtensionAttributes() {
        // Load resources in cache
        assertEquals(l1, collectionCache.getResource(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "l1").orElse(null));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
        assertEquals(apc1, collectionCache.getExtensionAttributes(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l1", "activePowerControl").orElse(null));
        assertTrue(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
        mockNetworkStoreClient.setExtensionAttributeLoaderCalled(false);
        assertEquals(apc1, collectionCache.getExtensionAttributes(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l1", "activePowerControl").orElse(null));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
    }

    @Test
    public void removeExtensionAttributes() {
        // Load resources in cache
        assertEquals(Arrays.asList(l1, l2, l3), collectionCache.getResources(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
        // Remove the extension by name then try to retrieve it. There should be no call to loader
        collectionCache.removeExtensionAttributesByExtensionName("l1", "activePowerControl");
        assertNull(collectionCache.getExtensionAttributes(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l1", "activePowerControl").orElse(null));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
        // Remove the resource by id then try to retrieve it. There should be no call to loader
        collectionCache.removeResource("l2");
        assertNull(collectionCache.getExtensionAttributes(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l2", "activePowerControl").orElse(null));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
    }

    @Test
    public void extensionIsCachedAndResourceIsRemovedAndAddedAgainWithoutExtensions() {
        // Load resources in cache
        assertEquals(l1, collectionCache.getResource(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "l1").orElse(null));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
        assertEquals(apc1, collectionCache.getExtensionAttributes(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l1", "activePowerControl").orElse(null));
        assertTrue(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
        mockNetworkStoreClient.setExtensionAttributeLoaderCalled(false);
        // Remove the resource by id then try to retrieve it. There should be no call to loader
        collectionCache.removeResource("l1");
        assertNull(collectionCache.getExtensionAttributes(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l1", "activePowerControl").orElse(null));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
        Resource<LoadAttributes> l1WithoutExtensions = Resource.loadBuilder()
                .id("l1")
                .attributes(LoadAttributes.builder()
                        .voltageLevelId("vl1")
                        .build())
                .build();
        collectionCache.addResource(l1WithoutExtensions);
        assertNull(collectionCache.getExtensionAttributes(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l1", "activePowerControl").orElse(null));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
    }

    @Test
    public void extensionIsCachedAndResourceIsRemovedAndAddedAgainWithDifferentExtensions() {
        // Load resources in cache
        assertEquals(l1, collectionCache.getResource(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "l1").orElse(null));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
        assertEquals(apc1, collectionCache.getExtensionAttributes(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l1", "activePowerControl").orElse(null));
        assertTrue(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
        mockNetworkStoreClient.setExtensionAttributeLoaderCalled(false);
        // Remove the resource by id then try to retrieve it. There should be no call to loader
        collectionCache.removeResource("l1");
        assertNull(collectionCache.getExtensionAttributes(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l1", "activePowerControl").orElse(null));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
        l1.getAttributes().getExtensionAttributes().put("activePowerControl", apc2);
        collectionCache.addResource(l1);
        assertEquals(apc2, collectionCache.getExtensionAttributes(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l1", "activePowerControl").orElse(null));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
    }

    @Test
    public void whenExtensionIsCachedThenRemovedClearExtensionCache() {
        // Load resources in cache
        assertEquals(l1, collectionCache.getResource(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, "l1").orElse(null));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
        assertEquals(apc1, collectionCache.getExtensionAttributes(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l1", "activePowerControl").orElse(null));
        assertTrue(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
        mockNetworkStoreClient.setExtensionAttributeLoaderCalled(false);
        // Remove the resource by id then try to retrieve it. There should be no call to loader
        collectionCache.removeExtensionAttributesByExtensionName("l1", "activePowerControl");
        assertNull(collectionCache.getExtensionAttributes(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l1", "activePowerControl").orElse(null));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
    }

    @Test
    public void getExtensionAttributesLoaderByResourceTypeAndName() {
        // Load resources in cache
        assertEquals(Arrays.asList(l1, l2, l3), collectionCache.getResources(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
        assertEquals(Map.of("l1", apc1, "l2", apc2), collectionCache.getAllExtensionsAttributesByResourceTypeAndExtensionName(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "activePowerControl"));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertTrue(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
        mockNetworkStoreClient.setExtensionAttributesLoaderByResourceTypeAndNameCalled(false);
        assertEquals(Map.of("l1", apc1, "l2", apc2), collectionCache.getAllExtensionsAttributesByResourceTypeAndExtensionName(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "activePowerControl"));
        assertEquals(apc1, collectionCache.getExtensionAttributes(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l1", "activePowerControl").orElse(null));
        assertEquals(apc2, collectionCache.getExtensionAttributes(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l2", "activePowerControl").orElse(null));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
    }

    @Test
    public void getExtensionAttributesLoaderById() {
        // Load resources in cache
        assertEquals(Arrays.asList(l1, l2, l3), collectionCache.getResources(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
        assertEquals(Map.of("activePowerControl", apc1, "operatingStatus", os1), collectionCache.getAllExtensionsAttributesByIdentifiableId(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l1"));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertTrue(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
        assertEquals(apc1, collectionCache.getExtensionAttributes(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l1", "activePowerControl").orElse(null));
        assertEquals(os1, collectionCache.getExtensionAttributes(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l1", "operatingStatus").orElse(null));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertTrue(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
    }

    @Test
    public void getExtensionAttributesLoaderByResourceType() {
        // Load resources in cache
        assertEquals(Arrays.asList(l1, l2, l3), collectionCache.getResources(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
        assertEquals(Map.of("l1", Map.of("activePowerControl", apc1, "operatingStatus", os1), "l2", Map.of("activePowerControl", apc2)), collectionCache.getAllExtensionsAttributesByResourceType(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertTrue(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
        mockNetworkStoreClient.setExtensionAttributesLoaderByResourceTypeCalled(false);
        assertEquals(Map.of("l1", Map.of("activePowerControl", apc1, "operatingStatus", os1), "l2", Map.of("activePowerControl", apc2)), collectionCache.getAllExtensionsAttributesByResourceType(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD));
        assertEquals(apc1, collectionCache.getExtensionAttributes(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l1", "activePowerControl").orElse(null));
        assertEquals(os1, collectionCache.getExtensionAttributes(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l1", "operatingStatus").orElse(null));
        assertEquals(apc2, collectionCache.getExtensionAttributes(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "l2", "activePowerControl").orElse(null));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
        // Verify that the map extensionAttributesByExtensionName is correctly updated when calling getAllExtensionsAttributesByResourceType()
        assertEquals(Map.of("l1", apc1, "l2", apc2), collectionCache.getAllExtensionsAttributesByResourceTypeAndExtensionName(NETWORK_UUID, Resource.INITIAL_VARIANT_NUM, ResourceType.LOAD, "activePowerControl"));
        assertFalse(mockNetworkStoreClient.isExtensionAttributeLoaderCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeAndNameCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByIdCalled());
        assertFalse(mockNetworkStoreClient.isExtensionAttributesLoaderByResourceTypeCalled());
    }
}

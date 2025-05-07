package com.powsybl.network.store.iidm.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.network.store.iidm.impl.util.TriFunction;
import com.powsybl.network.store.model.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Identifiable collection cache management.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CollectionCache<T extends IdentifiableAttributes> {
    private final ResourceCacheHandler<T> resourceHandler;
    private final ExtensionCacheHandler<T> extensionHandler;
    private final OperationalLimitsCacheHandler<T> operationalLimitsHandler;

    private final TriFunction<UUID, Integer, String, Optional<Resource<T>>> oneLoaderFunction;
    private final TriFunction<UUID, Integer, String, List<Resource<T>>> containerLoaderFunction;
    private final BiFunction<UUID, Integer, List<Resource<T>>> allLoaderFunction;
    private final NetworkStoreClient delegate;

    public CollectionCache(TriFunction<UUID, Integer, String, Optional<Resource<T>>> oneLoaderFunction,
                           TriFunction<UUID, Integer, String, List<Resource<T>>> containerLoaderFunction,
                           BiFunction<UUID, Integer, List<Resource<T>>> allLoaderFunction,
                           NetworkStoreClient delegate) {
        this.oneLoaderFunction = Objects.requireNonNull(oneLoaderFunction);
        this.containerLoaderFunction = containerLoaderFunction;
        this.allLoaderFunction = Objects.requireNonNull(allLoaderFunction);
        this.delegate = delegate;
        this.resourceHandler = new ResourceCacheHandler<>(oneLoaderFunction, containerLoaderFunction, allLoaderFunction);
        this.extensionHandler = new ExtensionCacheHandler<>(resourceHandler, delegate);
        this.operationalLimitsHandler = new OperationalLimitsCacheHandler<>(resourceHandler, delegate);
    }

    public boolean isResourceLoaded(String id) {
        return resourceHandler.isResourceLoaded(id);
    }

    public List<Resource<T>> getCachedResources() {
        return resourceHandler.getCachedResources();
    }

    public void init() {
        resourceHandler.init();
        extensionHandler.init();
        operationalLimitsHandler.init();
    }

    public boolean isFullyLoaded() {
        return resourceHandler.isFullyLoaded();
    }

    public void initContainer(String containerId) {
        resourceHandler.initContainer(containerId);
    }

    public Optional<Resource<T>> getResource(UUID networkUuid, int variantNum, String id) {
        return resourceHandler.getResource(networkUuid, variantNum, id);
    }

    public List<Resource<T>> getResources(UUID networkUuid, int variantNum) {
        return resourceHandler.getResources(networkUuid, variantNum);
    }

    public List<Resource<T>> getContainerResources(UUID networkUuid, int variantNum, String containerId) {
        return resourceHandler.getContainerResources(networkUuid, variantNum, containerId);
    }

    public void addOrReplaceResource(Resource<T> resource) {
        resourceHandler.addOrReplaceResource(resource);
    }

    public void createResource(Resource<T> resource) {
        resourceHandler.createResource(resource);
    }

    public void updateResource(Resource<T> resource) {
        resourceHandler.updateResource(resource);
    }

    public void removeResource(String id) {
        extensionHandler.removeExtensionAttributesByIdentifiableId(id);
        resourceHandler.removeResource(id);
    }

    public void removeResources(List<String> ids) {
        resourceHandler.removeResources(ids);
    }

    public int getResourceCount(UUID networkUuid, int variantNum) {
        return resourceHandler.getResourceCount(networkUuid, variantNum);
    }

    public Optional<ExtensionAttributes> getExtensionAttributes(UUID networkUuid, int variantNum, ResourceType type,
                                                                String identifiableId, String extensionName) {
        return extensionHandler.getExtensionAttributes(networkUuid, variantNum, type, identifiableId, extensionName);
    }

    public Map<String, ExtensionAttributes> getAllExtensionsAttributesByResourceTypeAndExtensionName(UUID networkUuid, int variantNum,
                                                                                                     ResourceType type, String extensionName) {
        return extensionHandler.getAllExtensionsAttributesByResourceTypeAndExtensionName(networkUuid, variantNum, type, extensionName);
    }

    public Map<String, ExtensionAttributes> getAllExtensionsAttributesByIdentifiableId(UUID networkUuid, int variantNum,
                                                                                       ResourceType type, String identifiableId) {
        return extensionHandler.getAllExtensionsAttributesByIdentifiableId(networkUuid, variantNum, type, identifiableId);
    }

    public Map<String, Map<String, ExtensionAttributes>> getAllExtensionsAttributesByResourceType(UUID networkUuid, int variantNum,
                                                                                                  ResourceType type) {
        return extensionHandler.getAllExtensionsAttributesByResourceType(networkUuid, variantNum, type);
    }

    public void removeExtensionAttributesByExtensionName(String identifiableId, String extensionName) {
        extensionHandler.removeExtensionAttributesByExtensionName(identifiableId, extensionName);
    }

    public void removeExtensionAttributesByIdentifiableId(String identifiableId) {
        extensionHandler.removeExtensionAttributesByIdentifiableId(identifiableId);
    }

    public Optional<OperationalLimitsGroupAttributes> getOperationalLimitsAttributes(UUID networkUuid, int variantNum, ResourceType type,
                                                                                     String branchId, String operationalLimitGroupName, int side) {
        return operationalLimitsHandler.getOperationalLimitsAttributes(networkUuid, variantNum, type, branchId, operationalLimitGroupName, side);
    }

    public void removeOperationalLimitsGroupAttributesByName(String branchId, String operationalLimitsGroupName, int side) {
        operationalLimitsHandler.removeOperationalLimitsGroupAttributesByName(branchId, operationalLimitsGroupName, side);
    }

    public Map<String, Map<OperationalLimitsGroupIdentifier, OperationalLimitsGroupAttributes>> getAllOperationalLimitsGroupAttributesByResourceType(
            UUID networkUuid, int variantNum, ResourceType type) {
        return operationalLimitsHandler.getAllOperationalLimitsGroupAttributesByResourceType(networkUuid, variantNum, type);
    }

    public Map<String, Map<OperationalLimitsGroupIdentifier, OperationalLimitsGroupAttributes>> getSelectedCurrentLimitsGroupAttributesByResourceType(
            UUID networkUuid, int variantNum, ResourceType type) {
        return operationalLimitsHandler.getSelectedCurrentLimitsGroupAttributesByResourceType(networkUuid, variantNum, type);
    }

    public Optional<OperationalLimitsGroupAttributes> getCurrentLimitsAttributes(UUID networkUuid, int variantNum, ResourceType type,
                                                                                 String branchId, String operationalLimitGroupName, int side) {
        return operationalLimitsHandler.getCurrentLimitsAttributes(networkUuid, variantNum, type, branchId, operationalLimitGroupName, side);
    }

    public CollectionCache<T> clone(ObjectMapper objectMapper, int newVariantNum, Consumer<Resource<T>> resourcePostProcessor) {
        CollectionCache<T> clonedCache = new CollectionCache<>(oneLoaderFunction, containerLoaderFunction, allLoaderFunction, delegate);

        resourceHandler.cloneTo(clonedCache.resourceHandler, objectMapper, newVariantNum, resourcePostProcessor);
        extensionHandler.cloneTo(clonedCache.extensionHandler);
        operationalLimitsHandler.cloneTo(clonedCache.operationalLimitsHandler);

        return clonedCache;
    }
}

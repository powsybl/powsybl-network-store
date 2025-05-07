package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.model.*;

import java.util.*;

public class ExtensionCacheHandler<T extends IdentifiableAttributes> implements CacheHandler {
    private final ResourceCacheHandler<T> resourceHandler;
    private final Set<String> fullyLoadedExtensionsByExtensionName = new HashSet<>();
    private final Set<String> fullyLoadedExtensionsByIdentifiableIds = new HashSet<>();
    private boolean fullyLoadedExtensions = false;
    private final Map<String, Set<String>> removedExtensionAttributes = new HashMap<>();
    private final NetworkStoreClient delegate;

    public ExtensionCacheHandler(ResourceCacheHandler<T> resourceHandler, NetworkStoreClient delegate) {
        this.resourceHandler = Objects.requireNonNull(resourceHandler);
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public void init() {
        fullyLoadedExtensions = true;
    }

    public Optional<ExtensionAttributes> getExtensionAttributes(UUID networkUuid, int variantNum, ResourceType type, String identifiableId, String extensionName) {
        Objects.requireNonNull(identifiableId);
        if (isExtensionAttributesCached(identifiableId, extensionName)) {
            return Optional.ofNullable(getCachedExtensionAttributes(identifiableId).get(extensionName));
        }
        if (!isFullyLoadedExtension(identifiableId, extensionName) && !isRemovedAttributes(identifiableId, extensionName)) {
            return delegate.getExtensionAttributes(networkUuid, variantNum, type, identifiableId, extensionName)
                    .map(attributes -> {
                        addExtensionAttributesToCache(identifiableId, extensionName, attributes);
                        return attributes;
                    });
        }
        return Optional.empty();
    }

    private Map<String, ExtensionAttributes> getCachedExtensionAttributes(String identifiableId) {
        Resource<T> resource = resourceHandler.getResourceById(identifiableId);
        if (resource != null) {
            return resource.getAttributes().getExtensionAttributes();
        } else {
            throw new PowsyblException("Cannot manipulate extensions for identifiable (" + identifiableId + ") as it has not been loaded into the cache.");
        }
    }

    private boolean isFullyLoadedExtension(String identifiableId, String extensionName) {
        return fullyLoadedExtensions || fullyLoadedExtensionsByIdentifiableIds.contains(identifiableId) || fullyLoadedExtensionsByExtensionName.contains(extensionName);
    }

    private boolean isFullyLoadedExtension(String extensionName) {
        return fullyLoadedExtensions || fullyLoadedExtensionsByExtensionName.contains(extensionName);
    }

    private boolean isRemovedAttributes(String id, String extensionName) {
        return resourceHandler.isResourceRemoved(id) || removedExtensionAttributes.containsKey(id) && removedExtensionAttributes.get(id).contains(extensionName);
    }

    private boolean isExtensionAttributesCached(String id, String extensionName) {
        return resourceHandler.isResourceLoaded(id) && getCachedExtensionAttributes(id).containsKey(extensionName);
    }

    private void addExtensionAttributesToCache(String identifiableId, String extensionName, ExtensionAttributes extensionAttributes) {
        Objects.requireNonNull(extensionAttributes);
        // if the resource has been removed from the cache but not yet on server, don't add extensions to it
        if (resourceHandler.isResourceRemoved(identifiableId)) {
            return;
        }
        getCachedExtensionAttributes(identifiableId).putIfAbsent(extensionName, extensionAttributes);
        Set<String> extensions = removedExtensionAttributes.get(identifiableId);
        if (extensions != null) {
            extensions.remove(extensionName);
            if (extensions.isEmpty()) {
                removedExtensionAttributes.remove(identifiableId);
            }
        }
    }

    public Map<String, ExtensionAttributes> getAllExtensionsAttributesByResourceTypeAndExtensionName(UUID networkUuid, int variantNum, ResourceType type, String extensionName) {
        if (!isFullyLoadedExtension(extensionName)) {
            // if collection has not yet been fully loaded we load it from the server
            Map<String, ExtensionAttributes> extensionAttributesMap = delegate.getAllExtensionsAttributesByResourceTypeAndExtensionName(networkUuid, variantNum, type, extensionName);
            // we update the full cache and set it as fully loaded
            extensionAttributesMap.forEach((identifiableId, extensionAttributes) -> addExtensionAttributesToCache(identifiableId, extensionName, extensionAttributes));
            fullyLoadedExtensionsByExtensionName.add(extensionName);
        }
        //TODO This method is only used to load extension attributes in the collection cache when using preloading collection.
        // The return is never used by the client as the call to getAllExtensionsAttributesByResourceTypeAndExtensionName() is always followed
        // by a call to getExtensionAttributes(). The latter returns something meaningful for the client
        // and it's used in the identifiable.getExtension() method. The map extensionAttributesMap can't be stored in the cache to be returned
        // as we can't ensure synchronization with the resources map (if extensions or identifiables are updated/removed).
        // We should refactor this method to return void.
        return null;
    }

    private boolean isExtensionAttributesCached(String identifiableId) {
        return (fullyLoadedExtensionsByIdentifiableIds.contains(identifiableId) || fullyLoadedExtensions) && resourceHandler.isResourceLoaded(identifiableId);
    }

    private boolean isFullyLoadedIdentifiable(String identifiableId) {
        return fullyLoadedExtensions || fullyLoadedExtensionsByIdentifiableIds.contains(identifiableId);
    }

    public Map<String, ExtensionAttributes> getAllExtensionsAttributesByIdentifiableId(UUID networkUuid, int variantNum, ResourceType type, String identifiableId) {
        Objects.requireNonNull(identifiableId);
        if (isExtensionAttributesCached(identifiableId)) {
            return getCachedExtensionAttributes(identifiableId);
        }
        if (!isFullyLoadedIdentifiable(identifiableId) && !resourceHandler.isResourceRemoved(identifiableId)) {
            Map<String, ExtensionAttributes> extensionAttributes = delegate.getAllExtensionsAttributesByIdentifiableId(networkUuid, variantNum, type, identifiableId);
            if (extensionAttributes != null) {
                addAllExtensionAttributesToCache(identifiableId, extensionAttributes);
                return getCachedExtensionAttributes(identifiableId);
            }
        }
        return Map.of();
    }

    private void addAllExtensionAttributesToCache(String id, Map<String, ExtensionAttributes> extensionAttributes) {
        Objects.requireNonNull(extensionAttributes);
        // if the resource has been removed from the cache but not yet on server, don't add extensions to it
        if (resourceHandler.isResourceRemoved(id)) {
            return;
        }
        extensionAttributes.forEach(getCachedExtensionAttributes(id)::putIfAbsent);
        fullyLoadedExtensionsByIdentifiableIds.add(id);
        removedExtensionAttributes.remove(id);
    }

    public Map<String, Map<String, ExtensionAttributes>> getAllExtensionsAttributesByResourceType(UUID networkUuid, int variantNum, ResourceType type) {
        if (!fullyLoadedExtensions) {
            // if collection has not yet been fully loaded we load it from the server
            Map<String, Map<String, ExtensionAttributes>> extensionAttributesMap = delegate.getAllExtensionsAttributesByResourceType(networkUuid, variantNum, type);
            // we update the full cache and set it as fully loaded
            extensionAttributesMap.forEach(this::addAllExtensionAttributesToCache);
            fullyLoadedExtensions = true;
        }
        //TODO This method is only used to load extension attributes in the collection cache when using preloading collection.
        // The return is never used by the client as the call to getAllExtensionsAttributesByResourceType() is always followed
        // by a call to getAllExtensionsAttributesByIdentifiableId(). The latter returns something meaningful for the client
        // and it's used in the identifiable.getExtensions() method. The map extensionAttributesMap can't be stored in the cache to be returned
        // as we can't ensure synchronization with the resources map (if extensions or identifiables are updated/removed).
        // We should refactor this method to return void.
        return null;
    }

    public void removeExtensionAttributesByExtensionName(String identifiableId, String extensionName) {
        Objects.requireNonNull(identifiableId);
        Objects.requireNonNull(extensionName);
        if (resourceHandler.isResourceLoaded(identifiableId)) {
            getCachedExtensionAttributes(identifiableId).remove(extensionName);
            removedExtensionAttributes.computeIfAbsent(identifiableId, k -> new HashSet<>()).add(extensionName);
        }
    }

    public void removeExtensionAttributesByIdentifiableId(String identifiableId) {
        Objects.requireNonNull(identifiableId);
        if (resourceHandler.isResourceLoaded(identifiableId)) {
            Set<String> removedExtensionNames = getCachedExtensionAttributes(identifiableId).keySet();
            removedExtensionAttributes.computeIfAbsent(identifiableId, k -> new HashSet<>()).addAll(removedExtensionNames);
            getCachedExtensionAttributes(identifiableId).clear();
        }
    }

    public void cloneTo(ExtensionCacheHandler<T> target) {
        for (Map.Entry<String, Set<String>> entry : removedExtensionAttributes.entrySet()) {
            target.removedExtensionAttributes.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        target.fullyLoadedExtensionsByExtensionName.addAll(fullyLoadedExtensionsByExtensionName);
        target.fullyLoadedExtensionsByIdentifiableIds.addAll(fullyLoadedExtensionsByIdentifiableIds);
        target.fullyLoadedExtensions = fullyLoadedExtensions;
    }
}

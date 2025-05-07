package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.model.*;

import java.util.*;

public class OperationalLimitsCacheHandler<T extends IdentifiableAttributes> implements CacheHandler {
    private final ResourceCacheHandler<T> resourceHandler;
    private boolean isFullyLoadedOperationalLimitsGroup = false;
    private boolean isFullyLoadedCurrentLimitsGroup = false;
    private final Set<String> fullyLoadedOperationalLimitsGroupsByBranchIds = new HashSet<>();
    private final Set<String> fullyLoadedCurrentLimitsGroupsByBranchIds = new HashSet<>();
    private final Set<OperationalLimitsGroupIdentifier> removedOperationalLimitsGroupsAttributes = new HashSet<>();
    private final NetworkStoreClient delegate;

    public OperationalLimitsCacheHandler(ResourceCacheHandler<T> resourceHandler, NetworkStoreClient delegate) {
        this.resourceHandler = Objects.requireNonNull(resourceHandler);
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public void init() {
        isFullyLoadedOperationalLimitsGroup = true;
        isFullyLoadedCurrentLimitsGroup = true;
    }

    public Optional<OperationalLimitsGroupAttributes> getOperationalLimitsAttributes(UUID networkUuid, int variantNum, ResourceType type,
                                                                                  String branchId, String operationalLimitGroupName, int side) {
        Objects.requireNonNull(branchId);
        if (isOperationalLimitsGroupAttributesCached(branchId)) {
            return Optional.ofNullable(getCachedOperationalLimitsGroupAttributes(branchId, side).get(operationalLimitGroupName));
        }
        if (!isFullyLoadedOperationalLimitsGroup(branchId) && !isRemovedOperationalLimitsGroupAttributes(branchId, operationalLimitGroupName, side)) {
            return delegate.getOperationalLimitsGroupAttributes(networkUuid, variantNum, type, branchId, operationalLimitGroupName, side)
                .map(attributes -> {
                    addOperationalLimitsGroupAttributesToCache(branchId, operationalLimitGroupName, side, attributes);
                    return attributes;
                });
        }
        return Optional.empty();
    }

    private Map<String, OperationalLimitsGroupAttributes> getCachedOperationalLimitsGroupAttributes(String branchId, int side) {
        Resource<T> resource = resourceHandler.getResourceById(branchId);
        if (resource != null && resource.getAttributes() instanceof BranchAttributes branchAttributes) {
            return branchAttributes.getOperationalLimitsGroups(side);
        } else {
            throw new PowsyblException("Cannot manipulate operational limits groups for branch (" + branchId + ") as it has not been loaded into the cache.");
        }
    }

    private boolean isOperationalLimitsGroupAttributesCached(String branchId) {
        return (fullyLoadedOperationalLimitsGroupsByBranchIds.contains(branchId) || isFullyLoadedOperationalLimitsGroup) && resourceHandler.isResourceLoaded(branchId);
    }

    private boolean isCurrentLimitsGroupAttributesCached(String branchId) {
        return (fullyLoadedOperationalLimitsGroupsByBranchIds.contains(branchId) || fullyLoadedCurrentLimitsGroupsByBranchIds.contains(branchId) || isFullyLoadedOperationalLimitsGroup || isFullyLoadedCurrentLimitsGroup) && resourceHandler.isResourceLoaded(branchId);
    }

    private boolean isFullyLoadedOperationalLimitsGroup(String branchId) {
        return isFullyLoadedOperationalLimitsGroup || fullyLoadedOperationalLimitsGroupsByBranchIds.contains(branchId);
    }

    private boolean isRemovedOperationalLimitsGroupAttributes(String branchId, String operationalLimitGroupName, int side) {
        return resourceHandler.isResourceRemoved(branchId) || removedOperationalLimitsGroupsAttributes.contains(OperationalLimitsGroupIdentifier.of(branchId, operationalLimitGroupName, side));
    }

    private void addOperationalLimitsGroupAttributesToCache(String branchId, String operationalLimitsGroupName, int side, OperationalLimitsGroupAttributes operationalLimitsGroupAttributes) {
        Objects.requireNonNull(operationalLimitsGroupAttributes);
        getCachedOperationalLimitsGroupAttributes(branchId, side).putIfAbsent(operationalLimitsGroupName, operationalLimitsGroupAttributes);
        OperationalLimitsGroupIdentifier identifier = OperationalLimitsGroupIdentifier.of(branchId, operationalLimitsGroupName, side);
        removedOperationalLimitsGroupsAttributes.remove(identifier);
        fullyLoadedOperationalLimitsGroupsByBranchIds.add(branchId);
    }

    public void removeOperationalLimitsGroupAttributesByName(String branchId, String operationalLimitsGroupName, int side) {
        Objects.requireNonNull(branchId);
        Objects.requireNonNull(operationalLimitsGroupName);
        if (resourceHandler.isResourceLoaded(branchId)) {
            getCachedOperationalLimitsGroupAttributes(branchId, side).remove(operationalLimitsGroupName);
            OperationalLimitsGroupIdentifier identifier = OperationalLimitsGroupIdentifier.of(branchId, operationalLimitsGroupName, side);
            removedOperationalLimitsGroupsAttributes.add(identifier);
        }
    }

    public Map<String, Map<OperationalLimitsGroupIdentifier, OperationalLimitsGroupAttributes>> getAllOperationalLimitsGroupAttributesByResourceType(UUID networkUuid, int variantNum, ResourceType type) {
        if (!isFullyLoadedOperationalLimitsGroup) {
            // if collection has not yet been fully loaded we load it from the server
            Map<String, Map<OperationalLimitsGroupIdentifier, OperationalLimitsGroupAttributes>> operationalLimitsGroupAttributesMap =
                delegate.getAllOperationalLimitsGroupAttributesByResourceType(networkUuid, variantNum, type);
            // we update the full cache and set it as fully loaded
            operationalLimitsGroupAttributesMap.forEach(this::addAllOperationalLimitsGroupAttributesToCache);
            isFullyLoadedOperationalLimitsGroup = true;
        }
        return Collections.emptyMap();
    }

    public Map<String, Map<OperationalLimitsGroupIdentifier, OperationalLimitsGroupAttributes>> getSelectedCurrentLimitsGroupAttributesByResourceType(UUID networkUuid, int variantNum, ResourceType type) {
        if (!isFullyLoadedCurrentLimitsGroup) {
            // if collection has not yet been fully loaded we load it from the server
            Map<String, Map<OperationalLimitsGroupIdentifier, OperationalLimitsGroupAttributes>> operationalLimitsGroupAttributesMap =
                delegate.getAllSelectedCurrentLimitsGroupAttributesByResourceType(networkUuid, variantNum, type);
            // we update the full cache and set it as fully loaded
            operationalLimitsGroupAttributesMap.forEach(this::addAllOperationalLimitsGroupAttributesToCache);
            isFullyLoadedCurrentLimitsGroup = true;
        }
        return Collections.emptyMap();
    }

    public Optional<OperationalLimitsGroupAttributes> getCurrentLimitsAttributes(UUID networkUuid, int variantNum, ResourceType type,
                                                                             String branchId, String operationalLimitGroupName, int side) {
        Objects.requireNonNull(branchId);
        if (isCurrentLimitsGroupAttributesCached(branchId)) {
            return Optional.ofNullable(getCachedOperationalLimitsGroupAttributes(branchId, side).get(operationalLimitGroupName));
        }
        if (!isFullyLoadedOperationalLimitsGroup(branchId) && !isRemovedOperationalLimitsGroupAttributes(branchId, operationalLimitGroupName, side)) {
            return delegate.getCurrentLimitsGroupAttributes(networkUuid, variantNum, type, branchId, operationalLimitGroupName, side)
                .map(attributes -> {
                    addCurrentLimitsGroupAttributesToCache(branchId, operationalLimitGroupName, side, attributes);
                    return attributes;
                });
        }
        return Optional.empty();
    }

    private void addCurrentLimitsGroupAttributesToCache(String branchId, String operationalLimitsGroupName, int side, OperationalLimitsGroupAttributes operationalLimitsGroupAttributes) {
        Objects.requireNonNull(operationalLimitsGroupAttributes);
        getCachedOperationalLimitsGroupAttributes(branchId, side).putIfAbsent(operationalLimitsGroupName, operationalLimitsGroupAttributes);
        OperationalLimitsGroupIdentifier identifier = OperationalLimitsGroupIdentifier.of(branchId, operationalLimitsGroupName, side);
        removedOperationalLimitsGroupsAttributes.remove(identifier);
        fullyLoadedCurrentLimitsGroupsByBranchIds.add(branchId);
    }

    private void addAllOperationalLimitsGroupAttributesToCache(String branchId, Map<OperationalLimitsGroupIdentifier, OperationalLimitsGroupAttributes> operationalLimitsGroupAttributesMap) {
        Objects.requireNonNull(operationalLimitsGroupAttributesMap);
        operationalLimitsGroupAttributesMap.forEach((identifier, limitsGroup) -> {
            if (limitsGroup != null) {
                getCachedOperationalLimitsGroupAttributes(identifier.getBranchId(), identifier.getSide())
                    .putIfAbsent(limitsGroup.getId(), limitsGroup);
                removedOperationalLimitsGroupsAttributes.remove(identifier);
            }
        });
        fullyLoadedOperationalLimitsGroupsByBranchIds.add(branchId);
    }

    public void cloneTo(OperationalLimitsCacheHandler<T> target) {
        target.removedOperationalLimitsGroupsAttributes.addAll(removedOperationalLimitsGroupsAttributes);
        target.fullyLoadedOperationalLimitsGroupsByBranchIds.addAll(fullyLoadedOperationalLimitsGroupsByBranchIds);
        target.fullyLoadedCurrentLimitsGroupsByBranchIds.addAll(fullyLoadedCurrentLimitsGroupsByBranchIds);
        target.isFullyLoadedOperationalLimitsGroup = isFullyLoadedOperationalLimitsGroup;
        target.isFullyLoadedCurrentLimitsGroup = isFullyLoadedCurrentLimitsGroup;
    }
}

/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.network.store.model.NetworkAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VariantManagerImpl implements VariantManager {

    private final NetworkObjectIndex index;

    public VariantManagerImpl(NetworkObjectIndex index) {
        this.index = index;
    }

    @Override
    public Collection<String> getVariantIds() {
        return index.getStoreClient().getNetworks(index.getNetwork().getUuid()).stream()
                .map(resource -> resource.getAttributes().getVariantId())
                .collect(Collectors.toList());
    }

    @Override
    public String getWorkingVariantId() {
        return index.getStoreClient().getNetworks(index.getNetwork().getUuid()).stream()
                .filter(resource -> resource.getVariantNum() == index.getWorkingVariantNum())
                .map(resource -> resource.getAttributes().getVariantId())
                .findFirst()
                .orElseThrow();
    }

    private int getVariantNum(String variantId) {
        Resource<NetworkAttributes> networkResource = index.getStoreClient().getNetworks(index.getNetwork().getUuid()).stream()
                .filter(resource -> resource.getAttributes().getVariantId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new PowsyblException("Variant '" + variantId + "' not found"));
        return networkResource.getVariantNum();
    }

    @Override
    public void setWorkingVariant(String variantId) {
        int variantNum = getVariantNum(variantId);
        index.setWorkingVariantNum(variantNum);
    }

    @Override
    public void cloneVariant(String sourceVariantId, List<String> targetVariantIds) {
        cloneVariant(sourceVariantId, targetVariantIds, false);
    }

    private int findFistAvailableVariantNum() {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            final int variantNum = i;
            if (index.getStoreClient().getNetworks().stream().noneMatch(resource -> resource.getVariantNum() == variantNum)) {
                return variantNum;
            }
        }
        throw new PowsyblException("Max number of variant reached: " + Integer.MAX_VALUE);
    }

    @Override
    public void cloneVariant(String sourceVariantId, List<String> targetVariantIds, boolean mayOverwrite) {
        // TODO support mayOverwrite
        Objects.requireNonNull(sourceVariantId);
        Objects.requireNonNull(targetVariantIds);
        int sourceVariantNum = getVariantNum(sourceVariantId);
        for (String targetVariantId : targetVariantIds) {
            int targetVariantNum = findFistAvailableVariantNum();
            // clone resources
            index.getStoreClient().cloneNetwork(index.getNetwork().getUuid(), sourceVariantNum, targetVariantNum, targetVariantId);
        }
    }

    @Override
    public void cloneVariant(String sourceVariantId, String targetVariantId) {
        cloneVariant(sourceVariantId, Collections.singletonList(targetVariantId));
    }

    @Override
    public void cloneVariant(String sourceVariantId, String targetVariantId, boolean mayOverwrite) {
        cloneVariant(sourceVariantId, targetVariantId, false);
    }

    @Override
    public void removeVariant(String variantId) {
        int variantNum = getVariantNum(variantId);
        index.getStoreClient().deleteNetwork(index.getNetwork().getUuid(), variantNum);
    }

    @Override
    public void allowVariantMultiThreadAccess(boolean allow) {
        throw new PowsyblException("Network store implementation does not support multi-thread access yet");
    }

    @Override
    public boolean isVariantMultiThreadAccessAllowed() {
        return false;
    }
}

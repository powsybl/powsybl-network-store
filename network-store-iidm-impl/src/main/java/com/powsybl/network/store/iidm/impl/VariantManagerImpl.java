/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VariantInfos;

import java.util.*;
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
        return index.getStoreClient().getVariantsInfos(index.getNetwork().getUuid()).stream()
                .map(VariantInfos::getId)
                .collect(Collectors.toList());
    }

    @Override
    public String getWorkingVariantId() {
        return index.getStoreClient().getVariantsInfos(index.getNetwork().getUuid()).stream()
                .filter(infos -> infos.getNum() == index.getWorkingVariantNum())
                .map(VariantInfos::getId)
                .findFirst()
                .orElseThrow();
    }

    private Optional<VariantInfos> getVariant(String variantId) {
        return index.getStoreClient().getVariantsInfos(index.getNetwork().getUuid()).stream()
                .filter(infos -> infos.getId().equals(variantId))
                .findFirst();
    }

    private int getVariantNum(String variantId) {
        return getVariant(variantId)
                .map(VariantInfos::getNum)
                .orElseThrow(() -> new PowsyblException("Variant '" + variantId + "' not found"));
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
        List<VariantInfos> variantsInfos = index.getStoreClient().getVariantsInfos(index.getNetwork().getUuid());
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            final int variantNum = i;
            if (variantsInfos.stream().noneMatch(infos -> infos.getNum() == variantNum)) {
                return variantNum;
            }
        }
        throw new PowsyblException("Max number of variant reached: " + Integer.MAX_VALUE);
    }

    @Override
    public void cloneVariant(String sourceVariantId, List<String> targetVariantIds, boolean mayOverwrite) {
        Objects.requireNonNull(sourceVariantId);
        Objects.requireNonNull(targetVariantIds);
        int sourceVariantNum = getVariantNum(sourceVariantId);
        for (String targetVariantId : targetVariantIds) {
            Optional<VariantInfos> targetVariant = getVariant(targetVariantId);
            if (targetVariant.isPresent()) {
                if (!mayOverwrite) {
                    throw new PowsyblException("Variant '" + targetVariantId + "' already exists");
                } else {
                    removeVariant(targetVariantId);
                }
            }
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
        cloneVariant(sourceVariantId, Collections.singletonList(targetVariantId), mayOverwrite);
    }

    @Override
    public void removeVariant(String variantId) {
        int variantNum = getVariantNum(variantId);
        index.getStoreClient().deleteNetwork(index.getNetwork().getUuid(), variantNum);
        // if removed variant is the working one, switch to initial one
        if (variantNum == index.getWorkingVariantNum()) {
            index.setWorkingVariantNum(Resource.INITIAL_VARIANT_NUM);
        }
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

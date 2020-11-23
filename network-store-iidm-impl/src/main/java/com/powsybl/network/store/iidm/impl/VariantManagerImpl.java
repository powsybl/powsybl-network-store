/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.network.store.model.NetworkAttributes;
import com.powsybl.network.store.model.VariantAttributes;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VariantManagerImpl implements VariantManager {

    protected final NetworkObjectIndex index;

    public VariantManagerImpl(NetworkObjectIndex index) {
        this.index = index;
    }

    private NetworkAttributes getNetworkAttributes() {
        return index.getNetwork().getResource().getAttributes();
    }

    private VariantAttributes getVariantById(String variantId) {
        return getNetworkAttributes().getVariants().stream()
                .filter(v -> v.getVariantId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new PowsyblException("Variant '" + variantId + "' not found"));
    }

    @Override
    public Collection<String> getVariantIds() {
        return getNetworkAttributes().getVariants().stream()
                .map(VariantAttributes::getVariantId)
                .collect(Collectors.toList());
    }

    @Override
    public String getWorkingVariantId() {
        return getNetworkAttributes().getWorkingVariantId();
    }

    @Override
    public void setWorkingVariant(String variantId) {
        VariantAttributes variant = getVariantById(variantId);
        getNetworkAttributes().setWorkingVariantId(variantId);
        getNetworkAttributes().setWorkingVariantUuid(variant.getVariantUuid());
    }

    @Override
    public void cloneVariant(String sourceVariantId, List<String> targetVariantIds) {
        // TODO
    }

    @Override
    public void cloneVariant(String sourceVariantId, List<String> targetVariantIds, boolean mayOverwrite) {
        // TODO
    }

    @Override
    public void cloneVariant(String sourceVariantId, String targetVariantId) {
        // TODO
    }

    @Override
    public void cloneVariant(String sourceVariantId, String targetVariantId, boolean mayOverwrite) {
        // TODO
    }

    @Override
    public void removeVariant(String variantId) {
        // TODO
    }

    @Override
    public void allowVariantMultiThreadAccess(boolean allow) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean isVariantMultiThreadAccessAllowed() {
        throw new UnsupportedOperationException("TODO");
    }
}

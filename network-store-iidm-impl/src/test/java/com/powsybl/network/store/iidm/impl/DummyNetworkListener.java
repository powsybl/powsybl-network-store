/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.NetworkListener;
import lombok.Getter;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@Getter
public class DummyNetworkListener implements NetworkListener {

    private int nbUpdatedExtensions = 0;

    @Override
    public void onCreation(Identifiable identifiable) {
        // Not tested here
    }

    @Override
    public void beforeRemoval(Identifiable identifiable) {
        // Not tested here
    }

    @Override
    public void afterRemoval(String id) {
        // Not tested here
    }

    public void onUpdate(Identifiable identifiable, String attribute, String variantId, Object oldValue,
                         Object newValue) {
    }

    @Override
    public void onVariantCreated(String sourceVariantId, String targetVariantId) {
        // Not tested here
    }

    @Override
    public void onVariantRemoved(String variantId) {
        // Not tested here
    }

    @Override
    public void onVariantOverwritten(String sourceVariantId, String targetVariantId) {
        // Not tested here
    }

    @Override
    public void onExtensionCreation(Extension<?> extension) {
    }

    @Override
    public void onExtensionAfterRemoval(Identifiable<?> identifiable, String extensionName) {
    }

    @Override
    public void onExtensionBeforeRemoval(Extension<?> extension) {
    }

    @Override
    public void onExtensionUpdate(Extension<?> extendable, String attribute, String variantId, Object oldValue, Object newValue) {
        nbUpdatedExtensions++;
    }

    @Override
    public void onPropertyAdded(Identifiable<?> identifiable, String key, Object newValue) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onPropertyAdded'");
    }

    @Override
    public void onPropertyReplaced(Identifiable<?> identifiable, String key, Object oldValue, Object newValue) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onPropertyReplaced'");
    }

    @Override
    public void onPropertyRemoved(Identifiable<?> identifiable, String key, Object oldValue) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onPropertyRemoved'");
    }
}

/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
public class MockNetworkListener implements NetworkListener {

    private int nbUpdatedExtensions = 0;
    private int nbRemovedExtension = 0;
    private int nbCreatedVariant = 0;
    private int nbRemovedVariant = 0;

    @Override
    public void onCreation(Identifiable identifiable) {
        throw new UnsupportedOperationException("Unimplemented method 'onCreation'");
    }

    @Override
    public void beforeRemoval(Identifiable identifiable) {
        throw new UnsupportedOperationException("Unimplemented method 'beforeRemoval'");
    }

    @Override
    public void afterRemoval(String id) {
        throw new UnsupportedOperationException("Unimplemented method 'afterRemoval'");
    }

    public void onUpdate(Identifiable identifiable, String attribute, String variantId, Object oldValue,
                         Object newValue) {
        throw new UnsupportedOperationException("Unimplemented method 'onUpdate'");
    }

    @Override
    public void onVariantCreated(String sourceVariantId, String targetVariantId) {
        nbCreatedVariant++;
    }

    @Override
    public void onVariantRemoved(String variantId) {
        nbRemovedVariant++;
    }

    @Override
    public void onVariantOverwritten(String sourceVariantId, String targetVariantId) {
        throw new UnsupportedOperationException("Unimplemented method 'onVariantOverwritten'");
    }

    @Override
    public void onExtensionCreation(Extension<?> extension) {
        throw new UnsupportedOperationException("Unimplemented method 'onExtensionCreation'");
    }

    @Override
    public void onExtensionAfterRemoval(Identifiable<?> identifiable, String extensionName) {
        nbRemovedExtension++;
    }

    @Override
    public void onExtensionBeforeRemoval(Extension<?> extension) {
        throw new UnsupportedOperationException("Unimplemented method 'onExtensionBeforeRemoval'");
    }

    @Override
    public void onExtensionUpdate(Extension<?> extendable, String attribute, String variantId, Object oldValue, Object newValue) {
        nbUpdatedExtensions++;
    }

    @Override
    public void onPropertyAdded(Identifiable<?> identifiable, String key, Object newValue) {
        throw new UnsupportedOperationException("Unimplemented method 'onPropertyAdded'");
    }

    @Override
    public void onPropertyReplaced(Identifiable<?> identifiable, String key, Object oldValue, Object newValue) {
        throw new UnsupportedOperationException("Unimplemented method 'onPropertyReplaced'");
    }

    @Override
    public void onPropertyRemoved(Identifiable<?> identifiable, String key, Object oldValue) {
        throw new UnsupportedOperationException("Unimplemented method 'onPropertyRemoved'");
    }
}

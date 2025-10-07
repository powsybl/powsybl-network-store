package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.NetworkListener;
import lombok.Getter;

@Getter
public class DummyNetworkListener implements NetworkListener {
    private int nbUpdatedExtensions = 0;
    private int nbUpdatedIdentifiables = 0;
    private Identifiable<?> lastIdentifiableUpdated = null;
    private String lastAttributeUpdated = null;
    private Object lastOldValueUpdated;
    private Object lastNewValueUpdated;

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
        nbUpdatedIdentifiables++;
        lastIdentifiableUpdated = identifiable;
        lastAttributeUpdated = attribute;
        lastOldValueUpdated = oldValue;
        lastNewValueUpdated = newValue;
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
        // Not tested here
    }

    @Override
    public void onExtensionAfterRemoval(Identifiable<?> identifiable, String extensionName) {
        // Not tested here
    }

    @Override
    public void onExtensionBeforeRemoval(Extension<?> extension) {
        // Not tested here
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

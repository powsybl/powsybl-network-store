package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuitAdder;

@AutoService(ExtensionAdderProvider.class)
public class IdentifiableShortCircuitAdderImplProvider<I extends Identifiable<I>>
        implements ExtensionAdderProvider<I, IdentifiableShortCircuit<I>, IdentifiableShortCircuitAdderImpl<I>> {
    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public String getExtensionName() {
        return IdentifiableShortCircuit.NAME;
    }

    @Override
    public Class<? super IdentifiableShortCircuitAdderImpl> getAdderClass() {
        return IdentifiableShortCircuitAdder.class;
    }

    @Override
    public IdentifiableShortCircuitAdderImpl<I> newAdder(I extendable) {
        return new IdentifiableShortCircuitAdderImpl<>(extendable);
    }
}

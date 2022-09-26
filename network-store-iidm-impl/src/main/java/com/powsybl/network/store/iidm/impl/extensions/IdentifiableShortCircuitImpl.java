package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuitAdder;

public class IdentifiableShortCircuitImpl<I extends Identifiable<I>> extends AbstractExtension<I> implements IdentifiableShortCircuit<I> {

    private I identifiable;

    public IdentifiableShortCircuitImpl(I identifiable) {
        this.identifiable = identifiable;
    }

    public IdentifiableShortCircuitImpl(I extendable, double ipMin, double ipMax) {
        super(extendable);
        this.setIpMin(ipMin);
        this.setIpMax(ipMax);
    }

    @Override
    public double getIpMin() {
        return 0;
    }

    @Override
    public IdentifiableShortCircuit<I> setIpMin(double v) {
//        return ((IdentifiableShortCircuitAdder) getExtendable().getExtension(getClass())).withIpMin(v);
        return null;
    }

    @Override
    public double getIpMax() {
        return 0;
    }

    @Override
    public IdentifiableShortCircuit<I> setIpMax(double v) {
        return null;
    }
}

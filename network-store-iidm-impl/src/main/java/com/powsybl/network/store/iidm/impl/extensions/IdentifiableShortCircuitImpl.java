package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import com.powsybl.network.store.iidm.impl.VoltageLevelImpl;

public class IdentifiableShortCircuitImpl<I extends Identifiable<I>> extends AbstractExtension<I> implements IdentifiableShortCircuit<I> {

    public IdentifiableShortCircuitImpl(I extendable, double ipMin, double ipMax) {
        super(extendable);
        ((VoltageLevelImpl) extendable).initIdentifiableShortCircuitAttributes(ipMin, ipMax);
    }

    @Override
    public double getIpMin() {
        return ((VoltageLevelImpl) getExtendable()).getResource().getAttributes().getIdentifiableShortCircuitAttributes().getIpMin();
    }

    @Override
    public IdentifiableShortCircuit<I> setIpMin(double ipMin) {
        ((VoltageLevelImpl) getExtendable()).getResource().getAttributes().getIdentifiableShortCircuitAttributes()
                .setIpMin(ipMin);
        return this;
    }

    @Override
    public double getIpMax() {
        return ((VoltageLevelImpl) getExtendable()).getResource().getAttributes().getIdentifiableShortCircuitAttributes().getIpMax();
    }

    @Override
    public IdentifiableShortCircuit<I> setIpMax(double ipMax) {
        ((VoltageLevelImpl) getExtendable()).getResource().getAttributes().getIdentifiableShortCircuitAttributes()
                .setIpMax(ipMax);
        return this;
    }
}

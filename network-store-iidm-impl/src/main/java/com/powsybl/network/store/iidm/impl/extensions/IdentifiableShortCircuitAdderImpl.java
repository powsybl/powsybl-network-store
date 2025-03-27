/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuitAdder;
import com.powsybl.network.store.iidm.impl.VoltageLevelImpl;
import com.powsybl.network.store.model.IdentifiableShortCircuitAttributes;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class IdentifiableShortCircuitAdderImpl<I extends Identifiable<I>> extends AbstractIidmExtensionAdder<I, IdentifiableShortCircuit<I>>
        implements IdentifiableShortCircuitAdder<I> {

    private double ipMin; // Minimum allowable peak short-circuit current
    private double ipMax; // Maximum allowable peak short-circuit current

    protected IdentifiableShortCircuitAdderImpl(I extendable) {
        super(extendable);
    }

    @Override
    protected IdentifiableShortCircuit<I> createExtension(I extendable) {
        var attributes = IdentifiableShortCircuitAttributes.builder()
                .ipMin(ipMin)
                .ipMax(ipMax)
                .build();
        ((VoltageLevelImpl) extendable).updateResource(res -> res.getAttributes().setIdentifiableShortCircuitAttributes(attributes));
        return new IdentifiableShortCircuitImpl<>(extendable);
    }

    @Override
    public IdentifiableShortCircuitAdder<I> withIpMin(double ipMin) {
        this.ipMin = ipMin;
        return this;
    }

    @Override
    public IdentifiableShortCircuitAdder<I> withIpMax(double ipMax) {
        this.ipMax = ipMax;
        return this;
    }
}

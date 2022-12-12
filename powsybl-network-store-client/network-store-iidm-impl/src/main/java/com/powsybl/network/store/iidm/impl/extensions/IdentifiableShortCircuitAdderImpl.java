/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuitAdder;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class IdentifiableShortCircuitAdderImpl<I extends Identifiable<I>> extends AbstractExtensionAdder<I, IdentifiableShortCircuit<I>>
        implements IdentifiableShortCircuitAdder<I> {

    private double ipMin; // Minimum allowable peak short-circuit current
    private double ipMax; // Maximum allowable peak short-circuit current

    protected IdentifiableShortCircuitAdderImpl(I extendable) {
        super(extendable);
    }

    @Override
    protected IdentifiableShortCircuit<I> createExtension(I extendable) {
        return new IdentifiableShortCircuitImpl<>(extendable, ipMin, ipMax);
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

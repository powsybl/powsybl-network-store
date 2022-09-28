/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import com.powsybl.network.store.iidm.impl.VoltageLevelImpl;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
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

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
import com.powsybl.network.store.model.IdentifiableShortCircuitAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class IdentifiableShortCircuitImpl<I extends Identifiable<I>> extends AbstractExtension<I> implements IdentifiableShortCircuit<I> {

    public IdentifiableShortCircuitImpl(I extendable) {
        super(extendable);
    }

    private VoltageLevelImpl getVoltageLevel() {
        return (VoltageLevelImpl) getExtendable();
    }

    private static IdentifiableShortCircuitAttributes getAttributes(Resource<VoltageLevelAttributes> resource) {
        return resource.getAttributes().getIdentifiableShortCircuitAttributes();
    }

    private IdentifiableShortCircuitAttributes getAttributes() {
        return getAttributes(getVoltageLevel().getResource());
    }

    @Override
    public double getIpMin() {
        return getAttributes().getIpMin();
    }

    @Override
    public IdentifiableShortCircuit<I> setIpMin(double ipMin) {
        getVoltageLevel().updateResource(res -> getAttributes(res).setIpMin(ipMin));
        return this;
    }

    @Override
    public double getIpMax() {
        return getAttributes().getIpMax();
    }

    @Override
    public IdentifiableShortCircuit<I> setIpMax(double ipMax) {
        getVoltageLevel().updateResource(res -> getAttributes(res).setIpMax(ipMax));
        return this;
    }
}

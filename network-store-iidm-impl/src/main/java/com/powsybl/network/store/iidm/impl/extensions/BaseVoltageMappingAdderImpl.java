/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.BaseVoltageMapping;
import com.powsybl.cgmes.extensions.BaseVoltageMappingAdder;
import com.powsybl.cgmes.extensions.Source;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.model.BaseVoltageSourceAttribute;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
public class BaseVoltageMappingAdderImpl extends AbstractExtensionAdder<Network, BaseVoltageMapping> implements BaseVoltageMappingAdder {

    Map<Double, BaseVoltageSourceAttribute> resourcesBaseVoltages = new HashMap<>();

    public BaseVoltageMappingAdderImpl(NetworkImpl extendable) {
        super(extendable);
    }

    @Override
    public BaseVoltageMappingAdder addBaseVoltage(String baseVoltageId, double nominalVoltage, Source source) {
        if (resourcesBaseVoltages.containsKey(nominalVoltage)) {
            if (resourcesBaseVoltages.get(nominalVoltage).getSource().equals(Source.IGM) && source.equals(Source.BOUNDARY)) {
                resourcesBaseVoltages.put(nominalVoltage, new BaseVoltageSourceAttribute(baseVoltageId, nominalVoltage, source));
            }
        } else {
            resourcesBaseVoltages.put(nominalVoltage, new BaseVoltageSourceAttribute(baseVoltageId, nominalVoltage, source));
        }

        resourcesBaseVoltages.put(nominalVoltage, new BaseVoltageSourceAttribute(baseVoltageId, nominalVoltage, source));
        return this;
    }

    @Override
    protected BaseVoltageMapping createExtension(Network network) {
        return new BaseVoltageMappingImpl(network, resourcesBaseVoltages);
    }

}

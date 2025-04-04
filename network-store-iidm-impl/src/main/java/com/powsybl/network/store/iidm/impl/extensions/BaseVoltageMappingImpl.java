/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.BaseVoltageMapping;
import com.powsybl.cgmes.extensions.Source;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.model.BaseVoltageSourceAttribute;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */

public class BaseVoltageMappingImpl extends AbstractExtension<Network> implements BaseVoltageMapping {

    public BaseVoltageMappingImpl(Network network) {
        super(network);
    }

    private NetworkImpl getNetwork() {
        return (NetworkImpl) getExtendable();
    }

    private Map<Double, BaseVoltageSourceAttribute> getResourcesBaseVoltages() {
        return getNetwork().getResource().getAttributes().getBaseVoltageMapping().getBaseVoltages();
    }

    @Override
    public Map<Double, BaseVoltageSource> getBaseVoltages() {
        return Collections.unmodifiableMap(getResourcesBaseVoltages());
    }

    @Override
    public BaseVoltageSource getBaseVoltage(double nominalVoltage) {
        return getResourcesBaseVoltages().get(nominalVoltage);
    }

    @Override
    public boolean isBaseVoltageMapped(double nominalVoltage) {
        return getResourcesBaseVoltages().containsKey(nominalVoltage);
    }

    @Override
    public boolean isBaseVoltageEmpty() {
        return getResourcesBaseVoltages().isEmpty();
    }

    @Override
    public BaseVoltageMapping addBaseVoltage(double nominalVoltage, String baseVoltageId, Source source) {
        Map<Double, BaseVoltageSourceAttribute> resourcesBaseVoltages = getResourcesBaseVoltages();
        if (resourcesBaseVoltages.containsKey(nominalVoltage)) {
            BaseVoltageSourceAttribute oldValue = resourcesBaseVoltages.get(nominalVoltage);
            if (oldValue.getSource().equals(Source.IGM) && source.equals(Source.BOUNDARY)) {
                BaseVoltageSourceAttribute attributes = new BaseVoltageSourceAttribute(baseVoltageId, nominalVoltage, source);
                getNetwork().updateResourceExtension(this, res -> res.getAttributes().getBaseVoltageMapping().getBaseVoltages().put(nominalVoltage, attributes), "base voltage(id=" + baseVoltageId + ", nominalVoltage=" + nominalVoltage + ")", oldValue, attributes);
            }
        } else {
            BaseVoltageSourceAttribute attributes = new BaseVoltageSourceAttribute(baseVoltageId, nominalVoltage, source);
            getNetwork().updateResourceExtension(this, res -> res.getAttributes().getBaseVoltageMapping().getBaseVoltages().put(nominalVoltage, attributes), "base voltage(id=" + baseVoltageId + ", nominalVoltage=" + nominalVoltage + ")", null, attributes);
        }
        return this;
    }

    @Override
    public Map<Double, BaseVoltageSource> baseVoltagesByNominalVoltageMap() {
        return new HashMap<>(getBaseVoltages());
    }
}

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

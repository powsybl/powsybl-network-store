package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.BaseVoltageMapping;
import com.powsybl.cgmes.extensions.BaseVoltageMappingAdder;
import com.powsybl.cgmes.extensions.Source;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.model.BaseVoltageMappingAttributes;
import com.powsybl.network.store.model.BaseVoltageSourceAttribute;

public class BaseVoltageMappingAdderImpl extends AbstractExtensionAdder<Network, BaseVoltageMapping> implements BaseVoltageMappingAdder {

    private final NetworkImpl network;

    public BaseVoltageMappingAdderImpl(NetworkImpl extendable) {
        super(extendable);
        network = extendable;
        network.getResource().getAttributes().setBaseVoltageMapping(new BaseVoltageMappingAttributes());
    }

    @Override
    public BaseVoltageMappingAdder addBaseVoltage(String s, double v, Source source) {
        network.getResource().getAttributes().getBaseVoltageMapping().getBaseVoltageMap()
            .put(v, new BaseVoltageSourceAttribute(s, v, source));
        return this;
    }

    @Override
    protected BaseVoltageMapping createExtension(Network network) {
        return new BaseVoltageMappingImpl((NetworkImpl) network);
    }
}

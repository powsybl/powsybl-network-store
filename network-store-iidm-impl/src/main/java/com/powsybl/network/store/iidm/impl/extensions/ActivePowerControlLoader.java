package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.network.store.model.ActivePowerControlAttributes;
import com.powsybl.network.store.model.ExtensionLoader;

@AutoService(ExtensionLoader.class)
public class ActivePowerControlLoader<I extends Injection<I>> implements ExtensionLoader<I, ActivePowerControl<I>, ActivePowerControlAttributes> {
    @Override
    public Extension<I> load(I injection) {
        return new ActivePowerControlImpl<>(injection);
    }

    @Override
    public String getName() {
        return ActivePowerControl.NAME;
    }

    @Override
    public Class<ActivePowerControl> getType() {
        return ActivePowerControl.class;
    }

    @Override
    public Class<ActivePowerControlAttributes> getAttributesType() {
        return ActivePowerControlAttributes.class;
    }
}

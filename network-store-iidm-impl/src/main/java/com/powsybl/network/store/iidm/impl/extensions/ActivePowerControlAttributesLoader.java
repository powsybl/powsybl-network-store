package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.network.store.model.ExtensionAttributesLoader;

@AutoService(ExtensionAttributesLoader.class)
public class ActivePowerControlAttributesLoader<I extends Injection<I>> implements ExtensionAttributesLoader<I> {
    @Override
    public Extension<I> load(I injection) {
        return new ActivePowerControlImpl<>(injection);
    }

    @Override
    public String getName() {
        return ActivePowerControl.NAME;
    }

    @Override
    public Class<?> getType() {
        return ActivePowerControl.class;
    }
}

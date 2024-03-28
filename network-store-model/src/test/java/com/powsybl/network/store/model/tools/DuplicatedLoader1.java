package com.powsybl.network.store.model.tools;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Injection;
import com.powsybl.network.store.model.ExtensionAttributes;
import com.powsybl.network.store.model.ExtensionLoader;

@AutoService(ExtensionLoader.class)
public class DuplicatedLoader1<I extends Injection<I>> implements ExtensionLoader<I, Extension<I>, ExtensionAttributes> {
    @Override
    public Extension<I> load(I injection) {
        return null;
    }

    @Override
    public String getName() {
        return "loader";
    }

    @Override
    public Class<Extension<I>> getType() {
        return null;
    }

    @Override
    public Class<ExtensionAttributes> getAttributesType() {
        return null;
    }
}

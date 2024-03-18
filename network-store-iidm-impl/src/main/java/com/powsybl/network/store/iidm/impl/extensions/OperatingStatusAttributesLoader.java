package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.OperatingStatus;
import com.powsybl.network.store.model.ExtensionAttributesLoader;

@AutoService(ExtensionAttributesLoader.class)
public class OperatingStatusAttributesLoader<I extends Identifiable<I>> implements ExtensionAttributesLoader<I> {
    @Override
    public Extension<I> load(I identifiable) {
        return new OperatingStatusImpl<>(identifiable); // code that was in the createExtension()
    }

    @Override
    public String getName() {
        return OperatingStatus.NAME;
    }

    @Override
    public Class<?> getType() {
        return OperatingStatus.class;
    }
}

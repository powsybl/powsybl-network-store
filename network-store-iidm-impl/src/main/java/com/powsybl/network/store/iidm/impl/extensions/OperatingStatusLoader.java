package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.OperatingStatus;
import com.powsybl.network.store.model.ExtensionLoader;
import com.powsybl.network.store.model.OperatingStatusAttributes;

@AutoService(ExtensionLoader.class)
public class OperatingStatusLoader<I extends Identifiable<I>> implements ExtensionLoader<I, OperatingStatus<I>, OperatingStatusAttributes> {
    @Override
    public Extension<I> load(I identifiable) {
        return new OperatingStatusImpl<>(identifiable);
    }

    @Override
    public String getName() {
        return OperatingStatus.NAME;
    }

    @Override
    public Class<OperatingStatus> getType() {
        return OperatingStatus.class;
    }

    @Override
    public Class<OperatingStatusAttributes> getAttributesType() {
        return OperatingStatusAttributes.class;
    }
}

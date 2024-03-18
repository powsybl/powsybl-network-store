package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.network.store.iidm.impl.AbstractInjectionImpl;
import com.powsybl.network.store.iidm.impl.ConnectablePositionImpl;
import com.powsybl.network.store.model.ConnectablePositionAttributes;
import com.powsybl.network.store.model.ExtensionAttributesLoader;

@AutoService(ExtensionAttributesLoader.class)
public class ConnectablePositionAttributesLoader<C extends Connectable<C>> implements ExtensionAttributesLoader<C> {
    @Override
    public Extension<C> load(C connectable) {
        // This should not all be null
        // How about getter 1, etc?
        return new ConnectablePositionImpl<>(connectable,
                connectableLambda -> (ConnectablePositionAttributes) ((AbstractInjectionImpl<?, ?>) connectableLambda).getResource().getAttributes().getExtensionAttributes().get(ConnectablePosition.NAME),
                null,
                null,
                null);
    }

    @Override
    public String getName() {
        return ConnectablePosition.NAME;
    }

    @Override
    public Class<?> getType() {
        return ConnectablePosition.class;
    }
}

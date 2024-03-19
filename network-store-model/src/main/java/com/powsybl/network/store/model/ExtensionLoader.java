package com.powsybl.network.store.model;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;

public interface ExtensionLoader<E extends Extendable> {

    Extension<E> load(E extendable);

    String getName();

    Class<?> getType();
}

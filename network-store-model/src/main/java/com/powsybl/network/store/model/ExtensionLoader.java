package com.powsybl.network.store.model;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;

public interface ExtensionLoader<T extends Extendable, E extends Extension<T>> {

    Extension<T> load(T extendable);

    String getName();

    Class<? super E> getType();
}

package com.powsybl.network.store.model;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;

public interface ExtensionLoader<T extends Extendable<T>, E extends Extension<T>, K extends ExtensionAttributes> {

    Extension<T> load(T extendable);

    String getName();

    Class<? super E> getType();

    Class<? super K > getAttributesType();
}

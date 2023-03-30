/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.network.store.model.InjectionAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public abstract class AbstractInjectionImpl<I extends Injection<I>, D extends InjectionAttributes> extends AbstractIdentifiableImpl<I, D> {

    protected final TerminalImpl<D> terminal;

    protected AbstractInjectionImpl(NetworkObjectIndex index, Resource<D> resource) {
        super(index, resource);
        terminal = TerminalImpl.create(index, resource.getAttributes(), getInjection());
    }

    protected abstract I getInjection();

    public List<? extends Terminal> getTerminals() {
        return Collections.singletonList(terminal);
    }

    public TerminalImpl<D> getTerminal() {
        return terminal;
    }

    public <E extends Extension<I>> E createConnectablePositionExtension() {
        E extension = null;
        var resource = getResource();
        if (resource.getAttributes().getPosition() != null) {
            return (E) new ConnectablePositionImpl<>(getInjection(),
                connectable -> ((AbstractInjectionImpl<?, ?>) connectable).getResource().getAttributes().getPosition(),
                null,
                null,
                null);
        }
        return extension;
    }

    @Override
    public <E extends Extension<I>> E getExtension(Class<? super E> type) {
        E extension;
        if (type == ConnectablePosition.class) {
            extension = createConnectablePositionExtension();
        } else {
            extension = super.getExtension(type);
        }
        return extension;
    }

    @Override
    public <E extends Extension<I>> E getExtensionByName(String name) {
        E extension;
        if (name.equals("position")) {
            extension = createConnectablePositionExtension();
        } else {
            extension = super.getExtensionByName(name);
        }
        return extension;
    }

    @Override
    public <E extends Extension<I>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        E extension = createConnectablePositionExtension();
        if (extension != null) {
            extensions.add(extension);
        }
        return extensions;
    }
}

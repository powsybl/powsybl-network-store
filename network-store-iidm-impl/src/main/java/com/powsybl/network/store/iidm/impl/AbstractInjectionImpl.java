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
import com.powsybl.network.store.iidm.impl.ConnectablePositionAdderImpl.ConnectablePositionCreator;
import com.powsybl.network.store.model.*;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;
import com.powsybl.sld.iidm.extensions.ConnectablePosition.Feeder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public abstract class AbstractInjectionImpl<I extends Injection<I>, D extends InjectionAttributes> extends AbstractIdentifiableImpl<I, D> implements ConnectablePositionCreator<I> {

    protected final Terminal terminal;

    private ConnectablePositionImpl<I> connectablePositionExtension;

    protected AbstractInjectionImpl(NetworkObjectIndex index, Resource<D> resource) {
        super(index, resource);
        terminal = TerminalImpl.create(index, resource.getAttributes(), getInjection());
        ConnectablePositionAttributes cpa = resource.getAttributes().getPosition();
        if (cpa != null) {
            connectablePositionExtension = new ConnectablePositionImpl<>(getInjection(),
                    new ConnectablePositionImpl.FeederImpl(cpa), null, null, null);
        }
    }

    protected abstract I getInjection();

    public List<? extends Terminal> getTerminals() {
        return Collections.singletonList(terminal);
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void remove() {
        //TODO
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public <E extends Extension<I>> void addExtension(Class<? super E> type, E extension) {
        var resource = checkResource();
        if (type == ConnectablePosition.class) {
            connectablePositionExtension = (ConnectablePositionImpl<I>) extension;
            resource.getAttributes().setPosition(connectablePositionExtension.getFeeder().getConnectablePositionAttributes());
            updateResource();
        } else {
            super.addExtension(type, extension);
        }
    }

    @Override
    public ConnectablePositionImpl<I> createConnectablePositionExtension(
            Feeder feeder, Feeder feeder1, Feeder feeder2, Feeder feeder3) {
        Objects.requireNonNull(feeder);
        ConnectablePosition.check(feeder, feeder1, feeder2, feeder3);
        ConnectablePositionAttributes cpa = ConnectablePositionAttributes.builder()
                .label(feeder.getName())
                .order(feeder.getOrder())
                .direction(ConnectableDirection.valueOf(feeder.getDirection().name()))
                .build();
        return new ConnectablePositionImpl<>(getInjection(),
                new ConnectablePositionImpl.FeederImpl(cpa),
                null, null, null);
    }

    @Override
    public <E extends Extension<I>> E getExtension(Class<? super E> type) {
        E extension;
        if (type == ConnectablePosition.class) {
            extension = (E) connectablePositionExtension;
        } else {
            extension = super.getExtension(type);
        }
        return extension;
    }

    @Override
    public <E extends Extension<I>> E getExtensionByName(String name) {
        E extension;
        if (name.equals("position")) {
            extension = (E) connectablePositionExtension;
        } else {
            extension = super.getExtensionByName(name);
        }
        return extension;
    }

    @Override
    public <E extends Extension<I>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        if (connectablePositionExtension != null) {
            extensions.add((E) connectablePositionExtension);
        }
        return extensions;
    }
}

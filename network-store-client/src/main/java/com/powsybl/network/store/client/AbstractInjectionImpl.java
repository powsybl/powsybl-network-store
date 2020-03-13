/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.ConnectableDirection;
import com.powsybl.network.store.model.ConnectablePositionAttributes;
import com.powsybl.network.store.model.InjectionAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;

import java.util.Collections;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractInjectionImpl<I extends Injection<I>, D extends InjectionAttributes> extends AbstractIdentifiableImpl<I, D> {

    protected final Terminal terminal;

    protected AbstractInjectionImpl(NetworkObjectIndex index, Resource<D> resource) {
        super(index, resource);
        terminal = TerminalImpl.create(index, resource.getAttributes(), getInjection());
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
    }

    @Override
    public <E extends Extension<I>> void addExtension(Class<? super E> type, E extension) {
        super.addExtension(type, extension);
        if (type == ConnectablePosition.class) {
            ConnectablePosition position = (ConnectablePosition) extension;
            resource.getAttributes().setPosition(ConnectablePositionAttributes.builder()
                    .label(position.getFeeder().getName())
                    .order(position.getFeeder().getOrder())
                    .direction(ConnectableDirection.valueOf(position.getFeeder().getDirection().name()))
                    .build());
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends Extension<I>> E createConnectablePositionExtension() {
        E extension = null;
        ConnectablePositionAttributes positionAttributes = resource.getAttributes().getPosition();
        if (positionAttributes != null) {
            extension = (E) new ConnectablePosition<>(getInjection(),
                                                      new ConnectablePosition.Feeder(positionAttributes.getLabel(),
                                                                                     positionAttributes.getOrder(),
                                                                                     ConnectablePosition.Direction.valueOf(positionAttributes.getDirection().name())),
                                                      null, null, null);
        }
        return extension;
    }

    @Override
    public <E extends Extension<I>> E getExtension(Class<? super E> type) {
        E extension = super.getExtension(type);
        if (type == ConnectablePosition.class) {
            extension = createConnectablePositionExtension();
        }
        return extension;
    }

    @Override
    public <E extends Extension<I>> E getExtensionByName(String name) {
        E extension = super.getExtensionByName(name);
        if (name.equals("position")) {
            extension = createConnectablePositionExtension();
        }
        return extension;
    }
}

/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.entsoe.util.Xnode;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.*;
import com.powsybl.sld.iidm.extensions.ConnectablePosition;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractInjectionImpl<I extends Injection<I>, D extends InjectionAttributes> extends AbstractIdentifiableImpl<I, D> {

    protected final Terminal terminal;

    protected AbstractInjectionImpl(NetworkObjectIndex index, Resource<D> resource) {
        super(index, resource);
        terminal = TerminalNodeBreakerImpl.create(index, resource, Function.identity(), getInjection());
    }

    protected abstract I getInjection();

    public List<? extends Terminal> getTerminals() {
        return Collections.singletonList(terminal);
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void remove() {
        throw new UnsupportedOperationException("TODO");
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
        } else if (type == Xnode.class) {
            Xnode xnode = (Xnode) extension;
            Resource<DanglingLineAttributes> danglingLineResource = (Resource<DanglingLineAttributes>) resource;
            danglingLineResource.getAttributes().setUcteXnodeCode(xnode.getCode());
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

    @SuppressWarnings("unchecked")
    private <E extends Extension<I>> E createXnodeExtension() {
        E extension = null;
        Resource<DanglingLineAttributes> danglingLineResource = (Resource<DanglingLineAttributes>) resource;
        DanglingLine dl = index.getDanglingLine(resource.getId())
                .orElseThrow(() -> new PowsyblException("DanglingLine " + resource.getId() + " doesn't exist"));
        String xNodeCode = danglingLineResource.getAttributes().getUcteXnodeCode();
        if (xNodeCode != null) {
            extension = (E) new Xnode(dl, xNodeCode);
        }
        return extension;
    }

    @Override
    public <E extends Extension<I>> E getExtension(Class<? super E> type) {
        E extension = super.getExtension(type);
        if (type == ConnectablePosition.class) {
            extension = createConnectablePositionExtension();
        } else if (type == Xnode.class) {
            extension = createXnodeExtension();
        }
        return extension;
    }

    @Override
    public <E extends Extension<I>> E getExtensionByName(String name) {
        E extension = super.getExtensionByName(name);
        if (name.equals("position")) {
            extension = createConnectablePositionExtension();
        } else if (name.equals("xnode")) {
            extension = createXnodeExtension();
        }
        return extension;
    }
}

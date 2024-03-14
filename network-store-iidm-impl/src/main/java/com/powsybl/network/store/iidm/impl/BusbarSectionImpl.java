/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.network.store.model.BusbarSectionAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class BusbarSectionImpl extends AbstractIdentifiableImpl<BusbarSection, BusbarSectionAttributes> implements BusbarSection {

    protected final TerminalImpl<BusbarSectionAttributes> terminal;

    public BusbarSectionImpl(NetworkObjectIndex index, Resource<BusbarSectionAttributes> resource) {
        super(index, resource);
        terminal = new TerminalImpl<>(index, this, r -> new BusbarSectionToInjectionAdapter(r.getAttributes()));
    }

    static BusbarSectionImpl create(NetworkObjectIndex index, Resource<BusbarSectionAttributes> resource) {
        return new BusbarSectionImpl(index, resource);
    }

    public List<? extends Terminal> getTerminals() {
        return Collections.singletonList(terminal);
    }

    @Override
    public void remove() {
        var resource = getResource();
        index.notifyBeforeRemoval(this);
        // invalidate calculated buses before removal otherwise voltage levels won't be accessible anymore for topology invalidation!
        invalidateCalculatedBuses(getTerminals());
        index.removeBusBarSection(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }

    public TerminalImpl<BusbarSectionAttributes> getTerminal() {
        return terminal;
    }

    private <E extends Extension<BusbarSection>> E createBusbarSectionPositionExtension() {
        E extension = null;
        var resource = getResource();
        var attributes = resource.getAttributes().getPosition();
        if (attributes != null) {
            extension = (E) new BusbarSectionPositionImpl(this);
        }
        return extension;
    }

    @Override
    public <E extends Extension<BusbarSection>> E getExtension(Class<? super E> type) {
        E extension;
        if (type == BusbarSectionPosition.class) {
            extension = createBusbarSectionPositionExtension();
        } else {
            extension = super.getExtension(type);
        }
        return extension;
    }

    @Override
    public <E extends Extension<BusbarSection>> E getExtensionByName(String name) {
        E extension;
        if (name.equals("position")) {
            extension = createBusbarSectionPositionExtension();
        } else {
            extension = super.getExtensionByName(name);
        }
        return extension;
    }

    @Override
    public <E extends Extension<BusbarSection>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        E extension = createBusbarSectionPositionExtension();
        if (extension != null) {
            extensions.add(extension);
        }
        return extensions;
    }

    @Override
    public double getV() {
        return getTerminal().isConnected() ? getTerminal().getBusView().getBus().getV() : Double.NaN;
    }

    @Override
    public double getAngle() {
        return getTerminal().isConnected() ? getTerminal().getBusView().getBus().getAngle() : Double.NaN;
    }

    @Override
    public boolean connect() {
        return terminal.connect();
    }

    @Override
    public boolean connect(Predicate<Switch> isTypeSwitchToOperate) {
        return terminal.connect(isTypeSwitchToOperate);
    }

    @Override
    public boolean disconnect() {
        return terminal.disconnect();
    }

    @Override
    public boolean disconnect(Predicate<Switch> isSwitchOpenable) {
        return terminal.disconnect(isSwitchOpenable);
    }
}

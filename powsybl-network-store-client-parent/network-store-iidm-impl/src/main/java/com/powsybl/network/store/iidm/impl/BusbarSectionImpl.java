/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.network.store.model.BusbarSectionAttributes;
import com.powsybl.network.store.model.BusbarSectionPositionAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class BusbarSectionImpl extends AbstractIdentifiableImpl<BusbarSection, BusbarSectionAttributes> implements BusbarSection {

    protected final TerminalImpl<BusbarSectionToInjectionAdapter> terminal;

    private BusbarSectionPositionImpl busbarSectionPosition;

    public BusbarSectionImpl(NetworkObjectIndex index, Resource<BusbarSectionAttributes> resource) {
        super(index, resource);
        terminal = TerminalImpl.create(index, new BusbarSectionToInjectionAdapter(resource.getAttributes()), this);
        BusbarSectionPositionAttributes bspa = resource.getAttributes().getPosition();
        if (bspa != null) {
            busbarSectionPosition = new BusbarSectionPositionImpl(this, bspa);
        }
    }

    static BusbarSectionImpl create(NetworkObjectIndex index, Resource<BusbarSectionAttributes> resource) {
        return new BusbarSectionImpl(index, resource);
    }

    public List<? extends Terminal> getTerminals() {
        return Collections.singletonList(terminal);
    }

    @Override
    public void remove() {
        var resource = checkResource();
        index.notifyBeforeRemoval(this);
        // invalidate calculated buses before removal otherwise voltage levels won't be accessible anymore for topology invalidation!
        invalidateCalculatedBuses(getTerminals());
        index.removeBusBarSection(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }

    public TerminalImpl<BusbarSectionToInjectionAdapter> getTerminal() {
        return terminal;
    }

    @Override
    public <E extends Extension<BusbarSection>> void addExtension(Class<? super E> type, E extension) {
        var resource = checkResource();
        if (type == BusbarSectionPosition.class) {
            busbarSectionPosition = (BusbarSectionPositionImpl) extension;
            resource.getAttributes().setPosition(busbarSectionPosition.getBusbarSectionPositionAttributes());
            updateResource();
        } else {
            super.addExtension(type, extension);
        }
    }

    @Override
    public <E extends Extension<BusbarSection>> E getExtension(Class<? super E> type) {
        E extension;
        if (type == BusbarSectionPosition.class) {
            extension = (E) busbarSectionPosition;
        } else {
            extension = super.getExtension(type);
        }
        return extension;
    }

    @Override
    public <E extends Extension<BusbarSection>> E getExtensionByName(String name) {
        E extension;
        if (name.equals("position")) {
            extension = (E) busbarSectionPosition;
        } else {
            extension = super.getExtensionByName(name);
        }
        return extension;
    }

    @Override
    public <E extends Extension<BusbarSection>> Collection<E> getExtensions() {
        Collection<E> superExtensions = super.getExtensions();
        Collection<E> result;
        if (busbarSectionPosition != null) {
            result = new ArrayList<E>(superExtensions);
            result.add((E) busbarSectionPosition);
        } else {
            result = superExtensions;
        }
        return result;
    }

    @Override
    public double getV() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public double getAngle() {
        throw new UnsupportedOperationException("TODO");
    }
}

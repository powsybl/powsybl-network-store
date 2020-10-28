/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.BusbarSectionAttributes;
import com.powsybl.network.store.model.BusbarSectionPositionAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.sld.iidm.extensions.BusbarSectionPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BusbarSectionImpl extends AbstractIdentifiableImpl<BusbarSection, BusbarSectionAttributes> implements BusbarSection {

    protected final Terminal terminal;

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

    @Override
    public ConnectableType getType() {
        return ConnectableType.BUSBAR_SECTION;
    }

    public List<? extends Terminal> getTerminals() {
        return Collections.singletonList(terminal);
    }

    @Override
    public void remove() {
        index.removeBusBarSection(resource.getId());
    }

    public Terminal getTerminal() {
        return terminal;
    }

    @Override
    public <E extends Extension<BusbarSection>> void addExtension(Class<? super E> type, E extension) {
        if (type == BusbarSectionPosition.class) {
            busbarSectionPosition = (BusbarSectionPositionImpl) extension;
            resource.getAttributes().setPosition(busbarSectionPosition.getBusbarSectionPositionAttributes());
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
            result = new ArrayList<E>();
            result.addAll(superExtensions);
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

    @Override
    protected String getTypeDescription() {
        return "Busbar section";
    }
}

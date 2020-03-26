/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.BusbarSectionAttributes;
import com.powsybl.network.store.model.BusbarSectionPositionAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.sld.iidm.extensions.BusbarSectionPosition;

import java.util.Collections;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BusbarSectionImpl extends AbstractIdentifiableImpl<BusbarSection, BusbarSectionAttributes> implements BusbarSection {

    protected final Terminal terminal;

    public BusbarSectionImpl(NetworkObjectIndex index, Resource<BusbarSectionAttributes> resource) {
        super(index, resource);
        terminal = TerminalImpl.create(index, new BusbarSectionToInjectionAdapter(resource.getAttributes()), this);
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
        throw new UnsupportedOperationException("TODO");
    }

    public Terminal getTerminal() {
        return terminal;
    }

    @Override
    public <E extends Extension<BusbarSection>> void addExtension(Class<? super E> type, E extension) {
        super.addExtension(type, extension);
        if (type == BusbarSectionPosition.class) {
            BusbarSectionPosition position = (BusbarSectionPosition) extension;
            resource.getAttributes().setPosition(BusbarSectionPositionAttributes.builder()
                    .busbarIndex(position.getBusbarIndex())
                    .sectionIndex(position.getSectionIndex())
                    .build());
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends Extension<BusbarSection>> E createBusbarSectionPositionExtension() {
        E extension = null;
        BusbarSectionPositionAttributes positionAttributes = resource.getAttributes().getPosition();
        if (positionAttributes != null) {
            extension = (E) new BusbarSectionPosition(this, positionAttributes.getBusbarIndex(), positionAttributes.getSectionIndex());
        }
        return extension;
    }

    @Override
    public <E extends Extension<BusbarSection>> E getExtension(Class<? super E> type) {
        E extension = super.getExtension(type);
        if (extension == null && type == BusbarSectionPosition.class) {
            extension = createBusbarSectionPositionExtension();
        }
        return extension;
    }

    @Override
    public <E extends Extension<BusbarSection>> E getExtensionByName(String name) {
        E extension = super.getExtensionByName(name);
        if (extension == null && name.equals("busbarSectionPosition")) {
            extension = createBusbarSectionPositionExtension();
        }
        return extension;
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

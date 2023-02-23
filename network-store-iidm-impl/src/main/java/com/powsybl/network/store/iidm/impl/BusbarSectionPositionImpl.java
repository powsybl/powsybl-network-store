/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.network.store.model.BusbarSectionAttributes;
import com.powsybl.network.store.model.BusbarSectionPositionAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.Objects;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public class BusbarSectionPositionImpl implements BusbarSectionPosition {

    private BusbarSectionImpl busbarSectionImpl;

    public BusbarSectionPositionImpl(BusbarSectionImpl busbarSectionImpl) {
        this.busbarSectionImpl = Objects.requireNonNull(busbarSectionImpl);
    }

    private static int checkIndex(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Busbar index has to be greater or equals to zero");
        }
        return index;
    }

    @Override
    public BusbarSection getExtendable() {
        return busbarSectionImpl;
    }

    @Override
    public void setExtendable(BusbarSection busbarSection) {
        this.busbarSectionImpl = (BusbarSectionImpl) busbarSection;
    }

    @Override
    public int getBusbarIndex() {
        return getPositionAttributes().getBusbarIndex();
    }

    private BusbarSectionPositionAttributes getPositionAttributes(Resource<BusbarSectionAttributes> attributes) {
        return attributes.getAttributes().getPosition();
    }

    private BusbarSectionPositionAttributes getPositionAttributes() {
        return getPositionAttributes(busbarSectionImpl.getResource());
    }

    @Override
    public BusbarSectionPosition setBusbarIndex(int busbarIndex) {
        busbarSectionImpl.updateResource(res -> getPositionAttributes(res).setBusbarIndex(checkIndex(busbarIndex)));
        return this;
    }

    @Override
    public int getSectionIndex() {
        return getPositionAttributes().getSectionIndex();
    }

    @Override
    public BusbarSectionPosition setSectionIndex(int sectionIndex) {
        busbarSectionImpl.updateResource(res -> getPositionAttributes(res).setSectionIndex(checkIndex(sectionIndex)));
        return this;
    }
}

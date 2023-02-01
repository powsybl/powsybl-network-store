/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.network.store.model.BusbarSectionPositionAttributes;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public class BusbarSectionPositionImpl implements BusbarSectionPosition {

    private BusbarSectionImpl busbarSectionImpl;

    private final BusbarSectionPositionAttributes busbarSectionPositionAttributes;

    public BusbarSectionPositionImpl(BusbarSectionImpl busbarSectionImpl,
            BusbarSectionPositionAttributes busbarSectionPositionAttributes) {
        this.busbarSectionImpl = busbarSectionImpl;
        this.busbarSectionPositionAttributes = busbarSectionPositionAttributes;
    }

    public BusbarSectionPositionImpl(BusbarSectionImpl busbarSectionImpl,
            BusbarSectionPositionAttributes busbarSectionPositionAttributes,
            int busbarIndex, int sectionIndex) {
        this(busbarSectionImpl, busbarSectionPositionAttributes);
        busbarSectionPositionAttributes.setBusbarIndex(checkIndex(busbarIndex));
        busbarSectionPositionAttributes.setSectionIndex(checkIndex(sectionIndex));
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
        return busbarSectionPositionAttributes.getBusbarIndex();
    }

    @Override
    public BusbarSectionPosition setBusbarIndex(int busbarIndex) {
        busbarSectionPositionAttributes.setBusbarIndex(checkIndex(busbarIndex));
        busbarSectionImpl.updateResource();
        return this;
    }

    @Override
    public int getSectionIndex() {
        return busbarSectionPositionAttributes.getSectionIndex();
    }

    @Override
    public BusbarSectionPosition setSectionIndex(int sectionIndex) {
        busbarSectionPositionAttributes.setSectionIndex(checkIndex(sectionIndex));
        busbarSectionImpl.updateResource();
        return this;
    }

    public BusbarSectionPositionAttributes getBusbarSectionPositionAttributes() {
        return busbarSectionPositionAttributes;
    }

}

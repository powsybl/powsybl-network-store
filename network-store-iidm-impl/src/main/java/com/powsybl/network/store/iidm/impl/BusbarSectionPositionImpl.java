/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.network.store.model.BusbarSectionAttributes;
import com.powsybl.network.store.model.BusbarSectionPositionAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.Objects;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public class BusbarSectionPositionImpl extends AbstractExtension<BusbarSection> implements BusbarSectionPosition {

    private enum BusbarSectionType {
        BUSBAR("Busbar"),
        SECTION("Section");

        final String description;

        BusbarSectionType(String description) {
            this.description = Objects.requireNonNull(description);
        }

        String getDescription() {
            return description;
        }
    }

    public BusbarSectionPositionImpl(BusbarSectionImpl busbarSectionImpl) {
        super(busbarSectionImpl);
        checkIndex(busbarSectionImpl, getBusbarIndex(), BusbarSectionType.BUSBAR);
        checkIndex(busbarSectionImpl, getSectionIndex(), BusbarSectionType.SECTION);
    }

    private static int checkIndex(Validable validable, int index, BusbarSectionType type) {
        if (index < 0) {
            throw new ValidationException(validable, type.getDescription() + " index has to be greater or equals to zero");
        }
        return index;
    }

    private BusbarSectionImpl getBusbarSection() {
        return (BusbarSectionImpl) getExtendable();
    }

    private BusbarSectionPositionAttributes getPositionAttributes() {
        return getPositionAttributes(getBusbarSection().getResource());
    }

    @Override
    public int getBusbarIndex() {
        return getPositionAttributes().getBusbarIndex();
    }

    private BusbarSectionPositionAttributes getPositionAttributes(Resource<BusbarSectionAttributes> resource) {
        return resource.getAttributes().getPosition();
    }

    @Override
    public BusbarSectionPosition setBusbarIndex(int busbarIndex) {
        getBusbarSection().updateResource(res -> getPositionAttributes(res).setBusbarIndex(checkIndex(getBusbarSection(), busbarIndex, BusbarSectionType.BUSBAR)));
        return this;
    }

    @Override
    public int getSectionIndex() {
        return getPositionAttributes().getSectionIndex();
    }

    @Override
    public BusbarSectionPosition setSectionIndex(int sectionIndex) {
        getBusbarSection().updateResource(res -> getPositionAttributes(res).setSectionIndex(checkIndex(getBusbarSection(), sectionIndex, BusbarSectionType.SECTION)));
        return this;
    }
}

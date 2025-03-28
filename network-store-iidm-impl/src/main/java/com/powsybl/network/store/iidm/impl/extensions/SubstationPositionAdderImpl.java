/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.SubstationPosition;
import com.powsybl.iidm.network.extensions.SubstationPositionAdder;
import com.powsybl.network.store.iidm.impl.SubstationImpl;
import com.powsybl.network.store.model.SubstationPositionAttributes;

import java.util.Objects;

/**
 * @author Seddik Yengui <seddik.yengui_externe at rte-france.com>
 */

public class SubstationPositionAdderImpl extends AbstractIidmExtensionAdder<Substation, SubstationPosition> implements SubstationPositionAdder {

    private Coordinate coordinate;

    protected SubstationPositionAdderImpl(Substation substation) {
        super(substation);
    }

    @Override
    protected SubstationPosition createExtension(Substation substation) {
        SubstationPositionAttributes oldValue = (SubstationPositionAttributes) ((SubstationImpl) substation).getResource().getAttributes().getExtensionAttributes().get(SubstationPosition.NAME);
        var attributes = new SubstationPositionAttributes(coordinate);
        ((SubstationImpl) substation).updateResource(res -> res.getAttributes().getExtensionAttributes().put(SubstationPosition.NAME, attributes),
            "position", oldValue, attributes);
        return new SubstationPositionImpl(substation);
    }

    @Override
    public SubstationPositionAdder withCoordinate(Coordinate coordinate) {
        this.coordinate = Objects.requireNonNull(coordinate);
        return this;
    }
}

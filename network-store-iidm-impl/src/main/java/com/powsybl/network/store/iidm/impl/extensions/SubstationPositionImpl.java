/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.SubstationPosition;
import com.powsybl.network.store.iidm.impl.SubstationImpl;
import com.powsybl.network.store.model.SubstationPositionAttributes;

/**
 * @author Seddik Yengui <seddik.yengui_externe at rte-france.com>
 */

public class SubstationPositionImpl extends AbstractExtension<Substation> implements SubstationPosition {

    public SubstationPositionImpl(Substation substation) {
        super(substation);
    }

    private SubstationImpl getSubstation() {
        return (SubstationImpl) getExtendable();
    }

    private SubstationPositionAttributes getSubstationPositionAttributes() {
        return (SubstationPositionAttributes) getSubstation().getResource().getAttributes().getExtensionAttributes().get(SubstationPosition.NAME);
    }

    @Override
    public Coordinate getCoordinate() {
        return new Coordinate(getSubstationPositionAttributes().getCoordinate().getLatitude(), getSubstationPositionAttributes().getCoordinate().getLongitude());
    }
}

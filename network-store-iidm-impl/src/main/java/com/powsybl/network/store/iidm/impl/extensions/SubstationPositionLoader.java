/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.extensions.SubstationPosition;
import com.powsybl.network.store.model.ExtensionLoader;
import com.powsybl.network.store.model.SubstationPositionAttributes;

/**
 * @author Seddik Yengui <seddik.yengui_externe at rte-france.com>
 */

public class SubstationPositionLoader implements ExtensionLoader<Substation, SubstationPosition, SubstationPositionAttributes> {
    @Override
    public Extension<Substation> load(Substation extendable) {
        return new SubstationPositionImpl(extendable);
    }

    @Override
    public String getName() {
        return SubstationPosition.NAME;
    }

    @Override
    public Class<? super SubstationPosition> getType() {
        return SubstationPosition.class;
    }

    @Override
    public Class<? super SubstationPositionAttributes> getAttributesType() {
        return SubstationPositionAttributes.class;
    }
}

/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.LinePosition;
import com.powsybl.network.store.model.ExtensionLoader;
import com.powsybl.network.store.model.LinePositionAttributes;

/**
 * @author Seddik Yengui <seddik.yengui_externe at rte-france.com>
 */

@AutoService(ExtensionLoader.class)
public class LinePositionLoader<I extends Identifiable<I>> implements ExtensionLoader<I, LinePosition<I>, LinePositionAttributes> {
    @Override
    public Extension<I> load(I identifiable) {
        return new LinePositionImpl<>(identifiable);
    }

    @Override
    public String getName() {
        return LinePosition.NAME;
    }

    @Override
    public Class<LinePosition> getType() {
        return LinePosition.class;
    }

    @Override
    public Class<LinePositionAttributes> getAttributesType() {
        return LinePositionAttributes.class;
    }
}

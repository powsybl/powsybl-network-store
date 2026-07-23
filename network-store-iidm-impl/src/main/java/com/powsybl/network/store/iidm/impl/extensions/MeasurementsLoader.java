/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.Measurements;
import com.powsybl.network.store.model.ExtensionLoader;
import com.powsybl.network.store.model.MeasurementsAttributes;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
@AutoService(ExtensionLoader.class)
public class MeasurementsLoader<C extends Connectable<C>> implements ExtensionLoader<C, Measurements<C>, MeasurementsAttributes> {

    @Override
    public Extension<C> load(C connectable) {
        return new MeasurementsImpl<>(connectable);
    }

    @Override
    public String getName() {
        return Measurements.NAME;
    }

    @Override
    public Class<Measurements> getType() {
        return Measurements.class;
    }

    @Override
    public Class<MeasurementsAttributes> getAttributesType() {
        return MeasurementsAttributes.class;
    }
}

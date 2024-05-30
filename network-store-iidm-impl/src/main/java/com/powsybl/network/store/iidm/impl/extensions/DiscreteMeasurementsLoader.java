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
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.DiscreteMeasurements;
import com.powsybl.network.store.model.DiscreteMeasurementsAttributes;
import com.powsybl.network.store.model.ExtensionLoader;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
@AutoService(ExtensionLoader.class)
public class DiscreteMeasurementsLoader<I extends Identifiable<I>> implements ExtensionLoader<I, DiscreteMeasurements<I>, DiscreteMeasurementsAttributes> {

    @Override
    public Extension<I> load(I identifiable) {
        return new DiscreteMeasurementsImpl<>(identifiable);
    }

    @Override
    public String getName() {
        return DiscreteMeasurements.NAME;
    }

    @Override
    public Class<DiscreteMeasurements> getType() {
        return DiscreteMeasurements.class;
    }

    @Override
    public Class<DiscreteMeasurementsAttributes> getAttributesType() {
        return DiscreteMeasurementsAttributes.class;
    }
}

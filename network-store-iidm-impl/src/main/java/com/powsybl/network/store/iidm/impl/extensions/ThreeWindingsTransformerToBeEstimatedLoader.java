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
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerToBeEstimated;
import com.powsybl.network.store.model.ExtensionLoader;
import com.powsybl.network.store.model.ThreeWindingsTransformerToBeEstimatedAttributes;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@AutoService(ExtensionLoader.class)
public class ThreeWindingsTransformerToBeEstimatedLoader implements ExtensionLoader<ThreeWindingsTransformer, ThreeWindingsTransformerToBeEstimated, ThreeWindingsTransformerToBeEstimatedAttributes> {
    @Override
    public Extension<ThreeWindingsTransformer> load(ThreeWindingsTransformer threeWindingsTransformer) {
        return new ThreeWindingsTransformerToBeEstimatedImpl(threeWindingsTransformer);
    }

    @Override
    public String getName() {
        return ThreeWindingsTransformerToBeEstimated.NAME;
    }

    @Override
    public Class<ThreeWindingsTransformerToBeEstimated> getType() {
        return ThreeWindingsTransformerToBeEstimated.class;
    }

    @Override
    public Class<ThreeWindingsTransformerToBeEstimatedAttributes> getAttributesType() {
        return ThreeWindingsTransformerToBeEstimatedAttributes.class;
    }
}

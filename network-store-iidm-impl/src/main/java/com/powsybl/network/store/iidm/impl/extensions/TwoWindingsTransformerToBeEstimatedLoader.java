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
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimated;
import com.powsybl.network.store.model.ExtensionLoader;
import com.powsybl.network.store.model.TwoWindingsTransformerToBeEstimatedAttributes;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@AutoService(ExtensionLoader.class)
public class TwoWindingsTransformerToBeEstimatedLoader implements ExtensionLoader<TwoWindingsTransformer, TwoWindingsTransformerToBeEstimated, TwoWindingsTransformerToBeEstimatedAttributes> {
    @Override
    public Extension<TwoWindingsTransformer> load(TwoWindingsTransformer twoWindingsTransformer) {
        return new TwoWindingsTransformerToBeEstimatedImpl(twoWindingsTransformer);
    }

    @Override
    public String getName() {
        return TwoWindingsTransformerToBeEstimated.NAME;
    }

    @Override
    public Class<TwoWindingsTransformerToBeEstimated> getType() {
        return TwoWindingsTransformerToBeEstimated.class;
    }

    @Override
    public Class<TwoWindingsTransformerToBeEstimatedAttributes> getAttributesType() {
        return TwoWindingsTransformerToBeEstimatedAttributes.class;
    }
}

/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescue;
import com.powsybl.network.store.model.ExtensionLoader;
import com.powsybl.network.store.model.TwoWindingsTransformerFortescueAttributes;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@AutoService(ExtensionLoader.class)
public class TwoWindingsTransformerFortescueLoader implements ExtensionLoader<TwoWindingsTransformer, TwoWindingsTransformerFortescue, TwoWindingsTransformerFortescueAttributes> {
    @Override
    public Extension<TwoWindingsTransformer> load(TwoWindingsTransformer twoWindingsTransformer) {
        return new TwoWindingsTransformerFortescueImpl(twoWindingsTransformer);
    }

    @Override
    public String getName() {
        return TwoWindingsTransformerFortescue.NAME;
    }

    @Override
    public Class<TwoWindingsTransformerFortescue> getType() {
        return TwoWindingsTransformerFortescue.class;
    }

    @Override
    public Class<TwoWindingsTransformerFortescueAttributes> getAttributesType() {
        return TwoWindingsTransformerFortescueAttributes.class;
    }
}

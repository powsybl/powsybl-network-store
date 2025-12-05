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
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerFortescue;
import com.powsybl.network.store.model.ExtensionLoader;
import com.powsybl.network.store.model.ThreeWindingsTransformerFortescueAttributes;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@AutoService(ExtensionLoader.class)
public class ThreeWindingsTransformerFortescueLoader implements ExtensionLoader<ThreeWindingsTransformer, ThreeWindingsTransformerFortescue, ThreeWindingsTransformerFortescueAttributes> {
    @Override
    public Extension<ThreeWindingsTransformer> load(ThreeWindingsTransformer threeWindingsTransformer) {
        return new ThreeWindingsTransformerFortescueImpl(threeWindingsTransformer);
    }

    @Override
    public String getName() {
        return ThreeWindingsTransformerFortescue.NAME;
    }

    @Override
    public Class<ThreeWindingsTransformerFortescue> getType() {
        return ThreeWindingsTransformerFortescue.class;
    }

    @Override
    public Class<ThreeWindingsTransformerFortescueAttributes> getAttributesType() {
        return ThreeWindingsTransformerFortescueAttributes.class;
    }
}

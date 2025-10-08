/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerFortescue;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class ThreeWindingsTransformerFortescueAdderImplProvider implements ExtensionAdderProvider<ThreeWindingsTransformer, ThreeWindingsTransformerFortescue, ThreeWindingsTransformerFortescueAdderImpl> {

    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public String getExtensionName() {
        return ThreeWindingsTransformerFortescue.NAME;
    }

    @Override
    public Class<ThreeWindingsTransformerFortescueAdderImpl> getAdderClass() {
        return ThreeWindingsTransformerFortescueAdderImpl.class;
    }

    @Override
    public ThreeWindingsTransformerFortescueAdderImpl newAdder(ThreeWindingsTransformer extendable) {
        return new ThreeWindingsTransformerFortescueAdderImpl(extendable);
    }
}

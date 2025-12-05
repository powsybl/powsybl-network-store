/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.LegFortescue;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerFortescue;
import com.powsybl.network.store.iidm.impl.ThreeWindingsTransformerImpl;
import com.powsybl.network.store.model.ThreeWindingsTransformerFortescueAttributes;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class ThreeWindingsTransformerFortescueImpl extends AbstractExtension<ThreeWindingsTransformer> implements ThreeWindingsTransformerFortescue {
    public ThreeWindingsTransformerFortescueImpl(ThreeWindingsTransformer threeWindingsTransformer) {
        super(threeWindingsTransformer);
    }

    private ThreeWindingsTransformerImpl getThreeWindingsTransformer() {
        return (ThreeWindingsTransformerImpl) getExtendable();
    }

    private ThreeWindingsTransformerFortescueAttributes getThreeWindingsTransformerFortescueAttributes() {
        return (ThreeWindingsTransformerFortescueAttributes) getThreeWindingsTransformer().getResource().getAttributes().getExtensionAttributes().get(ThreeWindingsTransformerFortescue.NAME);
    }

    @Override
    public LegFortescue getLeg1() {
        return new LegFortescueImpl(getThreeWindingsTransformerFortescueAttributes().getLeg1());
    }

    @Override
    public LegFortescue getLeg2() {
        return new LegFortescueImpl(getThreeWindingsTransformerFortescueAttributes().getLeg2());
    }

    @Override
    public LegFortescue getLeg3() {
        return new LegFortescueImpl(getThreeWindingsTransformerFortescueAttributes().getLeg3());
    }
}

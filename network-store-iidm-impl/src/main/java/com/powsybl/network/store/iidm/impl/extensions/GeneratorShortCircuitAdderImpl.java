/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuit;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuitAdder;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;
import com.powsybl.network.store.model.ShortCircuitAttributes;

/**
 * @author Seddik Yengui <seddik.yengui at rte-france.com>
 */

public class GeneratorShortCircuitAdderImpl extends AbstractShortCircuitAdderImpl<Generator, GeneratorShortCircuit, GeneratorShortCircuitAdder> implements GeneratorShortCircuitAdder {

    protected GeneratorShortCircuitAdderImpl(Generator extendable) {
        super(extendable);
    }

    @Override
    protected GeneratorShortCircuitAdder self() {
        return this;
    }

    @Override
    protected GeneratorShortCircuit createExtension(Generator generator) {
        var attributes = ShortCircuitAttributes.builder()
                .directSubtransX(directSubtransX)
                .directTransX(directTransX)
                .stepUpTransformerX(stepUpTransformerX)
                .build();
        ((GeneratorImpl) generator).updateResourceWithoutNotification(res -> res.getAttributes().setGeneratorShortCircuitAttributes(attributes));
        return new GeneratorShortCircuitImpl((GeneratorImpl) generator);
    }
}

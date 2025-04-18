/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuit;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;
import com.powsybl.network.store.model.ShortCircuitAttributes;

import java.util.function.Consumer;

/**
 * @author Seddik Yengui <seddik.yengui at rte-france.com>
 */

public class GeneratorShortCircuitImpl extends AbstractShortCircuitExtensionImpl<Generator, GeneratorShortCircuitImpl> implements GeneratorShortCircuit {

    public GeneratorShortCircuitImpl(GeneratorImpl generator) {
        super(generator, generator.getResource().getAttributes().getGeneratorShortCircuitAttributes());
    }

    private GeneratorImpl getGenerator() {
        return (GeneratorImpl) getExtendable();
    }

    @Override
    protected GeneratorShortCircuitImpl self() {
        return this;
    }

    @Override
    protected void updateAttributes(double newValue, double oldValue, String name, Consumer<ShortCircuitAttributes> modifier) {
        getGenerator().updateResourceExtension(this, res ->
            modifier.accept(res.getAttributes().getGeneratorShortCircuitAttributes()), name, oldValue, newValue);
    }
}

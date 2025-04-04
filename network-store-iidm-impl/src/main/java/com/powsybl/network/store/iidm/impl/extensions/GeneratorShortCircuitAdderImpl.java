/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuit;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuitAdder;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;
import com.powsybl.network.store.model.GeneratorShortCircuitAttributes;

/**
 * @author Seddik Yengui <seddik.yengui at rte-france.com>
 */

public class GeneratorShortCircuitAdderImpl extends AbstractIidmExtensionAdder<Generator, GeneratorShortCircuit> implements GeneratorShortCircuitAdder {

    private double directTransX = 0.0D;
    private double directSubtransX = Double.NaN;
    private double stepUpTransformerX = Double.NaN;

    protected GeneratorShortCircuitAdderImpl(Generator extendable) {
        super(extendable);
    }

    protected GeneratorShortCircuit createExtension(Generator generator) {
        var attributes = GeneratorShortCircuitAttributes.builder()
                .directSubtransX(directSubtransX)
                .directTransX(directTransX)
                .stepUpTransformerX(stepUpTransformerX)
                .build();
        ((GeneratorImpl) generator).updateResourceWithoutNotification(res -> res.getAttributes().setGeneratorShortCircuitAttributes(attributes));
        return new GeneratorShortCircuitImpl((GeneratorImpl) generator);
    }

    public GeneratorShortCircuitAdder withDirectTransX(double directTransX) {
        this.directTransX = directTransX;
        return this;
    }

    public GeneratorShortCircuitAdder withDirectSubtransX(double directSubtransX) {
        this.directSubtransX = directSubtransX;
        return this;
    }

    public GeneratorShortCircuitAdder withStepUpTransformerX(double stepUpTransformerX) {
        this.stepUpTransformerX = stepUpTransformerX;
        return this;
    }

    @Override
    public GeneratorShortCircuit add() {
        if (Double.isNaN(this.directTransX)) {
            throw new PowsyblException("Undefined directTransX");
        }
        return super.add();
    }
}

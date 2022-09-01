/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuit;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;

/**
 * @author Seddik Yengui <seddik.yengui at rte-france.com>
 */

public class GeneratorShortCircuitImpl extends AbstractExtension<Generator> implements GeneratorShortCircuit {

    public GeneratorShortCircuitImpl(GeneratorImpl generator, double directSubtransX, double directTransX, double stepUpTransformerX) {
        super(generator.initGeneratorShortCircuitAttributes(directSubtransX, directTransX, stepUpTransformerX));
    }

    @Override
    public double getDirectSubtransX() {
        return ((GeneratorImpl) getExtendable()).getResource().getAttributes().getGeneratorShortCircuitAttributes()
                .getDirectSubtransX();
    }

    @Override
    public GeneratorShortCircuit setDirectSubtransX(double directSubtransX) {
        ((GeneratorImpl) getExtendable()).getResource().getAttributes().getGeneratorShortCircuitAttributes()
                .setDirectSubtransX(directSubtransX);
        return this;
    }

    @Override
    public double getDirectTransX() {
        return ((GeneratorImpl) getExtendable()).getResource().getAttributes().getGeneratorShortCircuitAttributes()
                .getDirectTransX();
    }

    @Override
    public GeneratorShortCircuit setDirectTransX(double directTransX) {
        if (Double.isNaN(directTransX)) {
            throw new PowsyblException("Undefined directTransX");
        } else {
            ((GeneratorImpl) getExtendable()).getResource().getAttributes().getGeneratorShortCircuitAttributes()
                    .setDirectTransX(directTransX);
            return this;
        }
    }

    @Override
    public double getStepUpTransformerX() {
        return ((GeneratorImpl) getExtendable()).getResource().getAttributes().getGeneratorShortCircuitAttributes()
                .getStepUpTransformerX();
    }

    @Override
    public GeneratorShortCircuit setStepUpTransformerX(double stepUpTransformerX) {
        ((GeneratorImpl) getExtendable()).getResource().getAttributes().getGeneratorShortCircuitAttributes()
                .setStepUpTransformerX(stepUpTransformerX);
        return this;
    }
}

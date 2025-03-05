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

    public GeneratorShortCircuitImpl(GeneratorImpl generator) {
        super(generator);
    }

    private GeneratorImpl getGenerator() {
        return (GeneratorImpl) getExtendable();
    }

    @Override
    public double getDirectSubtransX() {
        return getGenerator().getResource().getAttributes().getGeneratorShortCircuitAttributes().getDirectSubtransX();
    }

    @Override
    public GeneratorShortCircuit setDirectSubtransX(double directSubtransX) {
        double oldValue = getDirectSubtransX();
        if (oldValue != directSubtransX) {
            getGenerator().updateResourceExtension(this, res -> res.getAttributes().getGeneratorShortCircuitAttributes().setDirectSubtransX(directSubtransX), "directSubtransX", oldValue, directSubtransX);
        }
        return this;
    }

    @Override
    public double getDirectTransX() {
        return getGenerator().getResource().getAttributes().getGeneratorShortCircuitAttributes().getDirectTransX();
    }

    @Override
    public GeneratorShortCircuit setDirectTransX(double directTransX) {
        if (Double.isNaN(directTransX)) {
            throw new PowsyblException("Undefined directTransX");
        }
        double oldValue = getDirectTransX();
        if (oldValue != directTransX) {
            getGenerator().updateResourceExtension(this, res -> res.getAttributes().getGeneratorShortCircuitAttributes().setDirectTransX(directTransX), "directTransX", oldValue, directTransX);
        }
        return this;
    }

    @Override
    public double getStepUpTransformerX() {
        return getGenerator().getResource().getAttributes().getGeneratorShortCircuitAttributes()
                .getStepUpTransformerX();
    }

    @Override
    public GeneratorShortCircuit setStepUpTransformerX(double stepUpTransformerX) {
        double oldValue = getStepUpTransformerX();
        if (oldValue != stepUpTransformerX) {
            getGenerator().updateResourceExtension(this, res -> res.getAttributes().getGeneratorShortCircuitAttributes().setStepUpTransformerX(stepUpTransformerX), "stepUpTransformerX", oldValue, stepUpTransformerX);
        }
        return this;
    }
}

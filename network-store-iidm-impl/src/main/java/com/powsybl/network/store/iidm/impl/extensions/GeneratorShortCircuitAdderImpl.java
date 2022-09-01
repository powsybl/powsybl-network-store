/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuit;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuitAdder;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;

/**
 * @author Seddik Yengui <seddik.yengui at rte-france.com>
 */

public class GeneratorShortCircuitAdderImpl extends AbstractExtensionAdder<Generator, GeneratorShortCircuit> implements GeneratorShortCircuitAdder {
    double directTransX = 0.0D;
    double directSubtransX = Double.NaN;
    double stepUpTransformerX = Double.NaN;

    protected GeneratorShortCircuitAdderImpl(Generator extendable) {
        super(extendable);
    }

    protected GeneratorShortCircuit createExtension(Generator extendable) {
        return new GeneratorShortCircuitImpl((GeneratorImpl) extendable, this.directSubtransX, this.directTransX, this.stepUpTransformerX);
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
    public Generator add() {
        if (Double.isNaN(this.directTransX)) {
            throw new PowsyblException("Undefined directTransX");
        } else {
            return super.add();
        }
    }
}

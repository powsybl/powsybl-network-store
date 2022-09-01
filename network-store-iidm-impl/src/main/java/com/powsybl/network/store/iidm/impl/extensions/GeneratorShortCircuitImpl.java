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
    private double directSubtransX;
    private double directTransX;
    private double stepUpTransformerX;

    public GeneratorShortCircuitImpl(GeneratorImpl generator, double directSubtransX, double directTransX, double stepUpTransformerX) {
        super(generator);
        this.directSubtransX = directSubtransX;
        this.directTransX = directTransX;
        this.stepUpTransformerX = stepUpTransformerX;
    }

    public double getDirectSubtransX() {
        return this.directSubtransX;
    }

    public GeneratorShortCircuit setDirectSubtransX(double directSubtransX) {
        this.directSubtransX = directSubtransX;
        return this;
    }

    public double getDirectTransX() {
        return this.directTransX;
    }

    public GeneratorShortCircuit setDirectTransX(double directTransX) {
        if (Double.isNaN(directTransX)) {
            throw new PowsyblException("Undefined directTransX");
        } else {
            this.directTransX = directTransX;
            return this;
        }
    }

    public double getStepUpTransformerX() {
        return this.stepUpTransformerX;
    }

    public GeneratorShortCircuit setStepUpTransformerX(double stepUpTransformerX) {
        this.stepUpTransformerX = stepUpTransformerX;
        return this;
    }
}

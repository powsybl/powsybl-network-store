/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.Extendable;
import com.powsybl.iidm.network.extensions.ShortCircuitExtension;
import com.powsybl.network.store.model.ShortCircuitAttributes;

import java.util.function.Consumer;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public abstract class AbstractShortCircuitExtensionImpl <T extends Extendable<T>, S extends AbstractShortCircuitExtensionImpl<T, ?>>
    extends AbstractExtension<T> implements ShortCircuitExtension<T> {

    private final ShortCircuitAttributes attributes;

    protected AbstractShortCircuitExtensionImpl(T extendable, ShortCircuitAttributes attributes) {
        super(extendable);
        this.attributes = attributes;
    }

    protected abstract S self();

    @Override
    public double getDirectSubtransX() {
        return attributes.getDirectSubtransX();
    }

    protected abstract void updateAttributes(double newValue, double oldValue, String name, Consumer<ShortCircuitAttributes> modifier);

    @Override
    public S setDirectSubtransX(double directSubtransX) {
        double oldValue = getDirectSubtransX();
        if (oldValue != directSubtransX) {
            updateAttributes(directSubtransX, oldValue, "directSubtransX", shortCircuitAttributes -> shortCircuitAttributes.setDirectSubtransX(directSubtransX));
        }
        return self();
    }

    @Override
    public double getDirectTransX() {
        return attributes.getDirectTransX();
    }

    @Override
    public S setDirectTransX(double directTransX) {
        if (Double.isNaN(directTransX)) {
            throw new PowsyblException("Undefined directTransX");
        }
        double oldValue = getDirectTransX();
        if (oldValue != directTransX) {
            updateAttributes(directTransX, oldValue, "directTransX",
                shortCircuitAttributes -> shortCircuitAttributes.setDirectTransX(directTransX));
        }
        return self();
    }

    @Override
    public double getStepUpTransformerX() {
        return attributes.getStepUpTransformerX();
    }

    @Override
    public S setStepUpTransformerX(double stepUpTransformerX) {
        double oldValue = getStepUpTransformerX();
        if (oldValue != stepUpTransformerX) {
            updateAttributes(stepUpTransformerX, oldValue, "stepUpTransformerX",
                shortCircuitAttributes -> shortCircuitAttributes.setStepUpTransformerX(stepUpTransformerX));
        }
        return self();
    }

}

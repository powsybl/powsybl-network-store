/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorFortescue;
import com.powsybl.iidm.network.extensions.GeneratorFortescueAdder;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;
import com.powsybl.network.store.model.GeneratorFortescueAttributes;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class GeneratorFortescueAdderImpl extends AbstractIidmExtensionAdder<Generator, GeneratorFortescue> implements GeneratorFortescueAdder {

    private double rz;
    private double xz;
    private double rn;
    private double xn;
    private boolean grounded;
    private double groundingR;
    private double groundingX;

    public GeneratorFortescueAdderImpl(Generator extendable) {
        super(extendable);
    }

    @Override
    public Class<? super GeneratorFortescue> getExtensionClass() {
        return GeneratorFortescue.class;
    }

    @Override
    protected GeneratorFortescue createExtension(Generator generator) {
        GeneratorFortescueAttributes attributes = GeneratorFortescueAttributes.builder()
            .rz(rz)
            .xz(xz)
            .rn(rn)
            .xn(xn)
            .grounded(grounded)
            .groundingR(groundingR)
            .groundingX(groundingX)
            .build();
        ((GeneratorImpl) generator).updateResourceWithoutNotification(res -> res.getAttributes().getExtensionAttributes().put(GeneratorFortescue.NAME, attributes));
        return new GeneratorFortescueImpl(generator);
    }

    @Override
    public GeneratorFortescueAdder withGrounded(boolean grounded) {
        this.grounded = grounded;
        return this;
    }

    @Override
    public GeneratorFortescueAdderImpl withRz(double rz) {
        this.rz = rz;
        return this;
    }

    @Override
    public GeneratorFortescueAdderImpl withXz(double xz) {
        this.xz = xz;
        return this;
    }

    @Override
    public GeneratorFortescueAdderImpl withRn(double rn) {
        this.rn = rn;
        return this;
    }

    @Override
    public GeneratorFortescueAdderImpl withXn(double xn) {
        this.xn = xn;
        return this;
    }

    @Override
    public GeneratorFortescueAdderImpl withGroundingR(double groundingR) {
        this.groundingR = groundingR;
        return this;
    }

    @Override
    public GeneratorFortescueAdderImpl withGroundingX(double groundingX) {
        this.groundingX = groundingX;
        return this;
    }
}

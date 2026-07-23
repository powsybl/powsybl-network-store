/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorFortescue;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;
import com.powsybl.network.store.model.GeneratorFortescueAttributes;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class GeneratorFortescueImpl extends AbstractExtension<Generator> implements GeneratorFortescue {
    public GeneratorFortescueImpl(Generator generator) {
        super(generator);
    }

    private GeneratorImpl getGenerator() {
        return (GeneratorImpl) getExtendable();
    }

    private GeneratorFortescueAttributes getGeneratorFortescueAttributes() {
        return (GeneratorFortescueAttributes) getGenerator().getResource().getAttributes().getExtensionAttributes().get(GeneratorFortescue.NAME);
    }

    @Override
    public double getRz() {
        return getGeneratorFortescueAttributes().getRz();
    }

    @Override
    public void setRz(double rz) {
        double oldValue = getRz();
        if (oldValue != rz) {
            getGenerator().updateResourceExtension(this, res ->
                getGeneratorFortescueAttributes().setRz(rz), "rz", oldValue, rz);
        }
    }

    @Override
    public double getXz() {
        return getGeneratorFortescueAttributes().getXz();
    }

    @Override
    public void setXz(double xz) {
        double oldValue = getXz();
        if (oldValue != xz) {
            getGenerator().updateResourceExtension(this, res ->
                getGeneratorFortescueAttributes().setXz(xz), "xz", oldValue, xz);
        }
    }

    @Override
    public double getRn() {
        return getGeneratorFortescueAttributes().getRn();
    }

    @Override
    public void setRn(double rn) {
        double oldValue = getRn();
        if (oldValue != rn) {
            getGenerator().updateResourceExtension(this, res ->
                getGeneratorFortescueAttributes().setRn(rn), "rn", oldValue, rn);
        }
    }

    @Override
    public double getXn() {
        return getGeneratorFortescueAttributes().getXn();
    }

    @Override
    public void setXn(double xn) {
        double oldValue = getXn();
        if (oldValue != xn) {
            getGenerator().updateResourceExtension(this, res ->
                getGeneratorFortescueAttributes().setXn(xn), "xn", oldValue, xn);
        }
    }

    @Override
    public boolean isGrounded() {
        return getGeneratorFortescueAttributes().isGrounded();
    }

    @Override
    public void setGrounded(boolean grounded) {
        boolean oldValue = isGrounded();
        if (oldValue != grounded) {
            getGenerator().updateResourceExtension(this, res ->
                getGeneratorFortescueAttributes().setGrounded(grounded), "grounded", oldValue, grounded);
        }
    }

    @Override
    public double getGroundingR() {
        return getGeneratorFortescueAttributes().getGroundingR();
    }

    @Override
    public void setGroundingR(double groundingR) {
        double oldValue = getGroundingR();
        if (oldValue != groundingR) {
            getGenerator().updateResourceExtension(this, res ->
                getGeneratorFortescueAttributes().setGroundingR(groundingR), "groundingR", oldValue, groundingR);
        }
    }

    @Override
    public double getGroundingX() {
        return getGeneratorFortescueAttributes().getGroundingX();
    }

    @Override
    public void setGroundingX(double groundingX) {
        double oldValue = getGroundingX();
        if (oldValue != groundingX) {
            getGenerator().updateResourceExtension(this, res ->
                getGeneratorFortescueAttributes().setGroundingX(groundingX), "groundingX", oldValue, groundingX);
        }
    }
}

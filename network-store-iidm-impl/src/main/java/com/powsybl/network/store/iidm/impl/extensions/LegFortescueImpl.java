/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.extensions.LegFortescue;
import com.powsybl.iidm.network.extensions.WindingConnectionType;
import com.powsybl.network.store.model.LegFortescueAttributes;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class LegFortescueImpl implements LegFortescue {

    private final LegFortescueAttributes legFortescueAttributes;

    public LegFortescueImpl(LegFortescueAttributes legFortescueAttributes) {
        this.legFortescueAttributes = legFortescueAttributes;
    }

    @Override
    public boolean isFreeFluxes() {
        return legFortescueAttributes.isFreeFluxes();
    }

    @Override
    public void setFreeFluxes(boolean freeFluxes) {
        legFortescueAttributes.setFreeFluxes(freeFluxes);
    }

    @Override
    public double getRz() {
        return legFortescueAttributes.getRz();
    }

    @Override
    public void setRz(double rz) {
        legFortescueAttributes.setRz(rz);
    }

    @Override
    public double getXz() {
        return legFortescueAttributes.getXz();
    }

    @Override
    public void setXz(double xz) {
        legFortescueAttributes.setXz(xz);
    }

    @Override
    public WindingConnectionType getConnectionType() {
        return legFortescueAttributes.getConnectionType();
    }

    @Override
    public void setConnectionType(WindingConnectionType windingConnectionType) {
        legFortescueAttributes.setConnectionType(windingConnectionType);
    }

    @Override
    public double getGroundingR() {
        return legFortescueAttributes.getGroundingR();
    }

    @Override
    public void setGroundingR(double groundingR) {
        legFortescueAttributes.setGroundingR(groundingR);
    }

    @Override
    public double getGroundingX() {
        return legFortescueAttributes.getGroundingX();
    }

    @Override
    public void setGroundingX(double groundingX) {
        legFortescueAttributes.setGroundingX(groundingX);
    }
}

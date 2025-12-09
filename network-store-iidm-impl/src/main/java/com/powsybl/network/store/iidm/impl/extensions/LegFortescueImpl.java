/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.extensions.LegFortescue;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerFortescue;
import com.powsybl.iidm.network.extensions.WindingConnectionType;
import com.powsybl.network.store.iidm.impl.ThreeWindingsTransformerImpl;
import com.powsybl.network.store.model.LegFortescueAttributes;
import com.powsybl.network.store.model.ThreeWindingsTransformerFortescueAttributes;

import java.util.function.Function;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class LegFortescueImpl implements LegFortescue {

    private final ThreeWindingsTransformerImpl transformer;
    private final Function<ThreeWindingsTransformerFortescueAttributes, LegFortescueAttributes> legFortescueGetter;
    private final ThreeWindingsTransformerFortescue extension;
    private final String side;

    public LegFortescueImpl(ThreeWindingsTransformerImpl transformer, ThreeWindingsTransformerFortescue extension,
                            Function<ThreeWindingsTransformerFortescueAttributes, LegFortescueAttributes> legFortescueGetter, String side) {
        this.legFortescueGetter = legFortescueGetter;
        this.transformer = transformer;
        this.extension = extension;
        this.side = side;
    }

    private ThreeWindingsTransformerFortescueAttributes getTransformerFortescueAttributes() {
        return (ThreeWindingsTransformerFortescueAttributes) transformer.getResource().getAttributes().getExtensionAttributes().get(ThreeWindingsTransformerFortescue.NAME);
    }

    private LegFortescueAttributes getLegFortecueAttributes() {
        return legFortescueGetter.apply(getTransformerFortescueAttributes());
    }

    @Override
    public boolean isFreeFluxes() {
        return getLegFortecueAttributes().isFreeFluxes();
    }

    @Override
    public void setFreeFluxes(boolean freeFluxes) {
        boolean oldValue = isFreeFluxes();
        if (oldValue != freeFluxes) {
            transformer.updateResourceExtension(extension, res ->
                    getLegFortecueAttributes().setFreeFluxes(freeFluxes), "freeFluxes" + side, oldValue, freeFluxes);
        }
    }

    @Override
    public double getRz() {
        return getLegFortecueAttributes().getRz();
    }

    @Override
    public void setRz(double rz) {
        double oldValue = getRz();
        if (oldValue != rz) {
            transformer.updateResourceExtension(extension, res ->
                    getLegFortecueAttributes().setRz(rz), "rz" + side, oldValue, rz);
        }
    }

    @Override
    public double getXz() {
        return getLegFortecueAttributes().getXz();
    }

    @Override
    public void setXz(double xz) {
        double oldValue = getXz();
        if (oldValue != xz) {
            transformer.updateResourceExtension(extension, res ->
                    getLegFortecueAttributes().setXz(xz), "xz" + side, oldValue, xz);
        }
    }

    @Override
    public WindingConnectionType getConnectionType() {
        return getLegFortecueAttributes().getConnectionType();
    }

    @Override
    public void setConnectionType(WindingConnectionType windingConnectionType) {
        WindingConnectionType oldValue = getConnectionType();
        if (oldValue != windingConnectionType) {
            transformer.updateResourceExtension(extension, res ->
                    getLegFortecueAttributes().setConnectionType(windingConnectionType), "connectionType" + side, oldValue, windingConnectionType);
        }
    }

    @Override
    public double getGroundingR() {
        return getLegFortecueAttributes().getGroundingR();
    }

    @Override
    public void setGroundingR(double groundingR) {
        double oldValue = getGroundingR();
        if (oldValue != groundingR) {
            transformer.updateResourceExtension(extension, res ->
                    getLegFortecueAttributes().setGroundingR(groundingR), "groundingR" + side, oldValue, groundingR);
        }
    }

    @Override
    public double getGroundingX() {
        return getLegFortecueAttributes().getGroundingX();
    }

    @Override
    public void setGroundingX(double groundingX) {
        double oldValue = getGroundingX();
        if (oldValue != groundingX) {
            transformer.updateResourceExtension(extension, res ->
                    getLegFortecueAttributes().setGroundingX(groundingX), "groundingX" + side, oldValue, groundingX);
        }
    }
}

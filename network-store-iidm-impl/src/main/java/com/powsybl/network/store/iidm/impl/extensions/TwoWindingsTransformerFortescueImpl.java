/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescue;
import com.powsybl.iidm.network.extensions.WindingConnectionType;
import com.powsybl.network.store.iidm.impl.TwoWindingsTransformerImpl;
import com.powsybl.network.store.model.TwoWindingsTransformerFortescueAttributes;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class TwoWindingsTransformerFortescueImpl extends AbstractExtension<TwoWindingsTransformer> implements TwoWindingsTransformerFortescue {
    public TwoWindingsTransformerFortescueImpl(TwoWindingsTransformer twoWindingsTransformer) {
        super(twoWindingsTransformer);
    }

    private TwoWindingsTransformerImpl getTwoWindingsTransformer() {
        return (TwoWindingsTransformerImpl) getExtendable();
    }

    private TwoWindingsTransformerFortescueAttributes getTwoWindingsTransformerFortescueAttributes() {
        return (TwoWindingsTransformerFortescueAttributes) getTwoWindingsTransformer().getResource().getAttributes().getExtensionAttributes().get(TwoWindingsTransformerFortescue.NAME);
    }

    @Override
    public double getRz() {
        return getTwoWindingsTransformerFortescueAttributes().getRz();
    }

    @Override
    public void setRz(double rz) {
        double oldValue = getRz();
        if (oldValue != rz) {
            getTwoWindingsTransformer().updateResourceExtension(this, res ->
                getTwoWindingsTransformerFortescueAttributes().setRz(rz), "rz", oldValue, rz);
        }
    }

    @Override
    public double getXz() {
        return getTwoWindingsTransformerFortescueAttributes().getXz();
    }

    @Override
    public void setXz(double xz) {
        double oldValue = getXz();
        if (oldValue != xz) {
            getTwoWindingsTransformer().updateResourceExtension(this, res ->
                getTwoWindingsTransformerFortescueAttributes().setXz(xz), "xz", oldValue, xz);
        }
    }

    @Override
    public boolean isFreeFluxes() {
        return getTwoWindingsTransformerFortescueAttributes().isFreeFluxes();
    }

    @Override
    public void setFreeFluxes(boolean freeFluxes) {
        boolean oldValue = isFreeFluxes();
        if (oldValue != freeFluxes) {
            getTwoWindingsTransformer().updateResourceExtension(this, res ->
                getTwoWindingsTransformerFortescueAttributes().setFreeFluxes(freeFluxes), "freeFluxes", oldValue, freeFluxes);
        }
    }

    @Override
    public WindingConnectionType getConnectionType1() {
        return getTwoWindingsTransformerFortescueAttributes().getConnectionType1();
    }

    @Override
    public void setConnectionType1(WindingConnectionType connectionType1) {
        WindingConnectionType oldValue = getConnectionType1();
        if (oldValue != connectionType1) {
            getTwoWindingsTransformer().updateResourceExtension(this, res ->
                getTwoWindingsTransformerFortescueAttributes().setConnectionType1(connectionType1), "connectionType1", oldValue, connectionType1);
        }
    }

    @Override
    public WindingConnectionType getConnectionType2() {
        return getTwoWindingsTransformerFortescueAttributes().getConnectionType2();
    }

    @Override
    public void setConnectionType2(WindingConnectionType connectionType2) {
        WindingConnectionType oldValue = getConnectionType2();
        if (oldValue != connectionType2) {
            getTwoWindingsTransformer().updateResourceExtension(this, res ->
                getTwoWindingsTransformerFortescueAttributes().setConnectionType2(connectionType2), "connectionType2", oldValue, connectionType2);
        }
    }

    @Override
    public double getGroundingR1() {
        return getTwoWindingsTransformerFortescueAttributes().getGroundingR1();
    }

    @Override
    public void setGroundingR1(double groundingR1) {
        double oldValue = getGroundingR1();
        if (oldValue != groundingR1) {
            getTwoWindingsTransformer().updateResourceExtension(this, res ->
                getTwoWindingsTransformerFortescueAttributes().setGroundingR1(groundingR1), "groundingR1", oldValue, groundingR1);
        }
    }

    @Override
    public double getGroundingR2() {
        return getTwoWindingsTransformerFortescueAttributes().getGroundingR2();
    }

    @Override
    public void setGroundingR2(double groundingR2) {
        double oldValue = getGroundingR2();
        if (oldValue != groundingR2) {
            getTwoWindingsTransformer().updateResourceExtension(this, res ->
                getTwoWindingsTransformerFortescueAttributes().setGroundingR2(groundingR2), "groundingR2", oldValue, groundingR2);
        }
    }

    @Override
    public double getGroundingX1() {
        return getTwoWindingsTransformerFortescueAttributes().getGroundingX1();
    }

    @Override
    public void setGroundingX1(double groundingX1) {
        double oldValue = getGroundingX1();
        if (oldValue != groundingX1) {
            getTwoWindingsTransformer().updateResourceExtension(this, res ->
                getTwoWindingsTransformerFortescueAttributes().setGroundingX1(groundingX1), "groundingX1", oldValue, groundingX1);
        }
    }

    @Override
    public double getGroundingX2() {
        return getTwoWindingsTransformerFortescueAttributes().getGroundingX2();
    }

    @Override
    public void setGroundingX2(double groundingX2) {
        double oldValue = getGroundingX2();
        if (oldValue != groundingX2) {
            getTwoWindingsTransformer().updateResourceExtension(this, res ->
                getTwoWindingsTransformerFortescueAttributes().setGroundingX2(groundingX2), "groundingX2", oldValue, groundingX2);
        }
    }
}

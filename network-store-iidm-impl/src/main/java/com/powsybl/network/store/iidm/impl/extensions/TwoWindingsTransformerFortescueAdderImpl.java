/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescue;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescueAdder;
import com.powsybl.iidm.network.extensions.WindingConnectionType;
import com.powsybl.network.store.iidm.impl.TwoWindingsTransformerImpl;
import com.powsybl.network.store.model.TwoWindingsTransformerFortescueAttributes;

import java.util.Objects;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class TwoWindingsTransformerFortescueAdderImpl extends AbstractIidmExtensionAdder<TwoWindingsTransformer, TwoWindingsTransformerFortescue> implements TwoWindingsTransformerFortescueAdder {

    private double rz;
    private double xz;
    private boolean freeFluxes;
    private WindingConnectionType connectionType1;
    private WindingConnectionType connectionType2;
    private double groundingR1;
    private double groundingX1;
    private double groundingR2;
    private double groundingX2;

    public TwoWindingsTransformerFortescueAdderImpl(TwoWindingsTransformer extendable) {
        super(extendable);
    }

    @Override
    public Class<? super TwoWindingsTransformerFortescue> getExtensionClass() {
        return TwoWindingsTransformerFortescue.class;
    }

    @Override
    protected TwoWindingsTransformerFortescue createExtension(TwoWindingsTransformer transformer) {
        TwoWindingsTransformerFortescueAttributes attributes = TwoWindingsTransformerFortescueAttributes.builder()
            .rz(rz)
            .xz(xz)
            .freeFluxes(freeFluxes)
            .connectionType1(connectionType1)
            .connectionType2(connectionType2)
            .groundingR1(groundingR1)
            .groundingX1(groundingX1)
            .groundingR2(groundingR2)
            .groundingX2(groundingX2)
            .build();
        ((TwoWindingsTransformerImpl) transformer).updateResourceWithoutNotification(res -> res.getAttributes().getExtensionAttributes().put(TwoWindingsTransformerFortescue.NAME, attributes));
        return new TwoWindingsTransformerFortescueImpl(transformer);
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withRz(double rz) {
        this.rz = rz;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withXz(double xz) {
        this.xz = xz;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withFreeFluxes(boolean freeFluxes) {
        this.freeFluxes = freeFluxes;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withConnectionType1(WindingConnectionType connectionType1) {
        this.connectionType1 = Objects.requireNonNull(connectionType1);
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withConnectionType2(WindingConnectionType connectionType2) {
        this.connectionType2 = Objects.requireNonNull(connectionType2);
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withGroundingR1(double groundingR1) {
        this.groundingR1 = groundingR1;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withGroundingX1(double groundingX1) {
        this.groundingX1 = groundingX1;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withGroundingR2(double groundingR2) {
        this.groundingR2 = groundingR2;
        return this;
    }

    @Override
    public TwoWindingsTransformerFortescueAdderImpl withGroundingX2(double groundingX2) {
        this.groundingX2 = groundingX2;
        return this;
    }
}

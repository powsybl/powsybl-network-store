/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerFortescue;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerFortescueAdder;
import com.powsybl.iidm.network.extensions.WindingConnectionType;
import com.powsybl.network.store.iidm.impl.ThreeWindingsTransformerImpl;
import com.powsybl.network.store.model.LegFortescueAttributes;
import com.powsybl.network.store.model.ThreeWindingsTransformerFortescueAttributes;

import java.util.Objects;

import static com.powsybl.iidm.network.extensions.FortescueConstants.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class ThreeWindingsTransformerFortescueAdderImpl extends AbstractIidmExtensionAdder<ThreeWindingsTransformer, ThreeWindingsTransformerFortescue> implements ThreeWindingsTransformerFortescueAdder {

    private final LegFortescueAdderImpl legAdder1 = new LegFortescueAdderImpl(DEFAULT_LEG1_CONNECTION_TYPE);
    private final LegFortescueAdderImpl legAdder2 = new LegFortescueAdderImpl(DEFAULT_LEG2_CONNECTION_TYPE);
    private final LegFortescueAdderImpl legAdder3 = new LegFortescueAdderImpl(DEFAULT_LEG3_CONNECTION_TYPE);

    @Override
    public LegFortescueAdder leg1() {
        return legAdder1;
    }

    @Override
    public LegFortescueAdder leg2() {
        return legAdder2;
    }

    @Override
    public LegFortescueAdder leg3() {
        return legAdder3;
    }

    private class LegFortescueAdderImpl implements LegFortescueAdder {

        private double rz = Double.NaN;
        private double xz = Double.NaN;
        private boolean freeFluxes = DEFAULT_FREE_FLUXES;
        private WindingConnectionType connectionType;
        private double groundingR = DEFAULT_GROUNDING_R;
        private double groundingX = DEFAULT_GROUNDING_X;

        public LegFortescueAdderImpl(WindingConnectionType connectionType) {
            this.connectionType = Objects.requireNonNull(connectionType);
        }

        @Override
        public LegFortescueAdder withRz(double rz) {
            this.rz = rz;
            return this;
        }

        @Override
        public LegFortescueAdder withXz(double xz) {
            this.xz = xz;
            return this;
        }

        @Override
        public LegFortescueAdder withFreeFluxes(boolean freeFluxes) {
            this.freeFluxes = freeFluxes;
            return this;
        }

        @Override
        public LegFortescueAdder withConnectionType(WindingConnectionType connectionType) {
            this.connectionType = Objects.requireNonNull(connectionType);
            return this;
        }

        @Override
        public LegFortescueAdder withGroundingR(double groundingR) {
            this.groundingR = groundingR;
            return this;
        }

        @Override
        public LegFortescueAdder withGroundingX(double groundingX) {
            this.groundingX = groundingX;
            return this;
        }

        @Override
        public LegFortescueAdder leg1() {
            return legAdder1;
        }

        @Override
        public LegFortescueAdder leg2() {
            return legAdder2;
        }

        @Override
        public LegFortescueAdder leg3() {
            return legAdder3;
        }

        @Override
        public ThreeWindingsTransformerFortescue add() {
            return ThreeWindingsTransformerFortescueAdderImpl.this.add();
        }
    }

    public ThreeWindingsTransformerFortescueAdderImpl(ThreeWindingsTransformer extendable) {
        super(extendable);
    }

    @Override
    public Class<? super ThreeWindingsTransformerFortescue> getExtensionClass() {
        return ThreeWindingsTransformerFortescue.class;
    }

    @Override
    protected ThreeWindingsTransformerFortescue createExtension(ThreeWindingsTransformer transformer) {
        var leg1 = new LegFortescueAttributes(legAdder1.rz, legAdder1.xz, legAdder1.freeFluxes, legAdder1.connectionType, legAdder1.groundingR, legAdder1.groundingX);
        var leg2 = new LegFortescueAttributes(legAdder2.rz, legAdder2.xz, legAdder2.freeFluxes, legAdder2.connectionType, legAdder2.groundingR, legAdder2.groundingX);
        var leg3 = new LegFortescueAttributes(legAdder3.rz, legAdder3.xz, legAdder3.freeFluxes, legAdder3.connectionType, legAdder3.groundingR, legAdder3.groundingX);
        ThreeWindingsTransformerFortescueAttributes attributes = ThreeWindingsTransformerFortescueAttributes.builder()
            .leg1(leg1)
            .leg2(leg2)
            .leg3(leg3)
            .build();
        ((ThreeWindingsTransformerImpl) transformer).updateResourceWithoutNotification(res -> res.getAttributes().getExtensionAttributes().put(ThreeWindingsTransformerFortescue.NAME, attributes));
        return new ThreeWindingsTransformerFortescueImpl(transformer);
    }
}

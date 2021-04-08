/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClock;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClockAdder;
import com.powsybl.network.store.iidm.impl.ThreeWindingsTransformerImpl;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class ThreeWindingsTransformerPhaseAngleClockAdderImpl extends AbstractExtensionAdder<ThreeWindingsTransformer, ThreeWindingsTransformerPhaseAngleClock> implements ThreeWindingsTransformerPhaseAngleClockAdder {

    private int phaseAngleClockLeg2;
    private int phaseAngleClockLeg3;

    public ThreeWindingsTransformerPhaseAngleClockAdderImpl(ThreeWindingsTransformer extendable) {
        super(extendable);
    }

    @Override
    protected ThreeWindingsTransformerPhaseAngleClock createExtension(ThreeWindingsTransformer threeWindingsTransformer) {
        return new ThreeWindingsTransformerPhaseAngleClockImpl((ThreeWindingsTransformerImpl) threeWindingsTransformer, phaseAngleClockLeg2, phaseAngleClockLeg3);
    }

    @Override
    public ThreeWindingsTransformerPhaseAngleClockAdder withPhaseAngleClockLeg2(int phaseAngleClockLeg2) {
        this.phaseAngleClockLeg2 = phaseAngleClockLeg2;
        return this;
    }

    @Override
    public ThreeWindingsTransformerPhaseAngleClockAdder withPhaseAngleClockLeg3(int phaseAngleClockLeg3) {
        this.phaseAngleClockLeg3 = phaseAngleClockLeg3;
        return this;
    }
}

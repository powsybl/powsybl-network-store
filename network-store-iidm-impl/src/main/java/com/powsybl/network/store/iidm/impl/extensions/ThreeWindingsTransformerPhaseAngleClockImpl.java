/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClock;
import com.powsybl.network.store.iidm.impl.ThreeWindingsTransformerImpl;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class ThreeWindingsTransformerPhaseAngleClockImpl implements ThreeWindingsTransformerPhaseAngleClock {

    private ThreeWindingsTransformerImpl threeWindingsTransformer;

    public ThreeWindingsTransformerPhaseAngleClockImpl(ThreeWindingsTransformerImpl threeWindingsTransformer) {
        this.threeWindingsTransformer = threeWindingsTransformer;
    }

    public ThreeWindingsTransformerPhaseAngleClockImpl(ThreeWindingsTransformerImpl threeWindingsTransformer, int phaseAngleClockLeg2, int phaseAngleClockLeg3) {
        this(threeWindingsTransformer.initPhaseAngleClockAttributes(phaseAngleClockLeg2, phaseAngleClockLeg3));
    }

    @Override
    public int getPhaseAngleClockLeg2() {
        return threeWindingsTransformer.getResource().getAttributes().getPhaseAngleClock().getPhaseAngleClockLeg2();
    }

    @Override
    public int getPhaseAngleClockLeg3() {
        return threeWindingsTransformer.getResource().getAttributes().getPhaseAngleClock().getPhaseAngleClockLeg3();
    }

    @Override
    public void setPhaseAngleClockLeg2(int phaseAngleClockLeg2) {
        threeWindingsTransformer.getResource().getAttributes().getPhaseAngleClock().setPhaseAngleClockLeg2(phaseAngleClockLeg2);
    }

    @Override
    public void setPhaseAngleClockLeg3(int phaseAngleClockLeg3) {
        threeWindingsTransformer.getResource().getAttributes().getPhaseAngleClock().setPhaseAngleClockLeg2(phaseAngleClockLeg3);
    }

    @Override
    public ThreeWindingsTransformer getExtendable() {
        return threeWindingsTransformer;
    }

    @Override
    public void setExtendable(ThreeWindingsTransformer threeWindingsTransformer) {
        this.threeWindingsTransformer = (ThreeWindingsTransformerImpl) threeWindingsTransformer;
    }
}

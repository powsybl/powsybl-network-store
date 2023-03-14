/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClock;
import com.powsybl.network.store.iidm.impl.ThreeWindingsTransformerImpl;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class ThreeWindingsTransformerPhaseAngleClockImpl extends AbstractExtension<ThreeWindingsTransformer> implements ThreeWindingsTransformerPhaseAngleClock {

    public ThreeWindingsTransformerPhaseAngleClockImpl(ThreeWindingsTransformerImpl threeWindingsTransformer) {
        super(threeWindingsTransformer);
    }

    private ThreeWindingsTransformerImpl getThreeWindingsTransformer() {
        return (ThreeWindingsTransformerImpl) getExtendable();
    }

    @Override
    public int getPhaseAngleClockLeg2() {
        return getThreeWindingsTransformer().checkResource().getAttributes().getPhaseAngleClock().getPhaseAngleClockLeg2();
    }

    @Override
    public int getPhaseAngleClockLeg3() {
        return getThreeWindingsTransformer().checkResource().getAttributes().getPhaseAngleClock().getPhaseAngleClockLeg3();
    }

    @Override
    public void setPhaseAngleClockLeg2(int phaseAngleClockLeg2) {
        getThreeWindingsTransformer().updateResource(res -> res.getAttributes().getPhaseAngleClock().setPhaseAngleClockLeg2(phaseAngleClockLeg2));
    }

    @Override
    public void setPhaseAngleClockLeg3(int phaseAngleClockLeg3) {
        getThreeWindingsTransformer().updateResource(res -> res.getAttributes().getPhaseAngleClock().setPhaseAngleClockLeg2(phaseAngleClockLeg3));
    }
}

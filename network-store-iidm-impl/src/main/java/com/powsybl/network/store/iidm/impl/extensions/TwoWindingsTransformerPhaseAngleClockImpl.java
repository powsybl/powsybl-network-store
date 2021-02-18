/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerPhaseAngleClock;
import com.powsybl.network.store.iidm.impl.TwoWindingsTransformerImpl;
import com.powsybl.network.store.model.TwoWindingsTransformerPhaseAngleClockAttributes;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class TwoWindingsTransformerPhaseAngleClockImpl implements TwoWindingsTransformerPhaseAngleClock {

    private TwoWindingsTransformerImpl twoWindingsTransformer;

    public TwoWindingsTransformerPhaseAngleClockImpl(TwoWindingsTransformerImpl twoWindingsTransformer, int phaseAngleClock) {
        this.twoWindingsTransformer = twoWindingsTransformer;
        this.twoWindingsTransformer.getResource().getAttributes().setPhaseAngleClockAttributes(
                TwoWindingsTransformerPhaseAngleClockAttributes.builder()
                        .phaseAngleClock(phaseAngleClock)
                        .build());
    }

    @Override
    public int getPhaseAngleClock() {
        return twoWindingsTransformer.getResource().getAttributes().getPhaseAngleClockAttributes().getPhaseAngleClock();
    }

    @Override
    public void setPhaseAngleClock(int i) {
        twoWindingsTransformer.getResource().getAttributes().getPhaseAngleClockAttributes().setPhaseAngleClock(i);
    }

    @Override
    public TwoWindingsTransformer getExtendable() {
        return twoWindingsTransformer;
    }

    @Override
    public void setExtendable(TwoWindingsTransformer twoWindingsTransformer) {
        this.twoWindingsTransformer = (TwoWindingsTransformerImpl) twoWindingsTransformer;
    }
}

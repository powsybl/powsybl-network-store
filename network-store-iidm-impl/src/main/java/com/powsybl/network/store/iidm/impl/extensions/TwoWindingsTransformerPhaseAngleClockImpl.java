/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerPhaseAngleClock;
import com.powsybl.network.store.iidm.impl.TwoWindingsTransformerImpl;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class TwoWindingsTransformerPhaseAngleClockImpl extends AbstractExtension<TwoWindingsTransformer> implements TwoWindingsTransformerPhaseAngleClock {

    public TwoWindingsTransformerPhaseAngleClockImpl(TwoWindingsTransformerImpl twoWindingsTransformer) {
        super(twoWindingsTransformer);
    }

    private TwoWindingsTransformerImpl getTwoWindingsTransformer() {
        return (TwoWindingsTransformerImpl) getExtendable();
    }

    @Override
    public int getPhaseAngleClock() {
        return getTwoWindingsTransformer().getResource().getAttributes().getPhaseAngleClockAttributes().getPhaseAngleClock();
    }

    @Override
    public void setPhaseAngleClock(int phaseAngleClock) {
        checkPhaseAngleClock(phaseAngleClock);
        int oldValue = getPhaseAngleClock();
        if (oldValue != phaseAngleClock) {
            getTwoWindingsTransformer().updateResourceExtension(this, res -> res.getAttributes().getPhaseAngleClockAttributes().setPhaseAngleClock(phaseAngleClock), "phaseAngleClock", oldValue, phaseAngleClock);
        }
    }

    private void checkPhaseAngleClock(int phaseAngleClock) {
        if (phaseAngleClock < 0 || phaseAngleClock > 11) {
            throw new PowsyblException("Unexpected value for phaseAngleClock: " + phaseAngleClock);
        }
    }
}

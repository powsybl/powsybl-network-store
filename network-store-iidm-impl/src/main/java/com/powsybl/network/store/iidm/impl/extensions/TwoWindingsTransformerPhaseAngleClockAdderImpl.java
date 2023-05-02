/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerPhaseAngleClock;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerPhaseAngleClockAdder;
import com.powsybl.network.store.iidm.impl.TwoWindingsTransformerImpl;
import com.powsybl.network.store.model.TwoWindingsTransformerPhaseAngleClockAttributes;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class TwoWindingsTransformerPhaseAngleClockAdderImpl extends AbstractExtensionAdder<TwoWindingsTransformer, TwoWindingsTransformerPhaseAngleClock> implements TwoWindingsTransformerPhaseAngleClockAdder {

    private int phaseAngleClock;

    public TwoWindingsTransformerPhaseAngleClockAdderImpl(TwoWindingsTransformer extendable) {
        super(extendable);
    }

    @Override
    protected TwoWindingsTransformerPhaseAngleClock createExtension(TwoWindingsTransformer twoWindingsTransformer) {
        var attributes = TwoWindingsTransformerPhaseAngleClockAttributes.builder()
                .phaseAngleClock(phaseAngleClock)
                .build();
        ((TwoWindingsTransformerImpl) twoWindingsTransformer).updateResource(res -> res.getAttributes().setPhaseAngleClockAttributes(attributes));
        return new TwoWindingsTransformerPhaseAngleClockImpl((TwoWindingsTransformerImpl) twoWindingsTransformer);
    }

    @Override
    public TwoWindingsTransformerPhaseAngleClockAdder withPhaseAngleClock(int i) {
        this.phaseAngleClock = i;
        return this;
    }
}

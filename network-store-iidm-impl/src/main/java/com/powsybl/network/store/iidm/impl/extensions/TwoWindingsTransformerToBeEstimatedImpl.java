/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimated;
import com.powsybl.network.store.iidm.impl.TwoWindingsTransformerImpl;
import com.powsybl.network.store.model.TwoWindingsTransformerToBeEstimatedAttributes;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TwoWindingsTransformerToBeEstimatedImpl extends AbstractExtension<TwoWindingsTransformer> implements TwoWindingsTransformerToBeEstimated {

    public TwoWindingsTransformerToBeEstimatedImpl(TwoWindingsTransformer twoWindingsTransformer) {
        super(twoWindingsTransformer);
    }

    private TwoWindingsTransformerImpl getTwoWindingsTransformer() {
        return (TwoWindingsTransformerImpl) getExtendable();
    }

    private TwoWindingsTransformerToBeEstimatedAttributes getTwoWindingsTransformerAttributes() {
        return (TwoWindingsTransformerToBeEstimatedAttributes) getTwoWindingsTransformer().getResource().getAttributes().getExtensionAttributes().get(TwoWindingsTransformerToBeEstimated.NAME);
    }

    @Override
    public boolean shouldEstimateRatioTapChanger() {
        return getTwoWindingsTransformerAttributes().isRtcStatus();
    }

    @Override
    public TwoWindingsTransformerToBeEstimated shouldEstimateRatioTapChanger(boolean toBeEstimated) {
        boolean oldValue = shouldEstimateRatioTapChanger();
        if (oldValue != toBeEstimated) {
            getTwoWindingsTransformer().updateResourceExtension(this, res -> getTwoWindingsTransformerAttributes().setRtcStatus(toBeEstimated), "rtcStatus", oldValue, toBeEstimated);
        }
        return this;
    }

    @Override
    public boolean shouldEstimatePhaseTapChanger() {
        return getTwoWindingsTransformerAttributes().isPtcStatus();
    }

    @Override
    public TwoWindingsTransformerToBeEstimated shouldEstimatePhaseTapChanger(boolean toBeEstimated) {
        boolean oldValue = shouldEstimatePhaseTapChanger();
        if (oldValue != toBeEstimated) {
            getTwoWindingsTransformer().updateResourceExtension(this, res -> getTwoWindingsTransformerAttributes().setPtcStatus(toBeEstimated), "ptcStatus", oldValue, toBeEstimated);
        }
        return this;
    }
}

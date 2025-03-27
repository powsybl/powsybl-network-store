/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimated;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimatedAdder;
import com.powsybl.network.store.iidm.impl.TwoWindingsTransformerImpl;
import com.powsybl.network.store.model.TwoWindingsTransformerToBeEstimatedAttributes;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class TwoWindingsTransformerToBeEstimatedAdderImpl extends AbstractIidmExtensionAdder<TwoWindingsTransformer, TwoWindingsTransformerToBeEstimated>
        implements TwoWindingsTransformerToBeEstimatedAdder {

    private boolean rtcStatus = false;

    private boolean ptcStatus = false;

    public TwoWindingsTransformerToBeEstimatedAdderImpl(TwoWindingsTransformer extendable) {
        super(extendable);
    }

    @Override
    protected TwoWindingsTransformerToBeEstimated createExtension(TwoWindingsTransformer twoWindingsTransformer) {
        TwoWindingsTransformerToBeEstimatedAttributes oldValue = (TwoWindingsTransformerToBeEstimatedAttributes) ((TwoWindingsTransformerImpl) twoWindingsTransformer).getResource().getAttributes().getExtensionAttributes().get(TwoWindingsTransformerToBeEstimated.NAME);
        TwoWindingsTransformerToBeEstimatedAttributes attributes = TwoWindingsTransformerToBeEstimatedAttributes.builder()
            .rtcStatus(rtcStatus)
            .ptcStatus(ptcStatus)
            .build();
        ((TwoWindingsTransformerImpl) twoWindingsTransformer).updateResource(res -> res.getAttributes().getExtensionAttributes().put(TwoWindingsTransformerToBeEstimated.NAME, attributes),
            "toBeEstimated", oldValue, attributes);
        return new TwoWindingsTransformerToBeEstimatedImpl(twoWindingsTransformer);
    }

    @Override
    public TwoWindingsTransformerToBeEstimatedAdder withRatioTapChangerStatus(boolean toBeEstimated) {
        this.rtcStatus = toBeEstimated;
        return this;
    }

    @Override
    public TwoWindingsTransformerToBeEstimatedAdder withPhaseTapChangerStatus(boolean toBeEstimated) {
        this.ptcStatus = toBeEstimated;
        return this;
    }
}

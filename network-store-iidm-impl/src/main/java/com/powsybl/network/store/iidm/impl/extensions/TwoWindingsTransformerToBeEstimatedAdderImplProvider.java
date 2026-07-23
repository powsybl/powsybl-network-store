/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerToBeEstimated;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class TwoWindingsTransformerToBeEstimatedAdderImplProvider implements
        ExtensionAdderProvider<TwoWindingsTransformer, TwoWindingsTransformerToBeEstimated, TwoWindingsTransformerToBeEstimatedAdderImpl> {

    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public String getExtensionName() {
        return TwoWindingsTransformerToBeEstimated.NAME;
    }

    @Override
    public Class<TwoWindingsTransformerToBeEstimatedAdderImpl> getAdderClass() {
        return TwoWindingsTransformerToBeEstimatedAdderImpl.class;
    }

    @Override
    public TwoWindingsTransformerToBeEstimatedAdderImpl newAdder(TwoWindingsTransformer extendable) {
        return new TwoWindingsTransformerToBeEstimatedAdderImpl(extendable);
    }
}

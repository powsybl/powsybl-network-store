/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.InjectionObservability;
import com.powsybl.network.store.model.ExtensionLoader;
import com.powsybl.network.store.model.InjectionObservabilityAttributes;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@AutoService(ExtensionLoader.class)
public class InjectionObservabilityLoader<I extends Injection<I>> implements ExtensionLoader<I, InjectionObservability<I>, InjectionObservabilityAttributes> {
    @Override
    public Extension<I> load(I injection) {
        return new InjectionObservabilityImpl<>(injection);
    }

    @Override
    public String getName() {
        return InjectionObservability.NAME;
    }

    @Override
    public Class<InjectionObservability> getType() {
        return InjectionObservability.class;
    }

    @Override
    public Class<InjectionObservabilityAttributes> getAttributesType() {
        return InjectionObservabilityAttributes.class;
    }
}

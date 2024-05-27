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
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.extensions.BranchObservability;
import com.powsybl.network.store.model.BranchObservabilityAttributes;
import com.powsybl.network.store.model.ExtensionLoader;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@AutoService(ExtensionLoader.class)
public class BranchObservabilityLoader<B extends Branch<B>> implements ExtensionLoader<B, BranchObservability<B>, BranchObservabilityAttributes> {
    @Override
    public Extension<B> load(B branch) {
        return new BranchObservabilityImpl<>(branch);
    }

    @Override
    public String getName() {
        return BranchObservability.NAME;
    }

    @Override
    public Class<BranchObservability> getType() {
        return BranchObservability.class;
    }

    @Override
    public Class<BranchObservabilityAttributes> getAttributesType() {
        return BranchObservabilityAttributes.class;
    }
}

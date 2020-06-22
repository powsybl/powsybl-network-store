/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.network.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.entsoe.util.MergedXnode;
import com.powsybl.entsoe.util.MergedXnodeAdderImpl;
import com.powsybl.iidm.network.Line;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class MergedXnodeAdderImplNetworkStoreProvider
        implements
        ExtensionAdderProvider<Line, MergedXnode, MergedXnodeAdderImpl> {

    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public Class<MergedXnodeAdderImpl> getAdderClass() {
        return MergedXnodeAdderImpl.class;
    }

    @Override
    public MergedXnodeAdderImpl newAdder(Line extendable) {
        return new MergedXnodeAdderImpl(extendable);
    }
}

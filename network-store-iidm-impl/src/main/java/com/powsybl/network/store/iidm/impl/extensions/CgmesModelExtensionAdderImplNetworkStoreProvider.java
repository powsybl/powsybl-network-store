/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.conversion.CgmesModelExtensionAdderImpl;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Network;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class CgmesModelExtensionAdderImplNetworkStoreProvider
        implements
        ExtensionAdderProvider<Network, CgmesModelExtension, CgmesModelExtensionAdderImpl> {

    /**
     * TODO HACK!!!! to fully implement
     */
    private static class CgmesModelExtensionAdderImplExt extends CgmesModelExtensionAdderImpl {

        public CgmesModelExtensionAdderImplExt(Network extendable) {
            super(extendable);
        }
    }

    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public Class<CgmesModelExtensionAdderImpl> getAdderClass() {
        return CgmesModelExtensionAdderImpl.class;
    }

    @Override
    public CgmesModelExtensionAdderImpl newAdder(Network extendable) {
        return new CgmesModelExtensionAdderImplExt(extendable);
    }
}

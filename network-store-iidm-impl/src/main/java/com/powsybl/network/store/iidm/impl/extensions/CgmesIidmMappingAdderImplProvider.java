/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.extensions.CgmesIidmMapping;
import com.powsybl.cgmes.extensions.CgmesIidmMappingAdderImpl;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Network;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class CgmesIidmMappingAdderImplProvider implements ExtensionAdderProvider<Network, CgmesIidmMapping, CgmesIidmMappingAdderImpl> {

    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public Class<? super CgmesIidmMappingAdderImpl> getAdderClass() {
        return CgmesIidmMappingAdderImpl.class;
    }

    @Override
    public CgmesIidmMappingAdderImpl newAdder(Network extendable) {
        return new CgmesIidmMappingAdderImpl(extendable);
    }

}

/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.entsoe.util.EntsoeArea;
import com.powsybl.entsoe.util.EntsoeAreaAdderImpl;
import com.powsybl.iidm.network.Substation;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class EntsoeAreaAdderImplNetworkStoreProvider implements
        ExtensionAdderProvider<Substation, EntsoeArea, EntsoeAreaAdderImpl> {

    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public Class<EntsoeAreaAdderImpl> getAdderClass() {
        return EntsoeAreaAdderImpl.class;
    }

    @Override
    public EntsoeAreaAdderImpl newAdder(Substation extendable) {
        return new EntsoeAreaAdderImpl(extendable);
    }
}

/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.conversion.extensions.CimCharacteristics;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Network;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class CimCharacteristicsAdderImplProvider
        implements
        ExtensionAdderProvider<Network, CimCharacteristics, CimCharacteristicsAdderImpl> {

    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public Class<CimCharacteristicsAdderImpl> getAdderClass() {
        return CimCharacteristicsAdderImpl.class;
    }

    @Override
    public CimCharacteristicsAdderImpl newAdder(Network extendable) {
        return new CimCharacteristicsAdderImpl(extendable);
    }
}

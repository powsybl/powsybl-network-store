/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.extensions.CgmesMetadataModels;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.model.CgmesMetadataModelsAttributes;
import com.powsybl.network.store.model.ExtensionLoader;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@AutoService(ExtensionLoader.class)
public class CgmesMetadataModelsLoader implements ExtensionLoader<Network, CgmesMetadataModels, CgmesMetadataModelsAttributes> {
    @Override
    public Extension<com.powsybl.iidm.network.Network> load(Network network) {
        return new CgmesMetadataModelsImpl(network);
    }

    @Override
    public String getName() {
        return CgmesMetadataModels.NAME;
    }

    @Override
    public Class<CgmesMetadataModels> getType() {
        return CgmesMetadataModels.class;
    }

    @Override
    public Class<CgmesMetadataModelsAttributes> getAttributesType() {
        return CgmesMetadataModelsAttributes.class;
    }
}

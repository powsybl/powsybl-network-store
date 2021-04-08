/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.conversion.extensions.CgmesSshMetadata;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Network;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
@AutoService(ExtensionAdderProvider.class)
public class CgmesSshMetadataAdderImplProvider
        implements
        ExtensionAdderProvider<Network, CgmesSshMetadata, CgmesSshMetadataAdderImpl> {

    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public Class<CgmesSshMetadataAdderImpl> getAdderClass() {
        return CgmesSshMetadataAdderImpl.class;
    }

    @Override
    public CgmesSshMetadataAdderImpl newAdder(Network extendable) {
        return new CgmesSshMetadataAdderImpl(extendable);
    }
}

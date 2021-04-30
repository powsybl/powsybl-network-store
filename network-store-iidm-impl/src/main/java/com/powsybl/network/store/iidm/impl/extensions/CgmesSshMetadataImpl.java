/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.CgmesSshMetadata;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.iidm.impl.NetworkImpl;

import java.util.List;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class CgmesSshMetadataImpl extends AbstractExtension<Network> implements CgmesSshMetadata {

    private final NetworkImpl network;

    public CgmesSshMetadataImpl(NetworkImpl network) {
        this.network = network;
    }

    public CgmesSshMetadataImpl(NetworkImpl network, String description, int svVersion, List<String> dependencies, String modelingAuthoritySet) {
        this(network.initCgmesSshMetadataAttributes(description, svVersion, dependencies, modelingAuthoritySet));
    }

    @Override
    public String getDescription() {
        return network.getResource().getAttributes().getCgmesSshMetadata().getDescription();
    }

    @Override
    public int getSshVersion() {
        return network.getResource().getAttributes().getCgmesSshMetadata().getSshVersion();
    }

    @Override
    public List<String> getDependencies() {
        return network.getResource().getAttributes().getCgmesSshMetadata().getDependencies();
    }

    @Override
    public String getModelingAuthoritySet() {
        return network.getResource().getAttributes().getCgmesSshMetadata().getModelingAuthoritySet();
    }
}

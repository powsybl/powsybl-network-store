/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.conversion.extensions.CgmesSvMetadata;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.iidm.impl.NetworkImpl;

import java.util.*;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class CgmesSvMetadataImpl extends AbstractExtension<Network> implements CgmesSvMetadata {

    private NetworkImpl network;

    public CgmesSvMetadataImpl(NetworkImpl network) {
        this.network = network;
    }

    public CgmesSvMetadataImpl(NetworkImpl network, String description, int svVersion, List<String> dependencies, String modelingAuthoritySet) {
        this(network.initCgmesSvMetadataAttributes(description, svVersion, dependencies, modelingAuthoritySet));
    }

    @Override
    public String getDescription() {
        return network.getResource().getAttributes().getCgmesSvMetadata().getDescription();
    }

    @Override
    public int getSvVersion() {
        return network.getResource().getAttributes().getCgmesSvMetadata().getSvVersion();
    }

    @Override
    public List<String> getDependencies() {
        return network.getResource().getAttributes().getCgmesSvMetadata().getDependencies();
    }

    @Override
    public String getModelingAuthoritySet() {
        return network.getResource().getAttributes().getCgmesSvMetadata().getModelingAuthoritySet();
    }
}

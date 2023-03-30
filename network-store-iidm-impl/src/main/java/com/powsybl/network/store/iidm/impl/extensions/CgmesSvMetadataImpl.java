/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.CgmesSvMetadata;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.iidm.impl.NetworkImpl;

import java.util.*;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class CgmesSvMetadataImpl extends AbstractExtension<Network> implements CgmesSvMetadata {

    public CgmesSvMetadataImpl(NetworkImpl network) {
        super(network);
    }

    private NetworkImpl getNetwork() {
        return (NetworkImpl) getExtendable();
    }

    @Override
    public String getDescription() {
        return getNetwork().getResource().getAttributes().getCgmesSvMetadata().getDescription();
    }

    @Override
    public int getSvVersion() {
        return getNetwork().getResource().getAttributes().getCgmesSvMetadata().getSvVersion();
    }

    @Override
    public List<String> getDependencies() {
        return getNetwork().getResource().getAttributes().getCgmesSvMetadata().getDependencies();
    }

    @Override
    public String getModelingAuthoritySet() {
        return getNetwork().getResource().getAttributes().getCgmesSvMetadata().getModelingAuthoritySet();
    }
}

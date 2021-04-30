/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.CgmesSvMetadata;
import com.powsybl.cgmes.extensions.CgmesSvMetadataAdder;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.iidm.impl.NetworkImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class CgmesSvMetadataAdderImpl extends AbstractExtensionAdder<Network, CgmesSvMetadata> implements CgmesSvMetadataAdder {
    private String description;
    private int svVersion = 0;
    private final List<String> dependencies = new ArrayList<>();
    private String modelingAuthoritySet;

    public CgmesSvMetadataAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    protected CgmesSvMetadata createExtension(Network network) {
        if (description == null) {
            throw new PowsyblException("cgmesSvMetadata.description is undefined");
        } else if (this.dependencies.isEmpty()) {
            throw new PowsyblException("cgmesSvMetadata.dependencies must have at least one dependency");
        } else if (this.modelingAuthoritySet == null) {
            throw new PowsyblException("cgmesSvMetadata.modelingAuthoritySet is undefined");
        } else {
            return new CgmesSvMetadataImpl((NetworkImpl) network, description, svVersion, dependencies, modelingAuthoritySet);
        }
    }

    @Override
    public CgmesSvMetadataAdder setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public CgmesSvMetadataAdder setSvVersion(int svVersion) {
        this.svVersion = svVersion;
        return this;
    }

    @Override
    public CgmesSvMetadataAdder addDependency(String dependency) {
        this.dependencies.add(dependency);
        return this;
    }

    @Override
    public CgmesSvMetadataAdder setModelingAuthoritySet(String modelingAuthoritySet) {
        this.modelingAuthoritySet = modelingAuthoritySet;
        return this;
    }
}

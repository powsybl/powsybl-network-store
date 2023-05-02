/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.CgmesSshMetadata;
import com.powsybl.cgmes.extensions.CgmesSshMetadataAdder;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.model.CgmesSshMetadataAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class CgmesSshMetadataAdderImpl extends AbstractExtensionAdder<Network, CgmesSshMetadata> implements CgmesSshMetadataAdder {

    private String description;

    private int sshVersion;

    private final List<String> dependencies = new ArrayList<>();

    private String modelingAuthoritySet;

    public CgmesSshMetadataAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    protected CgmesSshMetadata createExtension(Network network) {
        if (description == null) {
            throw new PowsyblException("cgmesSshMetadata.description is undefined");
        }
        if (dependencies.isEmpty()) {
            throw new PowsyblException("cgmesSshMetadata.dependencies must have at least one dependency");
        }
        if (modelingAuthoritySet == null) {
            throw new PowsyblException("cgmesSshMetadata.modelingAuthoritySet is undefined");
        }
        var attributes = CgmesSshMetadataAttributes.builder()
                .description(description)
                .sshVersion(sshVersion)
                .dependencies(dependencies)
                .modelingAuthoritySet(modelingAuthoritySet)
                .build();
        ((NetworkImpl) network).updateResource(res -> res.getAttributes().setCgmesSshMetadata(attributes));
        return new CgmesSshMetadataImpl((NetworkImpl) network);

    }

    @Override
    public CgmesSshMetadataAdder setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public CgmesSshMetadataAdder setSshVersion(int sshVersion) {
        this.sshVersion = sshVersion;
        return this;
    }

    @Override
    public CgmesSshMetadataAdder addDependency(String dependency) {
        this.dependencies.add(dependency);
        return this;
    }

    @Override
    public CgmesSshMetadataAdder setModelingAuthoritySet(String modelingAuthoritySet) {
        this.modelingAuthoritySet = modelingAuthoritySet;
        return this;
    }
}

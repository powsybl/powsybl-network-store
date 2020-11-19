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
import com.powsybl.network.store.model.CgmesSvMetadataAttributes;

import java.util.*;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class CgmesSvMetadataImpl extends AbstractExtension<Network> implements CgmesSvMetadata {
    private CgmesSvMetadataAttributes cgmesSvMetadataAttributes;

    public CgmesSvMetadataImpl(CgmesSvMetadataAttributes cgmesSvMetadataAttributes) {
        this.cgmesSvMetadataAttributes = cgmesSvMetadataAttributes;
    }

    public CgmesSvMetadataImpl(CgmesSvMetadataAttributes cgmesSvMetadataAttributes, String description, int svVersion, List<String> dependencies, String modelingAuthoritySet) {
        this(cgmesSvMetadataAttributes);
        this.cgmesSvMetadataAttributes.setDescription(description);
        this.cgmesSvMetadataAttributes.setSvVersion(svVersion);
        this.cgmesSvMetadataAttributes.setDependencies(dependencies);
        this.cgmesSvMetadataAttributes.setModelingAuthoritySet(modelingAuthoritySet);
    }

    public CgmesSvMetadataAttributes getCgmesSvMetadataAttributes() {
        return cgmesSvMetadataAttributes;
    }

    public String getDescription() {
        return this.cgmesSvMetadataAttributes.getDescription();
    }

    public int getSvVersion() {
        return this.cgmesSvMetadataAttributes.getSvVersion();
    }

    public List<String> getDependencies() {
        return this.cgmesSvMetadataAttributes.getDependencies();
    }

    public String getModelingAuthoritySet() {
        return this.cgmesSvMetadataAttributes.getModelingAuthoritySet();
    }
}

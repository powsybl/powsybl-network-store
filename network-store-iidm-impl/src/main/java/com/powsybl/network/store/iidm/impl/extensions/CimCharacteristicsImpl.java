/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.conversion.elements.CgmesTopologyKind;
import com.powsybl.cgmes.conversion.extensions.CimCharacteristics;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.model.CimCharacteristicsAttributes;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class CimCharacteristicsImpl extends AbstractExtension<Network> implements CimCharacteristics {
    private CimCharacteristicsAttributes cimCharacteristicsAttributes;

    public CimCharacteristicsImpl(CimCharacteristicsAttributes cimCharacteristicsAttributes) {
        this.cimCharacteristicsAttributes = cimCharacteristicsAttributes;
    }

    CimCharacteristicsImpl(CimCharacteristicsAttributes cimCharacteristicsAttributes, CgmesTopologyKind cgmesTopologyKind, int cimVersion) {
        this(cimCharacteristicsAttributes);
        this.cimCharacteristicsAttributes.setCgmesTopologyKind(cgmesTopologyKind);
        this.cimCharacteristicsAttributes.setCimVersion(cimVersion);
    }

    public CimCharacteristicsAttributes getCimCharacteristicsAttributes() {
        return this.cimCharacteristicsAttributes;
    }

    public CgmesTopologyKind getTopologyKind() {
        return this.cimCharacteristicsAttributes.getCgmesTopologyKind();
    }

    public int getCimVersion() {
        return this.cimCharacteristicsAttributes.getCimVersion();
    }
}

/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.CgmesTopologyKind;
import com.powsybl.cgmes.extensions.CimCharacteristics;
import com.powsybl.cgmes.extensions.CimCharacteristicsAdder;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.model.CimCharacteristicsAttributes;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class CimCharacteristicsAdderImpl extends AbstractIidmExtensionAdder<Network, CimCharacteristics> implements CimCharacteristicsAdder {
    private CgmesTopologyKind cgmesTopologyKind;
    private Integer cimVersion;

    protected CimCharacteristicsAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    public CimCharacteristicsAdder setTopologyKind(CgmesTopologyKind cgmesTopologyKind) {
        this.cgmesTopologyKind = cgmesTopologyKind;
        return this;
    }

    @Override
    public CimCharacteristicsAdder setCimVersion(int cimVersion) {
        this.cimVersion = cimVersion;
        return this;
    }

    @Override
    protected CimCharacteristics createExtension(Network network) {
        if (this.cgmesTopologyKind == null) {
            throw new PowsyblException("CimCharacteristics.topologyKind is undefined");
        } else if (this.cimVersion == null) {
            throw new PowsyblException("CimCharacteristics.cimVersion is undefined");
        } else {
            var attributes = CimCharacteristicsAttributes.builder()
                    .cgmesTopologyKind(cgmesTopologyKind)
                    .cimVersion(cimVersion)
                    .build();
            ((NetworkImpl) network).updateResourceWithoutNotification(res -> res.getAttributes().setCimCharacteristics(attributes));
            return new CimCharacteristicsImpl((NetworkImpl) network);
        }
    }
}

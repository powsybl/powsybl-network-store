/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.CgmesTopologyKind;
import com.powsybl.cgmes.extensions.CimCharacteristics;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.iidm.impl.NetworkImpl;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class CimCharacteristicsImpl extends AbstractExtension<Network> implements CimCharacteristics {

    public CimCharacteristicsImpl(NetworkImpl network) {
        super(network);
    }

    private NetworkImpl getNetwork() {
        return (NetworkImpl) getExtendable();
    }

    public CgmesTopologyKind getTopologyKind() {
        return getNetwork().checkResource().getAttributes().getCimCharacteristics().getCgmesTopologyKind();
    }

    public int getCimVersion() {
        Integer cimVersion = getNetwork().checkResource().getAttributes().getCimCharacteristics().getCimVersion();
        return cimVersion == null ? -1 : cimVersion;
    }
}

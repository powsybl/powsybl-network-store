/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.iidm.impl.AbstractForwardingNetworkStoreClient;
import com.powsybl.network.store.iidm.impl.NetworkStoreClient;
import com.powsybl.network.store.model.BusbarSectionAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.SubstationAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SubstationPreloadingNetworkStoreClient extends AbstractForwardingNetworkStoreClient implements NetworkStoreClient {

    public SubstationPreloadingNetworkStoreClient(NetworkStoreClient delegate) {
        super(delegate);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, int variantNum, String voltageLevelId) {
        delegate.getIdentifiablesWithSameSubstationAs(networkUuid, variantNum, voltageLevelId);
        return super.getVoltageLevelBusbarSections(networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, int variantNum, String substationId) {
        delegate.getIdentifiablesWithSameSubstationAs(networkUuid, variantNum, substationId);
        return super.getSubstation(networkUuid, variantNum, substationId);
    }
}

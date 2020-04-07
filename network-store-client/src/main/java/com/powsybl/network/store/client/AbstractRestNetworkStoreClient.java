/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.SwitchAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public abstract class AbstractRestNetworkStoreClient {

    protected abstract void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> resources);

    public void updateResource(UUID networkUuid, Resource resource) {
        switch (resource.getType()) {
            case SWITCH:
                updateSwitches(networkUuid, Arrays.asList(resource));
                break;

            default:
        }
    }
}

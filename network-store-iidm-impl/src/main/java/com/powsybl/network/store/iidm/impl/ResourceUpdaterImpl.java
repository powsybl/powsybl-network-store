/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ResourceUpdater;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ResourceUpdaterImpl implements ResourceUpdater {

    private final UUID networkUuid;

    private final NetworkStoreClient client;

    public ResourceUpdaterImpl(UUID networkUuid, NetworkStoreClient client) {
        this.networkUuid = Objects.requireNonNull(networkUuid);
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public void updateResource(Resource resource) {
        switch (resource.getType()) {
            case NETWORK:
                client.updateNetwork(networkUuid, resource);
                break;
            case VOLTAGE_LEVEL:
                client.updateVoltageLevel(networkUuid, resource);
                break;
            case SWITCH:
                client.updateSwitch(networkUuid, resource);
                break;
            case LINE:
                client.updateLine(networkUuid, resource);
                break;
            case TWO_WINDINGS_TRANSFORMER:
                client.updateTwoWindingsTransformer(networkUuid, resource);
                break;
            case THREE_WINDINGS_TRANSFORMER:
                client.updateThreeWindingsTransformer(networkUuid, resource);
                break;
            case DANGLING_LINE:
                client.updateDanglingLine(networkUuid, resource);
                break;
            case GENERATOR:
                client.updateGenerator(networkUuid, resource);
                break;
            case BATTERY:
                client.updateBattery(networkUuid, resource);
                break;
            case STATIC_VAR_COMPENSATOR:
                client.updateStaticVarCompensator(networkUuid, resource);
                break;
            case SHUNT_COMPENSATOR:
                client.updateShuntCompensator(networkUuid, resource);
                break;
            case LCC_CONVERTER_STATION:
                client.updateLccConverterStation(networkUuid, resource);
                break;
            case VSC_CONVERTER_STATION:
                client.updateVscConverterStation(networkUuid, resource);
                break;
            case LOAD:
                client.updateLoad(networkUuid, resource);
                break;
            case CONFIGURED_BUS:
                client.updateConfiguredBus(networkUuid, resource);
                break;
            case HVDC_LINE:
                client.updateHvdcLine(networkUuid, resource);
                break;
            default:
        }
    }
}

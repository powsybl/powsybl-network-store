/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ResourceUpdater;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ResourceUpdaterImpl implements ResourceUpdater {

    private final NetworkStoreClient client;

    public ResourceUpdaterImpl(NetworkStoreClient client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public void updateResource(UUID networkUuid, Resource resource) {
        switch (resource.getType()) {
            case NETWORK:
                client.updateNetwork(networkUuid, resource);
                break;
            case VOLTAGE_LEVEL:
                client.updateVoltageLevels(networkUuid, Collections.singletonList(resource));
                break;
            case SWITCH:
                client.updateSwitches(networkUuid, Collections.singletonList(resource));
                break;
            case LINE:
                client.updateLines(networkUuid, Collections.singletonList(resource));
                break;
            case TWO_WINDINGS_TRANSFORMER:
                client.updateTwoWindingsTransformers(networkUuid, Collections.singletonList(resource));
                break;
            case THREE_WINDINGS_TRANSFORMER:
                client.updateThreeWindingsTransformers(networkUuid, Collections.singletonList(resource));
                break;
            case DANGLING_LINE:
                client.updateDanglingLines(networkUuid, Collections.singletonList(resource));
                break;
            case GENERATOR:
                client.updateGenerators(networkUuid, Collections.singletonList(resource));
                break;
            case BATTERY:
                client.updateBatteries(networkUuid, Collections.singletonList(resource));
                break;
            case STATIC_VAR_COMPENSATOR:
                client.updateStaticVarCompensators(networkUuid, Collections.singletonList(resource));
                break;
            case SHUNT_COMPENSATOR:
                client.updateShuntCompensators(networkUuid, Collections.singletonList(resource));
                break;
            case LCC_CONVERTER_STATION:
                client.updateLccConverterStations(networkUuid, Collections.singletonList(resource));
                break;
            case VSC_CONVERTER_STATION:
                client.updateVscConverterStations(networkUuid, Collections.singletonList(resource));
                break;
            case LOAD:
                client.updateLoads(networkUuid, Collections.singletonList(resource));
                break;
            case CONFIGURED_BUS:
                client.updateConfiguredBuses(networkUuid, Collections.singletonList(resource));
                break;
            case HVDC_LINE:
                client.updateHvdcLines(networkUuid, Collections.singletonList(resource));
                break;
            case SUBSTATION:
                client.updateSubstations(networkUuid, Collections.singletonList(resource));
                break;
            case BUSBAR_SECTION:
                client.updateBusbarSections(networkUuid, Collections.singletonList(resource));
                break;
            default:
        }
    }
}

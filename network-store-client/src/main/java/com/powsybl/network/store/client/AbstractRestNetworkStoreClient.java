/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.network.store.model.ConfiguredBusAttributes;
import com.powsybl.network.store.model.DanglingLineAttributes;
import com.powsybl.network.store.model.GeneratorAttributes;
import com.powsybl.network.store.model.HvdcLineAttributes;
import com.powsybl.network.store.model.LccConverterStationAttributes;
import com.powsybl.network.store.model.LineAttributes;
import com.powsybl.network.store.model.LoadAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ShuntCompensatorAttributes;
import com.powsybl.network.store.model.StaticVarCompensatorAttributes;
import com.powsybl.network.store.model.SwitchAttributes;
import com.powsybl.network.store.model.ThreeWindingsTransformerAttributes;
import com.powsybl.network.store.model.TwoWindingsTransformerAttributes;
import com.powsybl.network.store.model.VscConverterStationAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public abstract class AbstractRestNetworkStoreClient {

    protected abstract void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> resources);

    protected abstract void updateLines(UUID networkUuid, List<Resource<LineAttributes>> resources);

    protected abstract void updateTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> resources);

    protected abstract void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> resources);

    protected abstract void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> resources);

    protected abstract void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> resources);

    protected abstract void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> resources);

    protected abstract void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> resources);

    protected abstract void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> resources);

    protected abstract void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> resources);

    protected abstract void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> resources);

    protected abstract void updateConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> resources);

    protected abstract void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> resources);

    public void updateResource(UUID networkUuid, Resource resource) {
        switch (resource.getType()) {
            case SWITCH:
                updateSwitches(networkUuid, Arrays.asList(resource));
                break;
            case LINE:
                updateLines(networkUuid, Arrays.asList(resource));
                break;
            case TWO_WINDINGS_TRANSFORMER:
                updateTwoWindingsTransformers(networkUuid, Arrays.asList(resource));
                break;
            case THREE_WINDINGS_TRANSFORMER:
                updateThreeWindingsTransformers(networkUuid, Arrays.asList(resource));
                break;
            case DANGLING_LINE:
                updateDanglingLines(networkUuid, Arrays.asList(resource));
                break;
            case GENERATOR:
                updateGenerators(networkUuid, Arrays.asList(resource));
                break;
            case STATIC_VAR_COMPENSATOR:
                updateStaticVarCompensators(networkUuid, Arrays.asList(resource));
                break;
            case SHUNT_COMPENSATOR:
                updateShuntCompensators(networkUuid, Arrays.asList(resource));
                break;
            case LCC_CONVERTER_STATION:
                updateLccConverterStations(networkUuid, Arrays.asList(resource));
                break;
            case VSC_CONVERTER_STATION:
                updateVscConverterStations(networkUuid, Arrays.asList(resource));
                break;
            case LOAD:
                updateLoads(networkUuid, Arrays.asList(resource));
                break;
            case CONFIGURED_BUS:
                updateConfiguredBuses(networkUuid, Arrays.asList(resource));
                break;
            case HVDC_LINE:
                updateHvdcLines(networkUuid, Arrays.asList(resource));
                break;
            default:
        }
    }
}

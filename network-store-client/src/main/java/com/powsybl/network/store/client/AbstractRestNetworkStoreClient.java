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

import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public abstract class AbstractRestNetworkStoreClient {

    protected abstract void updateSwitches(UUID networkUuid, Resource<SwitchAttributes> resource);

    protected abstract void updateLines(UUID networkUuid, Resource<LineAttributes> resource);

    protected abstract void updateTwoWindingsTransformers(UUID networkUuid, Resource<TwoWindingsTransformerAttributes> resource);

    protected abstract void updateThreeWindingsTransformers(UUID networkUuid, Resource<ThreeWindingsTransformerAttributes> resource);

    protected abstract void updateDanglingLines(UUID networkUuid, Resource<DanglingLineAttributes> resource);

    protected abstract void updateGenerators(UUID networkUuid, Resource<GeneratorAttributes> resource);

    protected abstract void updateStaticVarCompensators(UUID networkUuid, Resource<StaticVarCompensatorAttributes> resource);

    protected abstract void updateShuntCompensators(UUID networkUuid, Resource<ShuntCompensatorAttributes> resource);

    protected abstract void updateLccConverterStations(UUID networkUuid, Resource<LccConverterStationAttributes> resource);

    protected abstract void updateVscConverterStations(UUID networkUuid, Resource<VscConverterStationAttributes> resource);

    protected abstract void updateLoads(UUID networkUuid, Resource<LoadAttributes> resource);

    protected abstract void updateConfiguredBuses(UUID networkUuid, Resource<ConfiguredBusAttributes> resource);

    protected abstract void updateHvdcLines(UUID networkUuid, Resource<HvdcLineAttributes> resource);

    public void updateResource(UUID networkUuid, Resource resource) {
        switch (resource.getType()) {
            case SWITCH:
                updateSwitches(networkUuid, resource);
                break;
            case LINE:
                updateLines(networkUuid, resource);
                break;
            case TWO_WINDINGS_TRANSFORMER:
                updateTwoWindingsTransformers(networkUuid, resource);
                break;
            case THREE_WINDINGS_TRANSFORMER:
                updateThreeWindingsTransformers(networkUuid, resource);
                break;
            case DANGLING_LINE:
                updateDanglingLines(networkUuid, resource);
                break;
            case GENERATOR:
                updateGenerators(networkUuid, resource);
                break;
            case STATIC_VAR_COMPENSATOR:
                updateStaticVarCompensators(networkUuid, resource);
                break;
            case SHUNT_COMPENSATOR:
                updateShuntCompensators(networkUuid, resource);
                break;
            case LCC_CONVERTER_STATION:
                updateLccConverterStations(networkUuid, resource);
                break;
            case VSC_CONVERTER_STATION:
                updateVscConverterStations(networkUuid, resource);
                break;
            case LOAD:
                updateLoads(networkUuid, resource);
                break;
            case CONFIGURED_BUS:
                updateConfiguredBuses(networkUuid, resource);
                break;
            case HVDC_LINE:
                updateHvdcLines(networkUuid, resource);
                break;
            default:
        }
    }
}

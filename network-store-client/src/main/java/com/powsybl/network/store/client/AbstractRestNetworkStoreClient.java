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

    protected abstract void updateSwitch(UUID networkUuid, Resource<SwitchAttributes> resource);

    protected abstract void updateLine(UUID networkUuid, Resource<LineAttributes> resource);

    protected abstract void updateTwoWindingsTransformer(UUID networkUuid, Resource<TwoWindingsTransformerAttributes> resource);

    protected abstract void updateThreeWindingsTransformer(UUID networkUuid, Resource<ThreeWindingsTransformerAttributes> resource);

    protected abstract void updateDanglingLine(UUID networkUuid, Resource<DanglingLineAttributes> resource);

    protected abstract void updateGenerator(UUID networkUuid, Resource<GeneratorAttributes> resource);

    protected abstract void updateStaticVarCompensator(UUID networkUuid, Resource<StaticVarCompensatorAttributes> resource);

    protected abstract void updateShuntCompensator(UUID networkUuid, Resource<ShuntCompensatorAttributes> resource);

    protected abstract void updateLccConverterStation(UUID networkUuid, Resource<LccConverterStationAttributes> resource);

    protected abstract void updateVscConverterStation(UUID networkUuid, Resource<VscConverterStationAttributes> resource);

    protected abstract void updateLoad(UUID networkUuid, Resource<LoadAttributes> resource);

    protected abstract void updateConfiguredBus(UUID networkUuid, Resource<ConfiguredBusAttributes> resource);

    protected abstract void updateHvdcLine(UUID networkUuid, Resource<HvdcLineAttributes> resource);

    public void updateResource(UUID networkUuid, Resource resource) {
        switch (resource.getType()) {
            case SWITCH:
                updateSwitch(networkUuid, resource);
                break;
            case LINE:
                updateLine(networkUuid, resource);
                break;
            case TWO_WINDINGS_TRANSFORMER:
                updateTwoWindingsTransformer(networkUuid, resource);
                break;
            case THREE_WINDINGS_TRANSFORMER:
                updateThreeWindingsTransformer(networkUuid, resource);
                break;
            case DANGLING_LINE:
                updateDanglingLine(networkUuid, resource);
                break;
            case GENERATOR:
                updateGenerator(networkUuid, resource);
                break;
            case STATIC_VAR_COMPENSATOR:
                updateStaticVarCompensator(networkUuid, resource);
                break;
            case SHUNT_COMPENSATOR:
                updateShuntCompensator(networkUuid, resource);
                break;
            case LCC_CONVERTER_STATION:
                updateLccConverterStation(networkUuid, resource);
                break;
            case VSC_CONVERTER_STATION:
                updateVscConverterStation(networkUuid, resource);
                break;
            case LOAD:
                updateLoad(networkUuid, resource);
                break;
            case CONFIGURED_BUS:
                updateConfiguredBus(networkUuid, resource);
                break;
            case HVDC_LINE:
                updateHvdcLine(networkUuid, resource);
                break;
            default:
        }
    }
}

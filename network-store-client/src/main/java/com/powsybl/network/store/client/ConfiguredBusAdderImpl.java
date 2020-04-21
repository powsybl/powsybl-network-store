/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusAdder;
import com.powsybl.network.store.model.ConfiguredBusAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class ConfiguredBusAdderImpl extends AbstractIdentifiableAdder<ConfiguredBusAdderImpl> implements BusAdder {

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    ConfiguredBusAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(index);
        this.voltageLevelResource = voltageLevelResource;
    }

    @Override
    public Bus add() {
        String id = checkAndGetUniqueId();

        Resource<ConfiguredBusAttributes> resource = Resource.configuredBusBuilder(index.getNetwork().getUuid(), index.getResourceUpdater())
                .id(id)
                .attributes(ConfiguredBusAttributes.builder()
                        .id(id)
                        .name(getName())
                        .voltageLevelId(voltageLevelResource.getId())
                        .build())
                .build();
        return getIndex().createBus(resource);
    }

    @Override
    protected String getTypeDescription() {
        return "ConfiguredBus";
    }
}

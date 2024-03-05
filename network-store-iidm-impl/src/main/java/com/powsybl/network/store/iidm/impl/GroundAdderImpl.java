/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;

/**
 * @author Ayoub LABIDI <ayoub.labidi at rte-france.com>
 */
class GroundAdderImpl extends AbstractInjectionAdder<GroundAdderImpl> implements GroundAdder {

    GroundAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(voltageLevelResource, index);
    }

    @Override
    public Ground add() {
        String id = checkAndGetUniqueId();
        checkNodeBus();

        Resource<GroundAttributes> resource = Resource.groundBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .attributes(GroundAttributes.builder()
                        .voltageLevelId(getVoltageLevelResource().getId())
                        .name(getName())
                        .fictitious(isFictitious())
                        .node(getNode())
                        .bus(getBus())
                        .connectableBus(getConnectableBus() != null ? getConnectableBus() : getBus())
                        .build())
                .build();
        GroundImpl ground = getIndex().createGround(resource);
        ground.getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        return ground;
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.GENERATOR.getDescription();
    }
}

/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.SwitchAttributes;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
class SwitchAdderBusBreakerImpl extends AbstractSwitchAdder<SwitchAdderBusBreakerImpl> implements VoltageLevel.BusBreakerView.SwitchAdder {

    private String bus1;

    private String bus2;

    SwitchAdderBusBreakerImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        super(voltageLevelResource, index);
    }

    @Override
    public VoltageLevel.BusBreakerView.SwitchAdder setBus1(String bus1) {
        this.bus1 = bus1;
        return this;
    }

    @Override
    public VoltageLevel.BusBreakerView.SwitchAdder setBus2(String bus2) {
        this.bus2 = bus2;
        return this;
    }

    @Override
    public Switch add() {
        String id = checkAndGetUniqueId();
        if (bus1 == null) {
            throw new ValidationException(this, "first connection bus is not set");
        }
        if (bus2 == null) {
            throw new ValidationException(this, "second connection bus is not set");
        }

        Resource<SwitchAttributes> resource = Resource.switchBuilder()
                .id(id)
                .attributes(SwitchAttributes.builder()
                        .voltageLevelId(getVoltageLevelResource().getId())
                        .name(getName())
                        .fictitious(isFictitious())
                        .bus1(bus1)
                        .bus2(bus2)
                        .kind(SwitchKind.BREAKER)
                        .open(isOpen())
                        .retained(true)
                        .fictitious(isFictitious())
                        .build())
                .build();
        return getIndex().createSwitch(resource);
    }
}

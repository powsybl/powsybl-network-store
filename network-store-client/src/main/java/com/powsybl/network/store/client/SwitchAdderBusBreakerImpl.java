/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.SwitchAttributes;
import com.powsybl.network.store.model.VoltageLevelAttributes;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
class SwitchAdderBusBreakerImpl implements VoltageLevel.BusBreakerView.SwitchAdder {

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    private final NetworkObjectIndex index;

    private String id;

    private String name;

    private String bus1;

    private String bus2;

    private boolean open = false;

    private boolean fictitious = false;

    SwitchAdderBusBreakerImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        this.voltageLevelResource = voltageLevelResource;
        this.index = index;
    }

    @Override
    public VoltageLevel.BusBreakerView.SwitchAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public VoltageLevel.BusBreakerView.SwitchAdder setEnsureIdUnicity(boolean ensureIdUnicity) {
        // TODO
        return this;
    }

    @Override
    public VoltageLevel.BusBreakerView.SwitchAdder setName(String name) {
        this.name = name;
        return this;
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
    public VoltageLevel.BusBreakerView.SwitchAdder setOpen(boolean open) {
        this.open = open;
        return this;
    }

    @Override
    public VoltageLevel.BusBreakerView.SwitchAdder setFictitious(boolean fictitious) {
        this.fictitious = fictitious;
        return this;
    }

    @Override
    public Switch add() {
        Resource<SwitchAttributes> resource = Resource.switchBuilder(index.getNetwork().getUuid(), index.getStoreClient())
                .id(id)
                .attributes(SwitchAttributes.builder()
                        .voltageLevelId(voltageLevelResource.getId())
                        .name(name)
                        .bus1(bus1)
                        .bus2(bus2)
                        .kind(SwitchKind.BREAKER)
                        .open(open)
                        .fictitious(fictitious)
                        .build())
                .build();
        return index.createSwitch(resource);
    }
}

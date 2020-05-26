/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SwitchAdderNodeBreakerImpl implements VoltageLevel.NodeBreakerView.SwitchAdder {

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    private final NetworkObjectIndex index;

    private SwitchKind kind;

    private String id;

    private String name;

    private Integer node1;

    private Integer node2;

    private boolean open = false;

    private boolean retained = false;

    private boolean fictitious = false;

    SwitchAdderNodeBreakerImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index, SwitchKind kind) {
        this.voltageLevelResource = voltageLevelResource;
        this.index = index;
        this.kind = kind;
    }

    @Override
    public VoltageLevel.NodeBreakerView.SwitchAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public VoltageLevel.NodeBreakerView.SwitchAdder setEnsureIdUnicity(boolean ensureIdUnicity) {
        // TODO
        return this;
    }

    @Override
    public VoltageLevel.NodeBreakerView.SwitchAdder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public VoltageLevel.NodeBreakerView.SwitchAdder setNode1(int node1) {
        this.node1 = node1;
        return this;
    }

    @Override
    public VoltageLevel.NodeBreakerView.SwitchAdder setNode2(int node2) {
        this.node2 = node2;
        return this;
    }

    @Override
    public VoltageLevel.NodeBreakerView.SwitchAdder setKind(SwitchKind kind) {
        this.kind = kind;
        return this;
    }

    @Override
    public VoltageLevel.NodeBreakerView.SwitchAdder setKind(String kind) {
        this.kind = SwitchKind.valueOf(kind);
        return this;
    }

    @Override
    public VoltageLevel.NodeBreakerView.SwitchAdder setOpen(boolean open) {
        this.open = open;
        return this;
    }

    @Override
    public VoltageLevel.NodeBreakerView.SwitchAdder setRetained(boolean retained) {
        this.retained = retained;
        return this;
    }

    @Override
    public VoltageLevel.NodeBreakerView.SwitchAdder setFictitious(boolean fictitious) {
        this.fictitious = fictitious;
        return this;
    }

    @Override
    public Switch add() {
        Resource<SwitchAttributes> resource = Resource.switchBuilder(index.getNetwork().getUuid(), index.getResourceUpdater())
                .id(id)
                .attributes(SwitchAttributes.builder()
                        .voltageLevelId(voltageLevelResource.getId())
                        .name(name)
                        .kind(kind)
                        .node1(node1)
                        .node2(node2)
                        .open(open)
                        .retained(retained)
                        .fictitious(fictitious)
                        .build())
                .build();
        return index.createSwitch(resource);
    }
}

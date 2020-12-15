/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SwitchAdderNodeBreakerImpl extends AbstractSwitchAdder<SwitchAdderNodeBreakerImpl> implements VoltageLevel.NodeBreakerView.SwitchAdder {

    private SwitchKind kind;

    private Integer node1;

    private Integer node2;

    private boolean retained = false;

    SwitchAdderNodeBreakerImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index, SwitchKind kind) {
        super(voltageLevelResource, index);
        this.kind = kind;
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
        return setKind(SwitchKind.valueOf(kind));
    }

    @Override
    public VoltageLevel.NodeBreakerView.SwitchAdder setRetained(boolean retained) {
        this.retained = retained;
        return this;
    }

    @Override
    public Switch add() {
        String id = checkAndGetUniqueId();
        if (node1 == null) {
            throw new ValidationException(this, "first connection node is not set");
        }
        if (node2 == null) {
            throw new ValidationException(this, "second connection node is not set");
        }
        if (kind == null) {
            throw new ValidationException(this, "kind is not set");
        }

        Resource<SwitchAttributes> resource = Resource.switchBuilder(getIndex().getResourceUpdater())
                .id(id)
                .attributes(SwitchAttributes.builder()
                        .voltageLevelId(getVoltageLevelResource().getId())
                        .name(getName())
                        .fictitious(isFictitious())
                        .kind(kind)
                        .node1(node1)
                        .node2(node2)
                        .open(isOpen())
                        .retained(retained)
                        .fictitious(isFictitious())
                        .build())
                .build();
        return getIndex().createSwitch(resource);
    }
}

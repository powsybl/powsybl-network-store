/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.LineAdder;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.LineAttributes;
import com.powsybl.network.store.model.Resource;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class LineAdderImpl extends AbstractBranchAdder<LineAdderImpl> implements LineAdder {

    private double r = Double.NaN;

    private double x = Double.NaN;

    private double g1 = Double.NaN;

    private double b1 = Double.NaN;

    private double g2 = Double.NaN;

    private double b2 = Double.NaN;

    LineAdderImpl(NetworkObjectIndex index) {
        super(index);
    }

    @Override
    public LineAdder setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public LineAdder setX(double x) {
        this.x = x;
        return this;
    }

    @Override
    public LineAdder setG1(double g1) {
        this.g1 = g1;
        return this;
    }

    @Override
    public LineAdder setB1(double b1) {
        this.b1 = b1;
        return this;
    }

    @Override
    public LineAdder setG2(double g2) {
        this.g2 = g2;
        return this;
    }

    @Override
    public LineAdder setB2(double b2) {
        this.b2 = b2;
        return this;
    }

    @Override
    public Line add() {
        String id = checkAndGetUniqueId();
        checkVoltageLevel1();
        checkVoltageLevel2();
        checkNodeBus1();
        checkNodeBus2();

        ValidationUtil.checkR(this, r);
        ValidationUtil.checkX(this, x);
        ValidationUtil.checkG1(this, g1);
        ValidationUtil.checkG2(this, g2);
        ValidationUtil.checkB1(this, b1);
        ValidationUtil.checkB2(this, b2);

        Resource<LineAttributes> resource = Resource.lineBuilder(index.getNetwork().getUuid(), index.getResourceUpdater())
                .id(id)
                .attributes(LineAttributes.builder()
                        .voltageLevelId1(getVoltageLevelId1())
                        .voltageLevelId2(getVoltageLevelId2())
                        .name(getName())
                        .node1(getNode1())
                        .node2(getNode2())
                        .bus1(getBus1())
                        .bus2(getBus2())
                        .connectableBus1(getConnectableBus1())
                        .connectableBus2(getConnectableBus2())
                        .r(r)
                        .x(x)
                        .g1(g1)
                        .b1(b1)
                        .g2(g2)
                        .b2(b2)
                        .build())
                .build();
        return getIndex().createLine(resource);
    }

    @Override
    protected String getTypeDescription() {
        return "AC Line";
    }
}

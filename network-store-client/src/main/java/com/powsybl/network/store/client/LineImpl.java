/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.Line;
import com.powsybl.network.store.model.LineAttributes;
import com.powsybl.network.store.model.Resource;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LineImpl extends AbstractBranchImpl<Line, LineAttributes> implements Line {

    public LineImpl(NetworkObjectIndex index, Resource<LineAttributes> resource) {
        super(index, resource);
    }

    static LineImpl create(NetworkObjectIndex index, Resource<LineAttributes> resource) {
        return new LineImpl(index, resource);
    }

    @Override
    protected Line getBranch() {
        return this;
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.LINE;
    }

    @Override
    public boolean isTieLine() {
        return false;
    }

    @Override
    public double getR() {
        return resource.getAttributes().getR();
    }

    @Override
    public Line setR(double r) {
        resource.getAttributes().setR(r);
        return this;
    }

    @Override
    public double getX() {
        return resource.getAttributes().getX();
    }

    @Override
    public Line setX(double x) {
        resource.getAttributes().setX(x);
        return this;
    }

    @Override
    public double getG1() {
        return resource.getAttributes().getG1();
    }

    @Override
    public Line setG1(double g1) {
        resource.getAttributes().setG1(g1);
        return this;
    }

    @Override
    public double getG2() {
        return resource.getAttributes().getG2();
    }

    @Override
    public Line setG2(double g2) {
        resource.getAttributes().setG2(g2);
        return this;
    }

    @Override
    public double getB1() {
        return resource.getAttributes().getB1();
    }

    @Override
    public Line setB1(double b1) {
        resource.getAttributes().setB1(b1);
        return this;
    }

    @Override
    public double getB2() {
        return resource.getAttributes().getB2();
    }

    @Override
    public Line setB2(double b2) {
        resource.getAttributes().setB2(b2);
        return this;
    }
}

/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.LineAttributes;
import com.powsybl.network.store.model.Resource;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
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
    public double getR() {
        return getResource().getAttributes().getR();
    }

    @Override
    public Line setR(double r) {
        ValidationUtil.checkR(this, r);
        double oldValue = getResource().getAttributes().getR();
        if (r != oldValue) {
            updateResource(res -> res.getAttributes().setR(r));
            index.notifyUpdate(this, "r", null, oldValue, r);
        }
        return this;
    }

    @Override
    public double getX() {
        return getResource().getAttributes().getX();
    }

    @Override
    public Line setX(double x) {
        ValidationUtil.checkX(this, x);
        double oldValue = getResource().getAttributes().getX();
        if (x != oldValue) {
            updateResource(res -> res.getAttributes().setX(x));
            index.notifyUpdate(this, "x", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, x);
        }
        return this;
    }

    @Override
    public double getG1() {
        return getResource().getAttributes().getG1();
    }

    @Override
    public Line setG1(double g1) {
        ValidationUtil.checkG1(this, g1);
        double oldValue = getResource().getAttributes().getG1();
        if (g1 != oldValue) {
            updateResource(res -> res.getAttributes().setG1(g1));
            index.notifyUpdate(this, "g1", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, g1);
        }
        return this;
    }

    @Override
    public double getG2() {
        return getResource().getAttributes().getG2();
    }

    @Override
    public Line setG2(double g2) {
        ValidationUtil.checkG2(this, g2);
        double oldValue = getResource().getAttributes().getG2();
        if (g2 != oldValue) {
            updateResource(res -> res.getAttributes().setG2(g2));
            index.notifyUpdate(this, "g2", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, g2);
        }
        return this;
    }

    @Override
    public double getB1() {
        return getResource().getAttributes().getB1();
    }

    @Override
    public Line setB1(double b1) {
        var resource = getResource();
        ValidationUtil.checkB1(this, b1);
        double oldValue = resource.getAttributes().getB1();
        if (b1 != oldValue) {
            updateResource(res -> res.getAttributes().setB1(b1));
            index.notifyUpdate(this, "b1", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, b1);
        }
        return this;
    }

    @Override
    public double getB2() {
        return getResource().getAttributes().getB2();
    }

    @Override
    public Line setB2(double b2) {
        var resource = getResource();
        ValidationUtil.checkB2(this, b2);
        double oldValue = resource.getAttributes().getB2();
        if (b2 != oldValue) {
            updateResource(res -> res.getAttributes().setB2(b2));
            index.notifyUpdate(this, "b2", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, b2);
        }
        return this;
    }

    @Override
    public void remove() {
        var resource = getResource();
        index.notifyBeforeRemoval(this);
        for (Terminal terminal : getTerminals()) {
            ((TerminalImpl<?>) terminal).removeAsRegulatingPoint();
            ((TerminalImpl<?>) terminal).getReferrerManager().notifyOfRemoval();
        }
        // invalidate calculated buses before removal otherwise voltage levels won't be accessible anymore for topology invalidation!
        invalidateCalculatedBuses(getTerminals());
        index.removeLine(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }
}

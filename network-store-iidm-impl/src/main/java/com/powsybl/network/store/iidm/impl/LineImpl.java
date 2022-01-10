/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.entsoe.util.MergedXnode;
import com.powsybl.entsoe.util.MergedXnodeImpl;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.LineAttributes;
import com.powsybl.network.store.model.MergedXnodeAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.Collection;

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
    public boolean isTieLine() {
        return false;
    }

    @Override
    public double getR() {
        return checkResource().getAttributes().getR();
    }

    @Override
    public Line setR(double r) {
        var resource = checkResource();
        ValidationUtil.checkR(this, r);
        double oldValue = resource.getAttributes().getR();
        resource.getAttributes().setR(r);
        updateResource();
        index.notifyUpdate(this, "r", oldValue, r);
        return this;
    }

    @Override
    public double getX() {
        return checkResource().getAttributes().getX();
    }

    @Override
    public Line setX(double x) {
        var resource = checkResource();
        ValidationUtil.checkX(this, x);
        double oldValue = resource.getAttributes().getX();
        resource.getAttributes().setX(x);
        updateResource();
        index.notifyUpdate(this, "x", oldValue, x);
        return this;
    }

    @Override
    public double getG1() {
        return checkResource().getAttributes().getG1();
    }

    @Override
    public Line setG1(double g1) {
        var resource = checkResource();
        ValidationUtil.checkG1(this, g1);
        double oldValue = resource.getAttributes().getG1();
        resource.getAttributes().setG1(g1);
        updateResource();
        index.notifyUpdate(this, "g1", oldValue, g1);
        return this;
    }

    @Override
    public double getG2() {
        return checkResource().getAttributes().getG2();
    }

    @Override
    public Line setG2(double g2) {
        var resource = checkResource();
        ValidationUtil.checkG2(this, g2);
        double oldValue = resource.getAttributes().getG2();
        resource.getAttributes().setG2(g2);
        updateResource();
        index.notifyUpdate(this, "g2", oldValue, g2);
        return this;
    }

    @Override
    public double getB1() {
        return checkResource().getAttributes().getB1();
    }

    @Override
    public Line setB1(double b1) {
        var resource = checkResource();
        ValidationUtil.checkB1(this, b1);
        double oldValue = resource.getAttributes().getB1();
        resource.getAttributes().setB1(b1);
        updateResource();
        index.notifyUpdate(this, "b1", oldValue, b1);
        return this;
    }

    @Override
    public double getB2() {
        return checkResource().getAttributes().getB2();
    }

    @Override
    public Line setB2(double b2) {
        var resource = checkResource();
        ValidationUtil.checkB2(this, b2);
        double oldValue = resource.getAttributes().getB2();
        resource.getAttributes().setB2(b2);
        updateResource();
        index.notifyUpdate(this, "b2", oldValue, b2);
        return this;
    }

    @Override
    public <E extends Extension<Line>> void addExtension(Class<? super E> type, E extension) {
        var resource = checkResource();
        if (type == MergedXnode.class) {
            MergedXnode mergedXnode = (MergedXnode) extension;
            resource.getAttributes().setMergedXnode(
                    MergedXnodeAttributes.builder()
                            .code(mergedXnode.getCode())
                            .rdp(mergedXnode.getRdp())
                            .xdp(mergedXnode.getXdp())
                            .xnodeP1(mergedXnode.getXnodeP1())
                            .xnodeP2(mergedXnode.getXnodeP2())
                            .xnodeQ1(mergedXnode.getXnodeQ1())
                            .xnodeQ2(mergedXnode.getXnodeQ2())
                            .line1Name(mergedXnode.getLine1Name())
                            .line2Name(mergedXnode.getLine2Name())
                            .build());
            updateResource();
        }
        super.addExtension(type, extension);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Extension<Line>> E getExtension(Class<? super E> type) {
        if (type == MergedXnode.class) {
            return (E) createMergedXnode();
        }
        return super.getExtension(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Extension<Line>> E getExtensionByName(String name) {
        if (name.equals("mergedXnode")) {
            return (E) createMergedXnode();
        }
        return super.getExtensionByName(name);
    }

    private MergedXnode createMergedXnode() {
        var resource = checkResource();
        if (resource.getAttributes().getMergedXnode() != null) {
            return new MergedXnodeImpl(this,
                    resource.getAttributes().getMergedXnode().getRdp(),
                    resource.getAttributes().getMergedXnode().getXdp(),
                    resource.getAttributes().getMergedXnode().getXnodeP1(),
                    resource.getAttributes().getMergedXnode().getXnodeQ1(),
                    resource.getAttributes().getMergedXnode().getXnodeP2(),
                    resource.getAttributes().getMergedXnode().getXnodeQ2(),
                    resource.getAttributes().getMergedXnode().getLine1Name(),
                    resource.getAttributes().getMergedXnode().getLine2Name(),
                    resource.getAttributes().getMergedXnode().getCode());
        }
        return null;
    }

    @Override
    public <E extends Extension<Line>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        MergedXnode extension = createMergedXnode();
        if (extension != null) {
            extensions.add((E) extension);
        }
        return extensions;
    }

    @Override
    public void remove(boolean removeDanglingSwitches) {
        // TODO
        var resource = checkResource();
        index.removeLine(resource.getId());
        getTerminal1().getVoltageLevel().invalidateCalculatedBuses();
        getTerminal2().getVoltageLevel().invalidateCalculatedBuses();
        index.notifyBeforeRemoval(this);
    }
}

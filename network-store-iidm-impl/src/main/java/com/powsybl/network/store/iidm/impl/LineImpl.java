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
        ValidationUtil.checkR(this, r);
        double oldValue = resource.getAttributes().getR();
        resource.getAttributes().setR(r);
        updateResource();
        index.notifyUpdate(this, "r", oldValue, r);
        return this;
    }

    @Override
    public double getX() {
        return resource.getAttributes().getX();
    }

    @Override
    public Line setX(double x) {
        ValidationUtil.checkX(this, x);
        double oldValue = resource.getAttributes().getX();
        resource.getAttributes().setX(x);
        updateResource();
        index.notifyUpdate(this, "x", oldValue, x);
        return this;
    }

    @Override
    public double getG1() {
        return resource.getAttributes().getG1();
    }

    @Override
    public Line setG1(double g1) {
        ValidationUtil.checkG1(this, g1);
        double oldValue = resource.getAttributes().getG1();
        resource.getAttributes().setG1(g1);
        updateResource();
        index.notifyUpdate(this, "g1", oldValue, g1);
        return this;
    }

    @Override
    public double getG2() {
        return resource.getAttributes().getG2();
    }

    @Override
    public Line setG2(double g2) {
        ValidationUtil.checkG2(this, g2);
        double oldValue = resource.getAttributes().getG2();
        resource.getAttributes().setG2(g2);
        updateResource();
        index.notifyUpdate(this, "g2", oldValue, g2);
        return this;
    }

    @Override
    public double getB1() {
        return resource.getAttributes().getB1();
    }

    @Override
    public Line setB1(double b1) {
        ValidationUtil.checkB1(this, b1);
        double oldValue = resource.getAttributes().getB1();
        resource.getAttributes().setB1(b1);
        updateResource();
        index.notifyUpdate(this, "b1", oldValue, b1);
        return this;
    }

    @Override
    public double getB2() {
        return resource.getAttributes().getB2();
    }

    @Override
    public Line setB2(double b2) {
        ValidationUtil.checkB2(this, b2);
        double oldValue = resource.getAttributes().getB2();
        resource.getAttributes().setB2(b2);
        updateResource();
        index.notifyUpdate(this, "b2", oldValue, b2);
        return this;
    }

    @Override
    public <E extends Extension<Line>> void addExtension(Class<? super E> type, E extension) {
        if (type == MergedXnode.class) {
            MergedXnode mergedXnode = (MergedXnode) extension;
            resource.getAttributes().setMergedXnode(
                    MergedXnodeAttributes.builder()
                            .code(mergedXnode.getCode())
                            .rdp(mergedXnode.getRdp())
                            .xdp(mergedXnode.getXdp())
                            .line1Id(mergedXnode.getLine1Name())
                            .line1Name(mergedXnode.getLine1Name())
                            .line1Fictitious(mergedXnode.isLine1Fictitious())
                            .b1dp(mergedXnode.getB1dp())
                            .g1dp(mergedXnode.getG1dp())
                            .line2Id(mergedXnode.getLine2Name())
                            .line2Name(mergedXnode.getLine2Name())
                            .line2Fictitious(mergedXnode.isLine2Fictitious())
                            .b2dp(mergedXnode.getB2dp())
                            .g2dp(mergedXnode.getG2dp())
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
        if (resource.getAttributes().getMergedXnode() != null) {
            return new MergedXnodeImpl(this,
                    resource.getAttributes().getMergedXnode().getRdp(),
                    resource.getAttributes().getMergedXnode().getXdp(),
                    resource.getAttributes().getMergedXnode().getLine1Name(),
                    resource.getAttributes().getMergedXnode().isLine1Fictitious(),
                    0,
                    0,
                    resource.getAttributes().getMergedXnode().getB1dp(),
                    resource.getAttributes().getMergedXnode().getG1dp(),
                    resource.getAttributes().getMergedXnode().getLine2Name(),
                    resource.getAttributes().getMergedXnode().isLine2Fictitious(),
                    0,
                    0,
                    resource.getAttributes().getMergedXnode().getB2dp(),
                    resource.getAttributes().getMergedXnode().getG2dp(),
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
    protected String getTypeDescription() {
        return "AC Line";
    }

    @Override
    public void remove() {
        index.removeLine(resource.getId());
        index.notifyRemoval(this);
    }
}

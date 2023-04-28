/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.TieLineUtil;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TieLineAttributes;

import java.util.*;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

public class TieLineImpl extends AbstractIdentifiableImpl<TieLine, TieLineAttributes> implements TieLine {

    private DanglingLineImpl half1;
    private String danglingLineHalf1;

    private DanglingLineImpl half2;
    private String danglingLineHalf2;

    NetworkObjectIndex index;

    private final Map<Class<?>, Extension<TieLine>> extensions = new HashMap<>();

    private final Map<String, Extension<TieLine>> extensionsByName = new HashMap<>();

    public TieLineImpl(NetworkObjectIndex index, Resource<TieLineAttributes> resource) {
        super(index, resource);
        this.index = index;
        //FIXME Check mergedXNode status
        /*if (resource.getAttributes().getMergedXnode() == null) {
            throw new PowsyblException("A tie line must have MergedXnode extension");
        }*/
        danglingLineHalf1 = resource.getAttributes().getHalf1Id();
        danglingLineHalf2 = resource.getAttributes().getHalf2Id();
    }

    @Override
    public String getUcteXnodeCode() {
        return Optional.ofNullable(getHalf1().getUcteXnodeCode()).orElseGet(() -> getHalf2().getUcteXnodeCode());
    }

    @Override
    public DanglingLineImpl getHalf1() {
        return index.getDanglingLine(danglingLineHalf1).get();
    }

    @Override
    public DanglingLineImpl getHalf2() {
        return index.getDanglingLine(danglingLineHalf2).get();
    }

        /*@Override
        public String getId() {
            var resource = getResource();
            return one ? resource.getAttributes().getMergedXnode().getLine1Name()
                       : resource.getAttributes().getMergedXnode().getLine2Name();
        }*/
    @Override
    public DanglingLine getHalf(Branch.Side side) {
        return null;
    }

    @Override
    public DanglingLine getHalf(String s) {
        return null;
    }

    @Override
    public void remove() {
        var resource = getResource();
        index.notifyBeforeRemoval(this);
        index.removeTieLine(resource.getId());
        index.notifyAfterRemoval(resource.getId());
        /*NetworkImpl network = getNetwork();
        network.getListeners().notifyBeforeRemoval(this);

        // Remove dangling lines
        half1.removeTieLine();
        half2.removeTieLine();

        // Remove this voltage level from the network
        network.getIndex().remove(this);

        network.getListeners().notifyAfterRemoval(id);
        removed = true;*/
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public boolean hasProperty() {
        return false;
    }

    @Override
    public boolean hasProperty(String s) {
        return false;
    }

    @Override
    public String getProperty(String s) {
        return null;
    }

    @Override
    public String getProperty(String s, String s1) {
        return null;
    }

    @Override
    public String setProperty(String s, String s1) {
        return null;
    }

    @Override
    public boolean removeProperty(String s) {
        return false;
    }

    @Override
    public Set<String> getPropertyNames() {
        return null;
    }

    void attachDanglingLines(DanglingLineImpl half1, DanglingLineImpl half2) {
        this.half1 = half1;
        this.half2 = half2;
        this.half1.setParent(this, Branch.Side.ONE);
        this.half2.setParent(this, Branch.Side.TWO);
    }

    @Override
    public <E extends Extension<TieLine>> void addExtension(Class<? super E> aClass, E e) {
        Objects.requireNonNull(aClass);
        Objects.requireNonNull(e);
        e.setExtendable(this);
        extensions.put(aClass, e);
        extensionsByName.put(e.getName(), e);
    }

    @Override
    public <E extends Extension<TieLine>> E getExtension(Class<? super E> aClass) {
        Objects.requireNonNull(aClass);
        return (E) extensions.get(aClass);
    }

    @Override
    public <E extends Extension<TieLine>> E getExtensionByName(String name) {
        Objects.requireNonNull(name);
        return (E) extensionsByName.get(name);
    }

    @Override
    public <E extends Extension<TieLine>> boolean removeExtension(Class<E> aClass) {
        boolean removed = false;

        E extension = getExtension(aClass);
        if (extension != null) {
            extensions.remove(aClass);
            extensionsByName.remove(extension.getName());
            extension.setExtendable(null);
            removed = true;
        }

        return removed;
    }

    @Override
    public Collection<Extension<TieLine>> getExtensions() {
        return extensionsByName.values();
    }

    @Override
    public double getR() {
        DanglingLineImpl dl1 = getHalf1();
        DanglingLineImpl dl2 = getHalf2();
        return TieLineUtil.getR(dl1, dl2);
    }

    @Override
    public double getX() {
        DanglingLineImpl dl1 = getHalf1();
        DanglingLineImpl dl2 = getHalf2();
        return TieLineUtil.getX(dl1, dl2);
    }

    @Override
    public double getG1() {
        DanglingLineImpl dl1 = getHalf1();
        DanglingLineImpl dl2 = getHalf2();
        return TieLineUtil.getG1(dl1, dl2);
    }

    @Override
    public double getG2() {
        DanglingLineImpl dl1 = getHalf1();
        DanglingLineImpl dl2 = getHalf2();
        return TieLineUtil.getG2(dl1, dl2);
    }

    @Override
    public double getB1() {
        DanglingLineImpl dl1 = getHalf1();
        DanglingLineImpl dl2 = getHalf2();
        return TieLineUtil.getB1(dl1, dl2);
    }

    @Override
    public double getB2() {
        DanglingLineImpl dl1 = getHalf1();
        DanglingLineImpl dl2 = getHalf2();
        return TieLineUtil.getB2(dl1, dl2);
    }

    static TieLineImpl create(NetworkObjectIndex index, Resource<TieLineAttributes> resource) {
        return new TieLineImpl(index, resource);
    }
}

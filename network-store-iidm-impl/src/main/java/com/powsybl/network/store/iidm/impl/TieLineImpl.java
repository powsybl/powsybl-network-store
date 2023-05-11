/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
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

    private String danglingLineHalf1;

    private String danglingLineHalf2;

    NetworkObjectIndex index;

    private final Map<Class<?>, Extension<TieLine>> extensions = new HashMap<>();

    private final Map<String, Extension<TieLine>> extensionsByName = new HashMap<>();

    public TieLineImpl(NetworkObjectIndex index, Resource<TieLineAttributes> resource) {
        super(index, resource);
        this.index = index;
        danglingLineHalf1 = resource.getAttributes().getHalf1Id();
        danglingLineHalf2 = resource.getAttributes().getHalf2Id();
    }

    @Override
    public String getUcteXnodeCode() {
        return Optional.ofNullable(getDanglingLine1().getUcteXnodeCode()).orElseGet(() -> getDanglingLine2().getUcteXnodeCode());
    }

    @Override
    public DanglingLine getDanglingLine1() {
        return index.getDanglingLine(danglingLineHalf1).get();
    }

    @Override
    public DanglingLine getDanglingLine2() {
        return index.getDanglingLine(danglingLineHalf2).get();
    }

    @Override
    public DanglingLine getDanglingLine(Branch.Side side) {
        if (Branch.Side.ONE.equals(side)) {
            return getDanglingLine1();
        } else {
            return getDanglingLine2();
        }
    }

    @Override
    public DanglingLine getDanglingLine(String s) {
        if (s.equals(getDanglingLine1().getId())) {
            return getDanglingLine1();
        } else if (s.equals(getDanglingLine2().getId())) {
            return getDanglingLine2();
        } else {
            throw new PowsyblException("Unknown dangling line :" + s);
        }
    }

    @Override
    public void remove() {
        var resource = getResource();
        Optional<DanglingLineImpl> dl1 = index.getDanglingLine(danglingLineHalf1);
        Optional<DanglingLineImpl> dl2 = index.getDanglingLine(danglingLineHalf2);

        dl1.ifPresent(DanglingLineImpl::removeTieLine);
        dl2.ifPresent(DanglingLineImpl::removeTieLine);

        index.notifyBeforeRemoval(this);
        index.removeTieLine(resource.getId());
        index.notifyAfterRemoval(resource.getId());
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
        DanglingLine dl1 = getDanglingLine1();
        DanglingLine dl2 = getDanglingLine2();
        return TieLineUtil.getR(dl1, dl2);
    }

    @Override
    public double getX() {
        DanglingLine dl1 = getDanglingLine1();
        DanglingLine dl2 = getDanglingLine2();
        return TieLineUtil.getX(dl1, dl2);
    }

    @Override
    public double getG1() {
        DanglingLine dl1 = getDanglingLine1();
        DanglingLine dl2 = getDanglingLine2();
        return TieLineUtil.getG1(dl1, dl2);
    }

    @Override
    public double getG2() {
        DanglingLine dl1 = getDanglingLine1();
        DanglingLine dl2 = getDanglingLine2();
        return TieLineUtil.getG2(dl1, dl2);
    }

    @Override
    public double getB1() {
        DanglingLine dl1 = getDanglingLine1();
        DanglingLine dl2 = getDanglingLine2();
        return TieLineUtil.getB1(dl1, dl2);
    }

    @Override
    public double getB2() {
        DanglingLine dl1 = getDanglingLine1();
        DanglingLine dl2 = getDanglingLine2();
        return TieLineUtil.getB2(dl1, dl2);
    }

    static TieLineImpl create(NetworkObjectIndex index, Resource<TieLineAttributes> resource) {
        return new TieLineImpl(index, resource);
    }
}

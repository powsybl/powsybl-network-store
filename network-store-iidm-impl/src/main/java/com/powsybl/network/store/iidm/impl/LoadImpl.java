/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.network.store.iidm.impl.extensions.LoadDetailImpl;
import com.powsybl.network.store.model.LoadAttributes;
import com.powsybl.network.store.model.LoadDetailAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.Collection;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class LoadImpl extends AbstractInjectionImpl<Load, LoadAttributes> implements Load {

    public LoadImpl(NetworkObjectIndex index, Resource<LoadAttributes> resource) {
        super(index, resource);
    }

    static LoadImpl create(NetworkObjectIndex index, Resource<LoadAttributes> resource) {
        return new LoadImpl(index, resource);
    }

    @Override
    protected Load getInjection() {
        return this;
    }

    @Override
    public LoadType getLoadType() {
        return checkResource().getAttributes().getLoadType();
    }

    @Override
    public Load setLoadType(LoadType loadType) {
        var resource = checkResource();
        ValidationUtil.checkLoadType(this, loadType);
        LoadType oldValue = resource.getAttributes().getLoadType();
        resource.getAttributes().setLoadType(loadType);
        updateResource();
        index.notifyUpdate(this, "loadType", oldValue, loadType);
        return this;
    }

    @Override
    public double getP0() {
        return checkResource().getAttributes().getP0();
    }

    @Override
    public Load setP0(double p0) {
        ValidationUtil.checkP0(this, p0, ValidationLevel.STEADY_STATE_HYPOTHESIS);
        var resource = checkResource();
        double oldValue = resource.getAttributes().getP0();
        resource.getAttributes().setP0(p0);
        updateResource();
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "p0", variantId, oldValue, p0);
        return this;
    }

    @Override
    public double getQ0() {
        return checkResource().getAttributes().getQ0();
    }

    @Override
    public Load setQ0(double q0) {
        var resource = checkResource();
        ValidationUtil.checkQ0(this, q0, ValidationLevel.STEADY_STATE_HYPOTHESIS);
        double oldValue = resource.getAttributes().getQ0();
        resource.getAttributes().setQ0(q0);
        updateResource();
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "q0", variantId, oldValue, q0);
        return this;
    }

    @Override
    public <E extends Extension<Load>> void addExtension(Class<? super E> type, E extension) {
        if (type == LoadDetail.class) {
            LoadDetail loadDetail = (LoadDetail) extension;
            checkResource().getAttributes().setLoadDetail(
                    LoadDetailAttributes.builder()
                            .fixedActivePower(loadDetail.getFixedActivePower())
                            .fixedReactivePower(loadDetail.getFixedReactivePower())
                            .variableActivePower(loadDetail.getVariableActivePower())
                            .variableReactivePower(loadDetail.getVariableReactivePower())
                            .build());
        }
        super.addExtension(type, extension);
    }

    @Override
    public <E extends Extension<Load>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        E extension = createLoadDetail();
        if (extension != null) {
            extensions.add(extension);
        }
        return extensions;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Extension<Load>> E getExtension(Class<? super E> type) {
        if (type == LoadDetail.class) {
            return createLoadDetail();
        }
        return super.getExtension(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Extension<Load>> E getExtensionByName(String name) {
        if (name.equals("loadDetail")) {
            return createLoadDetail();
        }
        return super.getExtensionByName(name);
    }

    private <E extends Extension<Load>> E createLoadDetail() {
        E extension = null;
        LoadDetailAttributes attributes = checkResource().getAttributes().getLoadDetail();
        if (attributes != null) {
            extension = (E) new LoadDetailImpl(this);
        }
        return extension;
    }

    public LoadImpl initLoadDetailAttributes(double fixedActivePower, double fixedReactivePower, double variableActivePower, double variableReactivePower) {
        checkResource().getAttributes().setLoadDetail(new LoadDetailAttributes(fixedActivePower, fixedReactivePower, variableActivePower, variableReactivePower));
        updateResource();
        return this;
    }

    @Override
    public void remove(boolean removeDanglingSwitches) {
        var resource = checkResource();
        index.notifyBeforeRemoval(this);
        index.removeLoad(resource.getId());
        invalidateCalculatedBuses(getTerminals());
        index.notifyAfterRemoval(resource.getId());
        if (removeDanglingSwitches) {
            getTerminal().removeDanglingSwitches();
        }
    }

}

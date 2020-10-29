/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.LoadType;
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
    public ConnectableType getType() {
        return ConnectableType.LOAD;
    }

    @Override
    public LoadType getLoadType() {
        return resource.getAttributes().getLoadType();
    }

    @Override
    public Load setLoadType(LoadType loadType) {
        LoadType oldValue = resource.getAttributes().getLoadType();
        resource.getAttributes().setLoadType(loadType);
        index.notifyUpdate(this, "loadType", oldValue, loadType);
        return this;
    }

    @Override
    public double getP0() {
        return resource.getAttributes().getP0();
    }

    @Override
    public Load setP0(double p0) {
        double oldValue = resource.getAttributes().getP0();
        resource.getAttributes().setP0(p0);
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "p0", variantId, oldValue, p0);
        return this;
    }

    @Override
    public double getQ0() {
        return resource.getAttributes().getQ0();
    }

    @Override
    public Load setQ0(double q0) {
        double oldValue = resource.getAttributes().getQ0();
        resource.getAttributes().setQ0(q0);
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "q0", variantId, oldValue, q0);
        return this;
    }

    @Override
    public <E extends Extension<Load>> void addExtension(Class<? super E> type, E extension) {
        if (type == LoadDetail.class) {
            LoadDetail loadDetail = (LoadDetail) extension;
            resource.getAttributes().setLoadDetail(
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
            return (E) createLoadDetail();
        }
        return super.getExtension(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Extension<Load>> E getExtensionByName(String name) {
        if (name.equals("loadDetail")) {
            return (E) createLoadDetail();
        }
        return super.getExtensionByName(name);
    }

    private <E extends Extension<Load>> E createLoadDetail() {
        E extension = null;
        LoadDetailAttributes attributes = resource.getAttributes().getLoadDetail();
        if (attributes != null) {
            extension = (E) new LoadDetailImpl(this);
        }
        return extension;
    }

    public LoadImpl initLoadDetailAttributes(float fixedActivePower, float fixedReactivePower, float variableActivePower, float variableReactivePower) {
        resource.getAttributes().setLoadDetail(new LoadDetailAttributes(fixedActivePower, fixedReactivePower, variableActivePower, variableReactivePower));
        return this;
    }

    @Override
    protected String getTypeDescription() {
        return "Load";
    }

    @Override
    public void remove() {
        index.removeLoad(resource.getId());
    }

}

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
import java.util.Optional;
import java.util.function.Predicate;

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
        return getResource().getAttributes().getLoadType();
    }

    @Override
    public Load setLoadType(LoadType loadType) {
        ValidationUtil.checkLoadType(this, loadType);
        LoadType oldValue = getResource().getAttributes().getLoadType();
        if (loadType != oldValue) {
            updateResource(r -> r.getAttributes().setLoadType(loadType));
            index.notifyUpdate(this, "loadType", oldValue, loadType);
        }
        return this;
    }

    @Override
    public double getP0() {
        return getResource().getAttributes().getP0();
    }

    @Override
    public Load setP0(double p0) {
        ValidationUtil.checkP0(this, p0, ValidationLevel.STEADY_STATE_HYPOTHESIS);
        double oldValue = getResource().getAttributes().getP0();
        if (p0 != oldValue) {
            updateResource(r -> r.getAttributes().setP0(p0));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "p0", variantId, oldValue, p0);
        }
        return this;
    }

    @Override
    public double getQ0() {
        return getResource().getAttributes().getQ0();
    }

    @Override
    public Load setQ0(double q0) {
        ValidationUtil.checkQ0(this, q0, ValidationLevel.STEADY_STATE_HYPOTHESIS);
        double oldValue = getResource().getAttributes().getQ0();
        if (q0 != oldValue) {
            updateResource(r -> r.getAttributes().setQ0(q0));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "q0", variantId, oldValue, q0);
        }
        return this;
    }

    @Override
    public Optional<LoadModel> getModel() {
        return Optional.empty();
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
        LoadDetailAttributes attributes = getResource().getAttributes().getLoadDetail();
        if (attributes != null) {
            extension = (E) new LoadDetailImpl(this);
        }
        return extension;
    }

    @Override
    public void remove() {
        var resource = getResource();
        index.notifyBeforeRemoval(this);
        // invalidate calculated buses before removal otherwise voltage levels won't be accessible anymore for topology invalidation!
        invalidateCalculatedBuses(getTerminals());
        index.removeLoad(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }

    @Override
    public boolean connect() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'connect'");
    }

    @Override
    public boolean connect(Predicate<Switch> isTypeSwitchToOperate) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'connect'");
    }

    @Override
    public boolean disconnect() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'disconnect'");
    }

    @Override
    public boolean disconnect(Predicate<Switch> isSwitchOpenable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'disconnect'");
    }

}

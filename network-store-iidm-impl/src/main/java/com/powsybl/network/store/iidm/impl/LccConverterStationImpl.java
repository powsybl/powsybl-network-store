/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.network.store.iidm.impl.extensions.ActivePowerControlImpl;
import com.powsybl.network.store.model.ActivePowerControlAttributes;
import com.powsybl.network.store.model.LccConverterStationAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.Collection;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class LccConverterStationImpl extends AbstractHvdcConverterStationImpl<LccConverterStation, LccConverterStationAttributes> implements LccConverterStation {

    public LccConverterStationImpl(NetworkObjectIndex index, Resource<LccConverterStationAttributes> resource) {
        super(index, resource);
    }

    static LccConverterStationImpl create(NetworkObjectIndex index, Resource<LccConverterStationAttributes> resource) {
        return new LccConverterStationImpl(index, resource);
    }

    @Override
    protected LccConverterStation getInjection() {
        return this;
    }

    @Override
    public HvdcType getHvdcType() {
        return HvdcType.LCC;
    }

    @Override
    public float getPowerFactor() {
        return resource.getAttributes().getPowerFactor();
    }

    @Override
    public LccConverterStation setPowerFactor(float powerFactor) {
        ValidationUtil.checkPowerFactor(this, powerFactor);
        float oldValue = resource.getAttributes().getPowerFactor();
        resource.getAttributes().setPowerFactor(powerFactor);
        index.notifyUpdate(this, "powerFactor", oldValue, powerFactor);
        return this;
    }

    @Override
    public float getLossFactor() {
        return resource.getAttributes().getLossFactor();
    }

    @Override
    public LccConverterStation setLossFactor(float lossFactor) {
        ValidationUtil.checkLossFactor(this, lossFactor);
        float oldValue = resource.getAttributes().getLossFactor();
        resource.getAttributes().setLossFactor(lossFactor);
        index.notifyUpdate(this, "lossFactor", oldValue, lossFactor);
        return this;
    }

    @Override
    protected String getTypeDescription() {
        return "lccConverterStation";
    }

    @Override
    public void remove() {
        index.removeLccConverterStation(resource.getId());
        index.notifyRemoval(this);
    }

    private <E extends Extension<LccConverterStation>> E createActivePowerControlExtension() {
        E extension = null;
        ActivePowerControlAttributes attributes = resource.getAttributes().getActivePowerControl();
        if (attributes != null) {
            extension = (E) new ActivePowerControlImpl<>(getInjection(), attributes.isParticipate(), attributes.getDroop());
        }
        return extension;
    }

    @Override
    public <E extends Extension<LccConverterStation>> void addExtension(Class<? super E> type, E extension) {
        super.addExtension(type, extension);
        if (type == ActivePowerControl.class) {
            ActivePowerControl<LccConverterStation> activePowerControl = (ActivePowerControl) extension;
            resource.getAttributes().setActivePowerControl(ActivePowerControlAttributes.builder()
                    .participate(activePowerControl.isParticipate())
                    .droop(activePowerControl.getDroop())
                    .build());
        }
    }

    @Override
    public <E extends Extension<LccConverterStation>> E getExtension(Class<? super E> type) {
        if (type == ActivePowerControl.class) {
            return createActivePowerControlExtension();
        }
        return super.getExtension(type);
    }

    @Override
    public <E extends Extension<LccConverterStation>> E getExtensionByName(String name) {
        if (name.equals("activePowerControl")) {
            return createActivePowerControlExtension();
        }
        return super.getExtensionByName(name);
    }

    @Override
    public <E extends Extension<LccConverterStation>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        E extension = createActivePowerControlExtension();
        if (extension != null) {
            extensions.add(extension);
        }
        return extensions;
    }
}

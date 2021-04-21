/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRange;
import com.powsybl.network.store.iidm.impl.extensions.HvdcAngleDroopActivePowerControlImpl;
import com.powsybl.network.store.iidm.impl.extensions.HvdcOperatorActivePowerRangeImpl;
import com.powsybl.network.store.model.HvdcAngleDroopActivePowerControlAttributes;
import com.powsybl.network.store.model.HvdcLineAttributes;
import com.powsybl.network.store.model.HvdcOperatorActivePowerRangeAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.Collection;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class HvdcLineImpl extends AbstractIdentifiableImpl<HvdcLine, HvdcLineAttributes> implements HvdcLine {

    public HvdcLineImpl(NetworkObjectIndex index, Resource<HvdcLineAttributes> resource) {
        super(index, resource);
    }

    static HvdcLineImpl create(NetworkObjectIndex index, Resource<HvdcLineAttributes> resource) {
        return new HvdcLineImpl(index, resource);
    }

    @Override
    public HvdcConverterStation<?> getConverterStation1() {
        return resource.getAttributes().getConverterStationId1() != null ? index.getHvdcConverterStation(resource.getAttributes().getConverterStationId1()).orElse(null) : null;
    }

    @Override
    public HvdcConverterStation<?> getConverterStation2() {
        return resource.getAttributes().getConverterStationId2() != null ? index.getHvdcConverterStation(resource.getAttributes().getConverterStationId2()).orElse(null) : null;
    }

    @Override
    public void remove() {
        resource.getAttributes().setConverterStationId1(null);
        resource.getAttributes().setConverterStationId2(null);

        index.removeHvdcLine(resource.getId());
        index.notifyRemoval(this);
    }

    @Override
    public HvdcConverterStation<?> getConverterStation(Side side) {
        switch (side) {
            case ONE:
                return getConverterStation1();
            case TWO:
                return getConverterStation2();
            default:
                throw new IllegalStateException("Unknown side: " + side);
        }
    }

    @Override
    public ConvertersMode getConvertersMode() {
        return resource.getAttributes().getConvertersMode();
    }

    @Override
    public HvdcLine setConvertersMode(ConvertersMode mode) {
        ValidationUtil.checkConvertersMode(this, mode);
        ConvertersMode oldValue = resource.getAttributes().getConvertersMode();
        resource.getAttributes().setConvertersMode(mode);
        updateResource();
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "convertersMode", variantId, oldValue, mode);
        return this;
    }

    @Override
    public double getR() {
        return resource.getAttributes().getR();
    }

    @Override
    public HvdcLine setR(double r) {
        ValidationUtil.checkR(this, r);
        double oldValue = resource.getAttributes().getR();
        resource.getAttributes().setR(r);
        updateResource();
        index.notifyUpdate(this, "r", oldValue, r);
        return this;
    }

    @Override
    public double getNominalV() {
        return resource.getAttributes().getNominalV();
    }

    @Override
    public HvdcLine setNominalV(double nominalV) {
        ValidationUtil.checkNominalV(this, nominalV);
        double oldValue = resource.getAttributes().getNominalV();
        resource.getAttributes().setNominalV(nominalV);
        updateResource();
        index.notifyUpdate(this, "nominalV", oldValue, nominalV);
        return this;
    }

    @Override
    public double getActivePowerSetpoint() {
        return resource.getAttributes().getActivePowerSetpoint();
    }

    @Override
    public HvdcLine setActivePowerSetpoint(double activePowerSetpoint) {
        ValidationUtil.checkHvdcActivePowerSetpoint(this, activePowerSetpoint);
        double oldValue = resource.getAttributes().getActivePowerSetpoint();
        resource.getAttributes().setActivePowerSetpoint(activePowerSetpoint);
        updateResource();
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "activePowerSetpoint", variantId, oldValue, activePowerSetpoint);
        return this;
    }

    @Override
    public double getMaxP() {
        return resource.getAttributes().getMaxP();
    }

    @Override
    public HvdcLine setMaxP(double maxP) {
        ValidationUtil.checkHvdcMaxP(this, maxP);
        double oldValue = resource.getAttributes().getMaxP();
        resource.getAttributes().setMaxP(maxP);
        updateResource();
        index.notifyUpdate(this, "maxP", oldValue, maxP);
        return this;
    }

    @Override
    public <E extends Extension<HvdcLine>> void addExtension(Class<? super E> type, E extension) {
        if (type == HvdcAngleDroopActivePowerControl.class) {
            HvdcAngleDroopActivePowerControl hvdcAngleDroopActivePowerControl = (HvdcAngleDroopActivePowerControl) extension;
            resource.getAttributes().setHvdcAngleDroopActivePowerControl(
                HvdcAngleDroopActivePowerControlAttributes.builder()
                    .p0(hvdcAngleDroopActivePowerControl.getP0())
                    .droop(hvdcAngleDroopActivePowerControl.getDroop())
                    .enabled(hvdcAngleDroopActivePowerControl.isEnabled())
                    .build());
        } else if (type == HvdcOperatorActivePowerRange.class) {
            HvdcOperatorActivePowerRange hvdcOperatorActivePowerRange = (HvdcOperatorActivePowerRange) extension;
            resource.getAttributes().setHvdcOperatorActivePowerRange(
                HvdcOperatorActivePowerRangeAttributes.builder()
                    .oprFromCS1toCS2(hvdcOperatorActivePowerRange.getOprFromCS1toCS2())
                    .oprFromCS2toCS1(hvdcOperatorActivePowerRange.getOprFromCS2toCS1())
                    .build());
        }
        super.addExtension(type, extension);
    }

    @Override
    public <E extends Extension<HvdcLine>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        E extension = createHvdcAngleDroopActivePowerControl();
        if (extension != null) {
            extensions.add(extension);
        }
        extension = createHvdcOperatorActivePowerRange();
        if (extension != null) {
            extensions.add(extension);
        }
        return extensions;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Extension<HvdcLine>> E getExtension(Class<? super E> type) {
        if (type == HvdcAngleDroopActivePowerControl.class) {
            return createHvdcAngleDroopActivePowerControl();
        } else if (type == HvdcOperatorActivePowerRange.class) {
            return createHvdcOperatorActivePowerRange();
        }
        return super.getExtension(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Extension<HvdcLine>> E getExtensionByName(String name) {
        if (name.equals("hvdcAngleDroopActivePowerControl")) {
            return createHvdcAngleDroopActivePowerControl();
        } else if (name.equals("hvdcOperatorActivePowerRange")) {
            return createHvdcOperatorActivePowerRange();
        }
        return super.getExtensionByName(name);
    }

    private <E extends Extension<HvdcLine>> E createHvdcAngleDroopActivePowerControl() {
        E extension = null;
        HvdcAngleDroopActivePowerControlAttributes attributes = resource.getAttributes().getHvdcAngleDroopActivePowerControl();
        if (attributes != null) {
            extension = (E) new HvdcAngleDroopActivePowerControlImpl(this);
        }
        return extension;
    }

    private <E extends Extension<HvdcLine>> E createHvdcOperatorActivePowerRange() {
        E extension = null;
        HvdcOperatorActivePowerRangeAttributes attributes = resource.getAttributes().getHvdcOperatorActivePowerRange();
        if (attributes != null) {
            extension = (E) new HvdcOperatorActivePowerRangeImpl(this);
        }
        return extension;
    }

    public HvdcLineImpl initHvdcAngleDroopActivePowerControlAttributes(float p0, float droop, boolean enabled) {
        resource.getAttributes().setHvdcAngleDroopActivePowerControl(new HvdcAngleDroopActivePowerControlAttributes(p0, droop, enabled));
        updateResource();
        return this;
    }

    public HvdcLineImpl initHvdcOperatorActivePowerRangeAttributes(float oprFromCS1toCS2, float oprFromCS2toCS1) {
        resource.getAttributes().setHvdcOperatorActivePowerRange(new HvdcOperatorActivePowerRangeAttributes(oprFromCS1toCS2, oprFromCS2toCS1));
        updateResource();
        return this;
    }

    @Override
    protected String getTypeDescription() {
        return "hvdcLine";
    }
}

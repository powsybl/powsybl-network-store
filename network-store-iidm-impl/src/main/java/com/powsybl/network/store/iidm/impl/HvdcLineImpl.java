/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRange;
import com.powsybl.iidm.network.util.SwitchPredicates;
import com.powsybl.network.store.iidm.impl.extensions.HvdcAngleDroopActivePowerControlImpl;
import com.powsybl.network.store.iidm.impl.extensions.HvdcOperatorActivePowerRangeImpl;
import com.powsybl.network.store.model.HvdcAngleDroopActivePowerControlAttributes;
import com.powsybl.network.store.model.HvdcLineAttributes;
import com.powsybl.network.store.model.HvdcOperatorActivePowerRangeAttributes;
import com.powsybl.network.store.model.Resource;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

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
        var resource = getResource();
        return resource.getAttributes().getConverterStationId1() != null ? index.getHvdcConverterStation(resource.getAttributes().getConverterStationId1()).orElse(null) : null;
    }

    @Override
    public HvdcConverterStation<?> getConverterStation2() {
        var resource = getResource();
        return resource.getAttributes().getConverterStationId2() != null ? index.getHvdcConverterStation(resource.getAttributes().getConverterStationId2()).orElse(null) : null;
    }

    @Override
    public void remove() {
        var resource = getResource();
        index.notifyBeforeRemoval(this);
        resource.getAttributes().setConverterStationId1(null);
        resource.getAttributes().setConverterStationId2(null);
        index.removeHvdcLine(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }

    @Override
    public HvdcConverterStation<?> getConverterStation(TwoSides side) {
        return switch (side) {
            case ONE -> getConverterStation1();
            case TWO -> getConverterStation2();
        };
    }

    @Override
    public ConvertersMode getConvertersMode() {
        return getResource().getAttributes().getConvertersMode();
    }

    @Override
    public HvdcLine setConvertersMode(ConvertersMode mode) {
        ValidationUtil.checkConvertersMode(this, mode, ValidationLevel.STEADY_STATE_HYPOTHESIS, getNetwork().getReportNodeContext().getReportNode());
        ConvertersMode oldValue = getResource().getAttributes().getConvertersMode();
        if (mode != oldValue) {
            updateResource(res -> res.getAttributes().setConvertersMode(mode));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "convertersMode", variantId, oldValue, mode);
        }
        return this;
    }

    @Override
    public double getR() {
        return getResource().getAttributes().getR();
    }

    @Override
    public HvdcLine setR(double r) {
        ValidationUtil.checkR(this, r);
        double oldValue = getResource().getAttributes().getR();
        if (r != oldValue) {
            updateResource(res -> res.getAttributes().setR(r));
            index.notifyUpdate(this, "r", oldValue, r);
        }
        return this;
    }

    @Override
    public double getNominalV() {
        return getResource().getAttributes().getNominalV();
    }

    @Override
    public HvdcLine setNominalV(double nominalV) {
        ValidationUtil.checkNominalV(this, nominalV);
        double oldValue = getResource().getAttributes().getNominalV();
        if (nominalV != oldValue) {
            updateResource(res -> res.getAttributes().setNominalV(nominalV));
            index.notifyUpdate(this, "nominalV", oldValue, nominalV);
        }
        return this;
    }

    @Override
    public double getActivePowerSetpoint() {
        return getResource().getAttributes().getActivePowerSetpoint();
    }

    @Override
    public HvdcLine setActivePowerSetpoint(double activePowerSetpoint) {
        ValidationUtil.checkHvdcActivePowerSetpoint(this, activePowerSetpoint, ValidationLevel.STEADY_STATE_HYPOTHESIS, getNetwork().getReportNodeContext().getReportNode());
        double oldValue = getResource().getAttributes().getActivePowerSetpoint();
        if (activePowerSetpoint != oldValue) {
            updateResource(res -> res.getAttributes().setActivePowerSetpoint(activePowerSetpoint));
            String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
            index.notifyUpdate(this, "activePowerSetpoint", variantId, oldValue, activePowerSetpoint);
        }
        return this;
    }

    @Override
    public double getMaxP() {
        return getResource().getAttributes().getMaxP();
    }

    @Override
    public HvdcLine setMaxP(double maxP) {
        ValidationUtil.checkHvdcMaxP(this, maxP);
        double oldValue = getResource().getAttributes().getMaxP();
        if (maxP != oldValue) {
            updateResource(res -> res.getAttributes().setMaxP(maxP));
            index.notifyUpdate(this, "maxP", oldValue, maxP);
        }
        return this;
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
        var resource = getResource();
        HvdcAngleDroopActivePowerControlAttributes attributes = resource.getAttributes().getHvdcAngleDroopActivePowerControl();
        if (attributes != null) {
            extension = (E) new HvdcAngleDroopActivePowerControlImpl(this);
        }
        return extension;
    }

    private <E extends Extension<HvdcLine>> E createHvdcOperatorActivePowerRange() {
        E extension = null;
        var resource = getResource();
        HvdcOperatorActivePowerRangeAttributes attributes = resource.getAttributes().getHvdcOperatorActivePowerRange();
        if (attributes != null) {
            extension = (E) new HvdcOperatorActivePowerRangeImpl(this);
        }
        return extension;
    }

    @Override
    public boolean connectConverterStations() {
        return connectConverterStations(SwitchPredicates.IS_NONFICTIONAL_BREAKER, null);
    }

    @Override
    public boolean connectConverterStations(Predicate<Switch> isTypeSwitchToOperate) {
        return connectConverterStations(isTypeSwitchToOperate, null);
    }

    @Override
    public boolean connectConverterStations(Predicate<Switch> isTypeSwitchToOperate, TwoSides side) {
        return ConnectDisconnectUtil.connectAllTerminals(
            this,
            getTerminalsOfConverterStations(side),
            isTypeSwitchToOperate,
            getNetwork().getReportNodeContext().getReportNode());
    }

    @Override
    public boolean disconnectConverterStations() {
        return disconnectConverterStations(SwitchPredicates.IS_CLOSED_BREAKER, null);
    }

    @Override
    public boolean disconnectConverterStations(Predicate<Switch> isSwitchOpenable) {
        return disconnectConverterStations(isSwitchOpenable, null);
    }

    @Override
    public boolean disconnectConverterStations(Predicate<Switch> isSwitchOpenable, TwoSides side) {
        return ConnectDisconnectUtil.disconnectAllTerminals(
            this,
            getTerminalsOfConverterStations(side),
            isSwitchOpenable,
            getNetwork().getReportNodeContext().getReportNode());
    }

    public List<Terminal> getTerminalsOfConverterStations(TwoSides side) {
        return side == null ?
        List.of(getConverterStation1().getTerminal(), getConverterStation2().getTerminal()) :
        switch (side) {
            case ONE -> List.of(getConverterStation1().getTerminal());
            case TWO -> List.of(getConverterStation2().getTerminal());
        };
    }
}

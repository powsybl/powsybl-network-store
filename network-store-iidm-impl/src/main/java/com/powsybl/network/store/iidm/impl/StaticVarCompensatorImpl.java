/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;
import com.powsybl.network.store.iidm.impl.extensions.VoltagePerReactivePowerControlImpl;
import com.powsybl.network.store.model.*;

import java.util.Collection;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class StaticVarCompensatorImpl extends AbstractInjectionImpl<StaticVarCompensator, StaticVarCompensatorAttributes> implements StaticVarCompensator {

    public StaticVarCompensatorImpl(NetworkObjectIndex index, Resource<StaticVarCompensatorAttributes> resource) {
        super(index, resource);
    }

    static StaticVarCompensatorImpl create(NetworkObjectIndex index, Resource<StaticVarCompensatorAttributes> resource) {
        return new StaticVarCompensatorImpl(index, resource);
    }

    @Override
    protected StaticVarCompensator getInjection() {
        return this;
    }

    @Override
    public double getBmin() {
        return checkResource().getAttributes().getBmin();
    }

    @Override
    public StaticVarCompensator setBmin(double bMin) {
        var resource = checkResource();
        ValidationUtil.checkBmin(this, bMin);
        double oldValue = resource.getAttributes().getBmin();
        resource.getAttributes().setBmin(bMin);
        updateResource();
        index.notifyUpdate(this, "bMin", oldValue, bMin);
        return this;
    }

    @Override
    public double getBmax() {
        return checkResource().getAttributes().getBmax();
    }

    @Override
    public StaticVarCompensator setBmax(double bMax) {
        var resource = checkResource();
        ValidationUtil.checkBmax(this, bMax);
        double oldValue = resource.getAttributes().getBmax();
        resource.getAttributes().setBmax(bMax);
        updateResource();
        index.notifyUpdate(this, "bMax", oldValue, bMax);
        return this;
    }

    @Override
    public double getVoltageSetpoint() {
        return checkResource().getAttributes().getVoltageSetPoint();
    }

    @Override
    public StaticVarCompensator setVoltageSetpoint(double voltageSetPoint) {
        var resource = checkResource();
        ValidationUtil.checkSvcRegulator(this, voltageSetPoint, getReactivePowerSetpoint(), getRegulationMode());
        double oldValue = resource.getAttributes().getVoltageSetPoint();
        resource.getAttributes().setVoltageSetPoint(voltageSetPoint);
        updateResource();
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "voltageSetpoint", variantId, oldValue, voltageSetPoint);
        return this;
    }

    @Override
    public double getReactivePowerSetpoint() {
        return checkResource().getAttributes().getReactivePowerSetPoint();
    }

    @Override
    public StaticVarCompensator setReactivePowerSetpoint(double reactivePowerSetPoint) {
        var resource = checkResource();
        ValidationUtil.checkSvcRegulator(this, getVoltageSetpoint(), reactivePowerSetPoint, getRegulationMode());
        double oldValue = resource.getAttributes().getReactivePowerSetPoint();
        resource.getAttributes().setReactivePowerSetPoint(reactivePowerSetPoint);
        updateResource();
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "reactivePowerSetpoint", variantId, oldValue, reactivePowerSetPoint);
        return this;
    }

    @Override
    public RegulationMode getRegulationMode() {
        return checkResource().getAttributes().getRegulationMode();
    }

    @Override
    public StaticVarCompensator setRegulationMode(RegulationMode regulationMode) {
        var resource = checkResource();
        ValidationUtil.checkSvcRegulator(this, getVoltageSetpoint(), getReactivePowerSetpoint(), regulationMode);
        RegulationMode oldValue = resource.getAttributes().getRegulationMode();
        resource.getAttributes().setRegulationMode(regulationMode);
        updateResource();
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "regulationMode", variantId, oldValue, regulationMode);
        return this;
    }

    @Override
    public Terminal getRegulatingTerminal() {
        var resource = checkResource();
        TerminalRefAttributes terminalRefAttributes = resource.getAttributes().getRegulatingTerminal();
        Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, terminalRefAttributes);
        return regulatingTerminal != null ? regulatingTerminal : terminal;
    }

    @Override
    public StaticVarCompensator setRegulatingTerminal(Terminal regulatingTerminal) {
        var resource = checkResource();
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        resource.getAttributes().setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal));
        updateResource();
        return this;
    }

    private <E extends Extension<StaticVarCompensator>> E createVoltagePerReactiveControlExtension() {
        E extension = null;
        var resource = checkResource();
        VoltagePerReactivePowerControlAttributes attributes = resource.getAttributes().getVoltagePerReactiveControl();
        if (attributes != null) {
            extension = (E) new VoltagePerReactivePowerControlImpl((StaticVarCompensatorImpl) getInjection(), attributes.getSlope());
        }
        return extension;
    }

    @Override
    public <E extends Extension<StaticVarCompensator>> void addExtension(Class<? super E> type, E extension) {
        if (type == VoltagePerReactivePowerControl.class) {
            var resource = checkResource();
            resource.getAttributes().setVoltagePerReactiveControl(VoltagePerReactivePowerControlAttributes.builder()
                    .slope(((VoltagePerReactivePowerControl) extension).getSlope())
                    .build());
        } else {
            super.addExtension(type, extension);
        }
    }

    @Override
    public <E extends Extension<StaticVarCompensator>> E getExtension(Class<? super E> type) {
        if (type == VoltagePerReactivePowerControl.class) {
            return createVoltagePerReactiveControlExtension();
        }
        return super.getExtension(type);
    }

    @Override
    public <E extends Extension<StaticVarCompensator>> E getExtensionByName(String name) {
        if (name.equals("voltagePerReactivePowerControl")) {
            return createVoltagePerReactiveControlExtension();
        }
        return super.getExtensionByName(name);
    }

    @Override
    public <E extends Extension<StaticVarCompensator>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        E extension = createVoltagePerReactiveControlExtension();
        if (extension != null) {
            extensions.add(extension);
        }
        return extensions;
    }

    @Override
    public void remove(boolean removeDanglingSwitches) {
        var resource = checkResource();
        index.notifyBeforeRemoval(this);
        index.removeStaticVarCompensator(resource.getId());
        getTerminal().getVoltageLevel().invalidateCalculatedBuses();
        index.notifyAfterRemoval(resource.getId());
        if (removeDanglingSwitches) {
            getTerminal().removeDanglingSwitches();
        }
    }
}

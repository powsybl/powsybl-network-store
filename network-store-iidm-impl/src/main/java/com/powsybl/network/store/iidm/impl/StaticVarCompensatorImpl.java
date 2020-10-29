/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControlImpl;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.StaticVarCompensatorAttributes;
import com.powsybl.network.store.model.TerminalRefAttributes;
import com.powsybl.network.store.model.VoltagePerReactivePowerControlAttributes;

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
    public ConnectableType getType() {
        return ConnectableType.STATIC_VAR_COMPENSATOR;
    }

    @Override
    public double getBmin() {
        return resource.getAttributes().getBmin();
    }

    @Override
    public StaticVarCompensator setBmin(double bMin) {
        double oldValue = resource.getAttributes().getBmin();
        resource.getAttributes().setBmin(bMin);
        index.notifyUpdate(this, "bMin", oldValue, bMin);
        return this;
    }

    @Override
    public double getBmax() {
        return resource.getAttributes().getBmax();
    }

    @Override
    public StaticVarCompensator setBmax(double bMax) {
        double oldValue = resource.getAttributes().getBmax();
        resource.getAttributes().setBmax(bMax);
        index.notifyUpdate(this, "bMax", oldValue, bMax);
        return this;
    }

    @Override
    public double getVoltageSetpoint() {
        return resource.getAttributes().getVoltageSetPoint();
    }

    @Override
    public StaticVarCompensator setVoltageSetpoint(double voltageSetPoint) {
        double oldValue = resource.getAttributes().getVoltageSetPoint();
        resource.getAttributes().setVoltageSetPoint(voltageSetPoint);
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "voltageSetpoint", variantId, oldValue, voltageSetPoint);
        return this;
    }

    @Override
    public double getReactivePowerSetpoint() {
        return resource.getAttributes().getReactivePowerSetPoint();
    }

    @Override
    public StaticVarCompensator setReactivePowerSetpoint(double reactivePowerSetPoint) {
        double oldValue = resource.getAttributes().getReactivePowerSetPoint();
        resource.getAttributes().setReactivePowerSetPoint(reactivePowerSetPoint);
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "reactivePowerSetpoint", variantId, oldValue, reactivePowerSetPoint);
        return this;
    }

    @Override
    public RegulationMode getRegulationMode() {
        return resource.getAttributes().getRegulationMode();
    }

    @Override
    public StaticVarCompensator setRegulationMode(RegulationMode regulationMode) {
        RegulationMode oldValue = resource.getAttributes().getRegulationMode();
        resource.getAttributes().setRegulationMode(regulationMode);
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "regulationMode", variantId, oldValue, regulationMode);
        return this;
    }

    @Override
    public Terminal getRegulatingTerminal() {
        TerminalRefAttributes terminalRefAttributes = resource.getAttributes().getRegulatingTerminal();
        Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, terminalRefAttributes);
        return regulatingTerminal != null ? regulatingTerminal : terminal;
    }

    @Override
    public StaticVarCompensator setRegulatingTerminal(Terminal regulatingTerminal) {
        resource.getAttributes().setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal));
        return this;
    }

    private <E extends Extension<StaticVarCompensator>> E createVoltagePerReactiveControlExtension() {
        E extension = null;
        VoltagePerReactivePowerControlAttributes attributes = resource.getAttributes().getVoltagePerReactiveControl();
        if (attributes != null) {
            extension = (E) new VoltagePerReactivePowerControlImpl(getInjection(), attributes.getSlope());
        }
        return extension;
    }

    @Override
    public <E extends Extension<StaticVarCompensator>> void addExtension(Class<? super E> type, E extension) {
        if (type == VoltagePerReactivePowerControl.class) {
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

    protected String getTypeDescription() {
        return "staticVarCompensator";
    }

    @Override
    public void remove() {
        index.removeStaticVarCompensator(resource.getId());
    }
}

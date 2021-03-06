/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
import com.powsybl.network.store.iidm.impl.extensions.ActivePowerControlImpl;
import com.powsybl.network.store.iidm.impl.extensions.CoordinatedReactiveControlImpl;
import com.powsybl.network.store.model.*;

import java.util.Collection;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class GeneratorImpl extends AbstractInjectionImpl<Generator, GeneratorAttributes> implements Generator, ReactiveLimitsOwner {

    public GeneratorImpl(NetworkObjectIndex index, Resource<GeneratorAttributes> resource) {
        super(index, resource);
    }

    static GeneratorImpl create(NetworkObjectIndex index, Resource<GeneratorAttributes> resource) {
        return new GeneratorImpl(index, resource);
    }

    @Override
    protected Generator getInjection() {
        return this;
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.GENERATOR;
    }

    @Override
    public EnergySource getEnergySource() {
        return resource.getAttributes().getEnergySource();
    }

    @Override
    public Generator setEnergySource(EnergySource energySource) {
        ValidationUtil.checkEnergySource(this, energySource);
        EnergySource oldValue = resource.getAttributes().getEnergySource();
        resource.getAttributes().setEnergySource(energySource);
        updateResource();
        index.notifyUpdate(this, "energySource", oldValue.toString(), energySource.toString());
        return this;
    }

    @Override
    public double getMaxP() {
        return resource.getAttributes().getMaxP();
    }

    @Override
    public Generator setMaxP(double maxP) {
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActivePowerLimits(this, getMinP(), maxP);
        double oldValue = resource.getAttributes().getMaxP();
        resource.getAttributes().setMaxP(maxP);
        updateResource();
        index.notifyUpdate(this, "maxP", oldValue, maxP);
        return this;
    }

    @Override
    public double getMinP() {
        return resource.getAttributes().getMinP();
    }

    @Override
    public Generator setMinP(double minP) {
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkActivePowerLimits(this, minP, getMaxP());
        double oldValue = resource.getAttributes().getMinP();
        resource.getAttributes().setMinP(minP);
        updateResource();
        index.notifyUpdate(this, "minP", oldValue, minP);
        return this;
    }

    @Override
    public void remove() {
        index.removeGenerator(resource.getId());
        index.notifyRemoval(this);
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return resource.getAttributes().isVoltageRegulatorOn();
    }

    @Override
    public Generator setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, getTargetV(), getTargetQ());
        boolean oldValue = resource.getAttributes().isVoltageRegulatorOn();
        resource.getAttributes().setVoltageRegulatorOn(voltageRegulatorOn);
        updateResource();
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "voltageRegulatorOn", variantId, oldValue, voltageRegulatorOn);
        return this;
    }

    @Override
    public Terminal getRegulatingTerminal() {
        TerminalRefAttributes terminalRefAttributes = resource.getAttributes().getRegulatingTerminal();
        Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, terminalRefAttributes);
        return regulatingTerminal != null ? regulatingTerminal : terminal;
    }

    @Override
    public Generator setRegulatingTerminal(Terminal regulatingTerminal) {
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        TerminalRefAttributes oldValue = resource.getAttributes().getRegulatingTerminal();
        resource.getAttributes().setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal));
        updateResource();
        index.notifyUpdate(this, "regulatingTerminal", oldValue, TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal));
        return this;
    }

    @Override
    public double getTargetV() {
        return resource.getAttributes().getTargetV();
    }

    @Override
    public Generator setTargetV(double targetV) {
        ValidationUtil.checkVoltageControl(this, isVoltageRegulatorOn(), targetV, getTargetQ());
        double oldValue = resource.getAttributes().getTargetV();
        resource.getAttributes().setTargetV(targetV);
        updateResource();
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "targetV", variantId, oldValue, targetV);
        return this;
    }

    @Override
    public double getTargetP() {
        return resource.getAttributes().getTargetP();
    }

    @Override
    public Generator setTargetP(double targetP) {
        ValidationUtil.checkActivePowerSetpoint(this, targetP);
        double oldValue = resource.getAttributes().getTargetP();
        resource.getAttributes().setTargetP(targetP);
        updateResource();
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "targetP", variantId, oldValue, targetP);
        return this;
    }

    @Override
    public double getTargetQ() {
        return resource.getAttributes().getTargetQ();
    }

    @Override
    public Generator setTargetQ(double targetQ) {
        ValidationUtil.checkVoltageControl(this, isVoltageRegulatorOn(), getTargetV(), targetQ);
        double oldValue = resource.getAttributes().getTargetQ();
        resource.getAttributes().setTargetQ(targetQ);
        updateResource();
        String variantId = index.getNetwork().getVariantManager().getWorkingVariantId();
        index.notifyUpdate(this, "targetQ", variantId, oldValue, targetQ);
        return this;
    }

    @Override
    public double getRatedS() {
        return resource.getAttributes().getRatedS();
    }

    @Override
    public Generator setRatedS(double ratedS) {
        ValidationUtil.checkRatedS(this, ratedS);
        double oldValue = resource.getAttributes().getRatedS();
        resource.getAttributes().setRatedS(ratedS);
        updateResource();
        index.notifyUpdate(this, "ratedS", oldValue, ratedS);
        return this;
    }

    @Override
    public void setReactiveLimits(ReactiveLimitsAttributes reactiveLimits) {
        resource.getAttributes().setReactiveLimits(reactiveLimits);
        updateResource();
    }

    @Override
    public ReactiveLimits getReactiveLimits() {
        ReactiveLimitsAttributes reactiveLimitsAttributes = resource.getAttributes().getReactiveLimits();
        if (reactiveLimitsAttributes.getKind() == ReactiveLimitsKind.CURVE) {
            return new ReactiveCapabilityCurveImpl((ReactiveCapabilityCurveAttributes) reactiveLimitsAttributes);
        } else {
            return new MinMaxReactiveLimitsImpl((MinMaxReactiveLimitsAttributes) reactiveLimitsAttributes);
        }
    }

    @Override
    public <L extends ReactiveLimits> L getReactiveLimits(Class<L> type) {
        ReactiveLimits reactiveLimits = getReactiveLimits();
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }
        if (type.isInstance(reactiveLimits)) {
            return type.cast(reactiveLimits);
        } else {
            throw new PowsyblException("incorrect reactive limits type "
                    + type.getName() + ", expected " + reactiveLimits.getClass());
        }
    }

    @Override
    public ReactiveCapabilityCurveAdder newReactiveCapabilityCurve() {
        return new ReactiveCapabilityCurveAdderImpl(this);
    }

    @Override
    public MinMaxReactiveLimitsAdder newMinMaxReactiveLimits() {
        return new MinMaxReactiveLimitsAdderImpl(this);
    }

    @Override
    public <E extends Extension<Generator>> void addExtension(Class<? super E> type, E extension) {
        super.addExtension(type, extension);
        if (type == ActivePowerControl.class) {
            ActivePowerControl activePowerControl = (ActivePowerControl) extension;
            resource.getAttributes().setActivePowerControl(ActivePowerControlAttributes.builder()
                    .participate(activePowerControl.isParticipate())
                    .droop(activePowerControl.getDroop())
                    .build());
            updateResource();
        } else if (type == CoordinatedReactiveControl.class) {
            CoordinatedReactiveControl coordinatedReactiveControl = (CoordinatedReactiveControl) extension;
            resource.getAttributes().setCoordinatedReactiveControl(CoordinatedReactiveControlAttributes.builder()
                    .qPercent(coordinatedReactiveControl.getQPercent())
                    .build());
            updateResource();
        }
    }

    private <E extends Extension<Generator>> E createActivePowerControlExtension() {
        E extension = null;
        ActivePowerControlAttributes attributes = resource.getAttributes().getActivePowerControl();
        if (attributes != null) {
            extension = (E) new ActivePowerControlImpl<>(getInjection(), attributes.isParticipate(), attributes.getDroop());
        }
        return extension;
    }

    private <E extends Extension<Generator>> E createCoordinatedReactiveControlExtension() {
        E extension = null;
        CoordinatedReactiveControlAttributes attributes = resource.getAttributes().getCoordinatedReactiveControl();
        if (attributes != null) {
            extension = (E) new CoordinatedReactiveControlImpl((GeneratorImpl) getInjection(), attributes.getQPercent());
        }
        return extension;
    }

    @Override
    public <E extends Extension<Generator>> E getExtension(Class<? super E> type) {
        E extension = super.getExtension(type);
        if (type == ActivePowerControl.class) {
            extension = createActivePowerControlExtension();
        } else if (type == CoordinatedReactiveControl.class) {
            extension = createCoordinatedReactiveControlExtension();
        }
        return extension;
    }

    @Override
    public <E extends Extension<Generator>> E getExtensionByName(String name) {
        E extension = super.getExtensionByName(name);
        if (name.equals("activePowerControl")) {
            extension = createActivePowerControlExtension();
        } else if (name.equals("coordinatedReactiveControl")) {
            extension = createCoordinatedReactiveControlExtension();
        }
        return extension;
    }

    @Override
    public <E extends Extension<Generator>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        E extension = createActivePowerControlExtension();
        if (extension != null) {
            extensions.add(extension);
        }
        extension = createCoordinatedReactiveControlExtension();
        if (extension != null) {
            extensions.add(extension);
        }
        return extensions;
    }

    @Override
    protected String getTypeDescription() {
        return "Generator";
    }
}
